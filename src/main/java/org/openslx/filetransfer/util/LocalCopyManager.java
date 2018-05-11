package org.openslx.filetransfer.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.openslx.filetransfer.LocalChunkSource.ChunkSource;
import org.openslx.filetransfer.LocalChunkSource.SourceFile;
import org.openslx.util.Util;

public class LocalCopyManager extends Thread
{

	private static final Logger LOGGER = Logger.getLogger( LocalCopyManager.class );

	private FileChunk currentChunk = null;

	private final ChunkList chunkList;

	private final IncomingTransferBase transfer;

	private final Map<String, RandomAccessFile> sources = new HashMap<>();

	private Semaphore hasWork = new Semaphore( 0 );

	private AtomicInteger copyCount = new AtomicInteger();

	private boolean paused = true;

	public LocalCopyManager( IncomingTransferBase transfer, ChunkList list )
	{
		super( "LocalCopyManager" );
		this.transfer = transfer;
		this.chunkList = list;
	}

	/**
	 * Trigger copying of another block if possible
	 */
	public synchronized void trigger()
	{
		if ( this.paused )
			return;
		if ( !isAlive() ) {
			LOGGER.warn( "Cannot be triggered when Thread is not running." );
			if ( currentChunk != null ) {
				chunkList.markFailed( currentChunk );
				currentChunk = null;
			}
			return;
		}
		if ( currentChunk == null ) {
			currentChunk = chunkList.getCopyCandidate();
			hasWork.release();
		}
	}

	@Override
	public void run()
	{
		try {
			while ( !interrupted() ) {
				while ( currentChunk != null ) {
					hasWork.drainPermits();
					copyChunk();
				}
				if ( !hasWork.tryAcquire( 10, TimeUnit.SECONDS ) ) {
					if ( chunkList.isComplete() ) {
						transfer.finishUploadInternal();
						break;
					} else if ( !transfer.isActive() ) {
						break;
					} else {
						trigger();
					}
				}
			}
		} catch ( InterruptedException | IllegalStateException e ) {
			interrupt();
		}
		synchronized ( this ) {
			if ( currentChunk != null ) {
				LOGGER.warn( "Still had a chunk when thread was interrupted." );
				chunkList.markFailed( currentChunk );
				currentChunk = null;
			}
		}
		for ( RandomAccessFile file : sources.values() ) {
			Util.safeClose( file );
		}
		LOGGER.debug( "My work here is done. Copied " + copyCount.get() + " chunks from " + sources.size() + " files." );
	}

	private void copyChunk() throws InterruptedException
	{
		ChunkSource source = currentChunk.getSources();
		if ( source != null ) {
			// OK
			for ( ;; ) {
				// Try every possible source file
				SourceFile sourceFile = getOpenFile( source, currentChunk.range.getLength() );
				if ( sourceFile == null ) {
					// Was marked as having a source file, but now we got null -- most likely
					// the source file doesn't exist or isn't readable
					LOGGER.warn( "No open file for local copying!" );
					break;
				}
				// OK
				RandomAccessFile raf = sources.get( sourceFile.fileName );
				byte[] buffer;
				try {
					raf.seek( sourceFile.offset );
					// In order not to hinder (fast) upload of unknown blocks, throttle
					// local copying as long as chunks are missing - do before allocating buffer
					// so we don't hold allocated unused memory for no reason, but the seek has
					// been done so we know the file handle is not goofed up
					if ( chunkList.hasLocallyMissingChunk() ) {
						int delay;
						HashChecker hc = transfer.getHashChecker();
						if ( hc == null ) {
							delay = 50;
						} else {
							delay = ( hc.getQueueFill() * 500 ) / hc.getQueueCapacity();
						}
						Thread.sleep( delay );
					}
					buffer = new byte[ sourceFile.chunkSize ];
					raf.readFully( buffer );
				} catch ( InterruptedException e ) {
					throw e;
				} catch ( Exception e ) {
					LOGGER.warn( "Could not read chunk to replicate from " + sourceFile.fileName, e );
					buffer = null;
					if ( e instanceof IOException ) {
						// Mark file as messed up
						sources.put( sourceFile.fileName, null );
					}
				}
				if ( buffer != null ) {
					// All is well, read chunk locally, pass on
					transfer.chunkReceivedInternal( currentChunk, buffer );
					synchronized ( this ) {
						currentChunk = null;
					}
					copyCount.incrementAndGet();
					trigger();
					return;
				}
				// Reaching here means failure
				// We'll keep looping as long as there are source files available
			}
			// End of loop over source files
		}
		// FAILED
		LOGGER.info( "Local copying failed, queueing for normal upload..." );
		synchronized ( this ) {
			chunkList.markFailed( currentChunk );
			currentChunk = null;
		}
	}

	private SourceFile getOpenFile( ChunkSource source, int requiredSize )
	{
		for ( SourceFile candidate : source.sourceCandidates ) {
			if ( sources.get( candidate.fileName ) != null )
				return candidate;
		}
		// Have to open
		for ( SourceFile candidate : source.sourceCandidates ) {
			if ( sources.containsKey( candidate.fileName ) ) // Maps to null (otherwise upper loop would have returned)
				continue; // File is broken, don't use
			if ( candidate.chunkSize != requiredSize )
				continue;
			File f = new File( candidate.fileName );
			if ( !f.exists() ) {
				sources.put( candidate.fileName, null ); // Mark for future
				continue;
			}
			try {
				RandomAccessFile raf = new RandomAccessFile( f, "r" );
				sources.put( candidate.fileName, raf );
				return candidate;
			} catch ( Exception e ) {
				LOGGER.info( "Cannot open " + candidate.fileName, e );
				sources.put( candidate.fileName, null ); // Mark for future
			}
		}
		// Nothing worked
		return null;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void setPaused( boolean paused )
	{
		this.paused = paused;
	}

}
