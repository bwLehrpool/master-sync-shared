package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A floppy disk device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskFloppy extends Disk
{
	/**
	 * Creates an empty floppy disk device.
	 */
	public DiskFloppy()
	{
		super();
	}

	/**
	 * Creates a floppy disk device representing an existing Libvirt XML floppy disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML floppy disk device element.
	 */
	public DiskFloppy( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent floppy disk device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created floppy disk device instance.
	 */
	public static DiskFloppy createInstance( LibvirtXmlNode xmlNode )
	{
		return DiskFloppy.newInstance( xmlNode );
	}

	/**
	 * Creates a floppy disk device representing an existing Libvirt XML floppy disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML floppy disk device element.
	 * @return floppy disk device instance.
	 */
	public static DiskFloppy newInstance( LibvirtXmlNode xmlNode )
	{
		return new DiskFloppy( xmlNode );
	}
}
