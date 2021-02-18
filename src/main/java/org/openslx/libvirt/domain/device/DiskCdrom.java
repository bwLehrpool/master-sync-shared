package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A CDROM disk device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskCdrom extends Disk
{
	/**
	 * Creates an empty CDROM disk device.
	 */
	public DiskCdrom()
	{
		super();
	}

	/**
	 * Creates a CDROM disk device representing an existing Libvirt XML CDROM disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML CDROM disk device element.
	 */
	public DiskCdrom( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );

		// restrict CDROM disk device default read/write access always to readonly
		this.setReadOnly( true );
	}

	/**
	 * Creates a non-existent CDROM disk device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created CDROM disk device instance.
	 */
	public static DiskCdrom createInstance( LibvirtXmlNode xmlNode )
	{
		return DiskCdrom.newInstance( xmlNode );
	}

	/**
	 * Creates a CDROM disk device representing an existing Libvirt XML CDROM disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML CDROM disk device element.
	 * @return CDROM disk device instance.
	 */
	public static DiskCdrom newInstance( LibvirtXmlNode xmlNode )
	{
		return new DiskCdrom( xmlNode );
	}
}
