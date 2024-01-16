package org.openslx.thrifthelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.openslx.thrifthelper.ThriftManager.ErrorCallback;
import org.openslx.util.QuickTimer;
import org.openslx.util.QuickTimer.Task;

class ThriftHandler<T extends TServiceClient> implements InvocationHandler
{

	private final static Logger LOGGER = LogManager.getLogger( ThriftHandler.class );
	
	/**
	 * How long a client/connection should be allowed to be idle before we get rid of it and open a fresh one
	 */
	private final static long MAX_IDLE_MS = 110_000;

	protected interface WantClientCallback<T>
	{
		public T getNewClient();
	}

	/** Pool of (potentially) good connections, waiting for reuse */
	private final Deque<TWrap<T>> pool = new ArrayDeque<>();
	/** Old connections to be released by cleanup task */
	private Deque<TWrap<T>> trash = new ArrayDeque<>();

	private final WantClientCallback<? extends T> clientFactory;

	private final ErrorCallback errorCallback;

	private final Set<String> thriftMethods;

	public ThriftHandler( final Class<? extends T> clazz, WantClientCallback<? extends T> clientFactory, ErrorCallback errCb )
	{
		this.errorCallback = errCb;
		this.clientFactory = clientFactory;
		Set<String> tmpset = new HashSet<String>();
		Method[] methods = clazz.getMethods();
		// Iterate over all methods of this class
		for ( int i = 0; i < methods.length; i++ ) {
			boolean thrift = false;
			// If a method throws some form of TException, consider it a potential method from the thrift interface
			Class<?>[] type = methods[i].getExceptionTypes();
			for ( int e = 0; e < type.length; e++ ) {
				if ( TException.class.isAssignableFrom( type[e] ) ) {
					thrift = true;
					break;
				}
			}
			String name = methods[i].getName();
			if ( thrift && !name.startsWith( "send_" ) && !name.startsWith( "recv_" ) ) {
				// Exclude the send_ and recv_ helpers
				tmpset.add( name );
			}
		}
		thriftMethods = Collections.unmodifiableSet( tmpset );
		// Periodically scan for old connections, in case the application idles for extended periods of time...
		QuickTimer.scheduleAtFixedDelay( new Task() {
			@Override
			public void fire()
			{
				Deque<TWrap<T>> newTrash = new ArrayDeque<>();
				Deque<TWrap<T>> list;
				long now = System.currentTimeMillis();
				synchronized ( pool ) {
					list = trash;
					trash = newTrash;
					for ( Iterator<TWrap<T>> it = pool.iterator(); it.hasNext(); ) {
						TWrap<T> client = it.next();
						if ( client.deadline < now ) {
							list.add( client );
							it.remove();
						}
					}
				}
				for ( TWrap<T> client : list ) {
					freeClient( client );
				}
			}
		}, MAX_IDLE_MS * 5, MAX_IDLE_MS * 5 );
	}

	@Override
	public Object invoke( Object tproxy, Method method, Object[] args ) throws Throwable
	{

		// first find the thrift methods
		if ( !thriftMethods.contains( method.getName() ) ) {
			throw new IllegalAccessException( "Cannot call this method on a proxied thrift client" );
		}

		TWrap<T> clientWrap = getClient();
		try {
			Throwable cause = null;
			for ( int i = 1; clientWrap != null; i++ ) {
				try {
					return method.invoke( clientWrap.client, args );
				} catch ( InvocationTargetException e ) {
					cause = e.getCause(); // Get original exception
					if ( cause != null && ! ( cause instanceof TTransportException )
							&& ! ( cause instanceof TProtocolException ) ) {
						// If it's not an exception potentially hinting at dead connection, just pass it on
						throw cause;
					}
					// Get rid of broken connection
					freeClient( clientWrap );
					clientWrap = null;
					if ( cause == null ) {
						cause = e;
					}
				}
				// Call the error callback. As long as true is returned, keep retrying
				if ( !errorCallback.thriftError( i, method.getName(), cause ) ) {
					break;
				}
				// Apparently we should retry, get another client
				if ( clientWrap == null ) {
					clientWrap = getClient();
					cause = null;
				}
			}

			// Uh oh
			if ( cause != null )
				throw cause;
			throw new TTransportException( "Could not connect" );
		} finally {
			returnClient( clientWrap );
		}
	}

	private void freeClient( TWrap<T> client )
	{
		try {
			client.client.getInputProtocol().getTransport().close();
		} catch ( Exception e ) {
		}
		try {
			client.client.getOutputProtocol().getTransport().close();
		} catch ( Exception e ) {
		}

	}

	/**
	 * Get an available client connection. Prefer connection from pool,
	 * honoring the max idle time. If none are available, create a fresh
	 * client connection.
	 */
	private TWrap<T> getClient()
	{
		long now = System.currentTimeMillis();
		synchronized ( pool ) {
			TWrap<T> client;
			while ( ( client = pool.poll() ) != null ) {
				if ( client.deadline >= now )
					return client; // Still fresh
				trash.add( client );
			}
		}
		// No usable existing connection, create new client
		LOGGER.debug( "Creating new thrift client" );
		T client = clientFactory.getNewClient();
		if ( client == null )
			return null;
		return new TWrap<T>( client );
	}

	/**
	 * Return a client connection to the pool, updating its last
	 * use timestamp for proper idle timeout handling.
 	 */
	private void returnClient( TWrap<T> client )
	{
		if ( client == null )
			return;
		client.deadline = System.currentTimeMillis() + MAX_IDLE_MS;
		synchronized ( pool ) {
			pool.push( client );
		}
	}
	
	private static class TWrap<T>
	{
		private final T client;
		private long deadline;
		public TWrap(T client)
		{
			this.client = client;
		}
	}

}
