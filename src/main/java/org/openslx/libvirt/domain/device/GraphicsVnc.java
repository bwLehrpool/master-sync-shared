package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A graphics VNC device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class GraphicsVnc extends Graphics
{
	/**
	 * Creates an empty graphics VNC device.
	 */
	public GraphicsVnc()
	{
		super();
	}

	/**
	 * Creates a graphics VNC device representing an existing Libvirt XML graphics VNC device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics VNC device element.
	 */
	public GraphicsVnc( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent graphics VNC device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created graphics VNC device instance.
	 */
	public static GraphicsVnc createInstance( LibvirtXmlNode xmlNode )
	{
		return GraphicsVnc.newInstance( xmlNode );
	}

	/**
	 * Creates a graphics VNC device representing an existing Libvirt XML graphics VNC device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics VNC device element.
	 * @return graphics VNC device instance.
	 */
	public static GraphicsVnc newInstance( LibvirtXmlNode xmlNode )
	{
		return new GraphicsVnc( xmlNode );
	}
}
