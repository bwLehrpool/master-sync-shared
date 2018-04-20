package org.openslx.util.vm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

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
import org.openslx.util.vm.VmMetaData.DriveBusType;
import org.openslx.util.vm.VmMetaData.HardDisk;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class representing a .vbox machine description file
 */
public class VboxConfig
{
	private static final Logger LOGGER = Logger.getLogger( VboxConfig.class );
	XPath xPath = XPathFactory.newInstance().newXPath();

	private Document doc = null;
	private String displayNameExpression = "/VirtualBox/Machine/@name";
	private String displayName = new String();

	private String osTypeExpression = "/VirtualBox/Machine/@OSType";
	private String osName = new String();

	private String hddsExpression = "/VirtualBox/Machine/MediaRegistry/HardDisks/*";
	private ArrayList<HardDisk> hddsArray = new ArrayList<HardDisk>();

	// list of nodes to automatically remove when reading the vbox file
	private static String[] blackList = { "SharedFolders", "HID", "USB", "ExtraData", "Adapter", "GuestProperties", "LPT", "StorageController", "FloppyImages", "DVDImages",
			"AttachedDevice", "RemoteDisplay", "VideoCapture" };

	public static enum PlaceHolder
	{
		FLOPPYUUID( "%OpenSLX_FloppyUUID%" ), FLOPPYLOCATION( "%OpenSLX_Floppy_Location%" ), CPU( "%OpenSLX_CPU%" ), MEMORY( "%OpenSLX_MEMORY%" ), MACHINEUUID(
				"%OpenSLX_MUUID%" ), NETWORKMAC( "%OpenSLX_Networkcard_MACAddress%" ), HDDLOCATION( "%OpenSLX_HDD_Location%" ), HDDUUID( "%OpenSLX_HDDUUID_" );
		private final String holderName;

		private PlaceHolder( String name )
		{
			this.holderName = name;
		}

		public String holderName()
		{
			return holderName;
		}
	}

