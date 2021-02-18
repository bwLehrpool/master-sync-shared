package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A graphics SDL device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class GraphicsSdl extends Graphics
{
	/**
	 * Creates an empty graphics SDL device.
	 */
	public GraphicsSdl()
	{
		super();
	}

	/**
	 * Creates a graphics SDL device representing an existing Libvirt XML graphics SDL device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics SDL device element.
	 */
	public GraphicsSdl( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent graphics SDL device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created graphics SDL device instance.
	 */
	public static GraphicsSdl createInstance( LibvirtXmlNode xmlNode )
	{
		return GraphicsSdl.newInstance( xmlNode );
	}

	/**
	 * Creates a graphics SDL device representing an existing Libvirt XML graphics SDL device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML graphics SDL device element.
	 * @return graphics SDL device instance.
	 */
	public static GraphicsSdl newInstance( LibvirtXmlNode xmlNode )
	{
		return new GraphicsSdl( xmlNode );
	}
}
