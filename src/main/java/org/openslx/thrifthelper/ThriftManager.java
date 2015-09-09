package org.openslx.thrifthelper;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.openslx.bwlp.thrift.iface.MasterServer;
import org.openslx.bwlp.thrift.iface.SatelliteServer;
import org.openslx.thrifthelper.ThriftHandler.WantClientCallback;
import org.openslx.util.Util;

public class ThriftManager<T>
{

	private final static Logger LOGGER = Logger.getLogger( ThriftManager.class );

	public interface ErrorCallback
	{
		/**
		 * Called if connecting/reconnecting to the thrift server failed.
		 * 
		 * @param failCount how many failures occured for this call so far
		 * @param method name of method that failed
		 * @param t the exception that occured (may be null)
		 * @return true if we should retry, false otherwise
		 */
		public boolean thriftError( int failCount, String method, Throwable t );
	}

	private final T client;

	@SuppressWarnings( "unchecked" )
	private ThriftManager( Class<T> ifClazz, Class<? extends TServiceClient> clientClazz,
			WantClientCallback<? extends TServiceClient> internalCallback, ErrorCallback errorCb )
	{
		this.client = (T)Proxy.newProxyInstance(
				ifClazz.getClassLoader(),
				new Class[] { ifClazz }, new ThriftHandler<TServiceClient>(
						clientClazz, internalCallback, errorCb ) );
	}

	private static ThriftManager<MasterServer.Iface> masterManager = null;
	private static ThriftManager<SatelliteServer.Iface> satelliteManager = null;

	private static ErrorCallback satErrorCallback = null;
	private static ErrorCallback masterErrorCallback = null;

	/**
	 * Sets the address of the master server
	 * 
	 * @param host the ip/hostname of the master server
	 * @return true if setting the address worked, false otherwise
	 */
	public static synchronized boolean setMasterServerAddress( final SSLContext ctx, final String host, final int port, final int timeout )
	{
		if ( masterManager != null ) {
			LOGGER.error( "Master server address already set." );
			return false;
		}
		if ( host.isEmpty() ) {
			LOGGER.error( "Given address is empty." );
			return false;
		}
		// finally set it
		masterManager = new ThriftManager<MasterServer.Iface>( MasterServer.Iface.class, MasterServer.Client.class,
				new WantClientCallback<MasterServer.Client>() {
					@Override
					public MasterServer.Client getNewClient()
					{
						return getNewMasterClient( ctx, host, port, timeout );
					}
				}, new ErrorCallback() {
					@Override
					public boolean thriftError( int failCount, String method, Throwable t )
					{
						return masterErrorCallback != null && masterErrorCallback.thriftError( failCount, method, t );
					}
				} );
		return true;
	}

	/**
	 * Sets the IP of the satellite to connect to
	 * 
	 * @param host the ip/hostname of the satellite
	 * @return true if setting the address worked, false otherwise
	 */
	public static synchronized boolean setSatelliteAddress( final SSLContext ctx, final String host, final int port, final int timeout )
	{
		if ( satelliteManager != null ) {
			LOGGER.error( "Satellite server address already set." );
			return false;
		}
		if ( host.isEmpty() ) {
			LOGGER.error( "Given address is empty." );
			return false;
		}
		// finally set it
		satelliteManager = new ThriftManager<SatelliteServer.Iface>( SatelliteServer.Iface.class, SatelliteServer.Client.class,
				new WantClientCallback<SatelliteServer.Client>() {
					@Override
					public SatelliteServer.Client getNewClient()
					{
						return getNewSatelliteClient( ctx, host, port, timeout );
					}
				}, new ErrorCallback() {
					@Override
					public boolean thriftError( int failCount, String method, Throwable t )
					{
						return satErrorCallback != null && satErrorCallback.thriftError( failCount, method, t );
					}
				} );
		return true;
	}

	/**
	 * Returns the singleton client of the thrift connection to the satellite
	 * 
	 * @return the thrift client to the satellite server
	 */
	public static SatelliteServer.Iface getSatClient()
	{
		if ( satelliteManager == null ) {
			LOGGER.error( "Satellite server adress was not set prior to getting the client. Use setMasterServerAddress(<addr>)." );
			return null;
		}
		return satelliteManager.client;
	}

	/**
	 * Returns the singleton client of the master thrift connection
	 * 
	 * @return the thrift client to the master server
	 */
	public static MasterServer.Iface getMasterClient()
	{
		if ( masterManager == null ) {
			LOGGER.error( "Master server adress was not set prior to getting the client. Use setMasterServerAddress(<addr>)." );
			return null;
		}
		return masterManager.client;
	}

	/**
	 * Set the callback class for errors that occur on one of the
	 * thrift connections to the master server.
	 * 
	 * @param cb
	 */
	public static synchronized void setMasterErrorCallback( ErrorCallback cb )
	{
		masterErrorCallback = cb;
	}

	/**
	 * Set the callback class for errors that occur on one of the
	 * thrift connections to the satellite server.
	 * 
	 * @param cb
	 */
	public static synchronized void setSatelliteErrorCallback( ErrorCallback cb )
	{
		satErrorCallback = cb;
	}

	public static MasterServer.Client getNewMasterClient( SSLContext ctx, String address, int port, int timeout )
	{
		TProtocol protocol = newTransport( ctx, address, port, timeout );
		if ( protocol == null )
			return null;
		return new MasterServer.Client( protocol );
	}

	public static SatelliteServer.Client getNewSatelliteClient( SSLContext ctx, String address, int port, int timeout )
	{
		TProtocol protocol = newTransport( ctx, address, port, timeout );
		if ( protocol == null )
			return null;
		return new SatelliteServer.Client( protocol );
	}

	private static TProtocol newTransport( SSLContext ctx, String host, int port, int timeout )
	{
		try {
			TSocket tsock;
			Socket socket = null;
			try {
				if ( ctx == null ) {
					socket = SocketFactory.getDefault().createSocket();
				} else {
					socket = ctx.getSocketFactory().createSocket();
				}
				socket.connect( new InetSocketAddress( host, port ), 4000 );
				socket.setSoTimeout( timeout );
			} catch ( IOException e ) {
				if ( socket != null ) {
					Util.safeClose( socket );
				}
				throw new TTransportException();
			}
			tsock = new TSocket( socket );
			return new TBinaryProtocol( new TFramedTransport( tsock ) );
		} catch ( TTransportException e ) {
			LOGGER.error( "Could not open transport to thrift server at " + host + ":" + port );
			return null;
		}
	}

}
