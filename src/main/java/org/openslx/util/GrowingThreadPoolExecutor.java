package org.openslx.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Grows to maximum pool size before queueing. See
 * http://stackoverflow.com/a/20153234/2043481
 */
public class GrowingThreadPoolExecutor extends ThreadPoolExecutor {
	private int userSpecifiedCorePoolSize;
	private int taskCount;
	
    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();

	public GrowingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, defaultHandler);
	}
	
	public GrowingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
	}

	public GrowingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(),
				handler);
	}
	
	public GrowingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		userSpecifiedCorePoolSize = corePoolSize;
	}

	@Override
	public void execute(Runnable runnable) {
		synchronized (this) {
			taskCount++;
			setCorePoolSizeToTaskCountWithinBounds();
		}
		super.execute(runnable);
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable) {
		super.afterExecute(runnable, throwable);
		synchronized (this) {
			taskCount--;
			setCorePoolSizeToTaskCountWithinBounds();
		}
	}

	private void setCorePoolSizeToTaskCountWithinBounds() {
		int threads = taskCount;
		if (threads < userSpecifiedCorePoolSize)
			threads = userSpecifiedCorePoolSize;
		if (threads > getMaximumPoolSize())
			threads = getMaximumPoolSize();
		super.setCorePoolSize(threads);
	}
	
	public void setCorePoolSize(int corePoolSize) {
		synchronized (this) {
			userSpecifiedCorePoolSize = corePoolSize;
		}
	}
	
	@Override
	public int getCorePoolSize() {
		synchronized (this) {
			return userSpecifiedCorePoolSize;
		}
	}
}