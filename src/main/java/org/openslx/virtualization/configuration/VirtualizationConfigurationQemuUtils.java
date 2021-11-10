package org.openslx.virtualization.configuration;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.device.Disk;
import org.openslx.libvirt.domain.device.Disk.BusType;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.DriveBusType;

/**
 * Collection of utils to convert data types from bwLehrpool to Libvirt and vice versa.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizationConfigurationQemuUtils
{
	/**
	 * Separator symbol between Libvirt/QEMU machine name and machine version.
	 */
	private static final String OS_MACHINE_NAME_VERSION_SEPARATOR = "-";

	/**
	 * Converts a Libvirt disk device bus type to a VM metadata driver bus type.
	 * 
	 * @param busType Libvirt disk device bus type.
	 * @return VM metadata bus type of the disk drive.
	 */
	public static DriveBusType convertBusType( Disk.BusType busType )
	{
		DriveBusType type = null;

		switch ( busType ) {
		case IDE:
			type = DriveBusType.IDE;
			break;
		case SATA:
			type = DriveBusType.SATA;
			break;
		case SCSI:
			type = DriveBusType.SCSI;
			break;
		default:
			type = null;
			break;
		}

		return type;
	}

	/**
	 * Converts a VM metadata driver bus type to a Libvirt disk device bus type.
	 * 
	 * @param busType VM metadata bus type of the disk drive.
	 * @return Libvirt disk device bus type.
	 */
	public static Disk.BusType convertBusType( DriveBusType busType )
	{
		Disk.BusType type = null;

		switch ( busType ) {
		case IDE:
			type = BusType.IDE;
			break;
		case NVME:
			type = null;
			break;
		case SATA:
			type = BusType.SATA;
			break;
		case SCSI:
			type = BusType.SCSI;
			break;
		}

		return type;
	}

	/**
	 * Returns an item from a given {@link ArrayList}.
	 * 
	 * The item is selected by a given index. If the item is not available within the
	 * {@link ArrayList}, <code>null</code> is returned.
	 * 
	 * @param <T> type of the {@link ArrayList}.
	 * @param array {@link ArrayList} of type <code>T</code>.
	 * @param index selects the item from the {@link ArrayList}.
	 * @return selected item of the {@link ArrayList}.
	 */
	public static <T> T getArrayIndex( ArrayList<T> array, int index )
	{
		T ret;

		try {
			ret = array.get( index );
		} catch ( IndexOutOfBoundsException e ) {
			ret = null;
		}

		return ret;
	}

	/**
	 * Creates an alphabetical device name constructed from a device prefix and a device number.
	 * 
	 * @param devicePrefix prefix of the constructed device name.
	 * @param deviceNumber number of the device.
	 * @return alphabetical device name.
	 */
	private static String createAlphabeticalDeviceName( String devicePrefix, int deviceNumber )
	{
		if ( deviceNumber < 0 || deviceNumber >= ( 'z' - 'a' ) ) {
			String errorMsg = new String( "Device number is out of range to be able to create a valid device name." );
			throw new IllegalArgumentException( errorMsg );
		}

		return devicePrefix + Character.valueOf( (char) ( 'a' + deviceNumber ) ).toString();
	}

	/**
	 * Creates an alphabetical device name for a disk device with a bus <i>type</i> that is unique in
	 * a Libvirt domain XML configuration.
	 * 
	 * @param config Libvirt domain XML configuration.
	 * @param type device type for device name.
	 * @return alphabetical device name.
	 */
	public static String createDeviceName( final Domain config, final BusType type ) throws IllegalArgumentException
	{
		final String devicePrefix;
		final int deviceNumber;

		switch ( type ) {
		case FDC:
			devicePrefix = "fd";
			break;
		case IDE:
			devicePrefix = "hd";
			break;
		case SATA:
			devicePrefix = "sd";
			break;
		case VIRTIO:
			devicePrefix = "vd";
			break;
		default:
			return null;
		}

		final Predicate<Disk> bySpecifiedBusType = d -> d.getBusType() == type;
		deviceNumber = Long.valueOf( config.getDiskDevices().stream().filter( bySpecifiedBusType ).count() ).intValue();

		return VirtualizationConfigurationQemuUtils.createAlphabeticalDeviceName( devicePrefix, deviceNumber );
	}

	/**
	 * Data container to store a Libvirt/QEMU machine name with version information.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	static class OsMachineNameAndVersion
	{
		/**
		 * Stores the machine name.
		 */
		final private String osMachineName;

		/**
		 * Stores the machine version.
		 */
		final private Version osMachineVersion;

		/**
		 * Creates a data container for a machine name with version information.
		 * 
		 * @param osMachineName name of the machine.
		 * @param osMachineVersion version of the machine.
		 */
		public OsMachineNameAndVersion( String osMachineName, Version osMachineVersion )
		{
			this.osMachineName = osMachineName;
			this.osMachineVersion = osMachineVersion;
		}

		/**
		 * Returns the machine name.
		 * 
		 * @return machine name.
		 */
		public String getOsMachineName()
		{
			return this.osMachineName;
		}

		/**
		 * Returns the version information.
		 * 
		 * @return version information.
		 */
		public Version getOsMachineVersion()
		{
			return this.osMachineVersion;
		}
	}

	/**
	 * Parses a machine name with version information from a Libvirt/QEMU machine description.
	 * 
	 * @param osMachine Libvirt/QEMU machine description as {@link String}.
	 * @return data container containing the parsed machine name with version information.
	 */
	private static OsMachineNameAndVersion parseOsMachineNameAndVersion( String osMachine )
	{
		final String osMachineName;
		final Version osMachineVersion;

		if ( osMachine == null || osMachine.isEmpty() ) {
			// there is no machine description given, so we can not parse anything
			osMachineName = null;
			osMachineVersion = null;
		} else {
			// create regular expression based matcher to extract machine name and version number
			final Pattern osMachineNameAndVersionPattern = Pattern.compile( "^([a-z0-9\\-]+)"
					+ VirtualizationConfigurationQemuUtils.OS_MACHINE_NAME_VERSION_SEPARATOR + "([0-9]+).([0-9]+)$" );
			final Matcher osMachineNameAndVersionMatcher = osMachineNameAndVersionPattern.matcher( osMachine );

			final boolean matches = osMachineNameAndVersionMatcher.find();

			if ( matches ) {
				// get results of regular expression based matcher
				osMachineName = osMachineNameAndVersionMatcher.group( 1 );
				final String osMachineMajorString = osMachineNameAndVersionMatcher.group( 2 );
				final String osMachineMinorString = osMachineNameAndVersionMatcher.group( 3 );

				// create version representation
				final short osMachineMajor = Short.valueOf( osMachineMajorString );
				final short osMachineMinor = Short.valueOf( osMachineMinorString );
				osMachineVersion = new Version( osMachineMajor, osMachineMinor );
			} else {
				osMachineName = null;
				osMachineVersion = null;
			}
		}

		return new OsMachineNameAndVersion( osMachineName, osMachineVersion );
	}

	/**
	 * Parses a machine name from a Libvirt/QEMU machine description.
	 * 
	 * @param osMachine Libvirt/QEMU machine description as {@link String}.
	 * @return parsed machine name.
	 */
	public static String getOsMachineName( String osMachine )
	{
		final OsMachineNameAndVersion machineNameAndVersion = VirtualizationConfigurationQemuUtils
				.parseOsMachineNameAndVersion( osMachine );
		return machineNameAndVersion.getOsMachineName();
	}

	/**
	 * Parses a machine version from a Libvirt/QEMU machine description.
	 * 
	 * @param osMachine Libvirt/QEMU machine description as {@link String}.
	 * @return parsed machine version.
	 */
	public static Version getOsMachineVersion( String osMachine )
	{
		final OsMachineNameAndVersion machineNameAndVersion = VirtualizationConfigurationQemuUtils
				.parseOsMachineNameAndVersion( osMachine );
		return machineNameAndVersion.getOsMachineVersion();
	}

	/**
	 * Combines a machine name with a machine version and returns a Libvirt/QEMU machine description.
	 * 
	 * @param osMachineName name of the machine.
	 * @param osMachineVersion version of the machine.
	 * @return Libvirt/QEMU machine description.
	 */
	public static String getOsMachine( String osMachineName, String osMachineVersion )
	{
		return osMachineName + VirtualizationConfigurationQemuUtils.OS_MACHINE_NAME_VERSION_SEPARATOR + osMachineVersion;
	}

	/**
	 * Converts a {@link Version} to a Libvirt/QEMU machine version.
	 * 
	 * @param version Libvirt/QEMU machine version as {@link Version}.
	 * @return Libvirt/QEMU machine version.
	 */
	public static String getOsMachineVersion( Version version )
	{
		return String.format( "%d.%d", version.getMajor(), version.getMinor() );
	}

	/**
	 * Returns the size of a given architecture {@link String}.
	 * 
	 * @param osArch Libvirt/QEMU machine architecture as {@link String}.
	 * @return Size of the Libvirt/QEMU machine architecture.
	 */
	public static int getOsArchSize( String osArch )
	{
		final int archSize;

		if ( osArch == null || osArch.isEmpty() ) {
			archSize = 0;
		} else {
			switch ( osArch ) {
			case "alpha":
				archSize = 64;
				break;
			case "armv6l":
				archSize = 32;
				break;
			case "armv7l":
				archSize = 32;
				break;
			case "aarch64":
				archSize = 64;
				break;
			case "cris":
				archSize = 32;
				break;
			case "i686":
				archSize = 32;
				break;
			case "m68k":
				archSize = 32;
				break;
			case "microblaze":
				archSize = 32;
				break;
			case "microblazeel":
				archSize = 32;
				break;
			case "mips":
				archSize = 32;
				break;
			case "mipsel":
				archSize = 32;
				break;
			case "mips64":
				archSize = 64;
				break;
			case "mips64el":
				archSize = 64;
				break;
			case "ppc":
				archSize = 32;
				break;
			case "ppc64":
				archSize = 64;
				break;
			case "ppc64le":
				archSize = 64;
				break;
			case "riscv32":
				archSize = 32;
				break;
			case "riscv64":
				archSize = 64;
				break;
			case "s390x":
				archSize = 64;
				break;
			case "sh4":
				archSize = 32;
				break;
			case "sh4eb":
				archSize = 64;
				break;
			case "sparc":
				archSize = 32;
				break;
			case "sparc64":
				archSize = 64;
				break;
			case "x86_64":
				archSize = 64;
				break;
			case "xtensa":
				archSize = 32;
				break;
			case "xtensaeb":
				archSize = 32;
				break;
			default:
				archSize = 0;
			}
		}

		return archSize;
	}
}
