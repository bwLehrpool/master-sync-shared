package org.openslx.filetransfer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class HashChecker
{
	public static final int BLOCKING = 1;
	public static final int CALC_HASH = 2;
	public static final int CALC_CRC32 = 4;
	
	private static final Logger LOGGER = Logger.getLogger( HashChecker.class );

	private final BlockingQueue<HashTask> queue;

	private final List<Thread> threads = new ArrayList<>();

	private final String algorithm;

	private boolean invalid = false;
	
	private final int queueCapacity;

	public HashChecker( String algorithm ) throws NoSuchAlgorithmException
	{
		this( algorithm, 10 );
	}

	public HashChecker( String algorithm, int queueLen ) throws NoSuchAlgorithmException
	{
		this.algorithm = algorithm;
		this.queueCapacity = queueLen;
		this.queue = new LinkedBlockingQueue<>( queueLen );
		CheckThread thread = new CheckThread( false );
		thread.start();
		threads.add( thread );
	}

	private void threadFailed( CheckThread thread )
	{
		synchronized ( threads ) {
			threads.remove( thread );
			LOGGER.debug( "Check threads: " + threads.size() );
			if ( thread.extraThread )
				return;
			invalid = true;
		}
		LOGGER.debug( "Marking all queued chunks as failed" );
		for ( ;; ) {
			HashTask task = queue.poll();
			if ( task == null )
				break;
			execCallback( task, HashResult.FAILURE );
		}
	}

	@Override
	protected void finalize()
	{
		try {
			synchronized ( threads ) {
				for ( Thread t : threads ) {
					t.interrupt();
				}
			}
		} catch ( Throwable t ) {
			LOGGER.warn( "Something threw in finalize", t );
		}
	}

	private void execCallback( HashTask task, HashResult result )
	{
		if ( task.callback == null )
			return;
		try {
			task.callback.hashCheckDone( result, task.data, task.chunk );
		} catch ( Throwable t ) {
			LOGGER.warn( "HashCheck callback threw!", t );
		}
	}

	/**
	 * Queue the given chunk for hashing. The chunk should be in pending state.
	 * 
	 * @param chunk chunk to hash
	 * @param data binary data of this chunk
	 * @param callback callback to call when hashing is done
	 * @return true if the chunk was handled, false if the queue was full and rejected the chunk.
	 * @throws InterruptedException
	 */
	public boolean queue( FileChunk chunk, byte[] data, HashCheckCallback callback, int flags ) throws InterruptedException
	{
		boolean blocking = ( flags & BLOCKING ) != 0;
		boolean doHash = ( flags & CALC_HASH ) != 0;
		boolean doCrc32 = ( flags & CALC_CRC32 ) != 0;
		if ( doHash && chunk.getSha1Sum() == null )
			throw new NullPointerException( "Chunk has no sha1 hash" );
		HashTask task = new HashTask( data, chunk, callback, doHash, doCrc32 );
		synchronized ( threads ) {
			if ( invalid ) {
				execCallback( task, HashResult.FAILURE );
				return true;
			}
			if ( queue.remainingCapacity() <= 1 && threads.size() < Runtime.getRuntime().availableProcessors() ) {
				try {
					CheckThread thread = new CheckThread( true );
					thread.start();
					threads.add( thread );
					LOGGER.debug( "Check threads: " + threads.size() );
				} catch ( Exception e ) {
					LOGGER.warn( "Could not create additional hash checking thread", e );
				}
			}
		}
		if ( doHash ) {
			chunk.setStatus( ChunkStatus.HASHING );
		}
		if ( blocking ) {
			queue.put( task );
		} else {
			if ( !queue.offer( task ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get number of chunks currently waiting for a worker thread.
	 */
	public int getQueueFill()
	{
		return queue.size();
	}
	
	public int getQueueCapacity()
	{
		return queueCapacity;
	}

	// ############################################################# \\

	private class CheckThread extends Thread
	{
		private final MessageDigest md;
		private final boolean extraThread;

		/**
		 * Worker thread doing the sha1 calculations and comparison
		 * 
		 * @param isExtra whether this is an extra thread that should be shut down when the queue is
		 *           empty again.
		 * @throws NoSuchAlgorithmException
		 */
		public CheckThread( boolean isExtra ) throws NoSuchAlgorithmException
		{
			super( "HashCheck" );
			md = MessageDigest.getInstance( algorithm );
			extraThread = isExtra;
			setPriority( Thread.NORM_PRIORITY - 1 );
		}

		@Override
		public void run()
		{
			while ( !interrupted() ) {
				HashTask task;
				// Wait for work
				try {
					if ( extraThread ) {
						task = queue.poll( 30, TimeUnit.SECONDS );
						if ( task == null ) {
							break;
						}
					} else {
						task = queue.take();
						if ( task == null )
							continue;
					}
				} catch ( InterruptedException e ) {
					LOGGER.info( "Interrupted while waiting for hash task", e );
					break;
				}
				HashResult result = HashResult.NONE;
				if ( task.doHash ) {
					// Calculate digest
   				md.update( task.data, 0, task.chunk.range.getLength() );
   				byte[] digest = md.digest();
					result = Arrays.equals( digest, task.chunk.getSha1Sum() ) ? HashResult.VALID : HashResult.INVALID;
				}
				if ( task.doCrc32 ) {
   				// Calculate CRC32
   				task.chunk.calculateDnbd3Crc32( task.data );
				}
				execCallback( task, result );
			}
			if ( !extraThread ) {
				LOGGER.warn( "Stopped MAIN hash checker" );
			}
			threadFailed( this );
		}
	}

	public static enum HashResult
	{
		NONE, // No hashing tool place
		VALID, // Hash matches
		INVALID, // Hash does not match
		FAILURE // Error calculating hash
	}

	private static class HashTask
	{
		public final byte[] data;
		public final FileChunk chunk;
		public final HashCheckCallback callback;
		public final boolean doHash;
		public final boolean doCrc32;

		public HashTask( byte[] data, FileChunk chunk, HashCheckCallback callback, boolean doHash, boolean doCrc32 )
		{
			this.data = data;
			this.chunk = chunk;
			this.callback = callback;
			this.doHash = doHash;
			this.doCrc32 = doCrc32;
		}
	}

	public static interface HashCheckCallback
	{
		public void hashCheckDone( HashResult result, byte[] data, FileChunk chunk );
	}

}
