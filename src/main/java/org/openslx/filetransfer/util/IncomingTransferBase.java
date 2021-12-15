package org.openslx.filetransfer.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.bwlp.thrift.iface.TransferState;
import org.openslx.bwlp.thrift.iface.TransferStatus;
import org.openslx.filetransfer.DataReceivedCallback;
import org.openslx.filetransfer.Downloader;
import org.openslx.filetransfer.FileRange;
import org.openslx.filetransfer.LocalChunkSource;
import org.openslx.filetransfer.LocalChunkSource.ChunkSource;
import org.openslx.filetransfer.WantRangeCallback;
import org.openslx.filetransfer.util.HashChecker.HashCheckCallback;
import org.openslx.filetransfer.util.HashChecker.HashResult;
import org.openslx.util.ThriftUtil;

public abstract class IncomingTransferBase extends AbstractTransfer implements HashCheckCallback
{

	private static final Logger LOGGER = LogManager.getLogger( IncomingTransferBase.class );

	/**
	 * Remote peer is uploading, so on our end, we have Downloaders
	 */
	private List<Downloader> downloads = new ArrayList<>();

	private final File tmpFileName;

	private final RandomAccessFile tmpFileHandle;

	private final ChunkList chunks;

	private TransferState state = TransferState.IDLE;

	private final long fileSize;

	private static final HashChecker hashChecker;

	/*
	 * Overridable constants
	 */

	protected static int MAX_CONNECTIONS_PER_TRANSFER = 2;

	/**
	 * Whether file is (still) writable. Used for the file transfer callbacks.
	 */
	private boolean fileWritable = true;

	/**
	 * Called for getting local sources for certain chunks by checksum
	 */
	private final LocalChunkSource localChunkSource;
	
	/**
	 * Non-null if local copying is requested
	 */
	private final LocalCopyManager localCopyManager;

	static {
		long maxMem = Runtime.getRuntime().maxMemory();
		if ( maxMem == Long.MAX_VALUE ) {
			LOGGER.warn( "Cannot determine maximum JVM memory -- assuming 1GB -- this might not be safe" );
			maxMem = 1024;
		} else {
			maxMem /= ( 1024 * 1024 );
		}
		final int maxLen = Math.max( 6, Runtime.getRuntime().availableProcessors() );
		int hashQueueLen = (int) ( maxMem / 150 );
		if ( hashQueueLen < 1 ) {
			hashQueueLen = 1;
		} else if ( hashQueueLen > maxLen ) {
			hashQueueLen = maxLen;
		}
		LOGGER.debug( "Queue length: " + hashQueueLen );
		HashChecker hc;
		try {
			hc = new HashChecker( "SHA-1", hashQueueLen );
		} catch ( NoSuchAlgorithmException e ) {
			hc = null;
		}
		hashChecker = hc;
	}

	/*_*/

	public IncomingTransferBase( String transferId, File absFilePath, long fileSize, List<byte[]> blockHashes, LocalChunkSource localChunkSource )
			throws FileNotFoundException
	{
		super( transferId );
		this.fileSize = fileSize;
		this.localChunkSource = localChunkSource;
		// Prepare path
		tmpFileName = absFilePath;
		tmpFileName.getParentFile().mkdirs();
		tmpFileHandle = new RandomAccessFile( absFilePath, "rw" );
		try {
			if ( tmpFileHandle.length() > fileSize ) {
				tmpFileHandle.setLength( fileSize );
			}
		} catch ( IOException e ) {
			LOGGER.debug( "File " + tmpFileName + " is too long and could not be truncated" );
		}
		chunks = new ChunkList( fileSize, blockHashes );
		if ( this.localChunkSource != null ) {
			this.localCopyManager = new LocalCopyManager( this, this.chunks );
			checkLocalCopyCandidates( blockHashes, 0 );
		} else {
			this.localCopyManager = null;
		}
	}

	@Override
	public boolean isActive()
	{
		return state == TransferState.IDLE || state == TransferState.WORKING;
	}

	@Override
	public synchronized void cancel()
	{
		if ( state != TransferState.FINISHED && state != TransferState.ERROR ) {
			state = TransferState.ERROR;
		}
		synchronized ( downloads ) {
			for ( Downloader download : downloads ) {
				download.cancel();
			}
		}
		potentialFinishTime.set( 0 );
		if ( localCopyManager != null ) {
			localCopyManager.interrupt();
		}
		safeClose( tmpFileHandle );
		if ( getTransferInfo() != null && getTransferInfo().token != null ) {
			LOGGER.debug( "Cancelled upload " + getTransferInfo().token );
		}
	}

