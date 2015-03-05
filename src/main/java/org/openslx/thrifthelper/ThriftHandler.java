package org.openslx.thrifthelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

class ThriftHandler<T extends Object> implements InvocationHandler
{

	private final static Logger LOGGER = Logger.getLogger( ThriftHandler.class );

	public interface EventCallback<T>
	{
		public T getNewClient();

		public void error( Throwable t, String message );
	}

	private final ThreadLocal<T> clients = new ThreadLocal<T>();
	private final EventCallback<T> callback;

	public ThriftHandler( final Class<T> clazz, EventCallback<T> cb )
	{
		callback = cb;
		thriftMethods = Collections.unmodifiableSet( new HashSet<String>() {
			private static final long serialVersionUID = 8983506538154055231L;
			{
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
						add( name );
					}
				}
			}
		} );
	}

	private final Set<String> thriftMethods;

	public Object invoke( Object tproxy, Method method, Object[] args ) throws Throwable
	{

		// first find the thrift methods
		if ( !thriftMethods.contains( method.getName() ) ) {
			try {
				return method.invoke( method, args );
			} catch ( InvocationTargetException e ) {
				// TODO Auto-generated catch block
				Throwable cause = e.getCause();
				if ( cause == null ) {
					throw new RuntimeException();
				}
				throw cause;
			}
		}
		LOGGER.debug( "Proxying '" + method.getName() + "'" );

		T client = getClient( false );
		Throwable cause = null;
		for ( int i = 0; i < 3; i++ ) {
			try {
				return method.invoke( client, args );
			} catch ( InvocationTargetException e ) {
				cause = e.getCause();
				if ( cause instanceof TTransportException ) {
					LOGGER.debug( "Transport error - re-initialising ..." );
					// new client
					client = getClient( true );
				}
			}
		}
		
		// Uh oh
		callback.error( cause, "Could not reconnect to thrift server - network or server down?" );

		if ( cause != null )
			throw cause;
		return null;

	}

	private T getClient( boolean forceNew )
	{
		T client = clients.get();
		if ( client != null && !forceNew ) {
			return client;
		}
		client = callback.getNewClient();
		if ( client == null ) {
			// TODO own exception
			throw new RuntimeException();
		}
		clients.set( client );
		return client;
	}
}
