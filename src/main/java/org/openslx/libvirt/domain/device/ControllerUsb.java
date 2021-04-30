package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A USB controller device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerUsb extends Controller
{
	/**
	 * Creates an empty USB controller device.
	 */
	public ControllerUsb()
	{
		super();
	}

	/**
	 * Creates an USB controller device representing an existing Libvirt XML USB controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML USB controller device element.
	 */
	public ControllerUsb( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns hardware model of the PCI controller.
	 * 
	 * @return hardware model of the PCI controller.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model" );
		return Model.fromString( model );
	}

	/**
	 * Sets hardware model for the PCI controller.
	 * 
	 * @param model hardware model for the PCI controller.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", model.toString() );
	}

	/**
	 * Creates a non-existent USB controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created USB controller device instance.
	 */
	public static ControllerUsb createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerUsb.newInstance( xmlNode );
	}

	/**
	 * Creates an USB controller device representing an existing Libvirt XML USB controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML USB controller device element.
	 * @return USB controller device instance.
	 */
	public static ControllerUsb newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerUsb( xmlNode );
	}

	/**
	 * Model of PCI controller device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		NONE          ( "none" ),
		PIIX3_UHCI    ( "piix3-uhci" ),
		PIIX4_UHCI    ( "piix4-uhci" ),
		EHCI          ( "ehci" ),
		ICH9_EHCI1    ( "ich9-ehci1" ),
		ICH9_UHCI1    ( "ich9-uhci1" ),
		ICH9_UHCI2    ( "ich9-uhci2" ),
		ICH9_UHCI3    ( "ich9-uhci3" ),
		VT82C686B_UHCI( "vt82c686b-uhci" ),
		PCI_OHCI      ( "pci-ohci" ),
		NEC_XHCI      ( "nec-xhci" ),
		QUSB1         ( "qusb1" ),
		QUSB2         ( "qusb2" ),
		QEMU_XHCI     ( "qemu-xhci" );
		// @formatter:on

		/**
		 * Name of the USB controller device model.
		 */
		private String model = null;

		/**
		 * Creates USB controller device model.
		 * 
		 * @param type valid name of the USB controller device model in a Libvirt domain XML document.
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
		 * Creates USB controller device model from its name with error check.
		 * 
		 * @param model name of the USB controller device model in a Libvirt domain XML document.
		 * @return valid USB controller device model.
		 */
		public static Model fromString( String model )
		{
			for ( Model t : Model.values() ) {
				if ( t.model.equalsIgnoreCase( model ) ) {
					return t;
				}
			}

			return null;
		}
	}
}
