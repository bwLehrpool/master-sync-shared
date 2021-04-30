package org.openslx.virtualization.configuration.data;

/**
 * Data container to collect and store input arguments for a
 * {@link org.openslx.virtualization.configuration.logic.ConfigurationLogicDozModServerToStatelessClient}
 * transformation.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ConfigurationDataDozModServerToStatelessClient
{
	/**
	 * Display name for a transformation of a virtualization configuration.
	 */
	private final String displayName;

	/**
	 * Operating system identifier for a transformation of a virtualization configuration.
	 */
	private final String osId;

	/**
	 * State whether USB access is allowed or not for a transformation of a virtualization
	 * configuration.
	 */
	private final boolean hasUsbAccess;

	/**
	 * Creates a new data container to collect and store input arguments for a
	 * {@link org.openslx.virtualization.configuration.logic.ConfigurationLogicDozModServerToStatelessClient}
	 * transformation.
	 * 
	 * @param displayName display name for a transformation of a virtualization configuration.
	 * @param osId operating system identifier for a transformation of a virtualization
	 *           configuration.
	 * @param hasUsbAccess state whether USB access is allowed or not for a transformation of a
	 *           virtualization configuration.
	 */
	public ConfigurationDataDozModServerToStatelessClient( String displayName, String osId, boolean hasUsbAccess )
	{
		this.displayName = displayName;
		this.osId = osId;
		this.hasUsbAccess = hasUsbAccess;
	}

	/**
	 * Returns the display name for a transformation of a virtualization configuration.
	 * 
	 * @return display name for a transformation of a virtualization configuration.
	 */
	public String getDisplayName()
	{
		return this.displayName;
	}

	/**
	 * Returns the operating system identifier for a transformation of a virtualization
	 * configuration.
	 * 
	 * @return operating system identifier for a transformation of a virtualization configuration.
	 */
	public String getOsId()
	{
		return this.osId;
	}

	/**
	 * Returns the state whether USB access is allowed or not for a transformation of a
	 * virtualization configuration.
	 * 
	 * @return state whether USB access is allowed or not for a transformation of a virtualization
	 *         configuration.
	 */
	public boolean hasUsbAccess()
	{
		return this.hasUsbAccess;
	}
}
