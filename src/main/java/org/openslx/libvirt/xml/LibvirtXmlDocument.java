package org.openslx.libvirt.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A generic representation of a Libvirt XML file.
 * 
 * @implNote Base class to derive the representation of specific Libvirt XML files.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public abstract class LibvirtXmlDocument implements LibvirtXmlSerializable, LibvirtXmlValidatable
{
	/**
	 * Document builder to parse Libvirt XML document from file.
	 */
	private DocumentBuilder domBuilder = null;

	/**
	 * Representation of a Libvirt XML document.
	 */
	private Document xmlDocument = null;

	/**
	 * XML transformer to transform Libvirt XML document to a file.
	 */
	private Transformer xmlTransformer = null;

	/**
	 * XML root node of the Libvirt XML document.
	 */
	private LibvirtXmlNode rootXmlNode = null;

	/**
	 * RNG schema validator to validate the Libvirt XML document content.
	 */
	private LibvirtXmlSchemaValidator rngValidator = null;

	/**
	 * Creates and initializes XML context to create and transform a Libvirt XML file from/to a file.
	 * 
	 * @param rngSchema RNG schema to validate the Libvirt XML document content.
	 * 
	 * @throws LibvirtXmlDocumentException error occured during setup of the XML context to read and
	 *            write from/to a Libvirt XML file.
	 */
	private void createXmlContext( InputStream rngSchema ) throws LibvirtXmlDocumentException
	{
		// used for XML input
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setIgnoringElementContentWhitespace( true );
			domFactory.setNamespaceAware( true );
			this.domBuilder = domFactory.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			String errorMsg = "Setting up XML context for reading from the Libvirt XML document failed.";
			throw new LibvirtXmlDocumentException( errorMsg );
		}

		// used for XML output
		try {
			// use hack to load specific transformer factory implementation for XSLT
			System.setProperty( TransformerFactory.class.getName(),
					"org.apache.xalan.processor.TransformerFactoryImpl" );

			// create XML transformer factory to create XML transformer with specific indentation
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			// create XML transformer and apply settings for output XML transformation
			InputStream xslOutputSchemaStream = LibvirtXmlResources.getLibvirtXsl( "xml-output-transformation.xsl" );
			StreamSource xslOutputSchema = new StreamSource( xslOutputSchemaStream );
			this.xmlTransformer = transformerFactory.newTransformer( xslOutputSchema );
			this.xmlTransformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
		} catch ( TransformerConfigurationException e ) {
			String errorMsg = "Setting up XML context for writing to the Libvirt XML document failed.";
			throw new LibvirtXmlDocumentException( errorMsg );
		}

		// used for XML validation with RNG schema files
		if ( rngSchema != null ) {
			try {
				this.rngValidator = new LibvirtXmlSchemaValidator( rngSchema );
			} catch ( SAXException e ) {
				String errorMsg = "Setting up XML context for validating to the Libvirt XML document failed.";
				e.printStackTrace();
				throw new LibvirtXmlDocumentException( errorMsg );
			}
		}
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link String}.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( String xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this( xml, null );
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link String}.
	 * @param rngSchema RNG schema to validate XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( String xml, InputStream rngSchema )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this.createXmlContext( rngSchema );
		this.fromXml( xml );
		this.validateXml();
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link File}.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( File xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this( xml, null );
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link File}.
	 * @param rngSchema RNG schema to validate XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( File xml, InputStream rngSchema )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this.createXmlContext( rngSchema );
		this.fromXml( xml );
		this.validateXml();
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link InputStream}.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( InputStream xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this( xml, null );
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link InputStream}.
	 * @param rngSchema RNG schema to validate XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( InputStream xml, InputStream rngSchema )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this.createXmlContext( rngSchema );
		this.fromXml( xml );
		this.validateXml();
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link InputSource}.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( InputSource xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this( xml, null );
	}

	/**
	 * Creates a Libvirt XML document from a given XML content.
	 * 
	 * @param xml XML content as {@link InputSource}.
	 * @param rngSchema RNG schema to validate XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public LibvirtXmlDocument( InputSource xml, InputStream rngSchema )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		this.createXmlContext( rngSchema );
		this.fromXml( xml );
		this.validateXml();
	}

	/**
	 * Returns the XML root node of the Libvirt XML document.
	 * 
	 * @return root node of the Libvirt XML document.
	 */
	public LibvirtXmlNode getRootXmlNode()
	{
		return this.rootXmlNode;
	}

	@Override
	public void fromXml( String xml ) throws LibvirtXmlSerializationException
	{
		this.fromXml( new InputSource( new StringReader( xml ) ) );
	}

	@Override
	public void fromXml( File xml ) throws LibvirtXmlSerializationException
	{
		try {
			this.xmlDocument = this.domBuilder.parse( xml );
			this.xmlDocument.getDocumentElement().normalize();
		} catch ( SAXException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		} catch ( IOException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		}

		this.rootXmlNode = new LibvirtXmlNode( this.xmlDocument, this.xmlDocument.getDocumentElement() );
	}

	@Override
	public void fromXml( InputStream xml ) throws LibvirtXmlSerializationException
	{
		try {
			this.xmlDocument = this.domBuilder.parse( xml );
			this.xmlDocument.getDocumentElement().normalize();
		} catch ( SAXException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		} catch ( IOException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		}

		this.rootXmlNode = new LibvirtXmlNode( this.xmlDocument, this.xmlDocument.getDocumentElement() );
	}

	@Override
	public void fromXml( InputSource xml ) throws LibvirtXmlSerializationException
	{
		try {
			this.xmlDocument = this.domBuilder.parse( xml );
			this.xmlDocument.getDocumentElement().normalize();
		} catch ( SAXException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		} catch ( IOException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		}

		this.rootXmlNode = new LibvirtXmlNode( this.xmlDocument, this.xmlDocument.getDocumentElement() );
	}

	@Override
	public String toXml() throws LibvirtXmlSerializationException
	{
		StringWriter xmlWriter = null;
		String xml = null;

		try {
			xmlWriter = new StringWriter();
			DOMSource source = new DOMSource( this.xmlDocument );
			StreamResult xmlString = new StreamResult( xmlWriter );
			this.xmlTransformer.transform( source, xmlString );
			xml = xmlWriter.toString();
			xmlWriter.close();
		} catch ( TransformerException | IOException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		} finally {
			try {
				xmlWriter.close();
			} catch ( IOException e ) {
				throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
			}
		}

		return xml;
	}

	@Override
	public void toXml( File xml ) throws LibvirtXmlSerializationException
	{
		FileWriter xmlWriter = null;

		try {
			xmlWriter = new FileWriter( xml );
			DOMSource source = new DOMSource( this.xmlDocument );
			StreamResult xmlStream = new StreamResult( xmlWriter );
			this.xmlTransformer.transform( source, xmlStream );
			xmlWriter.close();
		} catch ( TransformerException | IOException e ) {
			throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
		} finally {
			try {
				xmlWriter.close();
			} catch ( IOException e ) {
				throw new LibvirtXmlSerializationException( e.getLocalizedMessage() );
			}
		}
	}

	@Override
	public void validateXml() throws LibvirtXmlValidationException
	{
		if ( this.rngValidator != null ) {
			this.rngValidator.validate( this.xmlDocument );
		}
	}

	@Override
	public String toString()
	{
		try {
			return this.toXml();
		} catch ( LibvirtXmlSerializationException e ) {
			return null;
		}
	}
}
