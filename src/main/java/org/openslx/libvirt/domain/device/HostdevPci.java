package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev PCI device node in a Libvirt domain XML document for PCI passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevPci extends Hostdev
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

	/**
	 * Checks if PCI hostdev device is managed.
	 * 
	 * If {@link #isManaged()} returns <code>true</code> the hostdev PCI device is detached from the
	 * host before being passed on to the guest and reattached to the host after the guest exits.
	 * 
	 * @return state whether PCI hostdev device is managed.
	 */
	public boolean isManaged()
	{
		return this.getXmlElementAttributeValueAsBool( "managed" );
	}

	/**
	 * Sets state whether PCI hostdev device is managed.
	 * 
	 * If the <code>managed</code> parameter is set to <code>true</code> the PCI hostdev device is
	 * detached from the host before being passed on to the guest and reattached to the host after
	 * the guest exits.
	 * 
	 * @return state whether PCI hostdev device is managed.
	 */
	public void setManaged( boolean managed )
	{
		this.setXmlElementAttributeValue( "managed", managed );
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
