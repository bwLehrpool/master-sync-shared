package org.openslx.libvirt.domain.device;

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
public class Device extends LibvirtXmlNode
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
	private static LibvirtXmlNode createDeviceElement( LibvirtXmlNode xmlParentNode, Type deviceType )
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
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.CONTROLLER );
			createdDevice = Controller.createInstance( Controller.class.cast( device ), xmlNode );
		} else if ( device instanceof Disk ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.DISK );
			createdDevice = Disk.createInstance( Disk.class.cast( device ), xmlNode );
		} else if ( device instanceof FileSystem ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.FILESYSTEM );
			createdDevice = FileSystem.createInstance( xmlNode );
		} else if ( device instanceof Hostdev ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.HOSTDEV );
			createdDevice = Hostdev.createInstance( Hostdev.class.cast( device ), xmlNode );
		} else if ( device instanceof Interface ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.INTERFACE );
			createdDevice = Interface.createInstance( Interface.class.cast( device ), xmlNode );
		} else if ( device instanceof Graphics ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.GRAPHICS );
			createdDevice = Graphics.createInstance( Graphics.class.cast( device ), xmlNode );
		} else if ( device instanceof Parallel ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.PARALLEL );
			createdDevice = Parallel.createInstance( xmlNode );
		} else if ( device instanceof Serial ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.SERIAL );
			createdDevice = Serial.createInstance( xmlNode );
		} else if ( device instanceof Sound ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.SOUND );
			createdDevice = Sound.createInstance( xmlNode );
		} else if ( device instanceof Video ) {
			LibvirtXmlNode xmlNode = Device.createDeviceElement( xmlParentNode, Type.VIDEO );
			createdDevice = Video.createInstance( xmlNode );
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
			Type type = Type.fromString( element.getNodeName() );

			if ( type == null ) {
				return null;
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
			case SERIAL:
				device = Serial.newInstance( xmlNode );
				break;
			case SOUND:
				device = Sound.newInstance( xmlNode );
				break;
			case VIDEO:
				device = Video.newInstance( xmlNode );
				break;
			}

			return device;
		}
	}

	/**
	 * Type of virtual machine devices.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Type
	{
		// @formatter:off
		CONTROLLER( "controller" ),
		DISK      ( "disk" ),
		FILESYSTEM( "filesystem" ),
		HOSTDEV   ( "hostdev" ),
		INTERFACE ( "interface" ),
		GRAPHICS  ( "graphics" ),
		PARALLEL  ( "parallel" ),
		SERIAL    ( "serial" ),
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
		 * Creates virtual machine device type from its name with error check.
		 * 
		 * @param type name of the virtual machine device type in a Libvirt domain XML document.
		 * @return valid virtual machine device type.
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
}
