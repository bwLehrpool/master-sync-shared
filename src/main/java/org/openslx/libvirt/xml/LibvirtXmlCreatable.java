package org.openslx.libvirt.xml;

import org.w3c.dom.Node;

/**
 * Serializability of a Libvirt XML object from/to its XML representation.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public interface LibvirtXmlCreatable
{
	/**
	 * Serializing an object from its XML representation.
	 * 
	 * @param xmlNode The object's XML representation.
	 */
	void fromXmlNode( Node xmlNode );

	/**
	 * Serializing the object to its XML representation.
	 * 
	 * @return XML representation of the object.
	 */
	Node toXmlNode();
}
