package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A network interface device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Interface extends Device
{
	/**
	 * Creates an empty network device.
	 */
	public Interface()
	{
		super();
	}

	/**
	 * Creates a network device representing an existing Libvirt XML network interface device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML network interface device element.
	 */
	public Interface( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns hardware model of the network device.
	 * 
	 * @return hardware model of the network device.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model", "type" );
		return Model.fromString( model );
	}

	/**
	 * Sets hardware model for the network device.
	 * 
	 * @param model hardware model for the network device.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", "type", model.toString() );
	}

	/**
	 * Returns type of the network device.
	 * 
	 * @return type of the network device.
	 */
	public Type getType()
	{
		return Type.fromString( this.getXmlElementAttributeValue( "type" ) );
	}

	/**
	 * Sets type of the network device.
	 * 
	 * @return type of the network device.
	 */
	public void setType( Type type )
	{
		String source = this.getSource();

		// change type and set source again
		this.setXmlElementAttributeValue( "type", type.toString() );
		this.setSource( source );
	}

	/**
	 * Returns the source of the network device.
	 * 
	 * @return source of the network device.
	 */
	public String getSource()
	{
		Type type = this.getType();
		String source = null;

		switch ( type ) {
		case BRIDGE:
			source = this.getXmlElementAttributeValue( "source", "bridge" );
			break;
		case NETWORK:
			source = this.getXmlElementAttributeValue( "source", "network" );
			break;
		}

		return source;
	}

	/**
	 * Sets the source for the network device.
	 * 
	 * @param source for the network device.
	 */
	public void setSource( String source )
	{
		Type type = this.getType();

		// remove all attributes from sub-element 'source'
		this.removeXmlElementAttributes( "source" );

		switch ( type ) {
		case BRIDGE:
			this.setXmlElementAttributeValue( "source", "bridge", source );
			break;
		case NETWORK:
			this.setXmlElementAttributeValue( "source", "network", source );
			break;
		}
	}

	/**
	 * Returns MAC address of the network device.
	 *
	 * @return MAC address of the network device.
	 */
	public String getMacAddress()
	{
		return this.getXmlElementAttributeValue( "mac", "address" );
	}

	/**
	 * Sets MAC address of the network device.
	 *
	 * @param macAddress MAC address for the network device.
	 */
	public void setMacAddress( String macAddress )
	{
		this.setXmlElementAttributeValue( "mac", "address", macAddress );
	}

	/**
	 * Removes boot oder entry of the network interface device.
	 */
	public void removeBootOrder()
	{
		this.removeXmlElement( "boot" );
	}

	/**
	 * Removes network source of the network interface device.
	 */
	public void removeSource()
	{
		this.removeXmlElement( "source" );
	}

	/**
	 * Removes MAC address of the network interface device.
	 */
	public void removeMacAddress()
	{
		this.removeXmlElement( "mac" );
	}

	/**
	 * Creates a non-existent network interface device as Libvirt XML device element.
	 * 
	 * @param iface network device that is created.
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created network device instance.
	 */
	public static Interface createInstance( Interface iface, LibvirtXmlNode xmlNode )
	{
		Interface addedInterface = null;

		if ( iface instanceof InterfaceBridge ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.BRIDGE.toString() );
			addedInterface = InterfaceBridge.createInstance( xmlNode );
		} else if ( iface instanceof InterfaceNetwork ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.NETWORK.toString() );
			addedInterface = InterfaceNetwork.createInstance( xmlNode );
		}

		return addedInterface;
	}

	/**
	 * Creates a network interface device representing an existing Libvirt XML network interface
	 * device element.
	 * 
	 * @param xmlNode existing Libvirt XML network interface device element.
	 * @return network interface device instance.
	 */
	public static Interface newInstance( LibvirtXmlNode xmlNode )
	{
		Interface deviceInterface = null;
		Type type = Type.fromString( xmlNode.getXmlElementAttributeValue( "type" ) );

		if ( type == null ) {
			return null;
		}

		switch ( type ) {
		case BRIDGE:
			deviceInterface = InterfaceBridge.newInstance( xmlNode );
			break;
		case NETWORK:
			deviceInterface = InterfaceNetwork.newInstance( xmlNode );
			break;
		}

		return deviceInterface;
	}

	/**
	 * Type of network interface device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Type
	{
		// @formatter:off
		BRIDGE ( "bridge" ),
		NETWORK( "network" );
		// @formatter:on

		/**
		 * Name of the network interface device type.
		 */
		private String type = null;

		/**
		 * Creates network interface device type.
		 * 
		 * @param type valid name of the network interface device type in a Libvirt domain XML
		 *           document.
		 */
		Type( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates network interface device type from its name with error check.
		 * 
		 * @param type name of the network interface device type in a Libvirt domain XML document.
		 * @return valid network interface device type.
		 */
		public static Type fromString( String type )
		{
			for ( Type t : Type.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Model of network interface device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		E1000                          ( "e1000" ),
		E1000_82544GC                  ( "e1000-82544gc" ),
		E1000_82545EM                  ( "e1000-82545em" ),
		E1000E                         ( "e1000e" ),
		I82550                         ( "i82550" ), 
		I82551                         ( "i82551" ), 
		I82557A                        ( "i82557a" ), 
		I82557B                        ( "i82557b" ), 
		I82557C                        ( "i82557c" ), 
		I82558A                        ( "i82558a" ),
		I82558B                        ( "i82558b" ),
		I82559A                        ( "i82559a" ),
		I82559B                        ( "i82559b" ),
		I82559C                        ( "i82559c" ),
		I82559ER                       ( "i82559er" ),
		I82562                         ( "i82562" ),
		I82801                         ( "i82801" ),
		NE2K_PCI                       ( "ne2k_pci" ),
		PCNET                          ( "pcnet" ),
		RTL8139                        ( "rtl8139" ),
		TULIP                          ( "tulip" ),
		VIRTIO                         ( "virtio" ),
		VIRTIO_NET_PCI                 ( "virtio-net-pci" ),
		VIRTIO_NET_PCI_NON_TRANSITIONAL( "virtio-net-pci-non-transitional" ),
		VIRTIO_NET_PCI_TRANSITIONAL    ( "virtio-net-pci-transitional" ),
		VMXNET3                        ( "vmxnet3" );
		// @formatter:on

		/**
		 * Name of the network interface device model.
		 */
		private String model = null;

		/**
		 * Creates network interface device model.
		 * 
		 * @param type valid name of the network interface device model in a Libvirt domain XML
		 *           document.
		 */
		Model( String model )
		{
			this.model = model;
		}

		@Override
		public String toString()
		{
			return this.model;
		}

		/**
		 * Creates network interface device model from its name with error check.
		 * 
		 * @param type name of the network interface device model in a Libvirt domain XML document.
		 * @return valid network interface device model.
		 */
		public static Model fromString( String model )
		{
			for ( Model m : Model.values() ) {
				if ( m.model.equalsIgnoreCase( model ) ) {
					return m;
				}
			}

			return null;
		}
	}
}
