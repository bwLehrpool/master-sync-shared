package org.openslx.libvirt.capabilities.guest;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * Implementation of a guest machine as part of the Libvirt XML capabilities document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Machine extends LibvirtXmlNode
{
	/**
	 * Creates an empty guest machine instance.
	 */
	public Machine()
	{
		super();
	}

	/**
	 * Creates an guest machine representing an existing Libvirt XML guest machine element.
	 * 
	 * @param xmlNode existing Libvirt XML guest machine element.
	 */
	public Machine( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the canonical machine name.
	 * 
	 * @return canonical machine name.
	 */
	public String getCanonicalMachine()
	{
		return this.getXmlElementAttributeValue( "canonical" );
	}

	/**
	 * Returns the maximum number of CPUs supported by the guest machine.
	 * 
	 * @return maximum number of CPUs supported by the guest machine.
	 */
	public int getMaxCpus()
	{
		final String numMaxCpus = this.getXmlElementAttributeValue( "maxCpus" );
		return Integer.parseUnsignedInt( numMaxCpus );
	}

	/**
	 * Returns the name of the guest machine.
	 * 
	 * @return name of the guest machine.
	 */
	public String getName()
	{
		return this.getXmlElementValue( null );
	}

	/**
	 * Creates an guest machine representing an existing Libvirt XML guest machine element.
	 * 
	 * @param xmlNode existing Libvirt XML guest machine element.
	 * @return guest machine instance.
	 */
	public static Machine newInstance( LibvirtXmlNode xmlNode )
	{
		return new Machine( xmlNode );
	}
}
