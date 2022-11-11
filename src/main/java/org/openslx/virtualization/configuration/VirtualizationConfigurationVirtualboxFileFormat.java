package org.openslx.virtualization.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.util.Resources;
import org.openslx.util.Util;
import org.openslx.util.XmlHelper;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.DriveBusType;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.HardDisk;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class handling the parsing of a .vbox machine description file
 */
public class VirtualizationConfigurationVirtualboxFileFormat
{
	private static final Logger LOGGER = LogManager.getLogger( VirtualizationConfigurationVirtualboxFileFormat.class );

	// key information set during initial parsing of the XML file
	private String osName = "";
	private ArrayList<HardDisk> hddsArray = new ArrayList<HardDisk>();

	// XPath and DOM parsing related members
	private Document doc = null;

	/**
	 * Version of the configuration file format.
	 */
	private Version version = null;

	/**
	 * File names of XML schema files for different file format versions.
	 */
	private final static HashMap<Version, String> FILE_FORMAT_SCHEMA_VERSIONS = new HashMap<Version, String>() {

		private static final long serialVersionUID = -3163681758191475625L;

		{
			put( Version.valueOf( "1.15" ), "VirtualBox-settings_v1-15.xsd" );
			put( Version.valueOf( "1.16" ), "VirtualBox-settings_v1-16.xsd" );
			put( Version.valueOf( "1.17" ), "VirtualBox-settings_v1-17.xsd" );
			put( Version.valueOf( "1.18" ), "VirtualBox-settings_v1-18.xsd" );
		}
	};

	/**
	 * Path to the VirtualBox file format schemas within the *.jar file.
	 */
	private final static String FILE_FORMAT_SCHEMA_PREFIX_PATH = Resources.PATH_SEPARATOR + "virtualbox"
			+ Resources.PATH_SEPARATOR + "xsd";

	// list of nodes to automatically remove when reading the vbox file
	private static String[] blacklist = {
			"/VirtualBox/Machine/Hardware/GuestProperties",
			"/VirtualBox/Machine/Hardware/VideoCapture",
			"/VirtualBox/Machine/Hardware/HID",
			"/VirtualBox/Machine/Hardware/LPT",
			"/VirtualBox/Machine/Hardware/SharedFolders",
			"/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']/*",
			"/VirtualBox/Machine/ExtraData",
			"/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice[not(@type='HardDisk')]",
			"/VirtualBox/Machine/Hardware/StorageControllers/StorageController/AttachedDevice[not(@type='HardDisk')]",
			"/VirtualBox/Machine/MediaRegistry/FloppyImages",
			"/VirtualBox/Machine/MediaRegistry/DVDImages" };

	public static enum PlaceHolder
	{
		FLOPPYUUID( "%VM_FLOPPY_UUID%" ),
		FLOPPYLOCATION( "%VM_FLOPPY_LOCATION%" ),
		CPU( "%VM_CPU_CORES%" ),
		MEMORY( "%VM_RAM%" ),
		MACHINEUUID( "%VM_MACHINE_UUID%" ),
		NETWORKMAC( "%VM_NIC_MAC%" ),
		HDDLOCATION( "%VM_HDD_LOCATION%" ),
		HDDUUID( "%VM_HDD_UUID_" );

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
	 * @throws VirtualizationConfigurationException if the given file is not a valid VirtualBox
	 *            configuration file.
	 */
	public VirtualizationConfigurationVirtualboxFileFormat( File file ) throws IOException, VirtualizationConfigurationException
	{
		doc = XmlHelper.parseDocumentFromStream( new FileInputStream( file ) );
		doc = XmlHelper.removeFormattingNodes( doc );
		if ( doc == null )
			throw new VirtualizationConfigurationException( "Could not parse given VirtualBox machine configuration file!" );

		this.parseConfigurationVersion();
		this.init();
	}

	/**
	 * Creates an vbox configuration by constructing a DOM from the XML content given as a byte
	 * array.
	 * 
	 * @param machineDescription content of the XML file saved as a byte array.
	 * @param length of the machine description byte array.
	 * @throws VirtualizationConfigurationException creation of VirtualBox configuration file representation failed.
	 */
	public VirtualizationConfigurationVirtualboxFileFormat( byte[] machineDescription, int length ) throws VirtualizationConfigurationException
	{
		ByteArrayInputStream is = new ByteArrayInputStream( machineDescription );

		doc = XmlHelper.parseDocumentFromStream( is );
		if ( doc == null ) {
			final String errorMsg = "Could not parse given VirtualBox machine description from byte array!";
			LOGGER.debug( errorMsg );
			throw new VirtualizationConfigurationException( errorMsg );
		}

		this.parseConfigurationVersion();
		this.init();
	}

