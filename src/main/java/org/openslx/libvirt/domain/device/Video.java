package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A video (GPU) device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Video extends Device
{
	/**
	 * Creates an empty video device.
	 */
	public Video()
	{
		super();
	}

	/**
	 * Creates a video device representing an existing Libvirt XML video device element.
	 * 
	 * @param xmlNode existing Libvirt XML video device element.
	 */
	public Video( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns hardware model of the video device.
	 * 
	 * @return hardware model of the video device.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model", "type" );
		return Model.fromString( model );
	}

	/**
	 * Sets hardware model for the video device.
	 * 
	 * @param model hardware model for the video device.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", "type", model.toString() );
	}

	/**
	 * Checks whether 2D hardware video acceleration is turned on or off.
	 * 
	 * @return state of 2D hardware video acceleration.
	 */
	public boolean get2DAcceleration()
	{
		return this.getXmlElementAttributeValueAsBool( "model/acceleration", "accel2d" );
	}

	/**
	 * Turns 2D hardware video acceleration on or off.
	 * 
	 * @param acceleration state of 2D hardware video acceleration.
	 */
	public void set2DAcceleration( boolean acceleration )
	{
		Model model = this.getModel();

		if ( model != null ) {
			if ( model == Model.VIRTIO ) {
				// only set acceleration on supported Virtio GPUs
				this.setXmlElementAttributeValue( "model/acceleration", "accel2d", acceleration );
			} else {
				String errorMsg = new String(
						"Video card model '" + model.toString() + "' does not support enabled 2D hardware acceleration." );
				throw new IllegalArgumentException( errorMsg );
			}
		}
	}

	/**
	 * Checks whether 3D hardware video acceleration is turned on or off.
	 * 
	 * @return state of 3D hardware video acceleration.
	 */
	public boolean get3DAcceleration()
	{
		return this.getXmlElementAttributeValueAsBool( "model/acceleration", "accel3d" );
	}

	/**
	 * Turns 3D hardware video acceleration on or off.
	 * 
	 * @param acceleration state of 3D hardware video acceleration.
	 */
	public void set3DAcceleration( boolean acceleration )
	{
		Model model = this.getModel();

		if ( model == Model.VIRTIO ) {
			// only set acceleration on supported Virtio GPUs
			this.setXmlElementAttributeValue( "model/acceleration", "accel3d", acceleration );
		} else {
			String errorMsg = new String(
					"Video card model '" + model.toString() + "' does not support enabled 3D hardware acceleration." );
			throw new IllegalArgumentException( errorMsg );
		}
	}

	/**
	 * Creates a non-existent video device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created video device instance.
	 */
	public static Video createInstance( LibvirtXmlNode xmlNode )
	{
		return Video.newInstance( xmlNode );
	}

	/**
	 * Creates a video device representing an existing Libvirt XML video device element.
	 * 
	 * @param xmlNode existing Libvirt XML video device element.
	 * @return video device instance.
	 */
	public static Video newInstance( LibvirtXmlNode xmlNode )
	{
		return new Video( xmlNode );
	}

	/**
	 * Model of video device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		NONE  ( "none" ),
		QXL   ( "qxl" ),
		VGA   ( "vga" ),
		VMVGA ( "vmvga" ),
		VIRTIO( "virtio" );
		// @formatter:on

		/**
		 * Name of the video device model.
		 */
		private String model = null;

		/**
		 * Creates video device model.
		 * 
		 * @param type valid name of the video device model in a Libvirt domain XML document.
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
		 * Creates video device model from its name with error check.
		 * 
		 * @param model name of the video device model in a Libvirt domain XML document.
		 * @return valid video device model.
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
