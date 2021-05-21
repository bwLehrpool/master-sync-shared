package org.openslx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlHelper
{
	private final static Logger LOGGER = Logger.getLogger( XmlHelper.class );

	// TODO check thread-safety
	private static final XPath XPath = XPathFactory.newInstance().newXPath();
	private static DocumentBuilder dBuilder;
	static {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware( true );
		dbFactory.setIgnoringComments( true );
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch ( ParserConfigurationException e ) {
			LOGGER.error( "Failed to initalize DOM parser with default configurations." );
		}
	}

	public static String globalXPathToLocalXPath( String xPath )
	{
		final StringBuilder exprBuilder = new StringBuilder();
		final String[] elements = xPath.split( "/" );

		for ( final String element : elements ) {
			if ( !element.isEmpty() ) {
				final Pattern arraySpecifierRegex = Pattern.compile( "^(.*)\\[(.*)\\]$" );
				final Matcher arraySpecifierMatcher = arraySpecifierRegex.matcher( element );
				final String elementName;
				final String elementSpecifier;

				if ( arraySpecifierMatcher.find() ) {
					elementName = arraySpecifierMatcher.group( 1 );
					elementSpecifier = arraySpecifierMatcher.group( 2 );
				} else {
					elementName = element;
					elementSpecifier = null;
				}

				if ( !elementName.startsWith( "@" ) && !elementName.equals( "*" ) ) {
					exprBuilder.append( "/*[local-name()='" + elementName + "']" );

				} else {
					exprBuilder.append( "/" + elementName );
				}

				if ( elementSpecifier != null && !elementSpecifier.isEmpty() ) {
					exprBuilder.append( "[" + elementSpecifier + "]" );
				}
			}
		}

		return exprBuilder.toString();
	}

	public static XPathExpression compileXPath( String xPath ) throws XPathExpressionException
	{
		final String localXPath = XmlHelper.globalXPathToLocalXPath( xPath );
		return XPath.compile( localXPath );
	}

	public static Document parseDocumentFromStream( InputStream is )
	{
		Document doc = null;

		// read document from stream
		try {
			doc = dBuilder.parse( is );
		} catch ( SAXException | IOException e ) {
			doc = null;
		}

		// normalize parsed document
		if ( doc != null ) {
			doc.getDocumentElement().normalize();
		}

		return doc;
	}

	public static Document removeFormattingNodes( Document doc )
	{
		NodeList empty;
		try {
			empty = (NodeList)XPath.evaluate( "//text()[normalize-space(.) = '']",
					doc, XPathConstants.NODESET );
		} catch ( XPathExpressionException e ) {
			LOGGER.error( "Bad XPath expression to find all empty text nodes." );
			return null;
		}

		for ( int i = 0; i < empty.getLength(); i++ ) {
			Node node = empty.item( i );
			node.getParentNode().removeChild( node );
		}
		return doc;
	}

	public static String getUnformattedXml( InputStream is )
	{
		// prune empty text nodes, essentially removing all formatting
		Document doc = parseDocumentFromStream( is );
		return getXmlFromDocument( removeFormattingNodes( doc ), false );
	}

	public static String getFormattedXml( InputStream is )
	{
		Document doc = parseDocumentFromStream( is );
		return getXmlFromDocument( doc, true );
	}

	public static String getXmlFromDocument( Document doc, boolean humanReadable )
	{
		try {
			StringWriter writer = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			if ( humanReadable ) {
				transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
				transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
			}
			transformer.transform( new DOMSource( doc ), new StreamResult( writer ) );
			return writer.toString();
		} catch ( Exception ex ) {
			LOGGER.error( "Failed to transform XML to String: ", ex );
			return null;
		}
	}
}