	public void validate() throws VirtualizationConfigurationException
	{
		this.validateFileFormatVersion( this.getVersion() );
	}

	public void validateFileFormatVersion( Version version ) throws VirtualizationConfigurationException
	{
		if ( this.getVersion() != null && this.doc != null ) {
			// check if specified version is supported
			final String fileName = FILE_FORMAT_SCHEMA_VERSIONS.get( version );

			if ( fileName == null ) {
				final String errorMsg = "File format version " + version.toString() + " is not supported!";
				LOGGER.debug( errorMsg );
				throw new VirtualizationConfigurationException( errorMsg );
			} else {
				// specified version is supported, so validate document with corresponding schema file
				final InputStream schemaResource = VirtualizationConfigurationVirtualboxFileFormat
						.getSchemaResource( fileName );

				if ( schemaResource != null ) {
					try {
						final SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
						final Schema schema = factory.newSchema( new StreamSource( schemaResource ) );
						final Validator validator = schema.newValidator();
						validator.validate( new DOMSource( this.doc ) );
					} catch ( SAXException | IOException e ) {
						final String errorMsg = "XML configuration is not a valid VirtualBox v" + version.toString()
								+ " configuration: " + e.getLocalizedMessage();
						LOGGER.debug( errorMsg );
						throw new VirtualizationConfigurationException( errorMsg );
					}
				}
			}
		}
	}

	private static InputStream getSchemaResource( String fileName )
	{
		final String schemaFilePath = FILE_FORMAT_SCHEMA_PREFIX_PATH + File.separator + fileName;
		return VirtualizationConfigurationVirtualboxFileFormat.class.getResourceAsStream( schemaFilePath );
	}

