package org.openslx.virtualization.configuration.logic;

import java.io.File;
import java.net.URL;

import org.openslx.util.Resources;

public class ConfigurationLogicTestResources
{
	private static final String VMWARE_PREFIX_PATH = Resources.PATH_SEPARATOR + "vmware";
	private static final String VMWARE_PREFIX_PATH_VMX = VMWARE_PREFIX_PATH + Resources.PATH_SEPARATOR + "vmx";

	private static final String VIRTUALBOX_PREFIX_PATH = Resources.PATH_SEPARATOR + "virtualbox";
	private static final String VIRTUALBOX_PREFIX_PATH_XML = VIRTUALBOX_PREFIX_PATH + Resources.PATH_SEPARATOR + "xml";

	private static File getFile( String prefixPath, String fileName )
	{
		final String filePath = prefixPath + Resources.PATH_SEPARATOR + fileName;
		final URL fileUrl = ConfigurationLogicTestResources.class.getResource( filePath );
		return new File( fileUrl.getFile() );
	}

	public static File getVmwareVmxFile( String vmwareVmxFileName )
	{
		return ConfigurationLogicTestResources.getFile( ConfigurationLogicTestResources.VMWARE_PREFIX_PATH_VMX,
				vmwareVmxFileName );
	}

	public static File getVirtualBoxXmlFile( String virtualBoxXmlFileName )
	{
		return ConfigurationLogicTestResources.getFile( ConfigurationLogicTestResources.VIRTUALBOX_PREFIX_PATH_XML,
				virtualBoxXmlFileName );
	}
}
