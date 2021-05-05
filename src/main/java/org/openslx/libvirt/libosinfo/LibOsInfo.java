package org.openslx.libvirt.libosinfo;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.openslx.libvirt.libosinfo.os.Os;
import org.openslx.libvirt.xml.LibvirtXmlDocument;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.openslx.libvirt.xml.LibvirtXmlResources;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.openslx.virtualization.Version;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Implementation of a libosinfo XML document.
 * 
 * The libosinfo XML document is used to describe existing operating systems, devices and their
 * configuration possibilities.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibOsInfo extends LibvirtXmlDocument
{
	/**
	 * Creates a libosinfo XML document from a {@link String} providing libosinfo XML content.
	 * 
	 * @param xml {@link String} with libosinfo XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the libosinfo XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid libosinfo XML document.
	 */
	public LibOsInfo( String xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibOsInfoRng( "osinfo.rng" ) );
	}

	/**
	 * Creates a libosinfo XML document from a {@link File} containing libosinfo XML content.
	 * 
	 * @param xml existing {@link File} containing libosinfo XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the libosinfo XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid libosinfo XML document.
	 */
	public LibOsInfo( File xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibOsInfoRng( "osinfo.rng" ) );
	}

	/**
	 * Creates a libosinfo XML document from an {@link InputStream} providing libosinfo XML content.
	 * 
	 * @param xml {@link InputStream} providing libosinfo XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the libosinfo XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid libosinfo XML document.
	 */
	public LibOsInfo( InputStream xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibOsInfoRng( "osinfo.rng" ) );
	}

	/**
	 * Creates libosinfo XML document from {@link InputSource} providing libosinfo XML content.
	 * 
	 * @param xml {@link InputSource} providing libosinfo XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the libosinfo XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid libosinfo XML document.
	 */
	public LibOsInfo( InputSource xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibOsInfoRng( "osinfo.rng" ) );
	}

	/**
	 * Returns the version of the libosinfo database.
	 * 
	 * @return version of the libosinfo database.
	 */
	public Version getVersion()
	{
		final String version = this.getRootXmlNode().getXmlElementAttributeValue( "version" );
		return Version.valueOf( version );
	}

	/**
	 * Returns a list of all defined operating systems.
	 * 
	 * @return list of all defined operating systems.
	 */
	public ArrayList<Os> getOses()
	{
		final ArrayList<Os> oses = new ArrayList<Os>();
		final NodeList osNodes = this.getRootXmlNode().getXmlNodes( "os" );

		if ( osNodes != null ) {
			for ( int i = 0; i < osNodes.getLength(); i++ ) {
				final Node childNode = osNodes.item( i );
				if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
					final LibvirtXmlNode osNode = new LibvirtXmlNode( this.getRootXmlNode().getXmlDocument(), childNode );
					final Os os = Os.newInstance( osNode );

					if ( os != null ) {
						oses.add( os );
					}
				}
			}
		}

		return oses;
	}

	/**
	 * Lookups an operating system in the libosinfo database specified by the operating system
	 * identifier.
	 * 
	 * @param osId identifier of the operating system to lookup in the libosinfo database.
	 * @return found operating system from the libosinfo database.
	 */
	public static Os lookupOs( String osId )
	{
		Os os = null;

		if ( osId != null && !osId.isEmpty() ) {
			ArrayList<Os> oses = null;

			try {
				final LibOsInfo osInfo = new LibOsInfo( LibvirtXmlResources.getLibOsInfoXml( "osinfo.xml" ) );
				oses = osInfo.getOses();
			} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
				oses = null;
			}

			if ( oses != null ) {
				final Predicate<Os> byOsId = osCandidate -> osId.equals( osCandidate.getId() );
				os = oses.stream().filter( byOsId ).findFirst().orElse( null );
			}
		}

		return os;
	}
}
