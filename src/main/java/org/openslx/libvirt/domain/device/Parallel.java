package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A parallel port device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Parallel extends Device
{
	/**
	 * Creates an empty parallel port device.
	 */
	public Parallel()
	{
		super();
	}

	/**
	 * Creates a parallel port device representing an existing Libvirt XML parallel port device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML parallel port device element.
	 */
	public Parallel( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the type of the parallel port device.
	 * 
	 * @return type of the parallel port device.
	 */
	public Type getType()
	{
		final String type = this.getXmlElementAttributeValue( "type" );
		return Type.fromString( type );
	}

	/**
	 * Sets the type for the parallel port device.
	 * 
	 * @param type type for the parallel port device.
	 */
	public void setType( Type type )
	{
		this.setXmlElementAttributeValue( "type", type.toString() );
	}

	/**
	 * Returns the source of the parallel port device.
	 * 
	 * @return source of the parallel port device.
	 */
	public String getSource()
	{
		return this.getXmlElementAttributeValue( "source", "path" );
	}

	/**
	 * Sets the source for the parallel port device.
	 * 
	 * @param source source for the parallel port device.
	 */
	public void setSource( String source )
	{
		this.setXmlElementAttributeValue( "source", "path", source );
	}

	/**
	 * Creates a non-existent parallel port device as Libvirt XML parallel port device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML parallel port device that is created.
	 * @return created parallel port device instance.
	 */
	public static Parallel createInstance( LibvirtXmlNode xmlNode )
	{
		return Parallel.newInstance( xmlNode );
	}

	/**
	 * Creates a parallel port device representing an existing Libvirt XML parallel port device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML parallel port device element.
	 * @return parallel port device instance.
	 */
	public static Parallel newInstance( LibvirtXmlNode xmlNode )
	{
		return new Parallel( xmlNode );
	}

	/**
	 * Type of parallel port device.
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
		 * Name of the parallel port device type.
		 */
		private String type;

		/**
		 * Creates parallel port device type.
		 * 
		 * @param type valid name of the parallel port device type in a Libvirt domain XML document.
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
		 * Creates parallel port device type from its name with error check.
		 * 
		 * @param type name of the parallel port device type in a Libvirt domain XML document.
		 * @return valid parallel port device type.
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
