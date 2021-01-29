package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A storage (HDD, SSD, ...) disk device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskStorage extends Disk
{
	/**
	 * Creates an empty storage disk device.
	 */
	public DiskStorage()
	{
		super();
	}

	/**
	 * Creates a storage disk device representing an existing Libvirt XML storage disk device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML storage disk device element.
	 */
	public DiskStorage( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent storage disk device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created storage disk device instance.
	 */
	public static DiskStorage createInstance( LibvirtXmlNode xmlNode )
	{
		return DiskStorage.newInstance( xmlNode );
	}

	/**
	 * Creates a storage disk device representing an existing Libvirt XML storage disk device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML storage disk device element.
	 * @return storage disk device instance.
	 */
	public static DiskStorage newInstance( LibvirtXmlNode xmlNode )
	{
		return new DiskStorage( xmlNode );
	}
}
