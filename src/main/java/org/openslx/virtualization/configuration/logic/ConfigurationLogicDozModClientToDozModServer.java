package org.openslx.virtualization.configuration.logic;

import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.VirtualizationConfigurationException;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModClientToDozModServer;
import org.openslx.virtualization.configuration.transformation.TransformationException;

/**
 * Transformation logic for virtualization configurations between a dozmod-client and a
 * dozmod-server.
 * <p>
 * This transformation logic is applied while uploading a new virtualization configuration from a
 * dozmod-client to a dozmod-server.
 * 
 * <pre>
 *   +------------------------------+  DozModClientToDozModServer   +------------------------------+
 *   | virtualization configuration | ----------------------------▶ | virtualization configuration |
 *   +---------------+--------------+     transformation logic      +---------------+--------------+
 *   | dozmod-client |                                              | dozmod-server |
 *   +---------------+                                              +---------------+
 * </pre>
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ConfigurationLogicDozModClientToDozModServer
		extends ConfigurationLogic<ConfigurationDataDozModClientToDozModServer>
{
	/**
	 * Name of the transformation logic for virtualization configurations.
	 */
	private static final String CONFIGURATION_LOGIC_NAME = "Transformation of virtualization configuration during upload from DozMod client to DozMod server";

	/**
	 * Creates a new transformation logic for virtualization configurations between a dozmod-client
	 * and a dozmod-server.
	 */
	public ConfigurationLogicDozModClientToDozModServer()
	{
		super( ConfigurationLogicDozModClientToDozModServer.CONFIGURATION_LOGIC_NAME );
	}

	/**
	 * Validates a virtualization configuration and input arguments for a transformation.
	 * 
	 * @param config virtualization configuration for the validation.
	 * @param args input arguments for the validation.
	 * @throws TransformationException validation has failed.
	 */
	private void validateInputs( VirtualizationConfiguration config,
			ConfigurationDataDozModClientToDozModServer args )
			throws TransformationException
	{
		if ( config == null || args == null ) {
			throw new TransformationException( "Virtualization configuration or input arguments are missing!" );
		} else if ( config.getDisplayName() == null ) {
			throw new TransformationException( "Display name is missing in virtualization configuration!" );
		}
	}

	@Override
	public void transform( VirtualizationConfiguration config,
			ConfigurationDataDozModClientToDozModServer args )
			throws TransformationException
	{
		// check if input parameters for a transformation are valid
		this.validateInputs( config, args );

		// apply the privacy filter on the given virtualization configuration
		try {
			config.transformPrivacy();
		} catch ( VirtualizationConfigurationException e ) {
			throw new TransformationException( e.getLocalizedMessage() );
		}
	}
}
