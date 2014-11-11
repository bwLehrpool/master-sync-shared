package org.openslx.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
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

		log.info( "Host: " + host );

		// If host equals localhost return empty list.
		if ( ! ( host.startsWith( "127." ) ) && ! ( host.equals( "localhost" ) ) ) {
			// log.info("host.startsWith(127.): " + host.startsWith( "127." ));
			// log.info( "host.equals(localhost): " + host.equals("localhost"));
			log.info( "Adding proxy to proxyList" );
			proxyList.add( this.proxy );
		}
		// log.info( "proxyList: " + proxyList.toString() );
		return proxyList;
	}

}
