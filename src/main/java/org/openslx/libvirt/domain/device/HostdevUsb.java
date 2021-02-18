package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev USB device node in a Libvirt domain XML document for USB passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevUsb extends Hostdev
{
	/**
	 * Creates an empty hostdev USB device.
	 */
	public HostdevUsb()
	{
		super();
	}

	/**
	 * Creates a hostdev USB device representing an existing Libvirt XML hostdev USB device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev USB device element.
	 */
	public HostdevUsb( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent hostdev USB device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created hostdev USB device instance.
	 */
	public static HostdevUsb createInstance( LibvirtXmlNode xmlNode )
	{
		return HostdevUsb.newInstance( xmlNode );
	}

	/**
	 * Creates a hostdev USB device representing an existing Libvirt XML hostdev USB device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev USB device element.
	 * @return hostdev USB device instance.
	 */
	public static HostdevUsb newInstance( LibvirtXmlNode xmlNode )
	{
		return new HostdevUsb( xmlNode );
	}
}
