package org.openslx.util;

/**
 * A reference holder that will only return the object it is holding if
 * the given timeout has not been reached.
 * The timeout will start anew if you set this reference to hold a new object,
 * or if you retrieve the object, and refreshOnGet is set to true.
 */
public class TimeoutReference<T>
{

	private final long timeoutMs;
	private long deadline;
	private boolean refreshOnGet;
	private T item;

	public TimeoutReference( boolean refreshOnGet, long timeoutMs, T item )
	{
		this.refreshOnGet = refreshOnGet;
		this.item = item;
		this.timeoutMs = timeoutMs;
		this.deadline = System.currentTimeMillis() + timeoutMs;
	}

	public TimeoutReference( long timeoutMs, T item )
	{
		this( false, timeoutMs, item );
	}

	public synchronized T get()
	{
		if ( item != null ) {
			final long now = System.currentTimeMillis();
			if ( deadline < now ) {
				item = null;
			} else if ( refreshOnGet ) {
				deadline = now + timeoutMs;
			}
		}
		return item;
	}

	public synchronized void set( T newItem )
	{
		this.item = newItem;
		this.deadline = System.currentTimeMillis() + timeoutMs;
	}

}