	@Override
	public final int getActiveConnectionCount()
	{
		return downloads.size();
	}

	public final boolean hashesEqual( List<ByteBuffer> blockHashes )
	{
		List<FileChunk> existing = chunks.getAll();
		if ( existing.size() != blockHashes.size() )
			return false;
		List<byte[]> hashes = ThriftUtil.unwrapByteBufferList( blockHashes );
		FileChunk first = existing.get( 0 );
		if ( first == null || first.getSha1Sum() == null )
			return false;
		Iterator<byte[]> it = hashes.iterator();
		for ( FileChunk existingChunk : existing ) {
			byte[] testChunk = it.next();
			if ( !Arrays.equals( testChunk, existingChunk.getSha1Sum() ) )
				return false;
		}
		return true;
	}

	/*
	 * Guettas for final/private fields
	 */

	public final long getFileSize()
	{
		return fileSize;
	}

	public final File getTmpFileName()
	{
		return tmpFileName;
	}

	public final TransferState getState()
	{
		return state;
	}

	public synchronized TransferStatus getStatus()
	{
		return new TransferStatus( chunks.getStatusArray(), getState() );
	}

	public final ChunkList getChunks()
	{
		return chunks;
	}

	/**
	 * It is possible to run a download where the remote peer didn't submit
	 * the full list of block hashes yet, as it might be about to hash the file
	 * while uploading. This method should be called to update the list
	 * of block hashes. This is a cumulative call, so the list must contain
	 * all hashes starting from block 0.
	 * 
	 * @param hashList (incomplete) list of block hashes
	 */
	public void updateBlockHashList( List<byte[]> hashList )
	{
		if ( state != TransferState.IDLE && state != TransferState.WORKING ) {
			LOGGER.debug( this.getId() + ": Rejecting block hash list in state " + state );
			return;
		}
		if ( hashList == null ) {
			LOGGER.debug( this.getId() + ": Rejecting null block hash list" );
			return;
		}
		int firstNew = chunks.updateSha1Sums( hashList );
		// No hash checker? Neither hashing nor server side dedup will make sense
		if ( hashChecker == null )
			return;
		// Check hashes of completed blocks
		for ( int cnt = 0; cnt < 3; ++cnt ) {
			FileChunk chunk = chunks.getUnhashedComplete();
			if ( chunk == null )
				break;
			byte[] data = null;
			try {
				data = loadChunkFromFile( chunk );
			} catch ( EOFException e1 ) {
				LOGGER.warn( "blockhash update: file too short, marking chunk as invalid" );
				chunks.markFailed( chunk );
				chunkStatusChanged( chunk );
				continue;
			} catch ( Exception e ) {
				LOGGER.warn( "unexpected fail while loading chunk from disk", e );
			}
			if ( data == null ) {
				LOGGER.warn( "blockhash update: Will mark unloadable unhashed chunk as valid :-(" );
				chunks.markCompleted( chunk, true );
				chunkStatusChanged( chunk );
				continue;
			}
			try {
				if ( !hashChecker.queue( chunk, data, this, HashChecker.CALC_HASH ) ) { // false == queue full, stop
					chunks.markCompleted( chunk, false );
					break;
				}
			} catch ( InterruptedException e ) {
				LOGGER.debug( "updateBlockHashList got interrupted" );
				chunks.markCompleted( chunk, false );
				Thread.currentThread().interrupt();
				return;
			}
		}
		// See if we have any candidates for local copy
		checkLocalCopyCandidates( hashList, firstNew );
	}

	private void checkLocalCopyCandidates( List<byte[]> hashList, int firstNew )
	{
		if ( localChunkSource == null || hashList == null || hashList.isEmpty() )
			return;
		List<byte[]> sums;
		if ( firstNew <= 0 ) {
			sums = hashList;
		} else {
			sums = hashList.subList( firstNew, hashList.size() );
		}
		if ( sums == null )
			return;
		sums = Collections.unmodifiableList( sums );
		List<ChunkSource> sources = null;
		try {
			sources = localChunkSource.getCloneSources( sums );
		} catch ( Exception e ) {
			LOGGER.warn( "Could not get chunk sources", e );
		}
		if ( sources != null && !sources.isEmpty() ) {
			chunks.markLocalCopyCandidates( sources );
		}
		localCopyManager.trigger();
	}

