package org.openslx.libvirt.domain;

import java.math.BigInteger;

import org.openslx.libvirt.domain.device.HostdevPciDeviceAddress;

/**
 * Collection of helper functions to maintain a Libvirt domain XML document.
 *
 * @author Manuel Bentele
 * @version 1.0
 */
public final class DomainUtils
{

	/**
	 * Converts memory value with specified SI unit to absolute value in bytes.
	 * 
	 * @param value amount of memory in specified SI unit.
	 * @param unit SI unit name, one of: bytes, KB, k, KiB, MB, M, MiB, GB, G, GiB, TB, T, TiB, for
	 *           <code>value</code>.
	 * @return absolute amount of memory in bytes.
	 */
	public static BigInteger decodeMemory( String value, String unit )
	{
		BigInteger factor = null;
		BigInteger result = new BigInteger( value );

		switch ( unit ) {
		case "b":
		case "bytes":
			factor = new BigInteger( "1" );
			break;
		case "KB":
			factor = new BigInteger( "1000" );
			break;
		case "k":
		case "KiB":
			factor = new BigInteger( "1024" );
			break;
		case "MB":
			factor = new BigInteger( "1000000" );
			break;
		case "M":
		case "MiB":
			factor = new BigInteger( "1048576" );
			break;
		case "GB":
			factor = new BigInteger( "1000000000" );
			break;
		case "G":
		case "GiB":
			factor = new BigInteger( "1073741824" );
			break;
		case "TB":
			factor = new BigInteger( "1000000000000" );
			break;
		case "T":
		case "TiB":
			factor = new BigInteger( "1099511627776" );
			break;
		default:
			return null;
		}

		return result.multiply( factor );
	}

	/**
	 * Convert memory from absolute value in bytes to value in specified SI unit.
	 * 
	 * @param value absolute amount of memory in bytes.
	 * @param unit SI unit name, one of: bytes, KB, k, KiB, MB, M, MiB, GB, G, GiB, TB, T, TiB for
	 *           returned memory value.
	 * @return amount of memory in specified SI unit.
	 */
	public static String encodeMemory( BigInteger value, String unit )
	{
		BigInteger dividend = null;

		switch ( unit ) {
		case "b":
		case "bytes":
			dividend = new BigInteger( "1" );
			break;
		case "KB":
			dividend = new BigInteger( "1000" );
			break;
		case "k":
		case "KiB":
			dividend = new BigInteger( "1024" );
			break;
		case "MB":
			dividend = new BigInteger( "1000000" );
			break;
		case "M":
		case "MiB":
			dividend = new BigInteger( "1048576" );
			break;
		case "GB":
			dividend = new BigInteger( "1000000000" );
			break;
		case "G":
		case "GiB":
			dividend = new BigInteger( "1073741824" );
			break;
		case "TB":
			dividend = new BigInteger( "1000000000000" );
			break;
		case "T":
		case "TiB":
			dividend = new BigInteger( "1099511627776" );
			break;
		default:
			return null;
		}

		return value.divide( dividend ).toString();
	}

	/**
	 * Builds a PCI slot usage map from all devices in the given domain configuration.
	 * <p>
	 * The returned array tracks which PCI device slots are in use on the primary bus
	 * (domain 0, bus 0). Array indices correspond to device numbers (0-63), with:
	 * </p>
	 * <ul>
	 *   <li>{@code 0} = slot is free</li>
	 *   <li>{@code >0} = slot is occupied (value encodes domain:bus:device lookup)</li>
	 * </ul>
	 * <p>
	 * Slots 0 and 1 are pre-marked as reserved.
	 * </p>
	 *
	 * @param config domain configuration to scan for existing PCI devices
	 * @return array of size 64 tracking PCI slot usage
	 */
	public static int[] buildPciSlotUsageMap( Domain config )
	{
		int[] inUse = new int[ 64 ];
		inUse[ 0 ] = Integer.MAX_VALUE;
		inUse[ 1 ] = Integer.MAX_VALUE;

		for ( org.openslx.libvirt.domain.device.Device dev : config.getDevices() ) {
			HostdevPciDeviceAddress target = dev.getPciTarget();
			if ( target == null )
				continue;
			if ( target.getPciDomain() != 0 || target.getPciBus() != 0 )
				continue; // Ignore non-primary bus
			if ( target.getPciDevice() >= inUse.length )
				continue;
			inUse[ target.getPciDevice() ] = Integer.MAX_VALUE;
		}

		return inUse;
	}

	/**
	 * Finds a free PCI device slot on the primary bus (domain 0, bus 0) that doesn't
	 * collide with existing device addresses.
	 * <p>
	 * The method first checks if the exact same PCI address was already assigned
	 * (for reuse), otherwise returns the first free slot starting from device 2.
	 * </p>
	 *
	 * @param inUse PCI slot usage map from {@link #buildPciSlotUsageMap(Domain)}
	 * @param pciDeviceAddress original PCI device address to find a slot for
	 * @return free PCI device number (0-63) for assignment
	 * @throws IllegalStateException if no free PCI slot is available
	 */
	public static int findFreePciDeviceSlot( int[] inUse, HostdevPciDeviceAddress pciDeviceAddress )
	{
		int devAddr;
		int firstFree = -1;
		int lookup = ( pciDeviceAddress.getPciDomain() << 16 )
				| ( pciDeviceAddress.getPciBus() << 8 )
				| ( pciDeviceAddress.getPciDevice() );

		for ( devAddr = 0; devAddr < inUse.length; ++devAddr ) {
			if ( firstFree == -1 && inUse[ devAddr ] == 0 ) {
				firstFree = devAddr;
			} else if ( inUse[ devAddr ] == lookup ) {
				return devAddr;
			}
		}

		if ( firstFree == -1 ) {
			throw new IllegalStateException( "No free PCI device slot available" );
		}

		inUse[ firstFree ] = lookup;
		return firstFree;
	}
}
