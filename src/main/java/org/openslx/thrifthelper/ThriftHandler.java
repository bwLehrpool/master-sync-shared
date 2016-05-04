package org.openslx.thrifthelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.transport.TTransportException;
import org.openslx.thrifthelper.ThriftManager.ErrorCallback;

class ThriftHandler<T extends TServiceClient> implements InvocationHandler
{

	private final static Logger LOGGER = Logger.getLogger( ThriftHandler.class );

	protected interface WantClientCallback<T>
	{
		public T getNewClient();
	}

	private final Deque<T> pool = new ArrayDeque<>();

	private final WantClientCallback<? extends T> callback;

	private final ErrorCallback errorCallback;

	private final Set<String> thriftMethods;

	public ThriftHandler( final Class<? extends T> clazz, WantClientCallback<? extends T> cb, ErrorCallback errCb )
	{
		errorCallback = errCb;
		callback = cb;
		Set<String> tmpset = new HashSet<String>();
		Method[] methods = clazz.getMethods();
		for ( int i = 0; i < methods.length; i++ ) {
			boolean thrift = false;
			Class<?>[] type = methods[i].getExceptionTypes();
			for ( int e = 0; e < type.length; e++ ) {
				if ( TException.class.isAssignableFrom( type[e] ) )
					thrift = true;
			}
			String name = methods[i].getName();
			if ( thrift && !name.startsWith( "send_" ) && !name.startsWith( "recv_" ) ) {
				tmpset.add( name );
			}
		}
		thriftMethods = Collections.unmodifiableSet( tmpset );
	}

	@Override
	public Object invoke( Object tproxy, Method method, Object[] args ) throws Throwable
	{

		// first find the thrift methods
		if ( !thriftMethods.contains( method.getName() ) ) {
			throw new IllegalAccessException( "Cannot call this method on a proxied thrift client" );
		}

		T client = getClient();
		try {
			Throwable cause = null;
			for ( int i = 1;; i++ ) {
				if ( client != null ) {
					try {
						return method.invoke( client, args );
					} catch ( InvocationTargetException e ) {
						cause = e.getCause();
						if ( cause != null && ! ( cause instanceof TException ) ) {
							throw cause;
						}
						freeClient( client );
						client = null;
						if ( cause == null ) {
							cause = e;
						}
					}
				}
				// Call the error callback. As long as true is returned, keep retrying
				if ( !errorCallback.thriftError( i, method.getName(), cause ) ) {
					break;
				}
				if ( client == null ) {
					client = getClient();
					cause = null;
				}
			}

			// Uh oh
			if ( cause != null )
				throw cause;
			throw new TTransportException( "Could not connect" );
		} finally {
			returnClient( client );
		}
	}

	private void freeClient( T client )
	{
		try {
			client.getInputProtocol().getTransport().close();
		} catch ( Exception e ) {
		}
		try {
			client.getOutputProtocol().getTransport().close();
		} catch ( Exception e ) {
		}

	}

	private T getClient()
	{
		T client;
		synchronized ( pool ) {
			client = pool.poll();
			if ( client != null ) {
				return client;
			}
		}
		// Pool is closed, create new client
		LOGGER.debug( "Creating new thrift client" );
		return callback.getNewClient();
	}

	private void returnClient( T client )
	{
		if ( client == null )
			return;
		synchronized ( pool ) {
			pool.push( client );
		}
	}

}