	private byte[] loadChunkFromFile( FileChunk chunk ) throws EOFException
	{
		synchronized ( tmpFileHandle ) {
			if ( state != TransferState.IDLE && state != TransferState.WORKING )
				return null;
			try {
				tmpFileHandle.seek( chunk.range.startOffset );
				byte[] buffer = new byte[ chunk.range.getLength() ];
				tmpFileHandle.readFully( buffer );
				return buffer;
			} catch ( EOFException e ) {
				throw e;
			} catch ( IOException e ) {
				LOGGER.error( "Could not read chunk " + chunk.getChunkIndex() + " of File " + getTmpFileName().toString(), e );
				return null;
			}
		}
	}

	/**
	 * Callback class for an instance of the Downloader, which supplies
	 * the Downloader with wanted file ranges, and handles incoming data.
	 */
	private class CbHandler implements WantRangeCallback, DataReceivedCallback
	{
		/**
		 * The current chunk being transfered.
		 */
		private FileChunk currentChunk = null;
		/**
		 * Current buffer to receive to
		 */
		private byte[] buffer = new byte[ FileChunk.CHUNK_SIZE ];
		/**
		 * Downloader object
		 */
		private final Downloader downloader;

		private CbHandler( Downloader downloader )
		{
			this.downloader = downloader;
		}

		@Override
		public boolean dataReceived( long fileOffset, int dataLength, byte[] data )
		{
			if ( currentChunk == null )
				throw new IllegalStateException( "dataReceived without current chunk" );
			if ( !currentChunk.range.contains( fileOffset, fileOffset + dataLength ) )
				throw new IllegalStateException( "dataReceived with file data out of range" );
			System.arraycopy( data, 0, buffer, (int) ( fileOffset - currentChunk.range.startOffset ), dataLength );
			return fileWritable;
		}

