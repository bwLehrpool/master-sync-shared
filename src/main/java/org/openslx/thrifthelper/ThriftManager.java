package org.openslx.thrifthelper;

import java.lang.reflect.Proxy;

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

	private static ErrorCallback errorCallback = null;

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
							// ok lets do it
							TTransport transport = new TFramedTransport(
									new TSocket(
											SATELLITE_IP, SATELLITE_PORT, SATELLITE_TIMEOUT ) );
							try {
								transport.open();
							} catch ( TTransportException e ) {
								LOGGER.error( "Could not open transport to thrift's server with IP: " + SATELLITE_IP );
								transport.close();
								return null;
							}
							final TProtocol protocol = new TBinaryProtocol(
									transport );
							// now we are ready to create the client, according to ClientType!
							LOGGER.info( "Satellite '" + SATELLITE_IP + "' reachable. Client initialised." );
							return new SatelliteServer.Client(
									protocol );
						}

						@Override
						public boolean error( int failCount, String method, Throwable t )
						{
							return errorCallback != null && errorCallback.thriftError( failCount, method, t );
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
							// first check if we have a sat ip
							if ( MASTERSERVER_ADDRESS == null ) {
								LOGGER.error( "Master server adress was not set prior to getting the client. Use setMasterServerAddress(<addr>)." );
								return null;
							}
							// ok lets do it
							TTransport transport =
									new TFramedTransport(
											new TSocket(
													MASTERSERVER_ADDRESS, MASTERSERVER_PORT, MASTERSERVER_TIMEOUT ) );
							try {
								transport.open();
							} catch ( TTransportException e ) {
								LOGGER.error( "Could not open transport to thrift's server with IP: " + MASTERSERVER_ADDRESS );
								transport.close();
								return null;
							}
							final TProtocol protocol = new TBinaryProtocol(
									transport );
							// now we are ready to create the client, according to ClientType!
							return new MasterServer.Client(
									protocol );

						}

						@Override
						public boolean error( int failCount, String method, Throwable t )
						{
							synchronized ( LOGGER ) {
								return errorCallback != null && errorCallback.thriftError( failCount, method, t );
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
	 * thrift connections.
	 * 
	 * @param cb
	 */
	public static void setErrorCallback( ErrorCallback cb )
	{
		synchronized ( LOGGER ) {
			errorCallback = cb;
		}
	}
}
