package org.openslx.virtualization.configuration;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openslx.bwlp.thrift.iface.OperatingSystem;

/**
 * Utilities to set up and edit virtualization configurations.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public final class VirtualizationConfigurationUtils
{
	/**
	 * Returns an operating system from a given list of operating systems determined by the
	 * virtualizer specific operating system parameters.
	 * 
	 * @param osList list of available operating systems.
	 * @param virtId virtualizer identifier, e.g. <code>vmware</code> for VMware
	 * @param virtOsId operating system identifier used by the virtualizer, eg.
	 *           <code>windows7-64</code> for 64bit Windows 7 on VMware.
	 */
	public static OperatingSystem getOsOfVirtualizerFromList( List<OperatingSystem> osList, String virtId,
			String virtOsId )
	{
		OperatingSystem os = null;

		for ( final OperatingSystem osCandidate : osList ) {
			final Map<String, String> osVirtualizerMapping = osCandidate.getVirtualizerOsId();
			if ( osVirtualizerMapping != null ) {
				for ( final Entry<String, String> entry : osVirtualizerMapping.entrySet() ) {
					// check if suitable OS has been found
					if ( entry.getKey().equals( virtId ) && entry.getValue().equals( virtOsId ) ) {
						// save OS and exit inner loop since OS has been found
						os = osCandidate;
						break;
					}
				}

				// exit outer loop if OS has been found
				if ( os != null ) {
					break;
				}
			}
		}

		return os;
	}
}
