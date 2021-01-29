package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * An IDE controller device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerIde extends Controller
{
	/**
	 * Creates an empty IDE controller device.
	 */
	public ControllerIde()
	{
		super();
	}

	/**
	 * Creates an IDE controller device representing an existing Libvirt XML IDE controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML IDE controller device element.
	 */
	public ControllerIde( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns emulated hardware model of the IDE controller.
	 * 
	 * @return hardware model of the IDE controller.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model" );
		return Model.fromString( model );
	}

	/**
	 * Sets hardware model for the IDE controller.
	 * 
	 * @param model hardware model for the IDE controller.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", model.toString() );
	}

	/**
	 * Creates a non-existent IDE controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created IDE controller device instance.
	 */
	public static ControllerIde createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerIde.newInstance( xmlNode );
	}

	/**
	 * Creates an IDE controller device representing an existing Libvirt XML IDE controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML IDE controller device element.
	 * @return IDE controller device instance.
	 */
	public static ControllerIde newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerIde( xmlNode );
	}

	/**
	 * Model of IDE controller device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Model
	{
		// @formatter:off
		PIIX3( "piix3" ),
		PIIX4( "pixx4" ),
		ICH6 ( "ich6" );
		// @formatter:on

		/**
		 * Name of the IDE controller device model.
		 */
		private String model = null;

		/**
		 * Creates IDE controller device model.
		 * 
		 * @param type valid name of the IDE controller device model in a Libvirt domain XML document.
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
		 * Creates IDE controller device model from its name with error check.
		 * 
		 * @param type name of the IDE controller device model in a Libvirt domain XML document.
		 * @return valid IDE controller device model.
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
