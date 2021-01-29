package org.openslx.libvirt.domain;

import java.math.BigInteger;

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
}
