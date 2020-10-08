package org.openslx.util.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.openslx.util.Util;
import org.openslx.util.XmlHelper;
import org.openslx.util.vm.VmMetaData.HardDisk;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Class handling the parsing of a .ovf machine description file
 * For now, only used to parse the xml to get the path of the vmdk
 * from the ovf for sanity checking and cleanup after conversion.
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
		LOGGER.info("Entering OvfConfig class creation");
		// first validate xml
		// try {
		// 	SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
		// 	InputStream xsdStream = OvfConfig.class.getResourceAsStream( "/master-sync-shared/xml/ovf_1.1.1.xsd" );
		// 	LOGGER.info("xsdStream creation worked");
		// 	if ( xsdStream == null ) {
		// 		LOGGER.warn( "Cannot validate ovf XML: No XSD found in JAR" );
		// 	} else {
		// 		LOGGER.info("Before schema creation");
		// 		Schema schema = factory.newSchema( new StreamSource( xsdStream ) );
		// 		LOGGER.info("Created schema class");
		// 		Validator validator = schema.newValidator();
		// 		LOGGER.info("Created validator class");
		// 		validator.validate( new StreamSource( file ) );
		// 		LOGGER.info("Validated");
		// 	}
		// } catch ( SAXException e ) {
		// 	LOGGER.error( "Selected ovf file was not validated against the XSD schema: " + e.getMessage() );
		// }
		LOGGER.info("XSD validation worked.");
		// valid xml, try to create the DOM
		doc = XmlHelper.parseDocumentFromStream( new FileInputStream( file ) );
		doc = XmlHelper.removeFormattingNodes( doc );
		if ( doc == null )
			throw new UnsupportedVirtualizerFormatException( "Could not create DOM from given ovf machine configuration file!" );
		LOGGER.info("DOM creation worked");
		init();
    }
    
    /**
	 * Main initialization functions parsing the document created during the constructor.
	 * @throws UnsupportedVirtualizerFormatException 
	 */
	private void init() throws UnsupportedVirtualizerFormatException
	{
		LOGGER.info("Entering OvfConfig init");
		if ( Util.isEmptyString( getDisplayName() ) ) {
			throw new UnsupportedVirtualizerFormatException( "Machine doesn't have a name" );
		}
		LOGGER.info( getDisplayName() );
		// try {

		// 	setHdds();
		// } catch ( XPathExpressionException e ) {
		// 	LOGGER.debug( "Could not initialize VBoxConfig", e );
		// 	return;
		// }
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