package org.openslx.util;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

public abstract class GenericDataCache<T>
{

	private static final Logger LOGGER = Logger.getLogger( GenericDataCache.class );

	/**
	 * How long the cached data is valid after fetching
	 */
	private final int validMs;

	/**
	 * Deadline when the cache goes invalid
	 */
	private long validUntil = 0;

	/**
	 * The data being held
	 */
	private final AtomicReference<T> item = new AtomicReference<>();

	public GenericDataCache( int validMs )
	{
		this.validMs = validMs;
	}

	/**
	 * Get the cached object, but refresh the cache first if
	 * the cached instance is too old.
	 * 
	 * @return
	 */
	public T get()
	{
		return get( CacheMode.DEFAULT );
	}

	/**
	 * Get the cached object, using the given cache access strategy.
	 * ALWAYS_CACHED: Never refresh the cache, except if it has never been fetched before
	 * DEFAULT: Only fetch from remote if the cached value is too old
	 * NEVER_CACHED: Always fetch from remote. If it fails, return null
	 * 
	 * @param mode Cache access strategy as described above
	 * @return T
	 */
	public T get( CacheMode mode )
	{
		switch ( mode ) {
		case FORCE_CACHED:
			break;
		case PREFER_CACHED:
			if ( validUntil == 0 )
				ensureUpToDate( true );
			break;
		case DEFAULT:
			ensureUpToDate( false );
			break;
		case NEVER_CACHED:
			if ( !ensureUpToDate( true ) )
				return null;
			break;
		}
		return item.get();
	}

	private synchronized boolean ensureUpToDate( boolean force )
	{
		final long now = System.currentTimeMillis();
		if ( !force && now < validUntil )
			return true;
		T fetched;
		try {
			fetched = update();
			if ( fetched == null )
				return false;
		} catch ( Exception e ) {
			LOGGER.warn( "Could not fetch fresh data", e );
			return false;
		}
		item.set( fetched );
		validUntil = now + validMs;
		return true;
	}

	protected abstract T update() throws Exception;

	//

	public static enum CacheMode
	{
		/**
		 * Use cache, even if the item has never been fetched before.
		 */
		FORCE_CACHED,
		/**
		 * Use cache if it's not empty, no matter how old it is.
		 */
		PREFER_CACHED,
		/**
		 * Obey the cache timeout value of this cache.
		 */
		DEFAULT,
		/**
		 * Always fetch a fresh instance of the item.
		 */
		NEVER_CACHED
	}

}
