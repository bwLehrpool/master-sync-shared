package org.openslx.libvirt.domain.device;

import org.apache.commons.lang3.NotImplementedException;
import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A virtual machines device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Device extends LibvirtXmlNode implements HostdevAddressableTarget<HostdevPciDeviceAddress>
{
	/**
	 * Creates an empty virtual machine device.
	 */
	public Device()
	{
		super();
	}

	/**
	 * Creates a virtual machine device representing an existing Libvirt XML device element.
	 * 
	 * @param xmlNode existing Libvirt XML device element.
	 */
	public Device( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Removes device from Libvirt domain XML document.
	 */
	public void remove()
	{
		Node node = this.getXmlElement();
		node.getParentNode().removeChild( node );
	}

	/**
	 * Creates a Libvirt XML device element as child of a given Libvirt XML parent node.
	 * 
	 * @param xmlParentNode parent Libvirt XML node of the Libvirt XML device element that is
	 *           created.
	 * @param deviceType type of the Libvirt XML device element.
	 * @return created Libvirt XML device node.
	 */
	private static LibvirtXmlNode createDeviceElement( LibvirtXmlNode xmlParentNode, DeviceClass deviceType )
	{
		// create XML element as part of the Libvirt XML document
		Document xmlDocument = xmlParentNode.getXmlDocument();
		Element deviceNode = xmlDocument.createElement( deviceType.toString() );

		// append the created XML element to the Libvirt XML document
		xmlParentNode.getXmlBaseNode().appendChild( deviceNode );

		return new LibvirtXmlNode( xmlParentNode.getXmlDocument(), deviceNode );
	}

	/**
	 * Creates a non-existent virtual machine device as Libvirt XML device element.
	 * 
	 * @param device virtual machine device that is created.
	 * @param xmlParentNode parent Libvirt XML node of the Libvirt XML device that is created.
	 * @return created virtual machine device instance.
	 */
	public static Device createInstance( Device device, LibvirtXmlNode xmlParentNode )
	{
		Device createdDevice = null;

		if ( device instanceof Controller ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.CONTROLLER );
			createdDevice = Controller.createInstance( Controller.class.cast( device ), xmlNode );
		} else if ( device instanceof Disk ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.DISK );
			createdDevice = Disk.createInstance( Disk.class.cast( device ), xmlNode );
		} else if ( device instanceof FileSystem ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.FILESYSTEM );
			createdDevice = FileSystem.createInstance( xmlNode );
		} else if ( device instanceof Hostdev ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.HOSTDEV );
			createdDevice = Hostdev.createInstance( Hostdev.class.cast( device ), xmlNode );
		} else if ( device instanceof Interface ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.INTERFACE );
			createdDevice = Interface.createInstance( Interface.class.cast( device ), xmlNode );
		} else if ( device instanceof Graphics ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.GRAPHICS );
			createdDevice = Graphics.createInstance( Graphics.class.cast( device ), xmlNode );
		} else if ( device instanceof Parallel ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.PARALLEL );
			createdDevice = Parallel.createInstance( xmlNode );
		} else if ( device instanceof Serial ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.SERIAL );
			createdDevice = Serial.createInstance( xmlNode );
		} else if ( device instanceof Shmem ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.SHMEM );
			createdDevice = Shmem.createInstance( xmlNode );
		} else if ( device instanceof Sound ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.SOUND );
			createdDevice = Sound.createInstance( xmlNode );
		} else if ( device instanceof Video ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.VIDEO );
			createdDevice = Video.createInstance( xmlNode );
		} else if ( device instanceof RedirDevice ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, DeviceClass.REDIRDEV );
			createdDevice = RedirDevice.createInstance( xmlNode );
		}

		return createdDevice;
	}

	/**
	 * Creates a virtual machine device representing an existing Libvirt XML device element.
	 * 
	 * @param xmlNode existing Libvirt XML device element.
	 * @return virtual machine device instance.
	 */
	public static Device newInstance( LibvirtXmlNode xmlNode )
	{

		Node element = xmlNode.getXmlElement();

		if ( element == null ) {
			return null;
		} else {
			Device device = null;
			DeviceClass type = DeviceClass.fromString( element.getNodeName() );

			if ( type == null ) {
				return new Device( xmlNode );
			}

			switch ( type ) {
			case CONTROLLER:
				device = Controller.newInstance( xmlNode );
				break;
			case DISK:
				device = Disk.newInstance( xmlNode );
				break;
			case FILESYSTEM:
				device = FileSystem.newInstance( xmlNode );
				break;
			case HOSTDEV:
				device = Hostdev.newInstance( xmlNode );
				break;
			case INTERFACE:
				device = Interface.newInstance( xmlNode );
				break;
			case GRAPHICS:
				device = Graphics.newInstance( xmlNode );
				break;
			case PARALLEL:
				device = Parallel.newInstance( xmlNode );
				break;
			case REDIRDEV:
				device = RedirDevice.newInstance( xmlNode );
				break;
			case SERIAL:
				device = Serial.newInstance( xmlNode );
				break;
			case SHMEM:
				device = Shmem.newInstance( xmlNode );
				break;
			case SOUND:
				device = Sound.newInstance( xmlNode );
				break;
			case VIDEO:
				device = Video.newInstance( xmlNode );
				break;
			default:
				throw new NotImplementedException( "Class not implemented" );
			}

			return device;
		}
	}

	/**
	 * Sets the USB device address for an XML address element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @param address USB device address for the selected XML address element.
	 */
	protected void setUsbAddress( final String expression, final HostdevUsbDeviceAddress address )
	{
		this.setXmlElementAttributeValue( expression, "bus", Integer.toString( address.getUsbBus() ) );
		this.setXmlElementAttributeValue( expression, "port", Integer.toString( address.getUsbPort() ) );
		this.setXmlElementAttributeValue( expression, "type", BusType.USB.toString() );
	}

	/**
	 * Sets the PCI device address for an XML address element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @param address PCI device address for the selected XML address element.
	 */
	protected void setPciAddress( final String expression, final HostdevPciDeviceAddress address, boolean includeType )
	{
		final String pciDomain = HostdevUtils.appendHexPrefix( address.getPciDomainAsString() );
		final String pciBus = HostdevUtils.appendHexPrefix( address.getPciBusAsString() );
		final String pciDevice = HostdevUtils.appendHexPrefix( address.getPciDeviceAsString() );
		final String pciFunction = HostdevUtils.appendHexPrefix( address.getPciFunctionAsString() );

		this.setXmlElementAttributeValue( expression, "domain", pciDomain );
		this.setXmlElementAttributeValue( expression, "bus", pciBus );
		this.setXmlElementAttributeValue( expression, "slot", pciDevice );
		this.setXmlElementAttributeValue( expression, "function", pciFunction );
		if ( includeType ) {
			this.setXmlElementAttributeValue( expression, "type", BusType.PCI.toString() );
		} else {
			this.removeXmlElementAttribute( expression, "type" );
		}
	}

	/**
	 * Returns the PCI device address from an address XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @return PCI device address from the selected XML address element.
	 */
	protected HostdevPciDeviceAddress getPciAddress( final String expression )
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
	 * Returns this devices PCI bus address, or null if it doesn't have an explicit one
	 * set, or if the address type isn't PCI.
	 */
	public HostdevPciDeviceAddress getPciTarget()
	{
		if ( !"pci".equals( this.getXmlElementAttributeValue( "address", "type" ) ) )
			return null;
		return this.getPciAddress( "address" );
	}

	public void setPciTarget( HostdevPciDeviceAddress address )
	{
		this.setPciAddress( "address", address, true );
	}

	/**
	 * Returns the USB device address from an address XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select the XML address element.
	 * @return USB device address from the selected XML address element.
	 */
	protected HostdevUsbDeviceAddress getUsbAddress( final String expression )
	{
		String bus = this.getXmlElementAttributeValue( expression, "bus" );
		String port = this.getXmlElementAttributeValue( expression, "port" );

		return HostdevUsbDeviceAddress.valueOf( bus, port );
	}

	/**
	 * Returns which bus this device is connected to, or null if unknown
	 */
	public BusType getDeviceBusType()
	{
		return BusType.fromString( this.getXmlElementAttributeValue( "bus" ) );
	}

	/**
	 * Get class of device, i.e. enum value representing the XML tag
	 */
	public DeviceClass getDeviceClass()
	{
		return DeviceClass.fromString( this.getXmlBaseNode().getLocalName() );
	}

	/**
	 * Returns this devices USB bus/port address, or null if it doesn't have an explicit one
	 * set, or if the address type isn't USB.
	 */
	public HostdevUsbDeviceAddress getUsbTarget()
	{
		if ( !"usb".equals( this.getXmlElementAttributeValue( "address", "type" ) ) )
			return null;
		return this.getUsbAddress( "address" );
	}

	public void setUsbTarget( HostdevUsbDeviceAddress address )
	{
		this.setUsbAddress( "address", address );
	}

	/**
	 * Type of virtual machine devices.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum DeviceClass
	{
		// @formatter:off
		CONTROLLER( "controller" ),
		DISK      ( "disk" ),
		FILESYSTEM( "filesystem" ),
		HOSTDEV   ( "hostdev" ),
		INTERFACE ( "interface" ),
		GRAPHICS  ( "graphics" ),
		PARALLEL  ( "parallel" ),
		REDIRDEV  ( "redirdev" ),
		SERIAL    ( "serial" ),
		SHMEM     ( "shmem" ),
		SOUND     ( "sound" ),
		VIDEO     ( "video" );
		// @formatter:on

		/**
		 * Name of the virtual machine device type.
		 */
		private String type = null;

		/**
		 * Creates virtual machine device type.
		 * 
		 * @param type valid name of the virtual machine device type in a Libvirt domain XML document.
		 */
		DeviceClass( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates virtual machine device type from its name with error check.
		 * 
		 * @param type name of the virtual machine device type in a Libvirt domain XML document.
		 * @return valid virtual machine device type.
		 */
		public static DeviceClass fromString( String type )
		{
			for ( DeviceClass t : DeviceClass.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}

}
