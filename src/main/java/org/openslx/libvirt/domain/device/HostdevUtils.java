package org.openslx.libvirt.domain.device;

/**
 * Collection of helper methods to maintain a Libvirt related hostdev XML element.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevUtils
{
	/**
	 * Appends the HEX prefix to a specified hostdev address component without any HEX prefix.
	 * 
	 * @param component address component without any HEX prefix.
	 * @return address component with the HEX prefix.
	 */
	public static String appendHexPrefix( String component )
	{
		return "0x" + component;
	}

	/**
	 * Removes a possible HEX prefix of a specified hostdev address component.
	 * 
	 * @param component address component with possible HEX prefix.
	 * @return address component without any HEX prefix.
	 */
	public static String removeHexPrefix( String component )
	{
		return component.replaceFirst( "0x", "" );
	}
}
