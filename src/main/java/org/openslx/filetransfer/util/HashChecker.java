package org.openslx.filetransfer.util;

import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class HashChecker
{
	private static final Logger LOGGER = Logger.getLogger( HashChecker.class );

	private final BlockingQueue<HashTask> queue = new LinkedBlockingQueue<>( 10 );

	private final List<Thread> threads = new ArrayList<>();

	private final String algorithm;

	private volatile boolean invalid = false;

	public HashChecker( String algorithm ) throws NoSuchAlgorithmException
	{
		this.algorithm = algorithm;
		CheckThread thread = new CheckThread( false );
		thread.start();
		threads.add( thread );
	}

	private void threadFailed( CheckThread thread )
	{
		synchronized ( threads ) {
			if ( thread.extraThread )
				return;
			invalid = true;
		}
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
		task.callback.hashCheckDone( result, task.data, task.chunk );
	}

	public void queue( FileChunk chunk, byte[] data, HashCheckCallback callback ) throws InterruptedException
	{
		if ( chunk.sha1sum == null )
			throw new NullPointerException( "Chunk has no sha1 hash" );
		if ( chunk.sha1sum.length != 20 )
			throw new InvalidParameterException( "Given chunk sha1 is not 20 bytes but " + chunk.sha1sum.length );
		HashTask task = new HashTask( data, chunk, callback );
		synchronized ( threads ) {
			if ( invalid ) {
				execCallback( task, HashResult.FAILURE );
				return;
			}
			if ( queue.remainingCapacity() <= 1 && threads.size() < Runtime.getRuntime().availableProcessors() ) {
				try {
					CheckThread thread = new CheckThread( true );
					thread.start();
					threads.add( thread );
				} catch ( Exception e ) {
					LOGGER.warn( "Could not create additional hash checking thread", e );
				}
			}
			queue.put( task );
		}
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
		}

		@Override
		public void run()
		{
			while ( !interrupted() ) {
				HashTask task;
				// Wait for work
				try {
					task = queue.take();
					if ( task == null )
						continue;
				} catch ( InterruptedException e ) {
					LOGGER.info( "Interrupted while waiting for hash task", e );
					threadFailed( this );
					break;
				}
				// Calculate digest
				md.update( task.data, 0, task.chunk.range.getLength() );
				byte[] digest = md.digest();
				HashResult result = Arrays.equals( digest, task.chunk.sha1sum ) ? HashResult.VALID : HashResult.INVALID;
				execCallback( task, result );
				if ( extraThread && queue.isEmpty() ) {
					LOGGER.info( "Stopping additional hash checker" );
					break;
				}
			}
		}
	}

	public static enum HashResult
	{
		VALID, // Hash matches
		INVALID, // Hash does not match
		FAILURE // Error calculating hash
	}

	private static class HashTask
	{
		public final byte[] data;
		public final FileChunk chunk;
		public final HashCheckCallback callback;

		public HashTask( byte[] data, FileChunk chunk, HashCheckCallback callback )
		{
			this.data = data;
			this.chunk = chunk;
			this.callback = callback;
		}
	}

	public static interface HashCheckCallback
	{
		public void hashCheckDone( HashResult result, byte[] data, FileChunk chunk );
	}

}