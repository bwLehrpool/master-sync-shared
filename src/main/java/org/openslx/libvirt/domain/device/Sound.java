package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A sound device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Sound extends Device
{
	/**
	 * Creates an empty sound device.
	 */
	public Sound()
	{
		super();
	}

	/**
	 * Creates a sound device representing an existing Libvirt XML sound device element.
	 * 
	 * @param xmlNode existing Libvirt XML sound device element.
	 */
	public Sound( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns hardware model of the sound device.
	 * 
	 * @return hardware model of the sound device.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model" );
		return Model.fromString( model );
	}

	/**
	 * Sets hardware model for the sound device.
	 * 
	 * @param model hardware model for the sound device.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", model.toString() );
	}

	/**
	 * Creates a non-existent sound device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created sound device instance.
	 */
	public static Sound createInstance( LibvirtXmlNode xmlNode )
	{
		return Sound.newInstance( xmlNode );
	}

	/**
	 * Creates a sound device representing an existing Libvirt XML sound device element.
	 * 
	 * @param xmlNode existing Libvirt XML sound device element.
	 * @return sound device instance.
	 */
	public static Sound newInstance( LibvirtXmlNode xmlNode )
	{
		return new Sound( xmlNode );
	}

	/**
	 * Model of sound device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		AC97  ( "ac97" ),
		ES1370( "es1370" ),
		ICH6  ( "ich6" ),
		ICH9  ( "ich9" ),
		SB16  ( "sb16" );
		// @formatter:on

		/**
		 * Name of the sound device model.
		 */
		private String model;

		/**
		 * Creates sound device model.
		 * 
		 * @param type valid name of the sound device model in a Libvirt domain XML document.
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
		 * Creates sound device model from its name with error check.
		 * 
		 * @param type name of the sound device model in a Libvirt domain XML document.
		 * @return valid sound device model.
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
