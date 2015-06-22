package org.openslx.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TimeoutHashMap<K, V> implements Map<K, V>
{

	private final Map<K, TimeoutReference<V>> map;
	private final long timeout;

	public TimeoutHashMap( long timeout )
	{
		this.map = new HashMap<>();
		this.timeout = timeout;
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey( Object key )
	{
		return map.containsKey( key );
	}

	@Override
	public boolean containsValue( Object value )
	{
		return map.containsValue( value );
	}

	@Override
	public V get( Object key )
	{
		TimeoutReference<V> timeoutReference = map.get( key );
		if ( timeoutReference == null )
			return null;
		return timeoutReference.get();
	}

	@Override
	public V put( K key, V value )
	{
		map.put( key, new TimeoutReference<V>(
				false, timeout, value ) );
		return value;
	}

	@Override
	public V remove( Object key )
	{
		TimeoutReference<V> remove = map.remove( key );
		if ( remove == null )
			return null;
		return remove.get();
	}

	@Override
	public void putAll( Map<? extends K, ? extends V> m )
	{
		for ( java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet() ) {
			put( entry.getKey(), entry.getValue() );
		}
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public Collection<V> values()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

}
