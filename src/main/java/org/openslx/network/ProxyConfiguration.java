package org.openslx.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openslx.util.Util;

public class ProxyConfiguration
{
	private static Logger log = Logger.getLogger( ProxyConfiguration.class );
	private static final Properties properties = new Properties();
	
	// Getting the proxy settings from config file stored in
	// "/opt/openslx/proxy/conf".
	public static String getProxyConf()
	{
		return properties.getProperty( "PROXY_CONF" );
	}
	
	public static String getProxyAddress()
	{
		return properties.getProperty( "PROXY_ADDR" );
	}
	
	public static String getProxyUsername()
	{
		return properties.getProperty( "PROXY_USERNAME" );
	}

	public static String getProxyPassword()
	{
		return properties.getProperty( "PROXY_PASSWORD" );
	}
	
	// Integers //
	public static int getProxyPort()
	{
		return Util.tryToParseInt( properties.getProperty( "PROXY_PORT" ) );
	}

	/**
	 * Load properties
	 */
	static {
		InputStreamReader stream = null;
		try {
			// Load all entries of the config file into properties
			stream = new InputStreamReader(
					new FileInputStream("/opt/openslx/proxy/config"), StandardCharsets.UTF_8);
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			log.error("Could not load proxy properties from '/opt/openslx/proxy/conf'. Exiting.");
			System.exit( 2 );
		} finally {
			Util.streamClose( stream );
		}
	}
	
	/**
	 * Check proxy settings for being not empty.
	 * @return
	 */
	public static boolean checkProxySettings() {
		return (
				(getProxyAddress() != "") &&
				(getProxyPort() != 0));
	}
}
