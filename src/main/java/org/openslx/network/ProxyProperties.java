package org.openslx.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openslx.util.Util;

public class ProxyProperties
{
	private static Logger log = Logger.getLogger( ProxyProperties.class );
	private static final Properties properties = new Properties();

	// Getting the proxy settings from config file stored in
	// "/opt/openslx/proxy/conf".
	public static String getProxyConf()
	{
		return properties.getProperty( "PROXY_CONF", "" );
	}

	public static String getProxyAddress()
	{
		return properties.getProperty( "PROXY_ADDR", "" );
	}

	public static String getProxyUsername()
	{
		return properties.getProperty( "PROXY_USERNAME", "" );
	}

	public static String getProxyPassword()
	{
		return properties.getProperty( "PROXY_PASSWORD", "" );
	}

	// Integers //
	public static int getProxyPort()
	{
		return Util.parseInt( properties.getProperty( "PROXY_PORT", "0" ), 0 );
	}

	static
	{
		load();
	}

	/**
	 * Load properties
	 */
	public static void load()
	{
		InputStreamReader stream = null;
		try {
			properties.clear();
			// Load all entries of the config file into properties
			stream = new InputStreamReader(
					new FileInputStream( "/opt/openslx/proxy/config" ), StandardCharsets.UTF_8 );
			properties.load( stream );
			stream.close();
		} catch ( IOException e ) {
			log.warn( "Could not load proxy properties from '/opt/openslx/proxy/conf'." );
		} finally {
			Util.safeClose( stream );
		}
	}

	/**
	 * Check proxy settings for being not empty.
	 * 
	 * @return true if address and port are set
	 */
	public static boolean hasProxyAddress()
	{
		return !getProxyAddress().isEmpty() && getProxyPort() != 0;
	}

	/**
	 * Check if a username or password is configured.
	 * 
	 * @return true if either username or password (or both) are set
	 */
	public static boolean hasProxyCredentials()
	{
		return !getProxyUsername().isEmpty() || !getProxyPassword().isEmpty();
	}
}
