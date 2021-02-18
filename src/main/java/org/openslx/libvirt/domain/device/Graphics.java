package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A graphics (display) device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Graphics extends Device
{
	/**
	 * Creates an empty graphics device.
	 */
	public Graphics()
	{
		super();
	}

	/**
	 * Creates a graphics device representing an existing Libvirt XML graphics device element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics device element.
	 */
	public Graphics( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent graphics device as Libvirt XML device element.
	 * 
	 * @param graphics graphics device that is created.
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created graphics device instance.
	 */
	public static Graphics createInstance( Graphics graphics, LibvirtXmlNode xmlNode )
	{
		Graphics addedGraphics = null;

		if ( graphics instanceof GraphicsSdl ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.SDL.toString() );
			addedGraphics = GraphicsSdl.createInstance( xmlNode );
		} else if ( graphics instanceof GraphicsSpice ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.SPICE.toString() );
			addedGraphics = GraphicsSpice.createInstance( xmlNode );
		} else if ( graphics instanceof GraphicsVnc ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.VNC.toString() );
			addedGraphics = GraphicsVnc.createInstance( xmlNode );
		}

		return addedGraphics;
	}

	/**
	 * Creates a graphics device representing an existing Libvirt XML graphics device element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics device element.
	 * @return graphics device instance.
	 */
	public static Graphics newInstance( LibvirtXmlNode xmlNode )
	{
		Graphics deviceGraphics = null;
		Type type = Type.fromString( xmlNode.getXmlElementAttributeValue( "type" ) );

		if ( type == null ) {
			return null;
		}

		switch ( type ) {
		case SDL:
			deviceGraphics = GraphicsSdl.newInstance( xmlNode );
			break;
		case SPICE:
			deviceGraphics = GraphicsSpice.newInstance( xmlNode );
			break;
		case VNC:
			deviceGraphics = GraphicsVnc.newInstance( xmlNode );
			break;
		}

		return deviceGraphics;
	}

	/**
	 * Type of graphics device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Type
	{
		// @formatter:off
		SDL  ( "sdl" ),
		SPICE( "spice" ),
		VNC  ( "vnc" );
      // @formatter:on

		/**
		 * Name of graphics device type.
		 */
		private String type = null;

		/**
		 * Creates graphics device type.
		 * 
		 * @param type valid name of the graphics device type in a Libvirt domain XML document.
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
		 * Creates graphics device type from its name with error check.
		 * 
		 * @param type name of the graphics device type in a Libvirt domain XML document.
		 * @return valid graphics device type.
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
