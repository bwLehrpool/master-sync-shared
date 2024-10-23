package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev PCI device node in a Libvirt domain XML document for PCI passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevPci extends Hostdev implements HostdevAddressableSource<HostdevPciDeviceAddress>
{
	/**
	 * Creates an empty hostdev PCI device.
	 */
	public HostdevPci()
	{
		super();
	}

	/**
	 * Creates a hostdev PCI device representing an existing Libvirt XML hostdev PCI device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev PCI device element.
	 */
	public HostdevPci( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	@Override
	public HostdevPciDeviceAddress getSource()
	{
		return this.getPciAddress( "source/address" );
	}

	@Override
	public void setSource( HostdevPciDeviceAddress address )
	{
		this.setPciAddress( "source/address", address, false );
	}

	/**
	 * Set multifunction mode.
	 * 
	 * If enabled, the device is said to have multiple functions.
	 */
	public void setMultifunction( boolean on )
	{
		this.setXmlElementAttributeValueOnOff( "address", "multifunction", on );
	}

	/**
	 * Creates a non-existent hostdev PCI device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created hostdev PCI device instance.
	 */
	public static HostdevPci createInstance( LibvirtXmlNode xmlNode )
	{
		return HostdevPci.newInstance( xmlNode );
	}

	/**
	 * Creates a hostdev PCI device representing an existing Libvirt XML hostdev PCI device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev PCI device element.
	 * @return hostdev PCI device instance.
	 */
	public static HostdevPci newInstance( LibvirtXmlNode xmlNode )
	{
		return new HostdevPci( xmlNode );
	}
}
