package org.openslx.util.vm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.openslx.util.ResourceLoader;
import org.openslx.util.XmlHelper;
import org.openslx.util.vm.VmMetaData.DriveBusType;
import org.openslx.util.vm.VmMetaData.HardDisk;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class handling the parsing of a .vbox machine description file
 */
public class VboxConfig
{
	private static final Logger LOGGER = Logger.getLogger( VboxConfig.class );

	// key information set during initial parsing of the XML file
	private String displayName = new String();
	private String osName = new String();
	private ArrayList<HardDisk> hddsArray = new ArrayList<HardDisk>();

	// XPath and DOM parsing related members
	private Document doc = null;

	// list of nodes to automatically remove when reading the vbox file
	private static String[] blacklist = {
			"/VirtualBox/Machine/Hardware/GuestProperties",
			"/VirtualBox/Machine/Hardware/VideoCapture",
			"/VirtualBox/Machine/Hardware/HID",
			"/VirtualBox/Machine/Hardware/USB",
			"/VirtualBox/Machine/Hardware/LPT",
			"/VirtualBox/Machine/Hardware/SharedFolders",
			"/VirtualBox/Machine/Hardware/Network/Adapter[@enabled='true']/*",
			"/VirtualBox/Machine/ExtraData",
			"/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice[not(@type='HardDisk')]",
			"/VirtualBox/Machine/MediaRegistry/FloppyImages",
			"/VirtualBox/Machine/MediaRegistry/DVDImages" };

	public static enum PlaceHolder
	{
		FLOPPYUUID( "%VM_FLOPPY_UUID%" ), FLOPPYLOCATION( "%VM_FLOPPY_LOCATION%" ), CPU( "%VM_CPU_CORES%" ), MEMORY( "%VM_RAM%" ), MACHINEUUID( "%VM_MACHINE_UUID%" ), NETWORKMAC(
				"%VM_NIC_MAC%" ), HDDLOCATION( "%VM_HDD_LOCATION%" ), HDDUUID( "%VM_HDD_UUID_" );

		private final String holderName;

		private PlaceHolder( String name )
		{
			this.holderName = name;
		}

		@Override
		public String toString()
		{
			return holderName;
		}
	}

	/**
	 * Creates a vbox configuration by constructing a DOM from the given VirtualBox machine
	 * configuration file.
	 * Will validate the given file against the VirtualBox XSD schema and only proceed if it is
	 * valid.
	 *
	 * @param file the VirtualBox machine configuration file
	 * @throws IOException if an error occurs while reading the file
	 * @throws UnsupportedVirtualizerFormatException if the given file is not a valid VirtualBox
	 *            configuration file.
	 */
	public VboxConfig( File file ) throws IOException, UnsupportedVirtualizerFormatException
	{
		// first validate xml
		try {
			SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
			Schema schema = factory.newSchema( new StreamSource( ResourceLoader.getStream( "/xml/VirtualBox-settings.xsd" ) ) );
			Validator validator = schema.newValidator();
			validator.validate( new StreamSource( file ) );
		} catch ( SAXException e ) {
			LOGGER.error( "Selected vbox file was not validated against the XSD schema: " + e.getMessage() );
			throw new UnsupportedVirtualizerFormatException( "Invalid VirtualBox machine configuration file!" );
		}
		// valid xml, try to create the DOM
		doc = XmlHelper.parseDocumentFromStream( new FileInputStream( file ) );
		doc = XmlHelper.removeFormattingNodes( doc );
		if ( doc == null )
			throw new UnsupportedVirtualizerFormatException( "Could not create DOM from given VirtualBox machine configuration file!" );
	}

	/**
	 * Creates an vbox configuration by constructing a DOM from the XML content given as a byte
	 * array.
	 * 
	 * @param machineDescription content of the XML file saved as a byte array.
	 * @param length of the machine description byte array.
	 * @throws IOException if an
	 */
	public VboxConfig( byte[] machineDescription, int length ) throws UnsupportedVirtualizerFormatException
	{
		ByteArrayInputStream is = new ByteArrayInputStream( machineDescription );
		doc = XmlHelper.parseDocumentFromStream( is );
		if ( doc == null ) {
			LOGGER.error( "Failed to create a DOM from given machine description." );
			throw new UnsupportedVirtualizerFormatException( "Could not create DOM from given machine description as. byte array." );
		}
	}

