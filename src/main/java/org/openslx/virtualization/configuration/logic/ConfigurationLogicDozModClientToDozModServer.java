package org.openslx.virtualization.configuration.logic;

import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.VirtualizationConfigurationException;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModClientToDozModServer;
import org.openslx.virtualization.configuration.transformation.TransformationException;

public class ConfigurationLogicDozModClientToDozModServer
		extends ConfigurationLogic<ConfigurationDataDozModClientToDozModServer>
{
	private static final String CONFIGURATION_LOGIC_NAME = "Transformation of virtualization configuration during upload from DozMod client to DozMod server";

	public ConfigurationLogicDozModClientToDozModServer()
	{
		super( ConfigurationLogicDozModClientToDozModServer.CONFIGURATION_LOGIC_NAME );
	}

	private void validateInputs( VirtualizationConfiguration<?, ?, ?, ?> config,
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
	public void transform( VirtualizationConfiguration<?, ?, ?, ?> config,
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
