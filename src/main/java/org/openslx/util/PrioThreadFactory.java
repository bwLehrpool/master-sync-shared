package org.openslx.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PrioThreadFactory implements ThreadFactory
{

	private final AtomicInteger counter = new AtomicInteger();
	private final String name;
	private final int priority;

	public PrioThreadFactory( String name, int priority )
	{
		this.name = name;
		this.priority = priority;
	}

	public PrioThreadFactory( String name )
	{
		this( name, Thread.NORM_PRIORITY );
	}

	@Override
	public Thread newThread( Runnable r )
	{
		Thread thread = new Thread( r, name + "-" + counter.incrementAndGet() );
		thread.setPriority( priority );
		return thread;
	}

}