		@Override
		public FileRange get()
		{
			boolean needNewBuffer = false;
			if ( currentChunk != null ) {
				try {
					if ( chunkReceivedInternal( currentChunk, buffer ) ) {
						needNewBuffer = true;
					}
				} catch ( InterruptedException e3 ) {
					LOGGER.info( "Downloader was interrupted when trying to hash" );
					currentChunk = null;
					return null;
				}
				if ( needNewBuffer ) {
					try {
						buffer = new byte[ buffer.length ];
					} catch ( OutOfMemoryError e ) {
						// Usually catching OOM errors is a bad idea, but it's quite safe here as
						// we know exactly where it happened, no hidden sub-calls through 20 objects.
						// The most likely cause here is that the hash checker/disk cannot keep up
						// writing out completed chunks, so we just sleep a bit and try again. If it still
						// fails, we exit completely.
						try {
							Thread.sleep( 6000 );
						} catch ( InterruptedException e1 ) {
							Thread.currentThread().interrupt();
							return null;
						}
						// Might raise OOM again, but THIS TIME I MEAN IT
						try {
							buffer = new byte[ buffer.length ];
						} catch ( OutOfMemoryError e2 ) {
							LOGGER.warn( "Out of JVM memory - aborting incoming " + IncomingTransferBase.this.getId() );
							downloader.sendErrorCode( "Out of RAM" );
							cancel();
						}
					}
				}
				currentChunk = null;
			}
			// Get next missing chunk
			try {
				currentChunk = chunks.getMissing();
			} catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				LOGGER.info("Incoming transfer connection was interrupted");
				return null;
			}
			if ( currentChunk == null ) {
				return null; // No more chunks, returning null tells the Downloader we're done.
			}
			// Check remaining disk space and abort if it's too low
			if ( !hasEnoughFreeSpace() ) {
				downloader.sendErrorCode( "Out of disk space" );
				LOGGER.error( "Out of space: Cancelling upload of " + getTmpFileName().getAbsolutePath() );
				cancel();
				return null;
			}
			if ( state == TransferState.IDLE ) {
				state = TransferState.WORKING;
			}
			return currentChunk.range;
		}
	}

	/**
	 * 
	 * @param currentChunk
	 * @param buffer
	 * @return true if buffer is used internally and should not be modified in the future, false if
	 *         reuse is safe
	 * @throws InterruptedException
	 */
	final boolean chunkReceivedInternal( FileChunk currentChunk, byte[] buffer ) throws InterruptedException
	{
		boolean needNewBuffer = false;
		try {
			needNewBuffer = chunkReceived( currentChunk, buffer );
		} catch (Exception e) {
			LOGGER.warn( "Callback chunkReceived caused exception", e );
			needNewBuffer = true; // To be on the safe side
		}
		InterruptedException passEx = null;
		if ( hashChecker != null && currentChunk.getSha1Sum() != null ) {
			try {
				hashChecker.queue( currentChunk, buffer, IncomingTransferBase.this, HashChecker.BLOCKING | HashChecker.CALC_HASH );
				return true;
			} catch ( InterruptedException e ) {
				passEx = e;
			}
		}
		// We have no hash checker, or hasher rejected block,
		// or the hash for the current chunk is unknown - flush to disk
		writeFileData( currentChunk.range.startOffset, currentChunk.range.getLength(), buffer );
		chunks.markCompleted( currentChunk, false );
		chunkStatusChanged( currentChunk );
		if ( passEx != null )
			throw passEx;
		return needNewBuffer;
	}

	public boolean addConnection( final Downloader connection, ExecutorService pool )
	{
		if ( state == TransferState.FINISHED ) {
			handleIncomingWhenFinished( connection, pool );
			return true;
		}
		if ( state == TransferState.ERROR )
			return false;
		synchronized ( downloads ) {
			if ( downloads.size() >= MAX_CONNECTIONS_PER_TRANSFER )
				return false;
			downloads.add( connection );
		}
		try {
			pool.execute( new Runnable() {
				@Override
				public void run()
				{
					try {
						CbHandler cbh = new CbHandler( connection );
						if ( connection.download( cbh, cbh ) ) {
							connectFails.set( 0 );
						} else {
							connectFails.incrementAndGet();
							if ( cbh.currentChunk != null ) {
								// If the download failed and we have a current chunk, put it back into
								// the queue, so it will be handled again later...
								chunks.markFailed( cbh.currentChunk );
								// Possibly queue for local copy
								if ( localCopyManager != null && cbh.currentChunk.sha1sum != null ) {
									List<byte[]> lst = new ArrayList<>( 1 );
									lst.add( cbh.currentChunk.sha1sum );
									checkLocalCopyCandidates( lst, 0 );
								}
								chunkStatusChanged( cbh.currentChunk );
							}
							LOGGER.debug( "Connection for " + getTmpFileName().getAbsolutePath() + " dropped" );
						}
						if ( state != TransferState.FINISHED && state != TransferState.ERROR ) {
							lastActivityTime.set( System.currentTimeMillis() );
						}
					} finally {
						synchronized ( downloads ) {
							downloads.remove( connection );
						}
					}
					if ( chunks.isComplete() ) {
						finishUploadInternal();
					} else if ( state == TransferState.WORKING ) {
						// Keep pumping unhashed chunks into the hasher
						queueUnhashedChunk( true );
						if ( localCopyManager != null ) {
							localCopyManager.trigger();
						}
					}
				}
			} );
		} catch ( Exception e ) {
			LOGGER.warn( "threadpool rejected the incoming file transfer", e );
			synchronized ( downloads ) {
				downloads.remove( connection );
			}
			return false;
		}
		if ( state == TransferState.IDLE ) {
			state = TransferState.WORKING;
		}
		return true;
	}

	private boolean handleIncomingWhenFinished( final Downloader connection, ExecutorService pool )
	{
		try {
			pool.execute( new Runnable() {
				@Override
				public void run()
				{
					connection.sendDoneAndClose();
				}
			} );
		} catch ( Exception e ) {
			return false;
		}
		return true;
	}

	/**
	 * Write some data to the local file. Thread safe so we can
	 * have multiple concurrent connections.
	 * 
	 * @param fileOffset
	 * @param dataLength
	 * @param data
	 * @return
	 */
	private void writeFileData( long fileOffset, int dataLength, byte[] data )
	{
		synchronized ( tmpFileHandle ) {
			if ( state != TransferState.WORKING )
				throw new IllegalStateException( "Cannot write to file if state != WORKING (is " + state.toString() + ")" );
			try {
				tmpFileHandle.seek( fileOffset );
				tmpFileHandle.write( data, 0, dataLength );
			} catch ( IOException e ) {
				LOGGER.error( "Cannot write to '" + getTmpFileName()
						+ "'. Disk full, network storage error, bad permissions, ...?", e );
				fileWritable = false;
			}
		}
		if ( !fileWritable ) {
			cancel();
		}
	}

	@Override
	public void hashCheckDone( HashResult result, byte[] data, FileChunk chunk )
	{
		if ( state != TransferState.IDLE && state != TransferState.WORKING ) {
			LOGGER.debug( "hashCheckDone called in bad state " + state.name() );
			return;
		}
		switch ( result ) {
		case FAILURE:
			LOGGER.warn( "Hash check of chunk " + chunk.toString()
					+ " could not be executed. Assuming valid :-(" );
			// Fall through
		case VALID:
			if ( chunk.isWrittenToDisk() ) {
				chunks.markCompleted( chunk, true );
			} else {
				try {
					writeFileData( chunk.range.startOffset, chunk.range.getLength(), data );
					chunks.markCompleted( chunk, true );
				} catch ( Exception e ) {
					LOGGER.warn( "Cannot write to file after hash check", e );
					chunks.markFailed( chunk );
				}
			}
			chunkStatusChanged( chunk );
			if ( chunks.isComplete() ) {
				finishUploadInternal();
			}
			break;
		case INVALID:
			LOGGER.warn( "Hash check of chunk " + chunk.getChunkIndex() + " resulted in mismatch "
					+ chunk.getFailCount() + "x :-(" );
			chunks.markFailed( chunk );
			chunkStatusChanged( chunk );
			break;
		case NONE:
			LOGGER.warn( "Got hashCheckDone with result NONE" );
			break;
		}
		// A block finished, see if we can queue a new one
		queueUnhashedChunk( false );
		if ( localCopyManager != null && localCopyManager.isAlive() ) {
			localCopyManager.trigger();
		}
	}

	/**
	 * Gets an unhashed chunk (if existent) and queues it for hashing
	 */
	protected void queueUnhashedChunk( boolean blocking )
	{
		FileChunk chunk = chunks.getUnhashedComplete();
		if ( chunk == null )
			return;
		byte[] data;
		try {
			data = loadChunkFromFile( chunk );
		} catch ( EOFException e1 ) {
			LOGGER.warn( "Cannot queue unhashed chunk: file too short. Marking is invalid." );
			chunks.markFailed( chunk );
			chunkStatusChanged( chunk );
			return;
		}
		if ( data == null ) {
			LOGGER.warn( "Cannot queue unhashed chunk: Will mark unloadable unhashed chunk as valid :-(" );
			chunks.markCompleted( chunk, true );
			chunkStatusChanged( chunk );
			return;
		}
		try {
			int flags = HashChecker.CALC_HASH;
			if ( blocking ) {
				flags |= HashChecker.BLOCKING;
			}
			if ( !hashChecker.queue( chunk, data, this, flags ) ) {
				chunks.markCompleted( chunk, false );
			}
		} catch ( InterruptedException e ) {
			LOGGER.debug( "Interrupted while trying to queueUnhashedChunk" );
			chunks.markCompleted( chunk, false );
			Thread.currentThread().interrupt();
		}
	}

	final synchronized void finishUploadInternal()
	{
		if ( state == TransferState.FINISHED || state == TransferState.ERROR ) {
			return;
		}
		try {
			if ( tmpFileHandle.length() < fileSize && chunks.lastChunkIsZero() ) {
				tmpFileHandle.setLength( fileSize );
			}
		} catch ( IOException e ) {
			LOGGER.warn( "Cannot extend file size to " + fileSize );
		}
		safeClose( tmpFileHandle );
		if ( localCopyManager != null ) {
			localCopyManager.interrupt();
		}
		state = TransferState.FINISHED; // Races...
		if ( !finishIncomingTransfer() ) {
			state = TransferState.ERROR;
		}
	}
	
	protected HashChecker getHashChecker()
	{
		return hashChecker;
	}

	/*
	 * 
	 */

	/**
	 * Override this and return true if the destination of this download has
	 * still enough free space so we don't run into disk full errors.
	 */
	protected abstract boolean hasEnoughFreeSpace();

	/**
	 * This will be called once the download is complete.
	 * The file handle used for writing has been closed before calling this.
	 */
	protected abstract boolean finishIncomingTransfer();

	protected abstract void chunkStatusChanged( FileChunk chunk );

	/**
	 * Called when a chunk has been received -- no validation has taken place yet
	 * @return whether we want to use the buffered data later on and it must not be written to
	 */
	protected boolean chunkReceived( FileChunk chunk, byte[] data )
	{
		return false;
	}

	public boolean isServerSideCopyingEnabled()
	{
		return localCopyManager != null && !localCopyManager.isPaused();
	}

	public void enableServerSideCopying( boolean serverSideCopying )
	{
		if ( localCopyManager != null ) {
			localCopyManager.setPaused( !serverSideCopying );
		}
	}

}
