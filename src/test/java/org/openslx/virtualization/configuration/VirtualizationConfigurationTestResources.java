package org.openslx.virtualization.configuration;

import java.io.File;

import org.openslx.virtualization.configuration.logic.ConfigurationLogicTestResources;

public class VirtualizationConfigurationTestResources
{
	public static File getVmwareVmxFile( String vmwareVmxFileName )
	{
		return ConfigurationLogicTestResources.getVmwareVmxFile( vmwareVmxFileName );
	}

	public static File getVirtualBoxXmlFile( String virtualBoxXmlFileName )
	{
		return ConfigurationLogicTestResources.getVirtualBoxXmlFile( virtualBoxXmlFileName );
	}
}
