package org.openslx.libvirt.xml;

import java.io.File;
import java.io.InputStream;

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
	private static final String LIBVIRT_PREFIX_PATH = File.separator + "libvirt";

	/**
	 * File path prefix of the absolute path to the libvirt XSL resource folder in a *.jar file.
	 */
	private static final String LIBVIRT_PREFIX_PATH_XSL = LIBVIRT_PREFIX_PATH + File.separator + "xsl";

	/**
	 * File path prefix of the absolute path to the libvirt RNG resource folder in a *.jar file.
	 */
	private static final String LIBVIRT_PREFIX_PATH_RNG = LIBVIRT_PREFIX_PATH + File.separator + "rng";

	/**
	 * Returns a Libvirt XSL resource as stream.
	 * 
	 * @param libvirtXslFileName file name of the XSL resource in the resources *.jar folder.
	 * @return Libvirt XSL resource as stream.
	 */
	public static InputStream getLibvirtXsl( String libvirtXslFileName )
	{
		String libvirtXslPath = LibvirtXmlResources.LIBVIRT_PREFIX_PATH_XSL + File.separator + libvirtXslFileName;
		return LibvirtXmlResources.class.getResourceAsStream( libvirtXslPath );
	}

	/**
	 * Returns a Libvirt RNG schema resource as stream.
	 * 
	 * @param libvirtRngFileName file name of the RNG schema resource in the resources *.jar folder.
	 * @return Libvirt RNG schema resource as stream.
	 */
	public static InputStream getLibvirtRng( String libvirtRngFileName )
	{
		String libvirtRngPath = LibvirtXmlResources.LIBVIRT_PREFIX_PATH_RNG + File.separator + libvirtRngFileName;
		return LibvirtXmlResources.class.getResourceAsStream( libvirtRngPath );
	}
}
