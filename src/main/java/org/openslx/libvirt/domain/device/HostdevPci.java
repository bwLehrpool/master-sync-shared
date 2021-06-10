package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev PCI device node in a Libvirt domain XML document for PCI passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevPci extends Hostdev implements HostdevAddressableSource<HostdevPciDeviceAddress>,
		HostdevAddressableTarget<HostdevPciDeviceAddress>
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
	 * Returns the PCI device address from an address XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @return PCI device address from the selected XML address element.
	 */
	private HostdevPciDeviceAddress getPciAddress( final String expression )
	{
		String pciDomain = this.getXmlElementAttributeValue( expression, "domain" );
		String pciBus = this.getXmlElementAttributeValue( expression, "bus" );
		String pciDevice = this.getXmlElementAttributeValue( expression, "slot" );
		String pciFunction = this.getXmlElementAttributeValue( expression, "function" );

		pciDomain = HostdevUtils.removeHexPrefix( pciDomain );
		pciBus = HostdevUtils.removeHexPrefix( pciBus );
		pciDevice = HostdevUtils.removeHexPrefix( pciDevice );
		pciFunction = HostdevUtils.removeHexPrefix( pciFunction );

		return HostdevPciDeviceAddress.valueOf( pciDomain + ":" + pciBus + ":" + pciDevice + "." + pciFunction );
	}

	/**
	 * Sets the PCI device address for an XML address element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @param address PCI device address for the selected XML address element.
	 */
	private void setPciAddress( final String expression, final HostdevPciDeviceAddress address )
	{
		final String pciDomain = HostdevUtils.appendHexPrefix( address.getPciDomainAsString() );
		final String pciBus = HostdevUtils.appendHexPrefix( address.getPciBusAsString() );
		final String pciDevice = HostdevUtils.appendHexPrefix( address.getPciDeviceAsString() );
		final String pciFunction = HostdevUtils.appendHexPrefix( address.getPciFunctionAsString() );

		this.setXmlElementAttributeValue( expression, "domain", pciDomain );
		this.setXmlElementAttributeValue( expression, "bus", pciBus );
		this.setXmlElementAttributeValue( expression, "slot", pciDevice );
		this.setXmlElementAttributeValue( expression, "function", pciFunction );
	}

	@Override
	public HostdevPciDeviceAddress getSource()
	{
		return this.getPciAddress( "source/address" );
	}

	@Override
	public void setSource( HostdevPciDeviceAddress address )
	{
		this.setPciAddress( "source/address", address );
	}

	@Override
	public HostdevPciDeviceAddress getTarget()
	{
		return this.getPciAddress( "address" );
	}

	@Override
	public void setTarget( HostdevPciDeviceAddress address )
	{
		this.setPciAddress( "address", address );
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
