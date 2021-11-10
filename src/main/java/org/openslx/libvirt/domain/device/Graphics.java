package org.openslx.libvirt.domain.device;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	 * Returns the listen type of the graphics device.
	 * 
	 * @return listen type of the graphics device.
	 */
	public ListenType getListenType()
	{
		final String listenType = this.getXmlElementAttributeValue( "listen", "type" );
		return ListenType.fromString( listenType );
	}

	/**
	 * Sets the listen type for the graphics device.
	 * 
	 * @param type listen type for the graphics device.
	 */
	public void setListenType( ListenType type )
	{
		this.setXmlElementAttributeValue( "listen", "type", type.toString() );
	}

	/**
	 * Returns the listen address of the graphics device.
	 * 
	 * @return listen address of the graphics device.
	 */
	public InetAddress getListenAddress()
	{
		InetAddress parsedListenAddress = null;

		if ( this.getListenType() == ListenType.ADDRESS ) {
			// only read listen address, if address listen type is set
			final String rawListenAddress = this.getXmlElementAttributeValue( "listen", "address" );

			try {
				parsedListenAddress = InetAddress.getByName( rawListenAddress );
			} catch ( UnknownHostException e ) {
				parsedListenAddress = null;
			}
		}

		return parsedListenAddress;
	}

	/**
	 * Sets the listen address for the graphics device.
	 * 
	 * @param listenAddress listen address for the graphics device.
	 */
	public void setListenAddress( InetAddress listenAddress )
	{
		if ( this.getListenType() == ListenType.ADDRESS && listenAddress != null ) {
			// only set listen address, if address listen type is set
			this.setXmlElementAttributeValue( "listen", "address", listenAddress.getHostAddress() );
		}
	}

	/**
	 * Returns the listen port of the graphics device.
	 * 
	 * @return listen port of the graphics device.
	 */
	public int getListenPort()
	{
		final String listenPort = this.getXmlElementAttributeValue( "port" );
		return Integer.valueOf( listenPort );
	}

	/**
	 * Sets the listen port for the graphics device.
	 * 
	 * @param listenPort listen port for the graphics device.
	 */
	public void setListenPort( int listenPort )
	{
		if ( this.getListenType() == ListenType.ADDRESS ) {
			// only set listen port, if address listen type is set
			this.setXmlElementAttributeValue( "port", Integer.toString( listenPort ) );
		}
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

		if ( graphics instanceof GraphicsSpice ) {
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
	 * Listen type of graphics device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum ListenType
	{
		// @formatter:off
		NONE   ( "none" ),
		ADDRESS( "address" ),
		NETWORK( "network" ),
		SOCKET ( "socket" );
      // @formatter:on

		/**
		 * Name of graphics device listen type.
		 */
		private String type = null;

		/**
		 * Creates graphics device listen type.
		 * 
		 * @param type valid name of the graphics device listen type in a Libvirt domain XML document.
		 */
		ListenType( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates graphics device listen type from its name with error check.
		 * 
		 * @param type name of the graphics device listen type in a Libvirt domain XML document.
		 * @return valid graphics device listen type.
		 */
		public static ListenType fromString( String type )
		{
			for ( ListenType t : ListenType.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
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
