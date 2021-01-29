package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A SATA controller device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerSata extends Controller
{
	/**
	 * Creates an empty SATA controller device.
	 */
	public ControllerSata()
	{
		super();
	}

	/**
	 * Creates a SATA controller device representing an existing Libvirt XML SATA controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML SATA controller device element.
	 */
	public ControllerSata( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent SATA controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created SATA controller device instance.
	 */
	public static ControllerSata createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerSata.newInstance( xmlNode );
	}

	/**
	 * Creates a SATA controller device representing an existing Libvirt XML SATA controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML SATA controller device element.
	 * @return SATA controller device instance.
	 */
	public static ControllerSata newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerSata( xmlNode );
	}
}
