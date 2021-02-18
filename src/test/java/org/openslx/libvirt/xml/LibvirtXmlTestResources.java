package org.openslx.libvirt.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class LibvirtXmlTestResources
{
	private static final String LIBVIRT_PREFIX_PATH = File.separator + "libvirt";
	private static final String LIBVIRT_PREFIX_PATH_XML = LIBVIRT_PREFIX_PATH + File.separator + "xml";

	private static final String LIBVIRT_TEMP_PREFIX = "libvirt-";
	private static final String LIBVIRT_TEMP_SUFFIX = ".xml";

	public static File getLibvirtXmlFile( String libvirtXmlFileName )
	{
		String libvirtXmlPath = LibvirtXmlTestResources.LIBVIRT_PREFIX_PATH_XML + File.separator + libvirtXmlFileName;
		URL libvirtXml = LibvirtXmlTestResources.class.getResource( libvirtXmlPath );
		return new File( libvirtXml.getFile() );
	}

	public static File createLibvirtXmlTempFile() throws IOException
	{
		File tempFile = File.createTempFile( LibvirtXmlTestResources.LIBVIRT_TEMP_PREFIX,
				LibvirtXmlTestResources.LIBVIRT_TEMP_SUFFIX );
		tempFile.deleteOnExit();
		return tempFile;
	}
}