	/**
	 * Main initialization functions parsing the document created during the constructor.
	 * @throws VirtualizationConfigurationException 
	 */
	private void init() throws VirtualizationConfigurationException
	{
		try {
			this.validate();
		} catch ( VirtualizationConfigurationException e ) {
			// do not print output of failed validation if placeholders are available
			// since thoses placeholder values violate the defined UUID pattern
			if ( !this.checkForPlaceholders() ) {
				final String errorMsg = "XML configuration is not a valid VirtualBox v" + version.toString()
						+ " configuration: " + e.getLocalizedMessage();
				LOGGER.debug( errorMsg );
			}
		}

		if ( Util.isEmptyString( getDisplayName() ) ) {
			throw new VirtualizationConfigurationException( "Machine doesn't have a name" );
		}
		try {
			ensureHardwareUuid();
			setOsType();
			fixUsb(); // Since we now support selecting specific speed
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
	
	private void parseConfigurationVersion() throws VirtualizationConfigurationException
	{
		String versionText;
		try {
			versionText = XmlHelper.compileXPath( "/VirtualBox/@version" ).evaluate( this.doc );
		} catch ( XPathExpressionException e ) {
			throw new VirtualizationConfigurationException(
					"Failed to parse the version number of the configuration file" );
		}

		if ( versionText == null || versionText.isEmpty() ) {
			throw new VirtualizationConfigurationException( "Configuration file does not contain any version number!" );
		} else {
			// parse version information from textual description
			final Pattern versionPattern = Pattern.compile( "^(\\d+\\.\\d+).*$" );
			final Matcher versionMatcher = versionPattern.matcher( versionText );

			if ( versionMatcher.find() ) {
				this.version = Version.valueOf( versionMatcher.group( 1 ) );
			}

			if ( this.version == null ) {
				throw new VirtualizationConfigurationException( "Configuration file version number is not valid!" );
			}
		}
	}

	private void fixUsb()
	{
		NodeList list = findNodes( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller" );
		if ( list != null && list.getLength() != 0 ) {
			LOGGER.info( "USB present, not fixing anything" );
			return;
		}
		// If there's no USB section, this can mean two things:
		// 1) Old config that would always default to USB 2.0 for "USB enabled" or nothing for disabled
		// 2) New config with USB disabled
		list = findNodes( "/VirtualBox/OpenSLX/USB[@disabled]" );
		if ( list != null && list.getLength() != 0 ) {
			LOGGER.info( "USB explicitly disabled" );
			return; // Explicitly marked as disabled, do nothing
		}
		// We assume case 1) and add USB 2.0
		LOGGER.info( "Fixing USB: Adding USB 2.0" );
		Element controller;
		Element node = createNodeRecursive( "/VirtualBox/Machine/Hardware/USB/Controllers" );
		controller = addNewNode( node, "Controller" );
		controller.setAttribute( "name", "OHCI" );
		controller.setAttribute( "type", "OHCI" );
		controller = addNewNode( node, "Controller" );
		controller.setAttribute( "name", "EHCI" );
		controller.setAttribute( "type", "EHCI" );
	}

	/**
	 * Saves the machine's uuid as hardware uuid to prevent VMs from
	 * believing in a hardware change.
	 *
	 * @throws XPathExpressionException
	 * @throws VirtualizationConfigurationException 
	 */
	private void ensureHardwareUuid() throws XPathExpressionException, VirtualizationConfigurationException
	{
		// we will need the machine uuid, so get it
		String machineUuid = XmlHelper.compileXPath( "/VirtualBox/Machine/@uuid" ).evaluate( this.doc );
		if ( machineUuid.isEmpty() ) {
			LOGGER.error( "Machine UUID empty, should never happen!" );
			throw new VirtualizationConfigurationException( "XML doesn't contain a machine uuid" );
		}

		NodeList hwNodes = findNodes( "/VirtualBox/Machine/Hardware" );
		int count = hwNodes.getLength();
		if ( count != 1 ) {
			throw new VirtualizationConfigurationException( "Zero or more '/VirtualBox/Machine/Hardware' node were found, should never happen!" );
		}
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
	}

	public Version getVersion()
	{
		return this.version;
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
		changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']", "MACAddress", PlaceHolder.NETWORKMAC.toString() );

		NodeList hdds = findNodes( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk" );
		for ( int i = 0; i < hdds.getLength(); i++ ) {
			Element hdd = (Element)hdds.item( i );
			if ( hdd == null )
				continue;
			String hddUuid = hdd.getAttribute( "uuid" );
			hdd.setAttribute( "uuid", PlaceHolder.HDDUUID.toString() + i + "%" );
			final NodeList images;
			if ( this.getVersion().isSmallerThan( Version.valueOf( "1.17" ) ) ) {
				images = findNodes( "/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice/Image" );
			} else {
				images = findNodes(
						"/VirtualBox/Machine/Hardware/StorageControllers/StorageController/AttachedDevice/Image" );
			}

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
			XPathExpression blackedExpr = XmlHelper.compileXPath( blackedTag );
			NodeList blackedNodes = (NodeList)blackedExpr.evaluate( this.doc, XPathConstants.NODESET );
			for ( int i = 0; i < blackedNodes.getLength(); i++ ) {
				// go through the child nodes of the blacklisted ones -> why?
				Element child = (Element)blackedNodes.item( i );
				removeNode( child );
			}
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
			return XmlHelper.compileXPath( "/VirtualBox/Machine/@name" ).evaluate( this.doc );
		} catch ( XPathExpressionException e ) {
			return "";
		}
	}

	/**
	 * Function finds and saves the name of the guest OS
	 * 
	 * @throws XPathExpressionException failed to find and retrieve name of the guest OS.
	 */
	public void setOsType() throws XPathExpressionException
	{
		String os = XmlHelper.compileXPath( "/VirtualBox/Machine/@OSType" ).evaluate( this.doc );
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
	 * Search for attached hard drives and determine their controller and their path.
	 *
	 * @throws XPathExpressionException failed to find attached hard drives and their controllers.
	 */
	public void setHdds() throws XPathExpressionException
	{
		final XPathExpression hddsExpr;
		if ( this.getVersion().isSmallerThan( Version.valueOf( "1.17" ) ) ) {
			hddsExpr = XmlHelper.compileXPath(
					"/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice[@type='HardDisk']/Image" );
		} else {
			hddsExpr = XmlHelper.compileXPath(
					"/VirtualBox/Machine/Hardware/StorageControllers/StorageController/AttachedDevice[@type='HardDisk']/Image" );
		}

		NodeList nodes = (NodeList)hddsExpr.evaluate( this.doc, XPathConstants.NODESET );
		if ( nodes == null ) {
			LOGGER.error( "Failed to find attached hard drives." );
			return;
		}
		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Element hddElement = (Element)nodes.item( i );
			if ( hddElement == null )
				continue;
			String uuid = hddElement.getAttribute( "uuid" );
			if ( uuid.isEmpty() )
				continue;
			// got uuid, check if it was registered
			XPathExpression hddsRegistered = XmlHelper.compileXPath( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk[@uuid='" + uuid + "']" );
			NodeList hddsRegisteredNodes = (NodeList)hddsRegistered.evaluate( this.doc, XPathConstants.NODESET );
			if ( hddsRegisteredNodes == null || hddsRegisteredNodes.getLength() != 1 ) {
				LOGGER.error( "Found hard disk with uuid '" + uuid + "' which does not appear (unique) in the Media Registry. Skipping." );
				continue;
			}
			Element hddElementReg = (Element)hddsRegisteredNodes.item( 0 );
			if ( hddElementReg == null )
				continue;
			String fileName = hddElementReg.getAttribute( "location" );
			String type = hddElementReg.getAttribute( "type" );
			if ( !type.equals( "Normal" ) && !type.equals( "Writethrough" ) ) {
				LOGGER.warn( "Type of the disk file is neither 'Normal' nor 'Writethrough' but: " + type );
				LOGGER.warn( "This makes the image not directly modificable, which might lead to problems when editing it locally." );
			}
			// search if it is also attached to a controller
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
			String controllerMode = hddController.getAttribute( "type" );
			String controllerType = hddController.getAttribute( "name" );
			DriveBusType busType;
			if ( controllerType.equals( "NVMe" ) ) {
				busType = DriveBusType.NVME;
			} else {
				try {
					// This assumes the type in the xml matches our enum constants.
					busType = DriveBusType.valueOf( controllerType );
				} catch (Exception e) {
					LOGGER.warn( "Skipping unknown HDD controller type '" + controllerType + "'" );
					continue;
				}
			}
			LOGGER.info( "Adding hard disk with controller: " + busType + " (" + controllerMode + ") from file '" + fileName + "'." );
			hddsArray.add( new HardDisk( controllerMode, busType, fileName ) );
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
			XPathExpression expr = XmlHelper.compileXPath( xpath );
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
	 * @return state of the change operation whether the attribute was changed successful or not.
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
	 * @param childName name of the node to be added
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

	public Element createNodeRecursive( String xPath )
	{
		String[] nodeNames = xPath.split( "/" );
		Node parent = this.doc;
		Element latest = null;
		for ( int nodeIndex = 0; nodeIndex < nodeNames.length; ++nodeIndex ) {
			if ( nodeNames[nodeIndex].length() == 0 )
				continue;
			Node node = skipNonElementNodes( parent.getFirstChild() );
			while ( node != null ) {
				if ( node.getNodeType() == Node.ELEMENT_NODE && nodeNames[nodeIndex].equals( node.getNodeName() ) )
					break; // Found existing
				// Check next on same level
				node = skipNonElementNodes( node.getNextSibling() );
			}
			if ( node == null ) {
				node = doc.createElement( nodeNames[nodeIndex] );
				parent.appendChild( node );
			}
			parent = node;
			latest = (Element)node;
		}
		return latest;
	}
	
	private Element skipNonElementNodes( Node nn )
	{
		while ( nn != null && nn.getNodeType() != Node.ELEMENT_NODE ) {
			nn = nn.getNextSibling();
		}
		return (Element)nn;
	}
	
	public void setExtraData( String key, String value )
	{
		NodeList nl = findNodes( "/VirtualBox/Machine/ExtraData/ExtraDataItem" );
		Element e = null;
		if ( nl != null ) {
			for ( int i = 0; i < nl.getLength(); ++i ) {
				Node n = nl.item( i );
				if ( n.getNodeType() == Node.ELEMENT_NODE ) {
					final Element ne = (Element)n;
					final String keyValue = ne.getAttribute( "name" );
					if ( keyValue != null && keyValue.equals( key ) ) {
						e = ne;
						break;
					}
				}
			}
		}
		if ( e == null ) {
			Element p = createNodeRecursive( "/VirtualBox/Machine/ExtraData" );
			e = addNewNode( p, "ExtraDataItem" );
			e.setAttribute( "name", key );
		}
		e.setAttribute( "value", value );
	}

	/**
	 * Creates a new element to the given parent node.
	 *
	 * @param parent to add the new element to
	 * @param childName name of the new element to create
	 * @return the newly created node
	 */
	public Element addNewNode( Node parent, String childName )
	{
		if ( parent == null || parent.getNodeType() != Node.ELEMENT_NODE ) {
			return null;
		}
		Element newNode = null;
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

	/**
	 * Remove all nodes with name childName from parentPath
	 * @param parentPath XPath to parent node of where child nodes are to be deleted
	 * @param childName Name of nodes to delete
	 */
	public void removeNodes( String parentPath, String childName )
	{
		NodeList parentNodes = findNodes( parentPath );
		// XPath might match multiple nodes
		for ( int i = 0; i < parentNodes.getLength(); ++i ) {
			Node parent = parentNodes.item( i );
			List<Node> delList = new ArrayList<>( 0 );
			// Iterate over child nodes
			for ( Node child = parent.getFirstChild(); child != null; child = child.getNextSibling() ) {
				if ( childName.equals( child.getNodeName() ) ) {
					// Remember all to be deleted (don't delete while iterating)
					delList.add( child );
				}
			}
			// Now delete them all
			for ( Node child : delList ) {
				parent.removeChild( child );
			}
		}
	}
}
