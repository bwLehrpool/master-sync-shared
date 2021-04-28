package org.openslx.virtualization.configuration.logic;

import java.util.Map;

import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.UsbSpeed;
import org.openslx.virtualization.configuration.VirtualizationConfigurationException;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModServerToDozModClient;
import org.openslx.virtualization.configuration.transformation.TransformationException;

/**
 * Transformation logic for virtualization configurations between a dozmod-server and a
 * dozmod-client.
 * <p>
 * This transformation logic is applied while downloading an existing virtualization configuration
 * from a dozmod-server to a dozmod-client.
 * 
 * <pre>
 *   +------------------------------+  DozModServerToDozModClient   +------------------------------+
 *   | virtualization configuration | ----------------------------> | virtualization configuration |
 *   +---------------+--------------+     transformation logic      +---------------+--------------+
 *   | dozmod-server |                                              | dozmod-client |
 *   +---------------+                                              +---------------+
 * </pre>
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ConfigurationLogicDozModServerToDozModClient
		extends ConfigurationLogic<ConfigurationDataDozModServerToDozModClient>
{
	/**
	 * Name of the transformation logic for virtualization configurations.
	 */
	private static final String CONFIGURATION_LOGIC_NAME = "Transformation of virtualization configuration during download from DozMod server to DozMod client";

	/**
	 * Default number of CPU cores set by the configuration logic for the virtualization
	 * configuration's virtualizer.
	 */
	private static final int CONFIGURATION_LOGIC_NUM_CPU_CORES = 1;

	/**
	 * Default memory in megabytes set by the configuration logic for the virtualization
	 * configuration's virtualizer.
	 */
	private static final int CONFIGURATION_LOGIC_MEMORY_MIN = 1024;

	/**
	 * Creates a new transformation logic for virtualization configurations between a dozmod-server
	 * and a dozmod-client.
	 */
	public ConfigurationLogicDozModServerToDozModClient()
	{
		super( ConfigurationLogicDozModServerToDozModClient.CONFIGURATION_LOGIC_NAME );
	}

	/**
	 * Validates a virtualization configuration and input arguments for a transformation.
	 * 
	 * @param config virtualization configuration for the validation.
	 * @param args input arguments for the validation.
	 * @throws TransformationException validation has failed.
	 */
	private void validateInputs( VirtualizationConfiguration<?, ?, ?, ?> config,
			ConfigurationDataDozModServerToDozModClient args )
			throws TransformationException
	{
		if ( config == null || args == null ) {
			throw new TransformationException( "Virtualization configuration or input arguments are missing!" );
		} else if ( args.getDisplayName() == null || args.getDisplayName().isEmpty() ) {
			throw new TransformationException( "Valid display name is not specified!" );
		} else if ( args.getDiskImage() == null || !args.getDiskImage().exists() ) {
			throw new TransformationException( "Valid disk image file is not specified!" );
		} else if ( ! ( args.getTotalMemory() > 0 ) ) {
			throw new TransformationException( "Total memory amount is not specified!" );
		}
	}

	/**
	 * Rounds a given value to the nearest factor.
	 * 
	 * @param value input value for the rounding.
	 * @param nearestFactor nearest factor for the rounding.
	 * @return rounded value as a multiple of the nearest factor.
	 * 
	 * @apiNote This utility method rounds the given value to an integer value and no to a floating
	 *          point value.
	 */
	private static int roundToNearest( int value, int nearestFactor )
	{
		return ( value / nearestFactor ) * nearestFactor;
	}

	/**
	 * Calculates the amount of memory for the virtualization configuration depending on the
	 * available resources of the dozmod-client's host system.
	 * 
	 * @param totalMemory maximum memory available on the dozmod-client's host system.
	 * @param osMaxMemory maximum memory supported by the defined operating system in the
	 *           virtualization configuration.
	 * @return amount of memory for the virtualization configuration in megabytes
	 */
	private static int calculateVirtualizationMemoryOnDozmodClient( int totalMemory, int osMaxMemory )
	{
		// calculate the amount of memory
		int memory = totalMemory / 2 - 512;

		// increase calculated memory if lower memory limit is undercut
		if ( memory < ConfigurationLogicDozModServerToDozModClient.CONFIGURATION_LOGIC_MEMORY_MIN ) {
			memory = ConfigurationLogicDozModServerToDozModClient.CONFIGURATION_LOGIC_MEMORY_MIN;
		}

		// limit virtualization memory if the available host's system memory amount is smaller
		if ( osMaxMemory > 0 && memory > osMaxMemory ) {
			memory = osMaxMemory;
		}

		// round to nearest factor of 4, otherwise VMware virtualization configuration files are invalid
		return ConfigurationLogicDozModServerToDozModClient.roundToNearest( memory, 4 );
	}

	@Override
	public void transform( VirtualizationConfiguration<?, ?, ?, ?> config,
			ConfigurationDataDozModServerToDozModClient args )
			throws TransformationException
	{
		// check if input parameters for a transformation are valid 
		this.validateInputs( config, args );

		// set display name
		if ( !config.addDisplayName( args.getDisplayName() ) ) {
			throw new TransformationException( "Can not set display name in virtualization configuration!" );
		}

		// append hard disk drive
		if ( !config.addHddTemplate( args.getDiskImage(), null, null ) ) {
			throw new TransformationException( "Can not configure hard disk in virtualization configuration!" );
		}

		// append default NAT interface
		if ( !config.addDefaultNat() ) {
			throw new TransformationException( "Can not configure NAT interface in virtualization configuration!" );
		}

		// set the guest OS if specified
		final OperatingSystem guestOs = args.getGuestOs();
		final String virtualizerId = args.getVirtualizerId();
		int osMaxMemory = 0;

		if ( guestOs != null && virtualizerId != null ) {
			final Map<String, String> virtOsIdMap = guestOs.getVirtualizerOsId();
			if ( virtOsIdMap != null ) {
				// set guest operating system if possible
				final String virtOsId = virtOsIdMap.get( virtualizerId );
				if ( virtOsId != null ) {
					config.setOs( virtOsId );
				}

				// get maximum memory of editable host for guestOs if possible
				final int maxMemMb = guestOs.getMaxMemMb();
				if ( maxMemMb > 0 ) {
					osMaxMemory = maxMemMb;
				}
			}
		}

		// set CPU core count
		if ( !config.addCpuCoreCount( ConfigurationLogicDozModServerToDozModClient.CONFIGURATION_LOGIC_NUM_CPU_CORES ) ) {
			throw new TransformationException( "Can not set CPU core count in virtualization configuration!" );
		}

		// calculate and set memory
		final int virtualizationMemory = ConfigurationLogicDozModServerToDozModClient
				.calculateVirtualizationMemoryOnDozmodClient( args.getTotalMemory(), osMaxMemory );
		if ( !config.addRam( virtualizationMemory ) ) {
			throw new TransformationException( "Can not set memory in virtualization configuration!" );
		}

		// append first empty floppy drive
		config.addFloppy( 0, null, true );
		// append second empty floppy drive
		config.addFloppy( 1, null, true );

		// append first empty (ISO-based) CDROM drive
		config.addCdrom( "" );
		// append second CDROM drive connected to the host's physical drive
		config.addCdrom( null );

		// set maximum USB speed
		if ( config.getMaxUsbSpeed() != UsbSpeed.USB3_0 ) {
			config.setMaxUsbSpeed( UsbSpeed.USB2_0 );
		}

		// apply settings to edit virtualized system locally
		try {
			config.transformEditable();
		} catch ( VirtualizationConfigurationException e ) {
			throw new TransformationException( e.getLocalizedMessage() );
		}
	}
}
