package org.openslx.network;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;

import org.apache.log4j.Logger;

import com.btr.proxy.search.wpad.WpadProxySearchStrategy;
import com.btr.proxy.util.ProxyException;

/**
 * Class for configuring proxy settings system wide, if necessary.
 * 
 * @author bjoern
 * 
 */
public class ProxyConfiguration
{
	private static final Logger log = Logger.getLogger( ProxyConfiguration.class );

	public static void configProxy()
	{
		// Reset proxy settings first
		ProxySelector.setDefault( null );
		Authenticator.setDefault( null );

		// Configuring proxy settings. First read options from config file.
		String proxyConfiguration = ProxyProperties.getProxyConf();

		if ( proxyConfiguration.equals( "AUTO" ) || proxyConfiguration.isEmpty() ) {
			log.info( "Configuring proxy settings automatically..." );
			// Configuring proxy settings automatically.
			WpadProxySearchStrategy wPSS = new WpadProxySearchStrategy();
			try {
				ProxySelector pS = wPSS.getProxySelector();
				ProxySelector.setDefault( pS );
			} catch ( ProxyException e ) {
				log.error( "Setting proxy configuration automatically failed.", e );
			}
			return;
		}

		if ( proxyConfiguration.equals( "YES" ) ) {
			// Take the proxy settings from config file.
			// First check if one of the following necessary options might not be set.
			if ( ProxyProperties.hasProxyAddress() ) {
				String proxyAddress = ProxyProperties.getProxyAddress();
				int proxyPort = ProxyProperties.getProxyPort();

				// Configure proxy.
				Proxy proxy = new Proxy( Proxy.Type.SOCKS, new InetSocketAddress( proxyAddress, proxyPort ) );
				StaticProxySelector sPS = new StaticProxySelector( proxy );
				ProxySelector.setDefault( sPS );

				if ( !ProxyProperties.hasProxyCredentials() ) {
					log.info( "Configuring proxy settings manually WITH authentication..." );
					// use Proxy with authentication.
					String proxyUname = ProxyProperties.getProxyUsername();
					String proxyPass = ProxyProperties.getProxyPassword();

					// Set authentication.
					StaticProxyAuthenticator sPA = new StaticProxyAuthenticator( proxyUname, proxyPass );
					Authenticator.setDefault( sPA );
				}
			}
		}

	}

}