	/**
	 * Main initialization functions parsing the document created during the constructor.
	 */
	public void init()
	{
		try {
			setMachineName();
			ensureHardwareUuid();
			setOsType();
			if ( checkForPlaceholders() ) {
				return;
			}
			setHdds();
			removeBlacklistedElements();
			addPlaceHolders();
		} catch ( XPathExpressionException e ) {
			LOGGER.debug( "Could not initialize VBoxConfig", e );
			return;
		}
	}

	/**
	 * Saves the machine's uuid as hardware uuid to prevent VMs from
	 * believing in a hardware change.
	 *
	 * @throws XPathExpressionException
	 */
	private void ensureHardwareUuid() throws XPathExpressionException
	{
		// we will need the machine uuid, so get it
		String machineUuid = XmlHelper.XPath.compile( "/VirtualBox/Machine/@uuid" ).evaluate( this.doc );
		if ( machineUuid.isEmpty() ) {
			LOGGER.error( "Machine UUID empty, should never happen!" );
			return;
		}

		NodeList hwNodes = findNodes( "/VirtualBox/Machine/Hardware" );
		int count = hwNodes.getLength();
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
			// HACK: hijack XPathExpressionException ...
			throw new XPathExpressionException( "Zero or more '/VirtualBox/Machine/Hardware' node were found, should never happen!" );
		}
	}

	/**
	 * Self-explanatory.
	 */
	public void addPlaceHolders()
	{
		// placeholder for the machine uuid
		changeAttribute( "/VirtualBox/Machine", "uuid", PlaceHolder.MACHINEUUID.toString() );

		// placeholder for the location of the virtual hdd
		changeAttribute( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk", "location", PlaceHolder.HDDLOCATION.toString() );

		// placeholder for the memory
		changeAttribute( "/VirtualBox/Machine/Hardware/Memory", "RAMSize", PlaceHolder.MEMORY.toString() );

		// placeholder for the CPU
		changeAttribute( "/VirtualBox/Machine/Hardware/CPU", "count", PlaceHolder.CPU.toString() );

		// placeholder for the MACAddress
		changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter", "MACAddress", PlaceHolder.NETWORKMAC.toString() );

		NodeList hdds = findNodes( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk" );
		for ( int i = 0; i < hdds.getLength(); i++ ) {
			Element hdd = (Element)hdds.item( i );
			if ( hdd == null )
				continue;
			String hddUuid = hdd.getAttribute( "uuid" );
			hdd.setAttribute( "uuid", PlaceHolder.HDDUUID.toString() + i + "%" );
			NodeList images = findNodes( "/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice/Image" );
			for ( int j = 0; j < images.getLength(); j++ ) {
				Element image = (Element)images.item( j );
				if ( image == null )
					continue;
				if ( hddUuid.equals( image.getAttribute( "uuid" ) ) ) {
					image.setAttribute( "uuid", PlaceHolder.HDDUUID.toString() + i + "%" );
					break;
				}
			}
		}
	}

	/**
	 * Function checks if the placeholders are present
	 * 
	 * @return true if the placeholders are present, false otherwise
	 */
	private boolean checkForPlaceholders()
	{
		// TODO this should be more robust...
		NodeList hdds = findNodes( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk" );
		for ( int i = 0; i < hdds.getLength(); i++ ) {
			Element hdd = (Element)hdds.item( i );
			if ( hdd == null )
				continue;
			if ( hdd.getAttribute( "location" ).equals( PlaceHolder.HDDLOCATION.toString() ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called during init(), prunes the DOM from the elements blacklisted defined
	 * in the member blacklist, a list of XPath expressions as String
	 * 
	 * @throws XPathExpressionException
	 */
	private void removeBlacklistedElements() throws XPathExpressionException
	{
		// iterate over the blackList
		for ( String blackedTag : blacklist ) {
			XPathExpression blackedExpr = XmlHelper.XPath.compile( blackedTag );
			NodeList blackedNodes = (NodeList)blackedExpr.evaluate( this.doc, XPathConstants.NODESET );
			for ( int i = 0; i < blackedNodes.getLength(); i++ ) {
				// go through the child nodes of the blacklisted ones -> why?
				Element child = (Element)blackedNodes.item( i );
				removeNode( child );
			}
		}
	}

	/**
	 * Sets the display name set within the DOM expected at the
	 * given XPath path.
	 * 
	 * @throws XPathExpressionException
	 */
	public void setMachineName() throws XPathExpressionException
	{
		String name = XmlHelper.XPath.compile( "/VirtualBox/Machine/@name" ).evaluate( this.doc );
		if ( name != null && !name.isEmpty() ) {
			displayName = name;
		}
	}

	/**
	 * Getter for the parsed display name
	 *
	 * @return the display name of this VM
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Function finds and saves the name of the guest OS
	 * 
	 * @throws XPathExpressionException
	 */
	public void setOsType() throws XPathExpressionException
	{
		String os = XmlHelper.XPath.compile( "/VirtualBox/Machine/@OSType" ).evaluate( this.doc );
		if ( os != null && !os.isEmpty() ) {
			osName = os;
		}
	}

	/**
	 * Getter for the parsed guest OS name
	 *
	 * @return name of the guest OS
	 */
	public String getOsName()
	{
		return osName;
	}

	/**
	 * Search disk drives within the DOM using this class
	 *
	 * @throws XPathExpressionException
	 */
	public void setHdds() throws XPathExpressionException
	{
		XPathExpression hddsExpr = XmlHelper.XPath.compile( "/VirtualBox/Machine/MediaRegistry/HardDisks/*" );
		NodeList nodes = (NodeList)hddsExpr.evaluate( this.doc, XPathConstants.NODESET );
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Element hddElement = (Element)nodes.item( i );
			if ( hddElement == null )
				continue;
			String fileName = hddElement.getAttribute( "location" );
			String type = hddElement.getAttribute( "type" );
			if ( !type.equals( "Normal" ) && !type.equals( "Writethrough" ) ) {
				LOGGER.warn( "Type of the disk file is neither 'Normal' nor 'Writethrough' but: " + type );
				LOGGER.warn( "This makes the image not directly modificable, which might lead to problems when editing it locally." );
			}
			Node hddDevice = hddElement.getParentNode();
			if ( hddDevice == null ) {
				LOGGER.error( "HDD node had a null parent, shouldn't happen" );
				continue;
			}
			Element hddController = (Element)hddDevice.getParentNode();
			if ( hddController == null ) {
				LOGGER.error( "HDD node had a null parent, shouldn't happen" );
				continue;
			}
			String controllerDevice = hddController.getAttribute( "type" );
			String bus = hddController.getAttribute( "name" );
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

	/**
	 * Getter for the list of detected hard drives.
	 *
	 * @return list of disk drives.
	 */
	public ArrayList<HardDisk> getHdds()
	{
		return hddsArray;
	}

	/**
	 * Enable USB by adding the element /VirtualBox/Machine/Hardware/USB
	 * and adding controllers for OHCI and EHCI (thus enabling USB 2.0).
	 */
	public void enableUsb()
	{
		addNewNode( "/VirtualBox/Machine/Hardware", "USB" );
		addNewNode( "/VirtualBox/Machine/Hardware/USB", "Controllers" );
		// OHCI for USB 1.0
		Node ohci = addNewNode( "/VirtualBox/Machine/Hardware/USB/Controllers", "Controller" );
		addAttributeToNode( ohci, "name", "OHCI" );
		addAttributeToNode( ohci, "type", "OHCI" );
		// EHCI for USB 2.0
		Node ehci = addNewNode( "/VirtualBox/Machine/Hardware/USB/Controllers", "Controller" );
		addAttributeToNode( ehci, "name", "EHCI" );
		addAttributeToNode( ehci, "type", "EHCI" );
	}

	/**
	 * Removes all USB elements
	 */
	public void disableUsb()
	{
		NodeList usbList = findNodes( "/VirtualBox/Machine/Hardware/USB" );
		for ( int i = 0; i < usbList.getLength(); i++ ) {
			removeNode( usbList.item( 0 ) );
		}
	}

	/**
	 * Detect if the vbox file has any machine snapshot by looking at
	 * the existance of '/VirtualBox/Machine/Snapshot' elements.
	 * 
	 * @return true if a machine snapshot is present, false otherwise.
	 */
	public boolean isMachineSnapshot()
	{
		// check if the vbox configuration file contains some machine snapshots.
		// by looking at the existance of /VirtualBox/Machine/Snapshot
		NodeList machineSnapshots = findNodes( "/VirtualBox/Machine/Snapshot" );
		return machineSnapshots != null && machineSnapshots.getLength() > 0;
	}

	/**
	 * Searches the DOM for the elements matching the given XPath expression.
	 * 
	 * @param xpath expression to search the DOM with
	 * @return nodes found by evaluating given XPath expression
	 */
	public NodeList findNodes( String xpath )
	{
		NodeList nodes = null;
		try {
			XPathExpression expr = XmlHelper.XPath.compile( xpath );
			Object nodesObject = expr.evaluate( this.doc, XPathConstants.NODESET );
			nodes = (NodeList)nodesObject;
		} catch ( XPathExpressionException e ) {
			LOGGER.error( "Could not build path", e );
		}
		return nodes;
	}

	/**
	 * Function used to change the value of an attribute of given element.
	 * The given xpath to the element needs to find a single node, or this function will return
	 * false. If only one element was found, it will return the result of calling addAttributeToNode.
	 * Note that due to the way setAttribute() works, this function to create the attribute if it
	 * doesn't exists.
	 *
	 * @param elementXPath given as an xpath expression
	 * @param attribute attribute to change
	 * @param value to set the attribute to
	 */
	public boolean changeAttribute( String elementXPath, String attribute, String value )
	{
		NodeList nodes = findNodes( elementXPath );
		if ( nodes == null || nodes.getLength() != 1 ) {
			LOGGER.error( "No unique node could be found for: " + elementXPath );
			return false;
		}
		return addAttributeToNode( nodes.item( 0 ), attribute, value );
	}

	/**
	 * Add given attribute with given value to the given node.
	 * NOTE: this will overwrite the attribute of the node if it already exists.
	 *
	 * @param node to add the attribute to
	 * @param attribute attribute to add to the node
	 * @param value of the attribute
	 * @return true if successful, false otherwise
	 */
	public boolean addAttributeToNode( Node node, String attribute, String value )
	{
		if ( node == null || node.getNodeType() != Node.ELEMENT_NODE ) {
			LOGGER.error( "Trying to change attribute of a non element node!" );
			return false;
		}
		try {
			( (Element)node ).setAttribute( attribute, value );
		} catch ( DOMException e ) {
			LOGGER.error( "Failed set '" + attribute + "' to '" + value + "' of xml node '" + node.getNodeName() + "': ", e );
			return false;
		}
		return true;
	}

	/**
	 * Adds a new node named nameOfNewNode to the given parent found by parentXPath.
	 *
	 * @param parentXPath XPath expression to the parent
	 * @param nameOfnewNode name of the node to be added
	 * @return the newly added Node
	 */
	public Node addNewNode( String parentXPath, String childName )
	{
		NodeList possibleParents = findNodes( parentXPath );
		if ( possibleParents == null || possibleParents.getLength() != 1 ) {
			LOGGER.error( "Could not find unique parent node to add new node to: " + parentXPath );
			return null;
		}
		return addNewNode( possibleParents.item( 0 ), childName );
	}

	/**
	 * Creates a new element to the given parent node.
	 *
	 * @param parent to add the new element to
	 * @param childName name of the new element to create
	 * @return the newly created node
	 */
	public Node addNewNode( Node parent, String childName )
	{
		if ( parent == null || parent.getNodeType() != Node.ELEMENT_NODE ) {
			return null;
		}
		Node newNode = null;
		try {
			newNode = doc.createElement( childName );
			parent.appendChild( newNode );
		} catch ( DOMException e ) {
			LOGGER.error( "Failed to add '" + childName + "' to '" + parent.getNodeName() + "'." );
		}
		return newNode;
	}

	/**
	 * Helper to remove given node from the DOM.
	 *
	 * @param node Node object to remove.
	 */
	private void removeNode( Node node )
	{
		if ( node == null )
			return;
		Node parent = node.getParentNode();
		if ( parent != null )
			parent.removeChild( node );
	}

	/**
	 * Helper to output the DOM as a String.
	 *
	 * @param prettyPrint sets whether to indent the output
	 * @return (un-)formatted XML
	 */
	public String toString( boolean prettyPrint )
	{
		return XmlHelper.getXmlFromDocument( doc, prettyPrint );
	}
}
