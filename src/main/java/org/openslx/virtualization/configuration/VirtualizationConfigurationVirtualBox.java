package org.openslx.virtualization.configuration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.Util;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfigurationVirtualboxFileFormat.PlaceHolder;
import org.openslx.virtualization.hardware.VirtOptionValue;
import org.openslx.virtualization.hardware.ConfigurationGroups;
import org.openslx.virtualization.hardware.Ethernet;
import org.openslx.virtualization.hardware.SoundCard;
import org.openslx.virtualization.hardware.Usb;
import org.openslx.virtualization.virtualizer.VirtualizerVirtualBox;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VirtualizationConfigurationVirtualBox extends VirtualizationConfiguration
{
	/**
	 * File name extension for VirtualBox virtualization configuration files..
	 */
	public static final String FILE_NAME_EXTENSION = "vbox";

	private static final Logger LOGGER = LogManager.getLogger( VirtualizationConfigurationVirtualBox.class );

	private final VirtualizationConfigurationVirtualboxFileFormat config;

	public static enum EthernetType
	{
		NAT( "vboxnet1" ), BRIDGED( "vboxnet0" ), HOST_ONLY( "vboxnet2" );

		public final String vnet;

		private EthernetType( String vnet )
		{
			this.vnet = vnet;
		}
	}

	public VirtualizationConfigurationVirtualBox( List<OperatingSystem> osList, File file )
			throws IOException, VirtualizationConfigurationException
	{
		super( new VirtualizerVirtualBox(), osList );
		this.config = new VirtualizationConfigurationVirtualboxFileFormat( file );
		init();
	}

	public VirtualizationConfigurationVirtualBox( List<OperatingSystem> osList, byte[] vmContent, int length )
			throws IOException, VirtualizationConfigurationException
	{
		super( new VirtualizerVirtualBox(), osList );
		this.config = new VirtualizationConfigurationVirtualboxFileFormat( vmContent, length );
		init();
	}

	private void init()
	{
		displayName = config.getDisplayName();
		setOs( config.getOsName() );
		this.isMachineSnapshot = config.isMachineSnapshot();
		for ( HardDisk hardDisk : config.getHdds() ) {
			hdds.add( hardDisk );
		}
	}

	private void disableEnhancedNetworkAdapters()
	{
		final NodeList disableAdapters = this.config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter[not(@slot='0')]" );

		if ( disableAdapters != null ) {
			for ( int i = 0; i < disableAdapters.getLength(); i++ ) {
				final Element disableAdapter = (Element)disableAdapters.item( i );
				disableAdapter.setAttribute( "enabled", "false" );
			}
		}
	}

	private void removeEnhancedNetworkAdapters()
	{
		final NodeList removeAdapters = this.config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter[not(@slot='0')]" );

		if ( removeAdapters != null ) {
			for ( int i = 0; i < removeAdapters.getLength(); i++ ) {
				final Node removeAdapter = removeAdapters.item( i );
				removeAdapter.getParentNode().removeChild( removeAdapter );
			}
		}
	}

	@Override
	public void transformEditable() throws VirtualizationConfigurationException
	{
		this.disableEnhancedNetworkAdapters();
	}

	@Override
	public void transformPrivacy() throws VirtualizationConfigurationException
	{

	}

	@Override
	public byte[] getConfigurationAsByteArray()
	{
		return config.toString( true ).getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public boolean addEmptyHddTemplate()
	{
		return this.addHddTemplate( "%VM_DISK_PATH%", "%VM_DISK_MODE%", "%VM_DISK_REDOLOGDIR%" );
	}

	@Override
	public boolean addHddTemplate( String diskImage, String hddMode, String redoDir )
	{
		config.changeAttribute( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk[@location='"
				+ PlaceHolder.HDDLOCATION.toString() + "']", "location", diskImage );
		config.changeAttribute( "/VirtualBox/Machine", "snapshotFolder", redoDir );
		return true;
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		String diskImagePath = diskImage.getName();
		config.changeAttribute( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk", "location", diskImagePath );

		UUID newhdduuid = UUID.randomUUID();

		// patching the new uuid in the vbox config file here
		String vboxUUid = "{" + newhdduuid.toString() + "}";
		config.changeAttribute( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk", "uuid", vboxUUid );
		config.changeAttribute( "/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice/Image", "uuid",
				vboxUUid );

		// the order of the UUID is BIG_ENDIAN but we need to change the order of the first 8 Bytes
		// to be able to write them to the vdi file... the PROBLEM here is that the first 8 
		// are in LITTLE_ENDIAN order in pairs of 4-2-2 not the whole 8 so just changing the 
		// order when we are adding them to the bytebuffer won't help
		//
		// the following is a workaround that works
		ByteBuffer buffer = ByteBuffer.wrap( new byte[ 16 ] );
		buffer.putLong( newhdduuid.getMostSignificantBits() );
		buffer.putLong( newhdduuid.getLeastSignificantBits() );
		byte[] oldOrder = buffer.array();
		// make a coppy here because the last 8 Bytes don't need to change position
		byte[] bytesToWrite = Arrays.copyOf( oldOrder, oldOrder.length );
		// use an offset int[] to help with the shuffle
		int[] offsets = { 3, 2, 1, 0, 5, 4, 7, 6 };
		for ( int index = 0; index < 8; index++ ) {
			bytesToWrite[index] = oldOrder[offsets[index]];
		}
		try ( RandomAccessFile file = new RandomAccessFile( diskImage, "rw" ) ) {
			file.seek( 392 );
			file.write( bytesToWrite, 0, 16 );
		} catch ( Exception e ) {
			LOGGER.warn( "could not patch new uuid in the vdi", e );
		}

		// we need a new machine uuid
		UUID newMachineUuid = UUID.randomUUID();
		if ( newMachineUuid.equals( newhdduuid ) ) {
			LOGGER.warn( "The new Machine UUID is the same as the new HDD UUID; tying again...this vm might not start" );
			newMachineUuid = UUID.randomUUID();
		}
		String machineUUid = "{" + newMachineUuid.toString() + "}";
		return config.changeAttribute( "/VirtualBox/Machine", "uuid", machineUUid );
	}

	@Override
	public boolean addDefaultNat()
	{
		final boolean status;

		final Node adapterSlot0 = config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']" ).item( 0 );
		if ( adapterSlot0 != null ) {
			// remove all child node to wipe existing networking mode
			final NodeList adapterSlot0SettingNodes = adapterSlot0.getChildNodes();
			while ( adapterSlot0.getChildNodes().getLength() > 0 ) {
				adapterSlot0.removeChild( adapterSlot0SettingNodes.item( 0 ) );
			}

			// add networking mode 'NAT'
			if ( config.addNewNode( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']", "NAT" ) == null ) {
				LOGGER.error( "Failed to set network adapter to NAT." );
				status = false;
			} else {
				status = config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']", "MACAddress", "080027B86D12" );
			}
		} else {
			status = false;
		}

		return status;
	}

	@Override
	public void setOs( String vendorOsId )
	{
		config.changeAttribute( "/VirtualBox/Machine", "OSType", vendorOsId );

		final OperatingSystem os = VirtualizationConfigurationUtils.getOsOfVirtualizerFromList( this.osList,
				TConst.VIRT_VIRTUALBOX, vendorOsId );
		this.setOs( os );
	}

	@Override
	public boolean addDisplayName( String name )
	{
		return config.changeAttribute( "/VirtualBox/Machine", "name", name );
	}

	@Override
	public boolean addRam( int mem )
	{
		return config.changeAttribute( "/VirtualBox/Machine/Hardware/Memory", "RAMSize", Integer.toString( mem ) );
	}

	@Override
	public void addFloppy( int index, String image, boolean readOnly )
	{
		Element floppyController = null;
		NodeList matches = (NodeList)config
				.findNodes( "/VirtualBox/Machine/StorageControllers/StorageController[@name='Floppy']" );
		if ( matches == null || matches.getLength() == 0 ) {
			floppyController = (Element)config.addNewNode( "/VirtualBox/Machine/StorageControllers", "StorageController" );
			if ( floppyController == null ) {
				LOGGER.error( "Failed to add <Image> to floppy device." );
				return;
			}
			floppyController.setAttribute( "name", "Floppy" );
			floppyController.setAttribute( "type", "I82078" );
			floppyController.setAttribute( "PortCount", "1" );
			floppyController.setAttribute( "useHostIOCache", "true" );
			floppyController.setAttribute( "Bootable", "false" );
		}
		// virtualbox only allows one controller per type
		if ( matches.getLength() > 1 ) {
			LOGGER.error( "Multiple floppy controllers detected, this should never happen! " );
			return;
		}
		// so if we had any matches, we know we have exactly one
		if ( floppyController == null )
			floppyController = (Element)matches.item( 0 );

		// add the floppy device
		Element floppyDevice = (Element)config.addNewNode( floppyController, "AttachedDevice" );
		if ( floppyDevice == null ) {
			LOGGER.error( "Failed to add <Image> to floppy device." );
			return;
		}
		floppyDevice.setAttribute( "type", "Floppy" );
		floppyDevice.setAttribute( "hotpluggable", "false" );
		floppyDevice.setAttribute( "port", "0" );
		floppyDevice.setAttribute( "device", Integer.toString( index ) );

		// finally add the image to it, if one was given
		if ( image != null ) {
			Element floppyImage = (Element)config.addNewNode( floppyDevice, "Image" );
			if ( floppyImage == null ) {
				LOGGER.error( "Failed to add <Image> to floppy device." );
				return;
			}
			floppyImage.setAttribute( "uuid",
					VirtualizationConfigurationVirtualboxFileFormat.PlaceHolder.FLOPPYUUID.toString() );
			// register the image in the media registry
			Element floppyImages = (Element)config.addNewNode( "/VirtualBox/Machine/MediaRegistry", "FloppyImages" );
			if ( floppyImages == null ) {
				LOGGER.error( "Failed to add <FloppyImages> to media registry." );
				return;
			}
			Element floppyImageReg = (Element)config.addNewNode( "/VirtualBox/Machine/MediaRegistry/FloppyImages",
					"Image" );
			if ( floppyImageReg == null ) {
				LOGGER.error( "Failed to add <Image> to floppy images in the media registry." );
				return;
			}
			floppyImageReg.setAttribute( "uuid",
					VirtualizationConfigurationVirtualboxFileFormat.PlaceHolder.FLOPPYUUID.toString() );
			floppyImageReg.setAttribute( "location",
					VirtualizationConfigurationVirtualboxFileFormat.PlaceHolder.FLOPPYLOCATION.toString() );
		}
	}

	@Override
	public boolean addCdrom( String image )
	{
		// TODO - done in run-virt currently
		return false;
	}

	@Override
	public boolean addCpuCoreCount( int nrOfCores )
	{
		return config.changeAttribute( "/VirtualBox/Machine/Hardware/CPU", "count", Integer.toString( nrOfCores ) );
	}
	
	class VBoxSoundCardModel extends VirtOptionValue
	{

		public VBoxSoundCardModel( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			// XXX I guess this "present" hack will be nicer with enum too
			if ( Util.isEmptyString( this.id ) ) {
				config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "enabled", "false" );
				return;
			}
			config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "enabled", "true" );
			config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "controller", this.id );
		}

		@Override
		public boolean isActive()
		{
			Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/AudioAdapter" ).item( 0 );
			if ( !x.hasAttribute( "enabled" )
					|| ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equals( "false" ) ) ) {
				return Util.isEmptyString( this.id ); // XXX enum
			}
			String val = "AC97";
			if ( x.hasAttribute( "controller" ) ) {
				val = x.getAttribute( "controller" );
			}
			return val.equals( this.id );
		}
		
	}
	
	class VBoxAccel3D extends VirtOptionValue
	{

		public VBoxAccel3D( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			config.changeAttribute( "/VirtualBox/Machine/Hardware/Display", "accelerate3D", this.id );
		}

		@Override
		public boolean isActive()
		{
			Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Display" ).item( 0 );
			String val = "false";
			if ( x.hasAttribute( "accelerate3D" ) ) {
				val = x.getAttribute( "accelerate3D" );
			}
			return val.equalsIgnoreCase( this.id );
		}
		
	}

	/**
	 * Function does nothing for Virtual Box;
	 * Virtual Box accepts per default only one hardware version and is hidden from the user
	 */
	@Override
	public void setVirtualizerVersion( Version type )
	{
	}

	public Version getConfigurationVersion()
	{
		return this.config.getVersion();
	}

	@Override
	public Version getVirtualizerVersion()
	{
		// Virtual Box uses only one virtual hardware version and can't be changed
		return null;
	}
	
	class VBoxNicModel extends VirtOptionValue
	{
		
		private final int cardIndex;

		public VBoxNicModel( int cardIndex, String id, String displayName )
		{
			super( id, displayName );
			this.cardIndex = cardIndex;
		}

		@Override
		public void apply()
		{
			String index = Integer.toString( this.cardIndex );
			String dev = this.id;
			boolean present = true;
			if ( "".equals( this.id ) ) {
				// none type needs to have a valid value; it takes the value of pcnetcpi2;
				// if value is left null or empty vm will not start because value is not valid
				dev = "Am79C970A";
				present = false;
			}
			config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "enabled",
					Boolean.toString( present ) );
			config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "type",
					dev );
		}

		@Override
		public boolean isActive()
		{
			Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter" ).item( 0 );
			if ( !x.hasAttribute( "enabled" )
					|| ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equalsIgnoreCase( "false" ) ) ) {
				return Util.isEmptyString( this.id );
			}
			// Has NIC
			if ( !x.hasAttribute( "type" ) ) {
				return "Am79C973".equals( this.id );
			}
			return x.getAttribute( "type" ).equals( this.id );
		}
		
	}

	public void registerVirtualHW()
	{
		List<VirtOptionValue> list;
		// none type needs to have a valid value; it takes the value of AC97; if value is left null or empty vm will not start because value is not valid
		// TODO: Maybe just remove the entire section from the XML? Same for ethernet...
		list = new ArrayList<>();
		list.add( new VBoxSoundCardModel( "AC97", SoundCard.NONE ) );
		list.add( new VBoxSoundCardModel( "SB16", SoundCard.SOUND_BLASTER ) );
		list.add( new VBoxSoundCardModel( "HDA", SoundCard.HD_AUDIO ) );
		list.add( new VBoxSoundCardModel( "AC97", SoundCard.AC ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.SOUND_CARD_MODEL, list ) );

		list = new ArrayList<>();
		list.add( new VBoxAccel3D( "true", "3D" ) );
		list.add( new VBoxAccel3D( "false", "2D" ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.GFX_TYPE, list ) );

		list = new ArrayList<>();
		list.add( new VBoxNicModel( 0, "", Ethernet.NONE ) );
		list.add( new VBoxNicModel( 0, "Am79C970A", Ethernet.PCNETPCI2 ) );
		list.add( new VBoxNicModel( 0, "Am79C973", Ethernet.PCNETFAST3 ) );
		list.add( new VBoxNicModel( 0, "82540EM", Ethernet.PRO1000MTD ) );
		list.add( new VBoxNicModel( 0, "82543GC", Ethernet.PRO1000TS ) );
		list.add( new VBoxNicModel( 0, "82545EM", Ethernet.PRO1000MTS ) );
		list.add( new VBoxNicModel( 0, "virtio", Ethernet.PARAVIRT ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.NIC_MODEL, list ) );

		list = new ArrayList<>();
		list.add( new VBoxUsbSpeed( null, Usb.NONE ) );
		list.add( new VBoxUsbSpeed( "OHCI", Usb.USB1_1 ) );
		list.add( new VBoxUsbSpeed( "EHCI", Usb.USB2_0 ) );
		list.add( new VBoxUsbSpeed( "XHCI", Usb.USB3_0 ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.USB_SPEED, list ) );
	}

	@Override
	public boolean addEthernet( VirtualizationConfiguration.EtherType type )
	{
		Node hostOnlyInterfaceNode = config.addNewNode( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']",
				"HostOnlyInterface" );
		if ( hostOnlyInterfaceNode == null ) {
			LOGGER.error( "Failed to create node for HostOnlyInterface." );
			return false;
		}
		return config.addAttributeToNode( hostOnlyInterfaceNode, "name", EthernetType.valueOf( type.name() ).vnet );
	}

	@Override
	public void transformNonPersistent() throws VirtualizationConfigurationException
	{
		// Cannot disable suspend
		// https://forums.virtualbox.org/viewtopic.php?f=6&t=77169
		// https://forums.virtualbox.org/viewtopic.php?f=8&t=80338
		// But some other stuff that won't make sense in non-persistent mode
		config.setExtraData( "GUI/LastCloseAction", "PowerOff" );
		// Could use "Default" instead of "Last" above, but you won't get any confirmation dialog in that case
		config.setExtraData( "GUI/RestrictedRuntimeHelpMenuActions", "All" );
		config.setExtraData( "GUI/RestrictedRuntimeMachineMenuActions", "TakeSnapshot,Pause,SaveState" );
		config.setExtraData( "GUI/RestrictedRuntimeMenus", "Help" );
		config.setExtraData( "GUI/PreventSnapshotOperations", "true" );
		config.setExtraData( "GUI/PreventApplicationUpdate", "true" );
		config.setExtraData( "GUI/RestrictedCloseActions", "SaveState,PowerOffRestoringSnapshot,Detach" );

		this.removeEnhancedNetworkAdapters();
	}
	
	class VBoxUsbSpeed extends VirtOptionValue
	{

		public VBoxUsbSpeed( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			// Wipe existing ones
			config.removeNodes( "/VirtualBox/Machine/Hardware", "USB" );
			if ( Util.isEmptyString( this.id ) ) {
				// Add marker so we know it's not an old config and we really want no USB
				Element node = config.createNodeRecursive( "/VirtualBox/OpenSLX/USB" );
				if ( node != null ) {
					node.setAttribute( "disabled", "true" );
				}
				return; // NO USB
			}
			Element node = config.createNodeRecursive( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller" );
			node.setAttribute( "type", this.id );
			node.setAttribute( "name", this.id );
			if ( this.id.equals( "EHCI" ) ) { // XXX "mechanically" ported, could make a special class for this special case
				// If EHCI (2.0) is selected, VBox adds an OHCI controller too...
				// XXX Isn't this broken anyways, it's working on the same node as above *facepalm*
				node.setAttribute( "type", "OHCI" );
				node.setAttribute( "name", "OHCI" );
			}
		}

		@Override
		public boolean isActive()
		{
			// XXX not technically correct wrt max speed
			NodeList nodes = config.findNodes( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller/@type" );
			for ( int i = 0; i < nodes.getLength(); ++i ) {
				if ( nodes.item( i ).getNodeType() != Node.ATTRIBUTE_NODE ) {
					LOGGER.info( "Not ATTRIBUTE type" );
					continue;
				}
				String type = ( (Attr)nodes.item( i ) ).getValue();
				if ( type.equals( this.id ) )
					return true;
			}
			return Util.isEmptyString( this.id );
		}

	}

	@Override
	public String getFileNameExtension()
	{
		return VirtualizationConfigurationVirtualBox.FILE_NAME_EXTENSION;
	}

	@Override
	public void validate() throws VirtualizationConfigurationException
	{
		this.config.validate();
	}
}
