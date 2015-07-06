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

		public boolean error( int failCount, String method, Throwable t );
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

	@Override
	public Object invoke( Object tproxy, Method method, Object[] args ) throws Throwable
	{

		// first find the thrift methods
		if ( !thriftMethods.contains( method.getName() ) ) {
			try {
				return method.invoke( getClient( false ), args );
			} catch ( InvocationTargetException e ) {
				Throwable cause = e.getCause();
				if ( cause == null ) {
					cause = e;
				}
				throw cause;
			}
		}
		LOGGER.debug( "Proxying '" + method.getName() + "'" );

		T client = getClient( false );
		Throwable cause = null;
		for ( int i = 1; ; i++ ) {
			if ( client == null ) {
				LOGGER.debug( "Transport error - re-initialising ..." );
				client = getClient( true );
				if ( client == null )
					continue;
			}
			try {
				return method.invoke( client, args );
			} catch ( InvocationTargetException e ) {
				cause = e.getCause();
				if ( cause != null && ! ( cause instanceof TTransportException ) )
					throw cause;
				client = null;
				if ( cause == null )
					cause = e;
			}
			if ( !callback.error( i, method.getName(), cause ) )
				break;
		}

		// Uh oh
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
		clients.set( client );
		return client;
	}
}
