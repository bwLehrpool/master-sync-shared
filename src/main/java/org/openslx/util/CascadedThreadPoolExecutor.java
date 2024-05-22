package org.openslx.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Cascaded thread pool. A thread pool behaving mostly like a normal
 * TPE, until it is saturated and would reject execution. In that case,
 * it will try to run the job in a global, shared fallback thread pool,
 * and only reject execution if this also fails.
 * The reasoning is that you can define rather small thread pools for
 * different jobs, without having to use particularly high maximumPoolSize
 * for cases of sudden high load. If you have a dozen thread pools that can
 * grow to hundreds of threads, worst case you suddenly have a thousand
 * threads around using up memory and everything's messed up. Instead,
 * use conservative values like 8 or 16 as the maximum size, and rely on
 * the CascadedThreadPoolExecutor to take load spikes. So, even if multiple
 * parts of your application suddenly are hit with an unexpectedly high
 * load, the overall number of threads can be kept within reasonable bounds
 * and OOM situations are less likely to occur.
 * Also, in case some part of the application saturated the shared pool
 * for extended periods of time, other "well behaving" parts of your
 * application can still make progress with their small pools, in contrast
 * to a design where everything in your application shares one giant
 * thread pool directly.
 */
public class CascadedThreadPoolExecutor extends ThreadPoolExecutor
{

	private static final RejectedExecutionHandler defaultHandler = new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
		{
			FALLBACK_TPE.execute( r );
		}
	};

	private static final ThreadPoolExecutor FALLBACK_TPE = new ThreadPoolExecutor( 16, 128,
			1, TimeUnit.MINUTES,
			new LinkedBlockingDeque<Runnable>( 4 ),
			new PrioThreadFactory( "FallbackTP" ),
			new AbortPolicy() );

	static {
		FALLBACK_TPE.allowCoreThreadTimeOut( true );
	}

	/**
	 * The RejectedExecutionHandler of this pool. We need to trigger this if the shared pool rejected
	 * the execution by throwing a RejectedExecutionException.
	 */
	private RejectedExecutionHandler customRejectionHandler = null;

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, BlockingQueue<Runnable> queue, String threadNamePrefix )
	{
		this( corePoolSize, maximumPoolSize, keepAliveTime, unit, queue,
				new PrioThreadFactory( threadNamePrefix ), null );
	}

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, int queueSize, String threadNamePrefix )
	{
		this( corePoolSize, maximumPoolSize, keepAliveTime, unit, queueSize, null, threadNamePrefix );
	}

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			int queueSize, ThreadFactory threadFactory )
	{
		this( corePoolSize, maximumPoolSize, keepAliveTime, unit, queueSize, threadFactory, null );
	}

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			int queueSize, RejectedExecutionHandler handler, String threadNamePrefix )
	{
		this( corePoolSize, maximumPoolSize, keepAliveTime, unit, queueSize, new PrioThreadFactory( threadNamePrefix ),
				handler );
	}

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			int queueSize, ThreadFactory threadFactory, RejectedExecutionHandler handler )
	{
		this( corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<Runnable>( queueSize ),
				threadFactory, handler );
	}

	public CascadedThreadPoolExecutor( int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> queue, ThreadFactory threadFactory, RejectedExecutionHandler handler )
	{
		// Only in super() call pass defaultHandler, not in this() calls!
		super( corePoolSize, maximumPoolSize, keepAliveTime, unit, queue,
				threadFactory, defaultHandler );
		this.customRejectionHandler = handler;
	}

	@Override
	public void execute( Runnable command )
	{
		try {
			super.execute( command );
		} catch ( RejectedExecutionException e ) {
			if ( customRejectionHandler == null || ( customRejectionHandler.getClass().equals( AbortPolicy.class ) ) ) {
				throw e;
			} else {
				customRejectionHandler.rejectedExecution( command, this );
			}
		}
	}

}
