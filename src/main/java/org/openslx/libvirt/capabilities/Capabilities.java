package org.openslx.libvirt.capabilities;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openslx.libvirt.capabilities.cpu.Cpu;
import org.openslx.libvirt.capabilities.guest.Guest;
import org.openslx.libvirt.xml.LibvirtXmlDocument;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.openslx.libvirt.xml.LibvirtXmlResources;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Implementation of the Libvirt capabilities XML document.
 * 
 * The Libvirt capabilities XML document is used to describe the configuration and capabilities of
 * the hypervisor's host.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Capabilities extends LibvirtXmlDocument
{
	/**
	 * Creates Libvirt capabilities XML document from {@link String} providing Libvirt capabilities
	 * XML content.
	 * 
	 * @param xml {@link String} with Libvirt capabilities XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the capabilities XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid capabilities XML document.
	 */
	public Capabilities( String xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "capabilities.rng" ) );
	}

	/**
	 * Creates Libvirt capabilities XML document from {@link File} containing Libvirt capabilities
	 * XML content.
	 * 
	 * @param xml existing {@link File} containing Libvirt capabilities XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the capabilities XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid capabilities XML document.
	 */
	public Capabilities( File xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "capabilities.rng" ) );
	}

	/**
	 * Creates Libvirt capabilities XML document from {@link InputStream} providing Libvirt
	 * capabilities XML content.
	 * 
	 * @param xml {@link InputStream} providing Libvirt capabilities XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the capabilities XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid capabilities XML document.
	 */
	public Capabilities( InputStream xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "capabilities.rng" ) );
	}

	/**
	 * Creates Libvirt capabilities XML document from {@link InputSource} providing Libvirt
	 * capabilities XML content.
	 * 
	 * @param xml {@link InputSource} providing Libvirt capabilities XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the capabilities XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid capabilities XML document.
	 */
	public Capabilities( InputSource xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "capabilities.rng" ) );
	}

	/**
	 * Returns UUID of the Libvirt host machine.
	 * 
	 * @return UUID of the host machine.
	 */
	public String getHostUuid()
	{
		return this.getRootXmlNode().getXmlElementValue( "host/uuid" );
	}

	/**
	 * Returns CPU capabilities of the host machine.
	 * 
	 * @return CPU capabilities of the host machine.
	 */
	public Cpu getHostCpu()
	{
		final Node hostCpuNode = this.getRootXmlNode().getXmlElement( "host/cpu" );

		if ( hostCpuNode == null ) {
			return null;
		} else {
			final LibvirtXmlNode hostCpuXmlNode = new LibvirtXmlNode( this.getRootXmlNode().getXmlDocument(),
					hostCpuNode );
			return Cpu.newInstance( hostCpuXmlNode );
		}
	}

	/**
	 * Checks whether the Libvirt host machine has IOMMU support.
	 * 
	 * @return State of the IOMMU support.
	 */
	public boolean hasHostIommuSupport()
	{
		return this.getRootXmlNode().getXmlElementAttributeValueAsBool( "host/iommu", "support" );
	}

	/**
	 * Returns capabilities of all possible guest machines.
	 * 
	 * @return capabilities of all possible guest machines.
	 */
	public List<Guest> getGuests()
	{
		final List<Guest> guestList = new ArrayList<Guest>();
		final NodeList guestNodes = this.getRootXmlNode().getXmlNodes( "guest" );

		for ( int i = 0; i < guestNodes.getLength(); i++ ) {
			final LibvirtXmlNode guestNode = new LibvirtXmlNode( this.getRootXmlNode().getXmlDocument(),
					guestNodes.item( i ) );
			final Guest guest = Guest.newInstance( guestNode );

			if ( guest != null ) {
				guestList.add( guest );
			}
		}

		return guestList;
	}
}
