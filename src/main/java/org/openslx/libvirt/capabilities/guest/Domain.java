package org.openslx.libvirt.capabilities.guest;

import org.openslx.libvirt.domain.Domain.Type;
import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * Implementation of a guest domain as part of the Libvirt capabilities XML capabilities document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Domain extends LibvirtXmlNode
{
	/**
	 * Creates an empty guest domain instance.
	 */
	public Domain()
	{
		super();
	}

	/**
	 * Creates a guest domain representing an existing Libvirt XML guest domain element.
	 * 
	 * @param xmlNode existing Libvirt XML guest domain element.
	 */
	public Domain( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the domain type of the guest domain.
	 * 
	 * @return type of the guest domain.
	 */
	public Type getType()
	{
		final String type = this.getXmlElementAttributeValue( "type" );
		return Type.fromString( type );
	}

	/**
	 * Creates a guest domain representing an existing Libvirt XML guest domain element.
	 * 
	 * @param xmlNode existing Libvirt XML guest domain element.
	 * @return guest domain instance.
	 */
	public static Domain newInstance( LibvirtXmlNode xmlNode )
	{
		return new Domain( xmlNode );
	}
}
