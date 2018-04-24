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
	private final boolean refreshOnGet;
	private final T item;
	private long deadline;
	private boolean invalid;

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
		if ( item == null || invalid )
			return null;
		final long now = System.currentTimeMillis();
		if ( deadline < now ) {
			invalid = true;
			return null;
		}
		if ( refreshOnGet ) {
			deadline = now + timeoutMs;
		}
		return item;
	}
	
	synchronized boolean isInvalid()
	{
		return invalid;
	}

	@Override
	public int hashCode()
	{
		return item == null ? super.hashCode() : item.hashCode();
	}

	@Override
	public boolean equals( Object o )
	{
		return ( this == o || this.item == o || ( o != null && o.equals( this.item ) ) );
	}

}
