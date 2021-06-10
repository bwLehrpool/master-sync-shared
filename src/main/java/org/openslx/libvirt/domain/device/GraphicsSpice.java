package org.openslx.libvirt.domain.device;

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
	 * @return tate whether OpenGL hardware acceleration is enabled or not.
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
}
