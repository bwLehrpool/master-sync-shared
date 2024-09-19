package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A video (GPU) device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class RedirDevice extends Device
{
	/**
	 * Creates an empty video device.
	 */
	public RedirDevice()
	{
		super();
	}

	/**
	 * Creates a redirect device representing an existing Libvirt XML redirect device element.
	 */
	public RedirDevice( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns type of redirect device.
	 */
	public SrcType getSrcType()
	{
		String type = this.getXmlElementAttributeValue( "type" );
		return SrcType.fromString( type );
	}

	/**
	 * Sets type for the redirect device.
	 */
	public void setSrcType( SrcType type )
	{
		this.setXmlElementAttributeValue( "type", type.toString() );
	}
	
	/**
	 * Get bus type.
	 */
	public BusType getBus()
	{
		return BusType.fromString( this.getXmlElementAttributeValue( "bus" ) );
	}

	/**
	 * Creates a non-existent video device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created video device instance.
	 */
	public static RedirDevice createInstance( LibvirtXmlNode xmlNode )
	{
		return RedirDevice.newInstance( xmlNode );
	}

	/**
	 * Creates a video device representing an existing Libvirt XML video device element.
	 * 
	 * @param xmlNode existing Libvirt XML video device element.
	 * @return video device instance.
	 */
	public static RedirDevice newInstance( LibvirtXmlNode xmlNode )
	{
		return new RedirDevice( xmlNode );
	}

	/**
	 * Type of redirected device.
	 */
	public enum SrcType
	{
		// @formatter:off
		DEV         ( "dev" ),
		FILE        ( "file" ),
		PIPE        ( "pipe" ),
		UNIX        ( "unix" ),
		TCP         ( "tcp" ),
		UDP         ( "udp" ),
		NULL        ( "null" ),
		STDIO       ( "stdio" ),
		VC          ( "vc" ),
		PTY         ( "pty" ),
		SPICEVMC    ( "spicevmc" ),
		SPICEPORT   ( "spiceport" ),
		NMDM        ( "nmdm" ),
		QEMU_VDAGENT( "qemu-vdagent" ),
		DBUS        ( "dbus" );
		// @formatter:on

		/**
		 * Type of device redirect, as expected in the xml file.
		 */
		private String typeString = null;

		SrcType( String typeString )
		{
			this.typeString = typeString;
		}

		@Override
		public String toString()
		{
			return this.typeString;
		}

		/**
		 * Returns Type instance from its name with error check.
		 */
		public static SrcType fromString( String model )
		{
			for ( SrcType m : SrcType.values() ) {
				if ( m.typeString.equalsIgnoreCase( model ) ) {
					return m;
				}
			}

			return null;
		}
	}
}
