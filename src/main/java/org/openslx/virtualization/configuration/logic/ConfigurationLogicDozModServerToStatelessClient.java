package org.openslx.virtualization.configuration.logic;

import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.EtherType;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.UsbSpeed;
import org.openslx.virtualization.configuration.VirtualizationConfigurationException;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModServerToStatelessClient;
import org.openslx.virtualization.configuration.transformation.TransformationException;

/**
 * Transformation logic for virtualization configurations between a dozmod-server and a stateless
 * client.
 * <p>
 * This transformation logic is applied while downloading an existing virtualization configuration
 * from a dozmod-server to a stateless client.
 * 
 * <pre>
 *   +------------------------------+  DozModServerToStatelessClient   +------------------------------+
 *   | virtualization configuration | -------------------------------▶ | virtualization configuration |
 *   +---------------+--------------+      transformation logic        +------------------+-----------+
 *   | dozmod-server |                                                 | stateless client |
 *   +---------------+                                                 +------------------+
 * </pre>
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ConfigurationLogicDozModServerToStatelessClient
		extends ConfigurationLogic<ConfigurationDataDozModServerToStatelessClient>
{
	/**
	 * Name of the transformation logic for virtualization configurations.
	 */
	private static final String CONFIGURATION_LOGIC_NAME = "Transformation of virtualization configuration during download from DozMod server to stateless client";

	/**
	 * Default type for an ethernet interface in a virtualization configuration.
	 */
	private static final EtherType CONFIGURATION_DEFAULT_ETHERNET_TYPE = EtherType.NAT;

	/**
	 * Creates a new transformation logic for virtualization configurations between a dozmod-server
	 * and a stateless client.
	 */
	public ConfigurationLogicDozModServerToStatelessClient()
	{
		super( ConfigurationLogicDozModServerToStatelessClient.CONFIGURATION_LOGIC_NAME );
	}

	/**
	 * Validates a virtualization configuration and input arguments for a transformation.
	 * 
	 * @param config virtualization configuration for the validation.
	 * @param args input arguments for the validation.
	 * @throws TransformationException validation has failed.
	 */
	private void validateInputs( VirtualizationConfiguration<?, ?, ?, ?> config,
			ConfigurationDataDozModServerToStatelessClient args )
			throws TransformationException
	{
		if ( config == null || args == null ) {
			throw new TransformationException( "Virtualization configuration or input arguments are missing!" );
		} else if ( args.getDisplayName() == null || args.getDisplayName().isEmpty() ) {
			throw new TransformationException( "Valid display name is not specified!" );
		}
	}

	@Override
	public void transform( VirtualizationConfiguration<?, ?, ?, ?> config,
			ConfigurationDataDozModServerToStatelessClient args )
			throws TransformationException
	{
		// check if input parameters for a transformation are valid
		this.validateInputs( config, args );

		// apply settings to run virtualized system in a stateless manner
		try {
			config.transformNonPersistent();
		} catch ( VirtualizationConfigurationException e ) {
			throw new TransformationException( e.getLocalizedMessage() );
		}

		// set display name of lecture
		if ( !config.addDisplayName( args.getDisplayName() ) ) {
			throw new TransformationException( "Can not set display name in virtualization configuration!" );
		}

		// append hard disk drive (with no referenced image as content)
		if ( !config.addEmptyHddTemplate() ) {
			throw new TransformationException( "Can not configure hard disk in virtualization configuration!" );
		}

		// append default NAT interface
		if ( !config.addEthernet(
				ConfigurationLogicDozModServerToStatelessClient.CONFIGURATION_DEFAULT_ETHERNET_TYPE ) ) {
			throw new TransformationException( "Can not configure NAT interface in virtualization configuration!" );
		}

		// set the guest OS if specified
		if ( args.getOsId() != null ) {
			config.setOs( args.getOsId() );
		}

		// disable USB if necessary
		if ( !args.hasUsbAccess() ) {
			config.setMaxUsbSpeed( UsbSpeed.NONE );
		}
	}
}
