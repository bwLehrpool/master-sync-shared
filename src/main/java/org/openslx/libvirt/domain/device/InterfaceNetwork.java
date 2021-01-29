package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A network interface device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class InterfaceNetwork extends Interface
{
	/**
	 * Creates an empty network interface device.
	 */
	public InterfaceNetwork()
	{
		super();
	}

	/**
	 * Creates a network interface device representing an existing Libvirt XML network interface
	 * device element.
	 * 
	 * @param xmlNode existing Libvirt XML network interface device element.
	 */
	public InterfaceNetwork( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent network interface device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created network interface device device instance.
	 */
	public static InterfaceNetwork createInstance( LibvirtXmlNode xmlNode )
	{
		return InterfaceNetwork.newInstance( xmlNode );
	}

	/**
	 * Creates a network interface device representing an existing Libvirt XML network interface
	 * device element.
	 * 
	 * @param xmlNode existing Libvirt XML network interface device element.
	 * @return network interface device instance.
	 */
	public static InterfaceNetwork newInstance( LibvirtXmlNode xmlNode )
	{
		return new InterfaceNetwork( xmlNode );
	}
}
