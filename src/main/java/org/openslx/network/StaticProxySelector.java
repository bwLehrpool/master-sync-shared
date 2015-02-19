package org.openslx.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class StaticProxySelector extends ProxySelector
{
	private static Logger log = Logger.getLogger( StaticProxySelector.class );

	private final Proxy proxy;
	private Set<String> localAddresses = null;
	private long nextAddressGet = 0;

	public StaticProxySelector( Proxy proxy )
	{
		this.proxy = proxy;
	}

	@Override
	public void connectFailed( URI uri, SocketAddress sa, IOException ioe )
	{
		// Just one fix proxy. So no code is necessary here for deactivating proxy.
	}

	@Override
	public List<Proxy> select( URI uri )
	{
		List<Proxy> proxyList = new ArrayList<Proxy>();

		String host = uri.getHost();
		if ( host == null ) // Host not set? Well, we can only guess then, so try to use the proxy
			return proxyList;

		host = host.replaceFirst( "%\\d+$", "" );
		if ( host.equals( "localhost" ) || host.startsWith( "127." )
				|| host.startsWith( "::1" ) || host.startsWith( "0:0:0:0:0:0:0:1" ) ) // Localhost = no proxy
			return proxyList;

		final Set<String> addrs;
		synchronized ( this ) {
			addrs = getLocalAddresses();
		}
		if ( !addrs.contains( host ) ) {
			proxyList.add( this.proxy );
		}

		return proxyList;
	}

	/**
	 * Get all local (IP) addresses
	 * 
	 * @return
	 */
	private Set<String> getLocalAddresses()
	{
		long now = System.currentTimeMillis();
		if ( now < nextAddressGet )
			return localAddresses;
		nextAddressGet = now + 60000;

		List<NetworkInterface> interfaces = getNetworkInterfaces();
		if ( interfaces == null )
			return localAddresses; // Fallback on last known data
		// iterate over network interfaces and get all addresses
		Set<String> addrs = new HashSet<>();
		for ( NetworkInterface iface : interfaces ) {
			Enumeration<InetAddress> e = iface.getInetAddresses();
			// iterate over InetAddresses of current interface
			while ( e.hasMoreElements() ) {
				addrs.add( e.nextElement().getHostAddress().replaceFirst( "%\\d+$", "" ) );
			}
		}
		synchronized ( this ) {
			localAddresses = addrs;
		}
		return localAddresses;
	}

	/**
	 * Get a list of all local network interfaces
	 * 
	 * @return
	 */
	private List<NetworkInterface> getNetworkInterfaces()
	{
		ArrayList<NetworkInterface> retList = new ArrayList<NetworkInterface>();
		Enumeration<NetworkInterface> e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch ( SocketException e1 ) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		while ( e.hasMoreElements() ) {
			retList.add( e.nextElement() );
		}
		return retList;
	}

}
