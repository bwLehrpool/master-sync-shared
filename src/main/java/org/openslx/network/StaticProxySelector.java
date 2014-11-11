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
import java.util.List;

import org.apache.log4j.Logger;

public class StaticProxySelector extends ProxySelector
{
	private static Logger log = Logger.getLogger( StaticProxySelector.class );

	private final Proxy proxy;

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

		log.info( "Connect to: " + host );

		List<NetworkInterface> nWI = getNetworkInterfaces();

		if ( nWI != null ) {
			// iterate over network interfaces and check for InetAddresses.
			for ( int i = 0; i < nWI.size(); ++i ) {
				Enumeration<InetAddress> e = nWI.get( i ).getInetAddresses();
				// iterate over InetAddresses of current interface.
				while ( e.hasMoreElements() ) {
					InetAddress address = (InetAddress)e.nextElement();
					// Add proxy to list, if host do not equals to address.
					if ( ! ( host.equals( address ) ) &&
							! ( host.startsWith( "127." ) ) &&
							! ( host.equals( "localhost" ) ) ) {
						proxyList.add( this.proxy );
					}
				}
			}
		} else if ( ! ( host.startsWith( "127." ) ) && ! ( host.equals( "localhost" ) ) ) {
			proxyList.add( this.proxy );
		}
		// log.info( "proxyList: " + proxyList.toString() );
		return proxyList;
	}

	// Getting ArrayList with all NetworkInterfaces.
	private ArrayList<NetworkInterface> getNetworkInterfaces()
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
			retList.add( (NetworkInterface)e.nextElement() );
		}
		return retList;
	}

}
