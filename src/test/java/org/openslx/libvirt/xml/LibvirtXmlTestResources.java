package org.openslx.libvirt.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openslx.util.Resources;

public final class LibvirtXmlTestResources
{
	private static final String LIBVIRT_PREFIX_PATH = Resources.PATH_SEPARATOR + "libvirt";
	private static final String LIBVIRT_PREFIX_PATH_XML = LIBVIRT_PREFIX_PATH + Resources.PATH_SEPARATOR + "xml";

	private static final String LIBVIRT_TEMP_PREFIX = "libvirt-";
	private static final String LIBVIRT_TEMP_SUFFIX = ".xml";

	public static File getLibvirtXmlFile( String libvirtXmlFileName )
	{
		String libvirtXmlPath = LibvirtXmlTestResources.LIBVIRT_PREFIX_PATH_XML + Resources.PATH_SEPARATOR
				+ libvirtXmlFileName;
		URL libvirtXml = LibvirtXmlTestResources.class.getResource( libvirtXmlPath );
		return new File( libvirtXml.getFile() );
	}

	public static InputStream getLibvirtXmlStream( String libvirtXmlFileName )
	{
		String libvirtXmlPath = LibvirtXmlTestResources.LIBVIRT_PREFIX_PATH_XML + Resources.PATH_SEPARATOR
				+ libvirtXmlFileName;
		return LibvirtXmlTestResources.class.getResourceAsStream( libvirtXmlPath );
	}

	public static File createLibvirtXmlTempFile() throws IOException
	{
		File tempFile = File.createTempFile( LibvirtXmlTestResources.LIBVIRT_TEMP_PREFIX,
				LibvirtXmlTestResources.LIBVIRT_TEMP_SUFFIX );
		tempFile.deleteOnExit();
		return tempFile;
	}
}
