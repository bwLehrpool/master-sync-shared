package org.openslx.libvirt.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Editability of XML nodes based on XPath expressions.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public interface LibvirtXmlEditable
{
	/**
	 * Returns XML node selected by a XPath expression
	 * 
	 * @param expression XPath expression to select XML node.
	 * @return selected XML node.
	 */
	public Node getXmlNode( String expression );

	/**
	 * Returns XML nodes selected by a XPath expression
	 * 
	 * @param expression XPath expression to select XML nodes.
	 * @return selected XML nodes.
	 */
	public NodeList getXmlNodes( String expression );

	/**
	 * Return current XML root element.
	 * 
	 * @return current XML root element.
	 */
	public default Node getXmlElement()
	{
		return this.getXmlElement( null );
	}

	/**
	 * Returns XML element from selection by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @return selected XML element.
	 */
	public Node getXmlElement( String expression );

	/**
	 * Sets an XML element selected by a XPath expression.
	 * 
	 * If the XML element selected by the given XPath expression does not exists, the XML
	 * element will be created.
	 * 
	 * @param expression XPath expression to select XML element.
	 */
	public default void setXmlElement( String expression )
	{
		this.setXmlElement( expression, null );
	}

	/**
	 * Sets a XML element selected by a XPath expression and appends child XML node.
	 * 
	 * If the XML element selected by the given XPath expression does not exists, the XML
	 * element will be created and the given XML child node is appended.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param child XML node that will be appended to the selected XML element.
	 */
	public void setXmlElement( String expression, Node child );

	/**
	 * Returns the text value of a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @return Text value of the selected XML element.
	 */
	public String getXmlElementValue( String expression );

	/**
	 * Sets the text value of a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param value text value to set selected XML element's text.
	 */
	public void setXmlElementValue( String expression, String value );

	/**
	 * Removes a XML element and all its childs selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 */
	public void removeXmlElement( String expression );

	/**
	 * Removes all child elements of a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 */
	public void removeXmlElementChilds( String expression );

	/**
	 * Returns the text value of a XML attribute from the current XML root element.
	 * 
	 * @param attributeName name to select XML attribute of the current XML root element.
	 * @return attribute text of the XML attribute from the current XML root element as
	 *         {@link String}.
	 */
	public default String getXmlElementAttributeValue( String attributeName )
	{
		return this.getXmlElementAttributeValue( null, attributeName );
	}

	/**
	 * Returns the binary choice of a XML attribute from the current XML root element.
	 * 
	 * If the text value of the XML attribute equals to <i>yes</i>, the returned {@link boolean}
	 * value is set to <i>true</i>. Otherwise, if the text value of the XML attribute equals to
	 * <i>no</i>, the returned {@link boolean} value is set to <i>false</i>.
	 * 
	 * @param attributeName name to select XML attribute of the current XML root element.
	 * @return attribute value of the XML attribute from the current XML root element as
	 *         {@link boolean}.
	 */
	public default boolean getXmlElementAttributeValueAsBool( String attributeName )
	{
		return "yes".equals( this.getXmlElementAttributeValue( attributeName ) );
	}

	/**
	 * Returns the binary choice of a XML attribute from a XML element selected by a XPath
	 * expression.
	 * 
	 * If the text value of the XML attribute equals to <i>yes</i>, the returned {@link boolean}
	 * value is set to <i>true</i>. Otherwise, if the text value of the XML attribute equals to
	 * <i>no</i>, the returned {@link boolean} value is set to <i>false</i>.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param attributeName name to select XML attribute of the current XML root element.
	 * @return attribute value of the XML attribute from the current XML root element as
	 *         {@link boolean}.
	 */
	public default boolean getXmlElementAttributeValueAsBool( String expression, String attributeName )
	{
		return "yes".equals( this.getXmlElementAttributeValue( expression, attributeName ) );
	}

	/**
	 * Returns the text value of a XML attribute from a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param attributeName name to select XML attribute of the selected XML element.
	 * @return attribute text of the XML attribute from the selected XML element.
	 */
	public String getXmlElementAttributeValue( String expression, String attributeName );

	/**
	 * Sets the text value of a XML attribute from the current XML root element.
	 * 
	 * @param attributeName name to select XML attribute of the current XML root element.
	 * @param value XML attribute value for the selected XML attribute from the current XML root
	 *           element.
	 */
	public default void setXmlElementAttributeValue( String attributeName, String value )
	{
		this.setXmlElementAttributeValue( null, attributeName, value );
	}

	/**
	 * Sets the binary choice value of a XML attribute from the current XML root element.
	 * 
	 * If the binary choice value for the XML attribute equals to <i>true</i>, the text value of the
	 * selected XML attribute is set to <i>yes</i>. Otherwise, if the binary choice value for the
	 * selected XML attribute equals to <i>false</i>, the text value of the selected XML attribute is
	 * set to <i>no</i>.
	 * 
	 * @param attributeName name to select XML attribute of the selected XML element.
	 * @param value binary choice value for the selected XML attribute from the selected XML element.
	 */
	public default void setXmlElementAttributeValue( String attributeName, boolean value )
	{
		final String valueYesNo = value ? "yes" : "no";
		this.setXmlElementAttributeValue( attributeName, valueYesNo );
	}

	/**
	 * Sets the binary choice value of a XML attribute from a XML element selected by a XPath
	 * expression.
	 * 
	 * If the binary choice value for the XML attribute equals to <i>true</i>, the text value of the
	 * selected XML attribute is set to <i>yes</i>. Otherwise, if the binary choice value for the
	 * selected XML attribute equals to <i>false</i>, the text value of the selected XML attribute is
	 * set to <i>no</i>.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param attributeName name to select XML attribute of the selected XML element.
	 * @param value binary choice value for the selected XML attribute from the selected XML element.
	 */
	public default void setXmlElementAttributeValue( String expression, String attributeName, boolean value )
	{
		final String valueYesNo = value ? "yes" : "no";
		this.setXmlElementAttributeValue( expression, attributeName, valueYesNo );
	}

	/**
	 * Sets the text value of a XML attribute from a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param attributeName name to select XML attribute of the selected XML element.
	 * @param value XML attribute value for the selected XML attribute from the selected XML element.
	 */
	public void setXmlElementAttributeValue( String expression, String attributeName, String value );

	/**
	 * Removes an XML attribute from the current XML root element.
	 * 
	 * @param attributeName name of the attribute which should be deleted.
	 */
	public default void removeXmlElementAttribute( String attributeName )
	{
		this.removeXmlElementAttribute( null, attributeName );
	}

	/**
	 * Removes an XML attribute from a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 * @param attributeName name of the attribute which should be deleted.
	 */
	public void removeXmlElementAttribute( String expression, String attributeName );

	/**
	 * Removes all XML attributes from a XML element selected by a XPath expression.
	 * 
	 * @param expression XPath expression to select XML element.
	 */
	public void removeXmlElementAttributes( String expression );

}
