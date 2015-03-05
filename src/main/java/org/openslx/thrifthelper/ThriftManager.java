package org.openslx.thrifthelper;

import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.openslx.imagemaster.thrift.iface.ImageServer;
import org.openslx.sat.thrift.iface.Server;
import org.openslx.thrifthelper.ThriftHandler.EventCallback;

public class ThriftManager
{

	private final static Logger LOGGER = Logger.getLogger( ThriftManager.class );

	public interface ErrorCallback
	{
		/**
		 * Called if connecting/reconnecting to the thrift server failed.
		 * 
		 * @param t the exception that occured last (may be null)
		 * @param message an optional message describing the circumstances
		 */
		public void thriftError( Throwable t, String message );
	}

	private static ErrorCallback _errorCallback = null;

	/**
	 * Private members for master connection information
	 */
	private static final String MASTERSERVER_ADDRESS = "bwlp-masterserver.ruf.uni-freiburg.de";
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
	private static Server.Iface _satClient = null;

	/**
	 * Master connection. As its address is known in advance, create the object right away.
	 */
	private static ImageServer.Iface _masterClient = (ImageServer.Iface)Proxy.newProxyInstance(
			ImageServer.Iface.class.getClassLoader(),
			new Class[] { ImageServer.Iface.class }, new ThriftHandler<ImageServer.Client>( ImageServer.Client.class, new EventCallback<ImageServer.Client>() {

				@Override
				public ImageServer.Client getNewClient()
				{
					// ok lets do it
					TTransport transport =
							new TFramedTransport( new TSocket( MASTERSERVER_ADDRESS, MASTERSERVER_PORT, MASTERSERVER_TIMEOUT ) );
					try {
						transport.open();
					} catch ( TTransportException e ) {
						LOGGER.error( "Could not open transport to thrift's server with IP: " + MASTERSERVER_ADDRESS );
						transport.close();
						return null;
					}
					final TProtocol protocol = new TBinaryProtocol( transport );
					// now we are ready to create the client, according to ClientType!
					return new ImageServer.Client( protocol );

				}

				@Override
				public void error( Throwable t, String message )
				{
					synchronized ( LOGGER ) {
						if ( _errorCallback != null )
							_errorCallback.thriftError( t, message );
					}
				}
			} ) );

	/**
	 * IP Validation Regex
	 */
	private static final String IP_VALID_PATTERN =
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	/**
	 * Sets the IP of the satellite to connect to
	 * 
	 * @param ip the ip of the satellite as String
	 * @return true if setting the ip worked, false otherwise
	 */
	public static boolean setSatellite( String ip )
	{
		if ( ip.isEmpty() ) {
			LOGGER.error( "Given IP for satellite is empty." );
			return false;
		}
		// validate
		if ( !ip.matches( IP_VALID_PATTERN ) ) {
			LOGGER.error( "Given form of IP is invalid: " + ip );
			return false;
		}
		// finally set it
		SATELLITE_IP = ip;

		// Create monster proxy class from interface
		_satClient = (Server.Iface)Proxy.newProxyInstance(
				Server.Iface.class.getClassLoader(),
				new Class[] { Server.Iface.class }, new ThriftHandler<Server.Client>( Server.Client.class, new EventCallback<Server.Client>() {

					@Override
					public Server.Client getNewClient()
					{
						// first check if we have a sat ip
						if ( SATELLITE_IP == null ) {
							LOGGER.error( "Satellite ip adress was not set prior to getting the sat client. Use setSatellite(<ip>)." );
							return null;
						}
						// ok lets do it
						TTransport transport =
								new TSocket( SATELLITE_IP, SATELLITE_PORT, SATELLITE_TIMEOUT );
						try {
							transport.open();
						} catch ( TTransportException e ) {
							LOGGER.error( "Could not open transport to thrift's server with IP: " + SATELLITE_IP );
							transport.close();
							return null;
						}
						final TProtocol protocol = new TBinaryProtocol( transport );
						// now we are ready to create the client, according to ClientType!
						LOGGER.info( "Satellite '" + SATELLITE_IP + "' reachable. Client initialised." );
						return new Server.Client( protocol );
					}

					@Override
					public void error( Throwable t, String message )
					{
						synchronized ( LOGGER ) {
							if ( _errorCallback != null )
								_errorCallback.thriftError( t, message );
						}
					}
				} ) );
		return true;
	}

	/**
	 * Returns the singleton client of the thrift connection to the satellite
	 * 
	 * @return the thrift client to the satellite server
	 */
	public static Server.Iface getSatClient()
	{
		return _satClient;
	}

	/**
	 * Returns the singleton client of the master thrift connection
	 * 
	 * @return the thrift client to the master server
	 */
	public static ImageServer.Iface getMasterClient()
	{
		return _masterClient;
	}

	/**
	 * Set the callback class for errors that occur on one of the
	 * thrift connections.
	 * 
	 * @param cb
	 */
	public static void setErrorCallback( ErrorCallback cb )
	{
		synchronized ( LOGGER ) {
			_errorCallback = cb;
		}
	}
}
