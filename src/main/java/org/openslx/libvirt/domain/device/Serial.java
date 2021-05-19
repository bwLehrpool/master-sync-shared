package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A serial port device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Serial extends Device
{
	/**
	 * Creates an empty serial port device.
	 */
	public Serial()
	{
		super();
	}

	/**
	 * Creates a serial port device representing an existing Libvirt XML serial port device element.
	 * 
	 * @param xmlNode existing Libvirt XML serial port device element.
	 */
	public Serial( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the type of the serial port device.
	 * 
	 * @return type of the serial port device.
	 */
	public Type getType()
	{
		final String type = this.getXmlElementAttributeValue( "type" );
		return Type.fromString( type );
	}

	/**
	 * Sets the type for the serial port device.
	 * 
	 * @param type type for the serial port device.
	 */
	public void setType( Type type )
	{
		this.setXmlElementAttributeValue( "type", type.toString() );
	}

	/**
	 * Returns the source of the serial port device.
	 * 
	 * @return source of the serial port device.
	 */
	public String getSource()
	{
		return this.getXmlElementAttributeValue( "source", "path" );
	}

	/**
	 * Sets the source for the serial port device.
	 * 
	 * @param source source for the serial port device.
	 */
	public void setSource( String source )
	{
		this.setXmlElementAttributeValue( "source", "path", source );
	}

	/**
	 * Creates a non-existent serial port device as Libvirt XML serial port device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML serial port device that is created.
	 * @return created serial port device instance.
	 */
	public static Serial createInstance( LibvirtXmlNode xmlNode )
	{
		return Serial.newInstance( xmlNode );
	}

	/**
	 * Creates a serial port device representing an existing Libvirt XML serial port device element.
	 * 
	 * @param xmlNode existing Libvirt XML serial port device element.
	 * @return serial port device instance.
	 */
	public static Serial newInstance( LibvirtXmlNode xmlNode )
	{
		return new Serial( xmlNode );
	}

	/**
	 * Type of serial port device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Type
	{
		// @formatter:off
		DEV      ( "dev" ),
		FILE     ( "file" ),
		PIPE     ( "pipe" ),
		UNIX     ( "unix" ),
		TCP      ( "tcp" ),
		UDP      ( "udp" ),
		NULL     ( "null" ),
		STDIO    ( "stdio" ),
		VC       ( "vc" ),
		PTY      ( "pty" ),
		SPICEVMC ( "spicevmc" ),
		SPICEPORT( "spiceport" ),
		NMDM     ( "nmdm" );
		// @formatter:on

		/**
		 * Name of the serial port device type.
		 */
		private String type;

		/**
		 * Creates serial port device type.
		 * 
		 * @param type valid name of the serial port device type in a Libvirt domain XML document.
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
		 * Creates serial port device type from its name with error check.
		 * 
		 * @param type name of the serial port device type in a Libvirt domain XML document.
		 * @return valid serial port device type.
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
