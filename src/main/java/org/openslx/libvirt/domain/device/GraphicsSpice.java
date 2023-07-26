package org.openslx.libvirt.domain.device;

import java.net.InetAddress;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A graphics SPICE device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class GraphicsSpice extends Graphics
{
	/**
	 * Default address of a SPICE graphics listener.
	 */
	public static final InetAddress DEFAULT_ADDRESS = InetAddress.getLoopbackAddress();

	/**
	 * Default port of a SPICE graphics listener.
	 */
	public static final int DEFAULT_PORT = 59000;

	/**
	 * Creates an empty graphics SPICE device.
	 */
	public GraphicsSpice()
	{
		super();
	}

	/**
	 * Creates a graphics SPICE device representing an existing Libvirt XML graphics SPICE device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics SPCIE device element.
	 */
	public GraphicsSpice( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the state whether OpenGL hardware acceleration is enabled or not.
	 * 
	 * @return state whether OpenGL hardware acceleration is enabled or not.
	 */
	public boolean isOpenGlEnabled()
	{
		return this.getXmlElementAttributeValueAsBool( "gl", "enable" );
	}

	/**
	 * Sets the state whether OpenGL hardware acceleration is enabled or not.
	 * 
	 * @param enabled state whether OpenGL hardware acceleration is enabled or not.
	 */
	public void setOpenGl( boolean enabled )
	{
		this.setXmlElementAttributeValueYesNo( "gl", "enable", enabled );
	}

	/**
	 * Returns the image compression type.
	 * 
	 * @return image compression type.
	 */
	public ImageCompression getImageCompression()
	{
		final String imageCompression = this.getXmlElementAttributeValue( "image", "compression" );
		return ImageCompression.fromString( imageCompression );
	}

	/**
	 * Sets the image compression type.
	 * 
	 * @param type image compression type.
	 */
	public void setImageCompression( ImageCompression type )
	{
		this.setXmlElementAttributeValue( "image", "compression", type.toString() );
	}

	/**
	 * Checks if audio playback compression is enabled or not.
	 * 
	 * @return state whether audio playback compression is enabled or not.
	 */
	public boolean isPlaybackCompressionOn()
	{
		return this.getXmlElementAttributeValueAsBool( "playback", "compression" );
	}

	/**
	 * Sets the state whether audio playback compression is enabled or not.
	 * 
	 * @param enabled state whether audio playback compression is enabled or not.
	 */
	public void setPlaybackCompression( boolean enabled )
	{
		this.setXmlElementAttributeValueOnOff( "playback", "compression", enabled );
	}

	/**
	 * Returns the streaming mode.
	 * 
	 * @return streaming mode type.
	 */
	public StreamingMode getStreamingMode()
	{
		final String streamingMode = this.getXmlElementAttributeValue( "streaming", "mode" );
		return StreamingMode.fromString( streamingMode );
	}

	/**
	 * Sets the streaming mode.
	 * 
	 * @param type streaming mode type.
	 */
	public void setStreamingMode( StreamingMode type )
	{
		this.setXmlElementAttributeValue( "streaming", "mode", type.toString() );
	}

	/**
	 * Creates a non-existent graphics SPICE device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created graphics SPICE device instance.
	 */
	public static GraphicsSpice createInstance( LibvirtXmlNode xmlNode )
	{
		return GraphicsSpice.newInstance( xmlNode );
	}

	/**
	 * Creates a graphics SPICE device representing an existing Libvirt XML graphics SPICE device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics SPICE device element.
	 * @return graphics SPICE device instance.
	 */
	public static GraphicsSpice newInstance( LibvirtXmlNode xmlNode )
	{
		return new GraphicsSpice( xmlNode );
	}

	/**
	 * Image compression type of graphics device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum ImageCompression
	{
		// @formatter:off
		AUTO_GLZ( "auto_glz" ),
		AUTO_LZ ( "auto_lz" ),
		QUIC    ( "quic" ),
		GLZ     ( "glz" ),
		LZ      ( "lz" ),
		OFF     ( "off" );
      // @formatter:on

		/**
		 * Name of graphics device image compression type.
		 */
		private String type = null;

		/**
		 * Creates graphics device image compression type.
		 * 
		 * @param type valid name of the graphics device image compression type in a Libvirt domain
		 *           XML document.
		 */
		ImageCompression( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates graphics device image compression type from its name with error check.
		 * 
		 * @param type name of the graphics device image compression type in a Libvirt domain XML
		 *           document.
		 * @return valid graphics device image compression type.
		 */
		public static ImageCompression fromString( String type )
		{
			for ( ImageCompression t : ImageCompression.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Streaming mode type of graphics device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum StreamingMode
	{
		// @formatter:off
		ALL   ( "all" ),
		FILTER( "filter" ),
		OFF   ( "off" );
      // @formatter:on

		/**
		 * Name of graphics device image compression type.
		 */
		private String type = null;

		/**
		 * Creates graphics device streaming mode type.
		 * 
		 * @param type valid name of the graphics device streaming mode type in a Libvirt domain XML
		 *           document.
		 */
		StreamingMode( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates graphics device streaming mode type from its name with error check.
		 * 
		 * @param type name of the graphics device streaming mode type in a Libvirt domain XML
		 *           document.
		 * @return valid graphics device streaming mode type.
		 */
		public static StreamingMode fromString( String type )
		{
			for ( StreamingMode t : StreamingMode.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}
}
