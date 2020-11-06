package org.openslx.util.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.openslx.util.Util;
import org.openslx.util.XmlHelper;
import org.openslx.util.vm.VmMetaData.HardDisk;
import org.w3c.dom.Document;

/**
 * Class handling the parsing of a .ovf machine description file For now only a
 * dummy for conversion and will be replaced in the image upload flow after
 * converting the ovf to vmx.
 */
public class OvfConfig
{
	private static final Logger LOGGER = Logger.getLogger( OvfConfig.class );

	// key information set during initial parsing of the XML file
	private String osName = new String();
	private ArrayList<HardDisk> hddsArray = new ArrayList<HardDisk>();

	// XPath and DOM parsing related members
	private Document doc = null;

	public OvfConfig( File file ) throws IOException, UnsupportedVirtualizerFormatException
	{
		doc = XmlHelper.parseDocumentFromStream( new FileInputStream( file ) );
		doc = XmlHelper.removeFormattingNodes( doc );
		if ( doc == null )
			throw new UnsupportedVirtualizerFormatException(
					"Could not create DOM from given ovf machine configuration file!" );
		init();
	}

	/**
	 * Main initialization functions parsing the document created during the
	 * constructor.
	 * 
	 * @throws UnsupportedVirtualizerFormatException
	 */
	private void init() throws UnsupportedVirtualizerFormatException
	{
		if ( Util.isEmptyString( getDisplayName() ) ) {
			throw new UnsupportedVirtualizerFormatException( "Machine doesn't have a name" );
		}
	}

	/**
	 * Getter for the display name
	 *
	 * @return the display name of this VM
	 */
	public String getDisplayName()
	{
		try {
			return XmlHelper.XPath.compile( "/Envelope/VirtualSystem/Name" ).evaluate( this.doc );
		} catch ( XPathExpressionException e ) {
			return "";
		}
	}

}
