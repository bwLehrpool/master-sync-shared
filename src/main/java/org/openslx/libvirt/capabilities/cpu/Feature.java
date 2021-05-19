package org.openslx.libvirt.capabilities.cpu;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * Implementation of a host CPU feature as part of the Libvirt capabilities XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Feature extends LibvirtXmlNode
{
	/**
	 * Creates an empty host CPU feature instance.
	 */
	public Feature()
	{
		super();
	}

	/**
	 * Creates an host CPU feature representing an existing Libvirt XML host CPU feature element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU feature element.
	 */
	public Feature( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the name of the host CPU feature.
	 * 
	 * @return name of the host CPU feature.
	 */
	public String getName()
	{
		return this.getXmlElementAttributeValue( "name" );
	}

	/**
	 * Creates an host CPU feature representing an existing Libvirt XML host CPU feature element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU feature element.
	 * @return host CPU feature instance.
	 */
	public static Feature newInstance( LibvirtXmlNode xmlNode )
	{
		return new Feature( xmlNode );
	}
}
