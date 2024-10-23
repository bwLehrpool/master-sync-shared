package org.openslx.libvirt.xml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A representation of a XML node as part of a {@link LibvirtXmlDocument}.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibvirtXmlNode implements LibvirtXmlCreatable, LibvirtXmlEditable
{
	/**
	 * Separation character for internal XPath expressions.
	 */
	private static final String XPATH_EXPRESSION_SEPARATOR = "/";

	/**
	 * Current XML node selection character for internal XPath expressions.
	 */
	private static final String XPATH_EXPRESSION_CURRENT_NODE = ".";

	/**
	 * Factory to create XPath objects.
	 */
	private XPathFactory xPathFactory = null;

	/**
	 * Representation of the XML document, in which this {@link LibvirtXmlNode} is part of.
	 */
	private Document xmlDocument = null;

	/**
	 * Current XML base node as XML root anchor for relative internal XPath expressions.
	 */
	private Node xmlBaseNode = null;

	/**
	 * Create and initialize XPath context to define and compile custom XPath expressions.
	 */
	private void createXPathContext()
	{
		this.xPathFactory = XPathFactory.newInstance();
	}

	/**
	 * Creates empty Libvirt XML node, which does not belong to any XML document and does not specify
	 * any XML base node.
	 * 
	 * @implNote Please call {@link LibvirtXmlNode#setXmlDocument(Document)} and
	 *           {@link LibvirtXmlNode#setXmlBaseNode(Node)} manually to obtain a functional Libvirt
	 *           XML node.
	 */
	public LibvirtXmlNode()
	{
		this( null, null );
	}

	/**
	 * Creates Libvirt XML node from a existing Libvirt XML node by reference.
	 * 
	 * @param xmlNode existing Libvirt XML node.
	 */
	public LibvirtXmlNode( LibvirtXmlNode xmlNode )
	{
		this( xmlNode.getXmlDocument(), xmlNode.getXmlBaseNode() );
	}

	/**
	 * Creates Libvirt XML node as part of a existing XML document.
	 * 
	 * @param xmlDocument existing XML document.
	 * 
	 * @implNote Please call {@link LibvirtXmlNode#setXmlBaseNode(Node)} manually to obtain a
	 *           functional Libvirt XML node.
	 */
	public LibvirtXmlNode( Document xmlDocument )
	{
		this( xmlDocument, null );
	}

	/**
	 * Creates Libvirt XML node with a specific XML base node.
	 * 
	 * @param xmlBaseNode existing XML base node.
	 * 
	 * @implNote Please call {@link LibvirtXmlNode#setXmlDocument(Document)} manually to obtain a
	 *           functional Libvirt XML node.
	 */
	public LibvirtXmlNode( Node xmlBaseNode )
	{
		this( null, xmlBaseNode );
	}

	/**
	 * Creates Libvirt XML node with a specific XML base node as part of a XML document.
	 * 
	 * @param xmlDocument existing XML document.
	 * @param xmlBaseNode existing XML base node.
	 */
	public LibvirtXmlNode( Document xmlDocument, Node xmlBaseNode )
	{
		this.createXPathContext();

		this.setXmlDocument( xmlDocument );
		this.setXmlBaseNode( xmlBaseNode );
	}

	/**
	 * Returns referenced XML document.
	 * 
	 * @return referenced XML document.
	 */
	public Document getXmlDocument()
	{
		return this.xmlDocument;
	}

	/**
	 * Sets existing XML document for Libvirt XML node.
	 * 
	 * @param xmlDocument existing XML document.
	 */
	public void setXmlDocument( Document xmlDocument )
	{
		this.xmlDocument = xmlDocument;
	}

	/**
	 * Returns current XML base node.
	 * 
	 * @return current XML base node as XML root anchor of relative internal XPath expressions.
	 */
	public Node getXmlBaseNode()
	{
		return this.xmlBaseNode;
	}

	/**
	 * Sets existing XML base node for Libvirt XML node.
	 * 
	 * @param xmlBaseNode existing XML base node as XML root anchor for relative internal XPath
	 *           expressions.
	 */
	public void setXmlBaseNode( Node xmlBaseNode )
	{
		this.xmlBaseNode = xmlBaseNode;
	}

	@Override
	public Node getXmlNode( String expression )
	{
		if ( XPATH_EXPRESSION_CURRENT_NODE.equals( expression ) ) {
			return this.xmlBaseNode;
		}
		NodeList nodes = this.getXmlNodes( expression );
		return nodes.item( 0 );
	}

	@Override
	public NodeList getXmlNodes( String expression )
	{
		Object nodes = null;

		try {
			XPath xPath = this.xPathFactory.newXPath();
			XPathExpression xPathExpr = xPath.compile( expression );
			nodes = xPathExpr.evaluate( this.xmlBaseNode, XPathConstants.NODESET );
		} catch ( XPathExpressionException e ) {
			e.printStackTrace();
		}

		return NodeList.class.cast( nodes );
	}

	@Override
	public Element getXmlElement( String expression )
	{
		String completeExpression = null;

		if ( expression == null || expression.isEmpty() ) {
			completeExpression = XPATH_EXPRESSION_CURRENT_NODE;
		} else {
			completeExpression = XPATH_EXPRESSION_CURRENT_NODE + XPATH_EXPRESSION_SEPARATOR + expression;
		}

		Node node = this.getXmlNode( completeExpression );

		if ( node != null && node.getNodeType() == Node.ELEMENT_NODE ) {
			return (Element)node;
		} else {
			return null;
		}
	}

	private Node createXmlElement( String expression )
	{
		Node parentNode = this.xmlBaseNode;
		Node currentNode = parentNode;

		if ( expression != null && !expression.isEmpty() ) {
			String[] nodeNames = expression.split( XPATH_EXPRESSION_SEPARATOR );
			String partialExpression = XPATH_EXPRESSION_CURRENT_NODE;

			for ( int i = 0; i < nodeNames.length; i++ ) {
				partialExpression += XPATH_EXPRESSION_SEPARATOR + nodeNames[i];
				currentNode = this.getXmlNode( partialExpression );

				if ( currentNode == null ) {
					currentNode = this.xmlDocument.createElement( nodeNames[i] );
					parentNode.appendChild( currentNode );
				}

				parentNode = currentNode;
			}
		}

		return currentNode;
	}

	@Override
	public void setXmlElement( String expression, Node child )
	{
		Node node = this.createXmlElement( expression );

		if ( child != null ) {
			node.appendChild( child );
		}
	}

	@Override
	public String getXmlElementValue( String expression )
	{
		Node node = this.getXmlElement( expression );

		if ( node != null ) {
			return node.getTextContent();
		} else {
			return null;
		}
	}

	@Override
	public void setXmlElementValue( String expression, String value )
	{
		Node node = this.createXmlElement( expression );
		node.setTextContent( value );
	}

	@Override
	public void removeXmlElement( String expression )
	{
		Node node = this.getXmlElement( expression );

		if ( node != null ) {
			node.getParentNode().removeChild( node );
		}
	}

	@Override
	public void removeXmlElementChilds( String expression )
	{
		Node node = this.getXmlElement( expression );

		if ( node != null ) {
			final NodeList childs = node.getChildNodes();
			while ( childs.getLength() > 0 ) {
				Node child = childs.item( 0 );
				node.removeChild( child );
			}
		}
	}

	@Override
	public String getXmlElementAttributeValue( String expression, String attributeName )
	{
		Node node = this.getXmlElement( expression );

		if ( node == null ) {
			return null;
		} else {
			Node attribute = node.getAttributes().getNamedItem( attributeName );

			if ( attribute == null ) {
				return null;
			} else {
				return attribute.getNodeValue();
			}
		}
	}

	@Override
	public void setXmlElementAttributeValue( String expression, String attributeName, String value )
	{
		Node node = this.createXmlElement( expression );
		Node attribute = node.getAttributes().getNamedItem( attributeName );

		if ( attribute == null ) {
			Element element = Element.class.cast( node );
			element.setAttribute( attributeName, value );
		} else {
			attribute.setNodeValue( value );
		}
	}

	@Override
	public void removeXmlElementAttribute( String expression, String attributeName )
	{
		Node node = this.getXmlElement( expression );

		if ( node != null ) {
			Node attribute = node.getAttributes().getNamedItem( attributeName );
			if ( attribute != null ) {
				node.getAttributes().removeNamedItem( attribute.getNodeName() );
			}
		}
	}

	@Override
	public void removeXmlElementAttributes( String expression )
	{
		Node node = this.getXmlElement( expression );

		if ( node != null ) {
			for ( int i = 0; i < node.getAttributes().getLength(); i++ ) {
				Node attribute = node.getAttributes().item( 0 );
				node.getAttributes().removeNamedItem( attribute.getNodeName() );
			}
		}
	}

	@Override
	public void fromXmlNode( Node xmlNode )
	{
		this.setXmlBaseNode( xmlNode );
	}

	@Override
	public Node toXmlNode()
	{
		return this.getXmlBaseNode();
	}
}