	/**
	 * constructor with input xml file
	 * used to set the doc variable of this class when creating vm
	 * 
	 * @param file as File - the input xml File
	 * @throws IOException
	 * @throws UnsupportedVirtualizerFormatException
	 */
	public VboxConfig( File file ) throws IOException, UnsupportedVirtualizerFormatException
	{
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			doc = dBuilder.parse( file );

			// TODO - does this test suffice??
			if ( !doc.getDocumentElement().getNodeName().equals( "VirtualBox" ) ) {
				throw new UnsupportedVirtualizerFormatException( "No <VirtualBox> root node." );
			}

		} catch ( ParserConfigurationException | SAXException | IOException e ) {
			LOGGER.warn( "Could not parse .Vbox", e );
			throw new UnsupportedVirtualizerFormatException( "Could not create VBoxConfig!" );
		}
	}

	/**
	 * constructor with input string from server
	 * used to set the doc variable of this class when rebuilding the doc
	 * 
	 * @param filtered as String - sent from server
	 * @param length
	 * @throws IOException
	 */
	public VboxConfig( byte[] filtered, int length ) throws IOException
	{
		try {
			String filteredString = new String( filtered );
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			// TODO following two settings should handle the formatting of the xml
			// but did not work correctly according to Victor... to test.
			//dbFactory.setValidating( true );
			//dbFactory.setIgnoringElementContentWhitespace( true );
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource( new StringReader( filteredString ) );

			doc = dBuilder.parse( is );

		} catch ( ParserConfigurationException | SAXException e ) {

			LOGGER.warn( "Could not recreate the dom", e );
		}
	}

	/**
	 * getter for the xmldoc
	 * 
	 * @return definition document
	 */
	public Document getConfigDoc()
	{
		return doc;
	}

	/**
	 * initialization function
	 * reads the doc, sets Machine name, os type, sets the hdds, adds placeholders, removes unwanted/
	 * unneeded nodes
	 */
	public void init()
	{
		if ( doc.getChildNodes().item( 0 ).getNodeType() == 8 ) {
			doc.removeChild( doc.getChildNodes().item( 0 ) );
		}
		try {
			setMachineName();
			ensureHardwareUuid();
			setOsType();
			if ( checkForPlaceholders() ) {
				return;
			}
			setHdds();
			removeBlackListedTags();
			addPlaceHolders();
		} catch ( XPathExpressionException e ) {
			LOGGER.debug( "Could not initialize VBoxConfig", e );
			return;
		}
	}

	private void ensureHardwareUuid() throws XPathExpressionException
	{
		NodeList hwNodes = findNodes( "Hardware" );
		int count = hwNodes.getLength();
		// we will need the machine uuid, so get it
		String machineUuid = xPath.compile( "/VirtualBox/Machine/@uuid" ).evaluate( this.doc );
		if ( machineUuid.isEmpty() ) {
			LOGGER.error( "Machine UUID empty, should never happen!" );
			return;
		}
		// now check if we had a <Hardware/> node, which we always should
		if ( count == 1 ) {
			Element hw = (Element)hwNodes.item( 0 );
			String hwUuid = hw.getAttribute( "uuid" );
			if ( !hwUuid.isEmpty() ) {
				LOGGER.info( "Found hardware uuid: " + hwUuid );
				return;
			} else {
				if ( !addAttributeToNode( hw, "uuid", machineUuid ) ) {
					LOGGER.error( "Failed to set machine UUID '" + machineUuid + "' as hardware UUID." );
					return;
				}
				LOGGER.info( "Saved machine UUID as hardware UUID." );
			}
		} else {
			// zero or more than 1 <Hardware/> were found, fatal either way
			// HACK: hijack XPathExpressionException ...
			throw new XPathExpressionException( "Zero or more than one <Hardware> node found, should never happen!" );
		}
	}

	/**
	 * Function checks if the placeholders are present
	 * 
	 * @return true if the placeholders are present, false otherwise
	 */
	private boolean checkForPlaceholders()
	{
		NodeList hdds = findNodes( "HardDisk" );
		for ( int i = 0; i < hdds.getLength(); i++ ) {
			Element hdd = (Element)hdds.item( i );
			if ( hdd == null )
				continue;
			if ( hdd.getAttribute( "location" ).equals( PlaceHolder.HDDLOCATION.holderName() ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Function finds and saves the name of the machine
	 * 
	 * @throws XPathExpressionException
	 */
	public void setMachineName() throws XPathExpressionException
	{
		String name = xPath.compile( displayNameExpression ).evaluate( this.doc );
		if ( name != null && !name.isEmpty() ) {
			displayName = name;
		}
	}

	/**
	 * Function finds and saves the name of the os
	 * 
	 * @throws XPathExpressionException
	 */
	public void setOsType() throws XPathExpressionException
	{
		String os = xPath.compile( osTypeExpression ).evaluate( this.doc );
		if ( os != null && !os.isEmpty() ) {
			osName = os;
		}
	}

	public void setHdds() throws XPathExpressionException
	{
		XPathExpression hddsExpr = xPath.compile( hddsExpression );
		Object result = hddsExpr.evaluate( this.doc, XPathConstants.NODESET );
		// take all the hdd nodes
		NodeList nodes = (NodeList)result;
		// foreach hdd in the hddnodes do:
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			// have the node
			// take the uuid
			// look under <AttachedDevice if found and do stuff with it
			Element hddElement = (Element)nodes.item( i );
			if ( hddElement == null )
				continue;
			// read the filePath
			String fileName = hddElement.getAttribute( "location" );
			// take the uuid
			String uuid = hddElement.getAttribute( "uuid" );
			// search in the xml object and give back the parent of the parent of the node that is called Image and has the given uuid
			String type = hddElement.getAttribute( "type" );
			if ( !type.equals( "Normal" ) && !type.equals( "Writethrough" ) ) {
				LOGGER.warn( "Type of the disk file is neither 'Normal' nor 'Writethrough' but: " + type );
				LOGGER.warn( "This makes the image not directly modificable, which might lead to problems when editing it locally." );
			}
			String pathToParent = givePathToStorageController( uuid );
			XPathExpression attachedDevicesExpr = xPath.compile( pathToParent );
			Object devicesResult = attachedDevicesExpr.evaluate( this.doc, XPathConstants.NODESET );
			NodeList devicesNodes = (NodeList)devicesResult;
			// TODO -- ehm...should only have 1 element...what do when there are more?
			if ( devicesNodes.getLength() > 1 ) {
				LOGGER.error( "There can not be more HDDs with the same UUID!" );
				return;
			}
			if ( devicesNodes.getLength() == 0 ) {
				LOGGER.error( "Image with UUID '" + uuid + "' does not seem connected to any storage controller. Is it a snapshot?" );
				return;
			}
			Element deviceElement = (Element)devicesNodes.item( 0 );
			String controllerDevice = deviceElement.getAttribute( "type" );
			String bus = deviceElement.getAttribute( "name" );
			DriveBusType busType = null;
			if ( bus.equals( "IDE" ) ) {
				busType = DriveBusType.IDE;
			} else if ( bus.equals( "SCSI" ) ) {
				busType = DriveBusType.SCSI;
			} else if ( bus.equals( "SATA" ) ) {
				busType = DriveBusType.SATA;
			}
			// add them together
			hddsArray.add( new HardDisk( controllerDevice, busType, fileName ) );
		}
	}

	public void addPlaceHolders()
	{
		// placeholder for the MACAddress
		changeAttribute( "Adapter", "MACAddress", PlaceHolder.NETWORKMAC.holderName() );

		// placeholder for the machine uuid
		changeAttribute( "Machine", "uuid", PlaceHolder.MACHINEUUID.holderName() );

		// placeholder for the location of the virtual hdd
		changeAttribute( "HardDisk", "location", PlaceHolder.HDDLOCATION.holderName() );

		// placeholder for the memory
		changeAttribute( "Memory", "RAMSize", PlaceHolder.MEMORY.holderName() );

		// placeholder for the CPU
		changeAttribute( "CPU", "count", PlaceHolder.CPU.holderName() );

		// add placeholder for the uuid of the virtual harddrive.
		// must be added on 2 positions...in the HardDisk tag and the attachedDevice tag
		// first find the uuid
		NodeList hdds = findNodes( "HardDisk" );
		for ( int i = 0; i < hdds.getLength(); i++ ) {
			Element hdd = (Element)findNodes( "HardDisk" ).item( i );
			if ( hdd == null )
				continue;
			String uuid = hdd.getAttribute( "uuid" );
			hdd.setAttribute( "uuid", PlaceHolder.HDDUUID.holderName() + i + "%" );
			NodeList images = findNodes( "Image" );
			Element image;
			for ( int j = 0; j < images.getLength(); j++ ) {
				if ( ( (Element)images.item( j ) ).getAttribute( "uuid" ).equals( uuid ) ) {
					image = (Element)images.item( j );
					image.setAttribute( "uuid", PlaceHolder.HDDUUID.holderName() + i + "%" );
					break;
				}
			}
		}
	}

	/**
	 * Function used to find nodes in the document
	 * Function returnes a NodeList of Nodes...not just a Node...even when the wanted Node is a
	 * single
	 * Node, you get a NodeList with just one element
	 * 
	 * @param targetTag as String
	 * @return nodes as NodeList
	 */
	public NodeList findNodes( String targetTag )
	{
		String path = ".//" + targetTag;
		XPathExpression expr;
		NodeList nodes = null;
		try {
			expr = xPath.compile( path );
			Object nodesObject = expr.evaluate( this.doc, XPathConstants.NODESET );
			nodes = (NodeList)nodesObject;
		} catch ( XPathExpressionException e ) {
			LOGGER.error( "Could not build path", e );
		}
		return nodes;
	}

	/**
	 * Function uses the findNodes function to narrow down the wanted node using 1 attribute and
	 * its value
	 * 
	 * @param targetTag
	 * @param targetAttr0
	 * @param value0
	 * @return
	 */
	public Node findNode( String targetTag, String targetAttr0, String value0 )
	{
		Node returnNode = null;

		NodeList foundNodes = findNodes( targetTag );

		for ( int i = 0; i < foundNodes.getLength(); i++ ) {
			Element node = (Element)foundNodes.item( i );
			if ( node != null && node.hasAttribute( targetAttr0 ) && node.getAttribute( targetAttr0 ).equals( value0 ) ) {
				returnNode = foundNodes.item( i );
			}
		}
		return returnNode;
	}

	/**
	 * Function used to change the value of an attribute
	 * Use this function if you know the targetNode is unique
	 * 
	 * @param targetTag
	 * @param attribute
	 * @param newValue
	 */
	public void changeAttribute( String targetTag, String attribute, String newValue )
	{
		changeAttribute( targetTag, attribute, newValue, null, null );
	}

	/**
	 * Function used to change the value of an attribute
	 * Use this function if you are not sure if the targetNode is unique
	 * Use refAttr and refVal to address the right node
	 * 
	 * @param targetTag
	 * @param targetAttr
	 * @param newValue
	 * @param refAttr
	 * @param refVal
	 */
	public void changeAttribute( String targetTag, String targetAttr, String newValue, String refAttr, String refVal )
	{
		NodeList nodes = findNodes( targetTag );

		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Element element = (Element)nodes.item( i );
			if ( element == null )
				return;
			if ( refAttr != null && refVal != null ) {
				if ( element.getAttribute( refAttr ).equals( refVal ) ) {
					element.setAttribute( targetAttr, newValue );
					break;
				}
			} else {
				if ( nodes.getLength() > 1 ) {
					LOGGER.error( "Action would change values of more than one node; stopped!" );
					return;
				}
				element.setAttribute( targetAttr, newValue );
			}
		}
	}

	public boolean addAttributeToNode( Node targetNode, String attrName, String value )
	{
		if ( targetNode == null ) {
			LOGGER.warn( "Node is null; stopped!" );
			return false;
		}
		try {
			( (Element)targetNode ).setAttribute( attrName, value );
		} catch ( DOMException e ) {
			LOGGER.error( "Failed set '" + attrName + "' to '" + value + "' of xml node '" + targetNode.getNodeName() + "': ", e );
			return false;
		}
		return true;
	}

	public Node addNewNode( String nameOfParent, String nameOfnewNode, boolean oneLiner )
	{
		return addNewNode( nameOfParent, nameOfnewNode, oneLiner, null, null );
	}

	public Node addNewNode( String nameOfParent, String nameOfnewNode, boolean oneLiner, String refAttr, String refVal )
	{
		Node parent = null;
		NodeList posibleParents = findNodes( nameOfParent );
		Element newNode;
		try {
			if ( posibleParents.getLength() > 1 ) {
				// if we have more then 1 parent we need to have an sanityArg s.t. we insert our new attribute in the right tag
				if ( refAttr == null ) {
					LOGGER.warn( "Action would change values of more than one node; stopped!" );
					return null;
				}
				for ( int i = 1; i < posibleParents.getLength(); i++ ) {
					if ( ( (Element)posibleParents.item( i ) ).getAttribute( refAttr ).equals( refVal ) ) {
						parent = posibleParents.item( i );
						break;
					}
				}
			} else {
				parent = posibleParents.item( 0 );
			}

			if ( parent == null ) {
				LOGGER.warn( "Node: '" + nameOfParent + "' could not be found" );
				return null;
			}
			newNode = doc.createElement( nameOfnewNode );

			if ( !oneLiner ) {
				org.w3c.dom.Text a = doc.createTextNode( "\n" );
				newNode.appendChild( a );
			}
			parent.appendChild( newNode );
		} catch ( DOMException e ) {
			LOGGER.error( "Something went wrong: ", e );
			return null;
		}

		return newNode;
	}

	/**
	 * USB will be enabled
	 */
	public void enableUsb()
	{
		addNewNode( "Hardware", "USB", false );
		addNewNode( "USB", "Controllers", false );
		// OHCI for USB 1.0
		Node controller1 = addNewNode( "Controllers", "Controller", true );
		addAttributeToNode( controller1, "name", "OHCI" );
		addAttributeToNode( controller1, "type", "OHCI" );
		// EHCI for USB 2.0
		Node controller2 = addNewNode( "Controllers", "Controller", true );
		addAttributeToNode( controller2, "name", "EHCI" );
		addAttributeToNode( controller2, "type", "EHCI" );
	}

	/**
	 * Disable usb by removing the USB tag
	 */
	public void disableUsb()
	{
		NodeList usbList = findNodes( "USB" );
		removeNode( usbList.item( 0 ) );
	}

	// function removes a given child and the format childNode 
	private void removeNode( Node node )
	{
		if ( node == null ) {
			LOGGER.warn( "node is null; unsafe" );
			return;
		}
		Node parent = node.getParentNode();
		// this node here is usually a type3 Node used only for the formating of the vbox file
		Node previousSibling = node.getPreviousSibling();

		parent.removeChild( node );

		// HACK remove empty lines
		// format children (\n or \t) have type 3
		if ( previousSibling.getNodeType() == 3 ) {
			// the value of these Nodes are characters
			String tmp = previousSibling.getNodeValue();
			boolean shouldDelete = true;
			for ( int i = 0; i < tmp.length(); i++ ) {
				if ( !Character.isWhitespace( tmp.charAt( i ) ) ) {
					shouldDelete = false;
					break;
				}
			}
			if ( shouldDelete )
				parent.removeChild( previousSibling );
		}
	}

	// cleanup part here
	private void removeBlackListedTags() throws XPathExpressionException
	{
		// iterate over the blackList
		for ( String blackedTag : blackList ) {
			String blackedExpression = ".//" + blackedTag;
			XPathExpression blackedExpr = xPath.compile( blackedExpression );

			NodeList blackedNodes = (NodeList)blackedExpr.evaluate( this.doc, XPathConstants.NODESET );
			for ( int i = 0; i < blackedNodes.getLength(); i++ ) {
				// get the child node
				Element child = (Element)blackedNodes.item( i );
				// remove child
				if ( child.getTagName().equals( "Adapter" ) && child.getAttribute( "enabled" ).equals( "true" ) ) {
					// we need to remove the children of the active network adapter
					// these are the mode of network connection and disabled modes...they go together -> see wiki
					Node firstChild = child.getChildNodes().item( 0 );
					Node secondChild = child.getChildNodes().item( 1 );
					if ( firstChild != null && secondChild != null ) {
						if ( firstChild.getNodeName().equals( "#text" ) && !secondChild.getNodeName().equals( "#text" ) ) {
							removeNode( child.getChildNodes().item( 1 ) );
						}
					}
					LOGGER.warn( "possible problem while removing formating node" );
					continue;
				}

				if ( ( child.getTagName().equals( "StorageController" ) && !child.getAttribute( "name" ).equals( "Floppy" ) )
						|| ( child.getTagName().equals( "AttachedDevice" ) && child.getAttribute( "type" ).equals( "HardDisk" ) ) ) {
					continue;
				}
				// the structure of the xml Document is achieved through children nodes that are made up of just /n and spaces
				// when a child node is deleted we get an empty line there the old child node used to be
				removeNode( child );
			}
		}
	}

	private String givePathToStorageController( String uuid )
	{
		// StorageController is the parent of the parent of node with given uuid
		return "//Image[contains(@uuid, \'" + uuid + "\')]/../..";
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getOsName()
	{
		return osName;
	}

	public ArrayList<HardDisk> getHdds()
	{
		return hddsArray;
	}

	public String toString()
	{
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );

			transformer.transform( new DOMSource( doc ), new StreamResult( sw ) );
			return sw.toString();
		} catch ( Exception ex ) {
			throw new RuntimeException( "Error converting to String", ex );
		}
	}
}
