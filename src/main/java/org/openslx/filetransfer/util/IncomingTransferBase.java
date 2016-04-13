package org.openslx.filetransfer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.TransferState;
import org.openslx.filetransfer.DataReceivedCallback;
import org.openslx.filetransfer.Downloader;
import org.openslx.filetransfer.FileRange;
import org.openslx.filetransfer.WantRangeCallback;
import org.openslx.filetransfer.util.HashChecker.HashCheckCallback;
import org.openslx.filetransfer.util.HashChecker.HashResult;
import org.openslx.util.ThriftUtil;

public abstract class IncomingTransferBase extends AbstractTransfer implements HashCheckCallback
{

	private static final Logger LOGGER = Logger.getLogger( IncomingTransferBase.class );

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

	static {
		long maxMem = Runtime.getRuntime().maxMemory();
		if ( maxMem == Long.MAX_VALUE ) {
			maxMem = 512;
		}
		int hashQueueLen = (int) ( maxMem / 100 );
		if ( hashQueueLen < 1 ) {
			hashQueueLen = 1;
		} else if ( hashQueueLen > 6 ) {
			hashQueueLen = 6;
		}
		HashChecker hc;
		try {
			hc = new HashChecker( "SHA-1", hashQueueLen );
		} catch ( NoSuchAlgorithmException e ) {
			hc = null;
		}
		hashChecker = hc;
	}

	/*_*/

	public IncomingTransferBase( String transferId, File absFilePath, long fileSize, List<byte[]> blockHashes )
			throws FileNotFoundException
	{
		super( transferId );
		this.fileSize = fileSize;
		// Prepare path
		tmpFileName = absFilePath;
		tmpFileName.getParentFile().mkdirs();
		tmpFileHandle = new RandomAccessFile( absFilePath, "rw" );
		chunks = new ChunkList( fileSize, blockHashes );
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
		lastActivityTime.set( 0 );
		safeClose( tmpFileHandle );
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
		chunks.updateSha1Sums( hashList );
		if ( hashChecker == null )
			return;
		FileChunk chunk;
		while ( null != ( chunk = chunks.getUnhashedComplete() ) ) {
			byte[] data = loadChunkFromFile( chunk );
			if ( data == null ) {
				LOGGER.warn( "Will mark unloadable chunk as valid :-(" );
				chunks.markSuccessful( chunk );
				chunkStatusChanged( chunk );
				continue;
			}
			try {
				hashChecker.queue( chunk, data, this );
			} catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private byte[] loadChunkFromFile( FileChunk chunk )
	{
		synchronized ( tmpFileHandle ) {
			if ( state != TransferState.IDLE && state != TransferState.WORKING )
				return null;
			try {
				tmpFileHandle.seek( chunk.range.startOffset );
				byte[] buffer = new byte[ chunk.range.getLength() ];
				tmpFileHandle.readFully( buffer );
				return buffer;
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
			if ( currentChunk != null ) {
				if ( hashChecker != null && currentChunk.getSha1Sum() != null ) {
					try {
						hashChecker.queue( currentChunk, buffer, IncomingTransferBase.this );
					} catch ( InterruptedException e ) {
						Thread.currentThread().interrupt();
						return null;
					}
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
							downloader.sendErrorCode( "Out of RAM" );
							cancel();
						}
					}
				} else {
					// We have no hash checker or the hash for the current chunk is unknown - flush to disk
					writeFileData( currentChunk.range.startOffset, currentChunk.range.getLength(), buffer );
					chunks.markSuccessful( currentChunk );
					chunkStatusChanged( currentChunk );
				}
			}
			// Get next missing chunk
			try {
				currentChunk = chunks.getMissing();
			} catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				cancel();
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
			return currentChunk.range;
		}
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
					CbHandler cbh = new CbHandler( connection );
					if ( !connection.download( cbh, cbh ) ) {
						if ( cbh.currentChunk != null ) {
							// If the download failed and we have a current chunk, put it back into
							// the queue, so it will be handled again later...
							chunks.markFailed( cbh.currentChunk );
							chunkStatusChanged( cbh.currentChunk );
						}
						LOGGER.warn( "Download of " + getTmpFileName().getAbsolutePath() + " failed" );
					}
					if ( state != TransferState.FINISHED && state != TransferState.ERROR ) {
						LOGGER.debug( "Download from satellite complete" );
						lastActivityTime.set( System.currentTimeMillis() );
					}
					synchronized ( downloads ) {
						downloads.remove( connection );
					}
					if ( chunks.isComplete() ) {
						finishUploadInternal();
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
				throw new IllegalStateException( "Cannot write to file if state != WORKING" );
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
		if ( state != TransferState.IDLE && state != TransferState.WORKING )
			return;
		switch ( result ) {
		case FAILURE:
			LOGGER.warn( "Hash check of chunk " + chunk.toString()
					+ " could not be executed. Assuming valid :-(" );
			// Fall through
		case VALID:
			if ( !chunk.isWrittenToDisk() ) {
				writeFileData( chunk.range.startOffset, chunk.range.getLength(), data );
			}
			chunks.markSuccessful( chunk );
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
		}
	}

	private synchronized void finishUploadInternal()
	{
		if ( state == TransferState.FINISHED ) {
			return;
		}
		safeClose( tmpFileHandle );
		if ( state != TransferState.WORKING ) {
			state = TransferState.ERROR;
		} else {
			state = TransferState.FINISHED; // Races...
			if ( !finishIncomingTransfer() ) {
				state = TransferState.ERROR;
			}
		}
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

}
