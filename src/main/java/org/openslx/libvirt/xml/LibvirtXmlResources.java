package org.openslx.libvirt.xml;

import java.io.InputStream;

import org.openslx.util.Resources;

/**
 * Collection of resource utils for a Libvirt XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public final class LibvirtXmlResources
{
	/**
	 * File path prefix of the absolute path to the libvirt resource folder in a *.jar file.
	 */
	private static final String LIBVIRT_PREFIX_PATH = Resources.PATH_SEPARATOR + "libvirt";

	/**
	 * File path prefix of the absolute path to the libosinfo resource folder in a *.jar file.
	 */
	private static final String LIBOSINFO_PREFIX_PATH = Resources.PATH_SEPARATOR + "libvirt" + Resources.PATH_SEPARATOR
			+ "libosinfo";

	/**
	 * File path prefix of the absolute path to the libvirt XSL resource folder in a *.jar file.
	 */
	private static final String LIBVIRT_PREFIX_PATH_XSL = LIBVIRT_PREFIX_PATH + Resources.PATH_SEPARATOR + "xsl";

	/**
	 * File path prefix of the absolute path to the libvirt RNG resource folder in a *.jar file.
	 */
	private static final String LIBVIRT_PREFIX_PATH_RNG = LIBVIRT_PREFIX_PATH + Resources.PATH_SEPARATOR + "rng";

	/**
	 * File path prefix of the absolute path to the libosinfo RNG resource folder in a *.jar file.
	 */
	private static final String LIBOSINFO_PREFIX_PATH_RNG = LIBOSINFO_PREFIX_PATH + Resources.PATH_SEPARATOR + "rng";

	/**
	 * File path prefix of the absolute path to the libosinfo XML resource folder in a *.jar file.
	 */
	private static final String LIBOSINFO_PREFIX_PATH_XML = LIBOSINFO_PREFIX_PATH + Resources.PATH_SEPARATOR + "xml";

	/**
	 * Returns a Libvirt resource as stream.
	 * 
	 * @param prefix file path of the Libvirt resource in the resources *.jar folder.
	 * @param fileName file name of the Libvirt resource in the resources *.jar folder.
	 * @return Libvirt resource as stream.
	 */
	private static InputStream getLibvirtResource( String prefix, String fileName )
	{
		final String path = prefix + Resources.PATH_SEPARATOR + fileName;
		return LibvirtXmlResources.class.getResourceAsStream( path );
	}

	/**
	 * Returns a Libvirt XSL resource as stream.
	 * 
	 * @param libvirtXslFileName file name of the XSL resource in the resources *.jar folder.
	 * @return Libvirt XSL resource as stream.
	 */
	public static InputStream getLibvirtXsl( String libvirtXslFileName )
	{
		return LibvirtXmlResources.getLibvirtResource( LibvirtXmlResources.LIBVIRT_PREFIX_PATH_XSL, libvirtXslFileName );
	}

	/**
	 * Returns a Libvirt RNG schema resource as stream.
	 * 
	 * @param libvirtRngFileName file name of the RNG schema resource in the resources *.jar folder.
	 * @return Libvirt RNG schema resource as stream.
	 */
	public static InputStream getLibvirtRng( String libvirtRngFileName )
	{
		return LibvirtXmlResources.getLibvirtResource( LibvirtXmlResources.LIBVIRT_PREFIX_PATH_RNG, libvirtRngFileName );
	}

	/**
	 * Returns a libosinfo RNG schema resource as stream.
	 * 
	 * @param libosInfoRngFileName file name of the RNG schema resource in the resources *.jar
	 *           folder.
	 * @return libosinfo RNG schema resource as stream.
	 */
	public static InputStream getLibOsInfoRng( String libosInfoRngFileName )
	{
		return LibvirtXmlResources.getLibvirtResource( LibvirtXmlResources.LIBOSINFO_PREFIX_PATH_RNG,
				libosInfoRngFileName );
	}

	/**
	 * Returns a libosinfo XML resource as stream.
	 * 
	 * @param libosInfoXmlFileName file name of the XML resource in the resources *.jar folder.
	 * @return libosinfo XML resource as stream.
	 */
	public static InputStream getLibOsInfoXml( String libosInfoXmlFileName )
	{
		return LibvirtXmlResources.getLibvirtResource( LibvirtXmlResources.LIBOSINFO_PREFIX_PATH_XML,
				libosInfoXmlFileName );
	}
}
