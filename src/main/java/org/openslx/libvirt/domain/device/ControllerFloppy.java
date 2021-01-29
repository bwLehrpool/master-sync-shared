package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A floppy controller device in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerFloppy extends Controller
{
	/**
	 * Creates an empty floppy controller device.
	 */
	public ControllerFloppy()
	{
		super();
	}

	/**
	 * Creates a floppy controller device representing an existing Libvirt XML floppy controller
	 * device element.
	 * 
	 * @param xmlNode existing Libvirt XML controller device element.
	 */
	public ControllerFloppy( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Creates a non-existent floppy controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created floppy controller device instance.
	 */
	public static ControllerFloppy createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerFloppy.newInstance( xmlNode );
	}

	/**
	 * Creates a floppy controller device representing an existing Libvirt XML floppy controller
	 * device element.
	 * 
	 * @param xmlNode existing Libvirt XML controller device element.
	 * @return floppy controller device instance.
	 */
	public static ControllerFloppy newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerFloppy( xmlNode );
	}
}
