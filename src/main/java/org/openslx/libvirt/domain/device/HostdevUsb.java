package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev USB device node in a Libvirt domain XML document for USB passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevUsb extends Hostdev implements HostdevAddressableSource<HostdevUsbDeviceDescription>,
		HostdevAddressableTarget<HostdevUsbDeviceAddress>
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

	@Override
	public HostdevUsbDeviceDescription getSource()
	{
		String vendorId = this.getXmlElementAttributeValue( "source/address/vendor", "id" );
		String productId = this.getXmlElementAttributeValue( "source/address/product", "id" );

		vendorId = HostdevUtils.removeHexPrefix( vendorId );
		productId = HostdevUtils.removeHexPrefix( productId );

		return HostdevUsbDeviceDescription.valueOf( vendorId + ":" + productId );
	}

	@Override
	public void setSource( HostdevUsbDeviceDescription description )
	{
		final String vendorId = HostdevUtils.appendHexPrefix( description.getVendorIdAsString() );
		final String productId = HostdevUtils.appendHexPrefix( description.getProductIdAsString() );

		this.setXmlElementAttributeValue( "source/address/vendor", "id", vendorId );
		this.setXmlElementAttributeValue( "source/address/product", "id", productId );
	}

	@Override
	public HostdevUsbDeviceAddress getPciTarget()
	{
		final String usbBus = this.getXmlElementAttributeValue( "address", "bus" );
		final String usbPort = this.getXmlElementAttributeValue( "address", "port" );

		return HostdevUsbDeviceAddress.valueOf( usbBus, usbPort );
	}

	@Override
	public void setPciTarget( HostdevUsbDeviceAddress address )
	{
		this.setXmlElementAttributeValue( "address", "bus", Integer.toString( address.getUsbBus() ) );
		this.setXmlElementAttributeValue( "address", "port", Integer.toString( address.getUsbPort() ) );
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
