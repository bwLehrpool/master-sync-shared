package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A network bridge interface device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class InterfaceBridge extends Interface
{
	/**
	 * Creates an empty network bridge interface device.
	 */
	public InterfaceBridge()
	{
		super();
	}

	/**
	 * Creates a network bridge interface device representing an existing Libvirt XML network bridge
	 * interface device element.
	 * 
	 * @param xmlNode existing Libvirt XML network bridge interface device element.
	 */
	public InterfaceBridge( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent network bridge interface device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created network bridge interface device instance.
	 */
	public static InterfaceBridge createInstance( LibvirtXmlNode xmlNode )
	{
		return InterfaceBridge.newInstance( xmlNode );
	}

	/**
	 * Creates a network bridge interface device representing an existing Libvirt XML network bridge
	 * interface device element.
	 * 
	 * @param xmlNode existing Libvirt XML network bridge interface device element.
	 * @return network bridge interface device instance.
	 */
	public static InterfaceBridge newInstance( LibvirtXmlNode xmlNode )
	{
		return new InterfaceBridge( xmlNode );
	}
}
