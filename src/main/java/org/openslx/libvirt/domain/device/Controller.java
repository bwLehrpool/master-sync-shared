package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A controller (PCI, USB, ...) device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Controller extends Device
{
	/**
	 * Creates an empty controller device.
	 */
	public Controller()
	{
		super();
	}

	/**
	 * Creates a controller device representing an existing Libvirt XML controller device element.
	 * 
	 * @param xmlNode existing Libvirt XML controller device element.
	 */
	public Controller( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns index of the controller.
	 * 
	 * @return index of the controller.
	 */
	public int getIndex()
	{
		String index = this.getXmlElementAttributeValue( "index" );
		return Integer.parseInt( index );
	}

	/**
	 * Sets index for the controller.
	 * 
	 * @param index index for the controller.
	 */
	public void setIndex( int index )
	{
		this.setXmlElementAttributeValue( "index", Integer.toString( index ) );
	}

	/**
	 * Creates a non-existent controller device as Libvirt XML device element.
	 * 
	 * @param controller controller device that is created.
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created controller device instance.
	 */
	public static Controller createInstance( Controller controller, LibvirtXmlNode xmlNode )
	{
		Controller addedController = null;

		if ( controller instanceof ControllerFloppy ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.FLOPPY.toString() );
			addedController = ControllerFloppy.createInstance( xmlNode );
		} else if ( controller instanceof ControllerIde ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.IDE.toString() );
			addedController = ControllerIde.createInstance( xmlNode );
		} else if ( controller instanceof ControllerPci ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.PCI.toString() );
			addedController = ControllerPci.createInstance( xmlNode );
		} else if ( controller instanceof ControllerSata ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.SATA.toString() );
			addedController = ControllerSata.createInstance( xmlNode );
		} else if ( controller instanceof ControllerScsi ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.SCSI.toString() );
			addedController = ControllerScsi.createInstance( xmlNode );
		} else if ( controller instanceof ControllerUsb ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.USB.toString() );
			addedController = ControllerUsb.createInstance( xmlNode );
		}

		return addedController;
	}

	/**
	 * Creates a controller device representing an existing Libvirt XML controller device element.
	 * 
	 * @param xmlNode existing Libvirt XML controller device element.
	 * @return controller device instance.
	 */
	public static Controller newInstance( LibvirtXmlNode xmlNode )
	{

		Controller deviceController = null;
		Type type = Type.fromString( xmlNode.getXmlElementAttributeValue( "type" ) );

		if ( type == null ) {
			return null;
		}

		switch ( type ) {
		case FLOPPY:
			deviceController = ControllerFloppy.newInstance( xmlNode );
			break;
		case IDE:
			deviceController = ControllerIde.newInstance( xmlNode );
			break;
		case PCI:
			deviceController = ControllerPci.newInstance( xmlNode );
			break;
		case SATA:
			deviceController = ControllerSata.newInstance( xmlNode );
			break;
		case SCSI:
			deviceController = ControllerScsi.newInstance( xmlNode );
			break;
		case USB:
			deviceController = ControllerUsb.newInstance( xmlNode );
			break;
		}

		return deviceController;
	}

	/**
	 * Type of controller device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Type
	{
		// @formatter:off
		FLOPPY( "fdc" ),
		IDE   ( "ide" ),
		PCI   ( "pci" ),
		SATA  ( "sata" ),
		SCSI  ( "scsi" ),
		USB   ( "usb" );
		// @formatter:on

		/**
		 * Name of the controller device type.
		 */
		private String type = null;

		/**
		 * Creates controller device type.
		 * 
		 * @param type valid name of the controller device type in a Libvirt domain XML document.
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
		 * Creates controller device type from its name with error check.
		 * 
		 * @param type name of the controller device type in a Libvirt domain XML document.
		 * @return valid controller device type.
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
