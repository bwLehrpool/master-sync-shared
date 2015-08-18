package org.openslx.thrifthelper;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.openslx.bwlp.thrift.iface.MasterServer;
import org.openslx.bwlp.thrift.iface.SatelliteServer;
import org.openslx.thrifthelper.ThriftHandler.EventCallback;
import org.openslx.util.Util;

public class ThriftManager
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

	private static ErrorCallback masterErrorCallback = null;
	
	private static ErrorCallback satelliteErrorCallback = null;

	/**
	 * Private members for master connection information
	 */
	private static String MASTERSERVER_ADDRESS = null;
	private static final int MASTERSERVER_PORT = 9090;
	private static final int MASTERSERVER_TIMEOUT = 15000;

	/**
	 * Private members for satellite connection information
	 */
	private static String SATELLITE_IP = null;
	private static final int SATELLITE_PORT = 9090;
	private static final int SATELLITE_TIMEOUT = 15000;

	/**
	 * Sat connection. Initialized when we know the sat server IP.
	 */
	private static SatelliteServer.Iface satClient = (SatelliteServer.Iface)Proxy.newProxyInstance(
			SatelliteServer.Iface.class.getClassLoader(),
			new Class[] { SatelliteServer.Iface.class }, new ThriftHandler<SatelliteServer.Client>(
					SatelliteServer.Client.class, new EventCallback<SatelliteServer.Client>() {

						@Override
						public SatelliteServer.Client getNewClient()
						{
							// first check if we have a sat ip
							if ( SATELLITE_IP == null ) {
								LOGGER.error( "Satellite ip adress was not set prior to getting the sat client. Use setSatelliteAddress(<addr>)." );
								return null;
							}
							return getNewSatClient( SATELLITE_IP );
						}

						@Override
						public boolean error( int failCount, String method, Throwable t )
						{
							return satelliteErrorCallback != null && satelliteErrorCallback.thriftError( failCount, method, t );
						}
					} ) );

	/**
	 * Master connection. As its address is known in advance, create the object right away.
	 */
	private static MasterServer.Iface masterClient = (MasterServer.Iface)Proxy.newProxyInstance(
			MasterServer.Iface.class.getClassLoader(),
			new Class[] { MasterServer.Iface.class }, new ThriftHandler<MasterServer.Client>(
					MasterServer.Client.class, new EventCallback<MasterServer.Client>() {

						@Override
						public MasterServer.Client getNewClient()
						{
							return getNewMasterClient();

						}

						@Override
						public boolean error( int failCount, String method, Throwable t )
						{
							synchronized ( LOGGER ) {
								return masterErrorCallback != null && masterErrorCallback.thriftError( failCount, method, t );
							}
						}
					} ) );

	/**
	 * Sets the address of the master server
	 * 
	 * @param host the ip/hostname of the master server
	 * @return true if setting the address worked, false otherwise
	 */
	public static boolean setMasterServerAddress( String host )
	{
		if ( MASTERSERVER_ADDRESS != null ) {
			LOGGER.error( "Master server address already set." );
			return false;
		}
		if ( host.isEmpty() ) {
			LOGGER.error( "Given address is empty." );
			return false;
		}
		// finally set it
		MASTERSERVER_ADDRESS = host;
		return true;
	}

	/**
	 * Sets the IP of the satellite to connect to
	 * 
	 * @param host the ip/hostname of the satellite
	 * @return true if setting the address worked, false otherwise
	 */
	public static boolean setSatelliteAddress( String host )
	{
		if ( SATELLITE_IP != null ) {
			LOGGER.error( "Satellite address already set." );
			return false;
		}
		if ( host.isEmpty() ) {
			LOGGER.error( "Given address for satellite is empty." );
			return false;
		}
		// finally set it
		SATELLITE_IP = host;
		return true;
	}

	/**
	 * Returns the singleton client of the thrift connection to the satellite
	 * 
	 * @return the thrift client to the satellite server
	 */
	public static SatelliteServer.Iface getSatClient()
	{
		return satClient;
	}

	/**
	 * Returns the singleton client of the master thrift connection
	 * 
	 * @return the thrift client to the master server
	 */
	public static MasterServer.Iface getMasterClient()
	{
		return masterClient;
	}

	/**
	 * Set the callback class for errors that occur on one of the
	 * thrift connections to the master server.
	 * 
	 * @param cb
	 */
	public static void setMasterErrorCallback( ErrorCallback cb )
	{
		synchronized ( LOGGER ) {
			masterErrorCallback = cb;
		}
	}

	/**
	 * Set the callback class for errors that occur on one of the
	 * thrift connections to the satellite server.
	 * 
	 * @param cb
	 */
	public static void setSatelliteErrorCallback( ErrorCallback cb )
	{
		synchronized ( LOGGER ) {
			satelliteErrorCallback = cb;
		}
	}

	public static SatelliteServer.Client getNewSatClient( String satelliteIp )
	{
		TTransport transport = null;
		try {
			transport = newTransport( null, satelliteIp, SATELLITE_PORT, SATELLITE_TIMEOUT );
		} catch ( TTransportException e ) {
			LOGGER.error( "Could not open transport to thrift's server with IP: " + satelliteIp );
			return null;
		}
		final TProtocol protocol = new TBinaryProtocol( transport );
		// now we are ready to create the client, according to ClientType!
		LOGGER.info( "Satellite '" + satelliteIp + "' reachable. Client initialised." );
		return new SatelliteServer.Client( protocol );
	}
	
	public static MasterServer.Client getNewMasterClient() {
		// first check if we have a sat ip
		if ( MASTERSERVER_ADDRESS == null ) {
			LOGGER.error( "Master server adress was not set prior to getting the client. Use setMasterServerAddress(<addr>)." );
			return null;
		}
		
		TTransport transport;
		try {
			transport = newTransport( null, MASTERSERVER_ADDRESS, MASTERSERVER_PORT, MASTERSERVER_TIMEOUT );
		} catch ( TTransportException e ) {
			LOGGER.error( "Could not open transport to thrift's server with IP: " + MASTERSERVER_ADDRESS );
			return null;
		}
		final TProtocol protocol = new TBinaryProtocol(
				transport );
		// now we are ready to create the client, according to ClientType!
		return new MasterServer.Client( protocol );
	}
	
	private static TTransport newTransport( SSLContext ctx, String host, int port, int timeout ) throws TTransportException {
		TSocket tsock;
		if (ctx == null) {
			tsock = new TSocket( host, port, timeout );
			tsock.open();
		} else {
			Socket socket = null;
			try {
				socket = ctx.getSocketFactory().createSocket();
				socket.setSoTimeout(timeout);
				socket.connect( new InetSocketAddress( host, port ), timeout );
			} catch (IOException e) {
				if ( socket != null ) {
					Util.safeClose( socket );
				}
				throw new TTransportException();
			}
			tsock = new TSocket( socket );
		}
		return new TFramedTransport( tsock );
	}

}
