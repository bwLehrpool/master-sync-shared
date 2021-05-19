package org.openslx.libvirt.capabilities.cpu;

import java.math.BigInteger;

import org.openslx.libvirt.domain.DomainUtils;
import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * Implementation of a host CPU memory pages instance as part of the Libvirt capabilities XML
 * document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Pages extends LibvirtXmlNode
{
	/**
	 * Creates an empty host CPU memory pages instance.
	 */
	public Pages()
	{
		super();
	}

	/**
	 * Creates a host CPU memory pages instance representing an existing Libvirt XML host CPU pages
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU pages element.
	 */
	public Pages( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns size of the memory pages instance.
	 * 
	 * @return size of the memory pages instance.
	 */
	public BigInteger getSize()
	{
		final String pagesValue = this.getXmlElementAttributeValue( "size" );
		final String pagesUnit = this.getXmlElementAttributeValue( "unit" );

		return DomainUtils.decodeMemory( pagesValue, pagesUnit );
	}

	/**
	 * Creates a host CPU memory pages instance representing an existing Libvirt XML host CPU pages
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU pages element.
	 * @return host CPU memory pages instance.
	 */
	public static Pages newInstance( LibvirtXmlNode xmlNode )
	{
		return new Pages( xmlNode );
	}
}
