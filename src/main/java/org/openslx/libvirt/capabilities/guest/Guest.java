package org.openslx.libvirt.capabilities.guest;

import java.util.ArrayList;
import java.util.List;

import org.openslx.libvirt.domain.Domain.OsType;
import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.w3c.dom.NodeList;

/**
 * Implementation of the guest capabilities as part of the Libvirt capabilities XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Guest extends LibvirtXmlNode
{
	/**
	 * Creates an empty guest instance.
	 */
	public Guest()
	{
		super();
	}

	/**
	 * Creates a guest representing an existing Libvirt XML guest capabilities element.
	 * 
	 * @param xmlNode existing Libvirt XML guest capabilities element.
	 */
	public Guest( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Return OS type of the guest.
	 * 
	 * @return OS type of the guest.
	 */
	public OsType getOsType()
	{
		final String osType = this.getXmlElementValue( "os_type" );
		return OsType.fromString( osType );
	}

	/**
	 * Returns the architecture name of the guest.
	 * 
	 * @return architecture name of the guest.
	 */
	public String getArchName()
	{
		return this.getXmlElementAttributeValue( "arch", "name" );
	}

	/**
	 * Return word size of the guest's architecture.
	 * 
	 * @return word size of the guest's architecture.
	 */
	public int getArchWordSize()
	{
		final String archWordSize = this.getXmlElementValue( "arch/wordsize" );
		return Integer.parseInt( archWordSize );
	}

	public String getArchEmulator()
	{
		return this.getXmlElementValue( "arch/emulator" );
	}

	/**
	 * Returns the available machines of the guest's architecture.
	 * 
	 * @return available machines of the guest's architecture.
	 */
	public List<Machine> getArchMachines()
	{
		final List<Machine> machinesList = new ArrayList<Machine>();
		final NodeList machineNodes = this.getXmlNodes( "arch/machine" );

		for ( int i = 0; i < machineNodes.getLength(); i++ ) {
			final LibvirtXmlNode machineNode = new LibvirtXmlNode( this.getXmlDocument(), machineNodes.item( i ) );
			final Machine machine = Machine.newInstance( machineNode );

			if ( machine != null ) {
				machinesList.add( machine );
			}
		}

		return machinesList;
	}

	/**
	 * Returns the supported domains of the guest.
	 * 
	 * @return supported domains of the guest.
	 */
	public List<Domain> getArchDomains()
	{
		final List<Domain> domainList = new ArrayList<Domain>();
		final NodeList domainNodes = this.getXmlNodes( "arch/domain" );

		for ( int i = 0; i < domainNodes.getLength(); i++ ) {
			final LibvirtXmlNode domainNode = new LibvirtXmlNode( this.getXmlDocument(), domainNodes.item( i ) );
			final Domain domain = Domain.newInstance( domainNode );

			if ( domain != null ) {
				domainList.add( domain );
			}
		}

		return domainList;
	}

	/**
	 * Creates a guest representing an existing Libvirt XML guest capabilities element.
	 * 
	 * @param xmlNode existing Libvirt XML guest capabilities element.
	 * @return guest capabilities instance.
	 */
	public static Guest newInstance( LibvirtXmlNode xmlNode )
	{
		return new Guest( xmlNode );
	}
}
