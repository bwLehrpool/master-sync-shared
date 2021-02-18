package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A SCSI controller device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerScsi extends Controller
{
	/**
	 * Creates an empty SCSI controller device.
	 */
	public ControllerScsi()
	{
		super();
	}

	/**
	 * Creates a SCSI controller device representing an existing Libvirt XML SCSI controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML SCSI controller device element.
	 */
	public ControllerScsi( LibvirtXmlNode xmlNode )
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
	 * Creates a non-existent SCSI controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created SCSI controller device instance.
	 */
	public static ControllerScsi createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerScsi.newInstance( xmlNode );
	}

	/**
	 * Creates a SCSI controller device representing an existing Libvirt XML SCSI controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML SCSI controller device element.
	 * @return SCSI controller device instance.
	 */
	public static ControllerScsi newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerScsi( xmlNode );
	}

	/**
	 * Model of SCSI controller device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Model
	{
		// @formatter:off
		AUTO                   ( "auto" ),
		BUSLOGIC               ( "buslogic" ),
		IBMVSCSI               ( "ibmvscsi" ),
		LSISAS1068             ( "lsisas1068" ),
		LSISAS1078             ( "lsisas1078" ),
		VIRTIO_SCSI            ( "virtio-scsi" ),
		VMPVSCSI               ( "vmpvscsi" ),
		VIRTIO_TRANSITIONAL    ( "virtio-transitional" ),
		VIRTIO_NON_TRANSITIONAL( "virtio-non-transitional" ),
		NCR53C90               ( "ncr53c90" ),
		AM53C974               ( "am53c974" ),
		DC390                  ( "dc390" );
		// @formatter:on

		/**
		 * Name of the SCSI controller device model.
		 */
		private String model = null;

		/**
		 * Creates SCSI controller device model.
		 * 
		 * @param type valid name of the SCSI controller device model in a Libvirt domain XML
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
		 * Creates SCSI controller device model from its name with error check.
		 * 
		 * @param type name of the SCSI controller device model in a Libvirt domain XML document.
		 * @return valid SCSI controller device model.
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
