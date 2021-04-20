package org.openslx.virtualization.configuration.data;

public class ConfigurationDataDozModServerToStatelessClient
{
	private final String displayName;
	private final String osId;
	private final boolean hasUsbAccess;

	public ConfigurationDataDozModServerToStatelessClient( String displayName, String osId, boolean hasUsbAccess )
	{
		this.displayName = displayName;
		this.osId = osId;
		this.hasUsbAccess = hasUsbAccess;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public String getOsId()
	{
		return this.osId;
	}

	public boolean hasUsbAccess()
	{
		return this.hasUsbAccess;
	}
}
