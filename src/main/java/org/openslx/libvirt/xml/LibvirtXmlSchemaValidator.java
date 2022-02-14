package org.openslx.libvirt.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * Resource resolver input for RelaxNG schemas.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class LibvirtXmlSchemaResourceInput implements LSInput
{
	/**
	 * Stores the public identification of the schema resource.
	 */
	private String publicId;

	/**
	 * Stores the system identification of the schema resource.
	 */
	private String systemId;

	/**
	 * Stream to process and read the schema resource.
	 */
	private BufferedInputStream inputStream;

	/**
	 * Creates a resource resolver input for a RelaxNG schema.
	 * 
	 * @param publicId public identification of the schema resource.
	 * @param sysId system identification of the schema resource.
	 * @param input stream of the schema resource.
	 */
	public LibvirtXmlSchemaResourceInput( String publicId, String sysId, InputStream input )
	{
		this.publicId = publicId;
		this.systemId = sysId;
		this.inputStream = new BufferedInputStream( input );
	}

	@Override
	public String getBaseURI()
	{
		return null;
	}

	@Override
	public InputStream getByteStream()
	{
		return null;
	}

	@Override
	public boolean getCertifiedText()
	{
		return false;
	}

	@Override
	public Reader getCharacterStream()
	{
		return null;
	}

	@Override
	public String getEncoding()
	{
		return null;
	}

	@Override
	public String getPublicId()
	{
		return this.publicId;
	}

	@Override
	public String getStringData()
	{
		String data = null;

		synchronized ( this.inputStream ) {
			try {
				int inputLength = this.inputStream.available();
				byte[] input = new byte[ inputLength ];
				this.inputStream.read( input );
				data = new String( input, StandardCharsets.UTF_8 );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}

		return data;
	}

	@Override
	public String getSystemId()
	{
		return this.systemId;
	}

	@Override
	public void setBaseURI( String arg0 )
	{
	}

	@Override
	public void setByteStream( InputStream arg0 )
	{
	}

	@Override
	public void setCertifiedText( boolean arg0 )
	{
	}

	@Override
	public void setCharacterStream( Reader arg0 )
	{
	}

	@Override
	public void setEncoding( String arg0 )
	{
	}

	@Override
	public void setPublicId( String arg0 )
	{
		this.publicId = arg0;
	}

	@Override
	public void setStringData( String arg0 )
	{
	}

	@Override
	public void setSystemId( String arg0 )
	{
		this.systemId = arg0;
	}
}

/**
 * Resource resolver for RelaxNG schemas.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class LibvirtXmlSchemaResourceResolver implements LSResourceResolver
{
	@Override
	public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
	{
		InputStream rngResourceStream = LibvirtXmlResources.getLibvirtRng( systemId );
		return new LibvirtXmlSchemaResourceInput( publicId, systemId, rngResourceStream );
	}
}

/**
 * Validator for validation of Libvirt XML documents with RelaxNG schemas.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibvirtXmlSchemaValidator
{
	/**
	 * RelaxNG based validator for validation of Libvirt XML documents.
	 */
	private Validator rngSchemaValidator;

	/**
	 * Creates a validator for validation of Libvirt XML documents with RelaxNG schemas.
	 * 
	 * @param rngSchema RelaxNG schema used for validation with {@link #validate(Document)}.
	 * 
	 * @throws SAXException creation of a Libvirt XML validator failed.
	 */
	public LibvirtXmlSchemaValidator( InputStream rngSchema ) throws SAXException
	{
		this.createValidationContext( rngSchema );
	}

	/**
	 * Creates context for validation of Libvirt XML documents with a RelaxNG schema.
	 * 
	 * @param rngSchema RelaxNG schema used for validation with {@link #validate(Document)}.
	 * 
	 * @throws SAXException Loading, creation and processing of <code>rngSchema</code> has failed.
	 */
	private void createValidationContext( InputStream rngSchema ) throws SAXException
	{
		// use hack to load specific schema factory implementation for RelaxNG schemas
		System.setProperty( SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI,
				"com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory" );

		// create schema resource resolver to resolve schema resources during parsing and validation
		LibvirtXmlSchemaResourceResolver schemaResolver = new LibvirtXmlSchemaResourceResolver();

		// create schema factory to be able to create a RelaxNG schema validator
		SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.RELAXNG_NS_URI );
		factory.setResourceResolver( schemaResolver );
		Schema schema = factory.newSchema( new StreamSource( rngSchema ) );

		// create the RelaxNG schema validator
		this.rngSchemaValidator = schema.newValidator();
		this.rngSchemaValidator.setResourceResolver( schemaResolver );
	}

	/**
	 * Transforms a DOM source to a Stream source.
	 * 
	 * @param domSource DOM source of a Libvirt XML document.
	 * @return Stream source of a Libvirt XML document.
	 * 
	 * @throws TransformerException Transformation of DOM source to a Stream source has failed.
	 * 
	 * @implNote This utility method is necessary in {@link #validate(Document)} to be able to
	 *           validate a DOM Libvirt XML document with the schema and validator implementation for
	 *           RelaxNG schema files from
	 *           <code>com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory</code>.
	 */
	private static StreamSource toStreamSource( DOMSource domSource ) throws TransformerException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult( outputStream );

		// create identity transformer
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform( domSource, result );

		ByteArrayInputStream inputStream = new ByteArrayInputStream( outputStream.toByteArray() );
		return new StreamSource( inputStream );
	}

	/**
	 * Validates a given (and parsed) DOM Libvirt XML document.
	 * 
	 * Validation takes place if the specified <code>xmlDocument</code> is non-null, otherwise the
	 * validation succeeds immediately. If the validation of the <code>xmlDocument</code> fails, a
	 * validation exception is thrown.
	 * 
	 * @param xmlDocument Libvirt XML document.
	 * 
	 * @throws LibvirtXmlValidationException Validation of Libvirt XML document failed.
	 */
	public void validate( Document xmlDocument ) throws LibvirtXmlValidationException
	{
		if ( xmlDocument != null ) {
			try {
				DOMSource domSource = new DOMSource( xmlDocument );
				StreamSource source = LibvirtXmlSchemaValidator.toStreamSource( domSource );
				this.rngSchemaValidator.validate( source );
			} catch ( SAXException | TransformerException | IOException e ) {
				throw new LibvirtXmlValidationException( e.getLocalizedMessage() );
			}
		}
	}
}
