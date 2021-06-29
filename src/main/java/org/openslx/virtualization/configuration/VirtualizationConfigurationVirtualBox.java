package org.openslx.virtualization.configuration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfigurationVirtualboxFileFormat.PlaceHolder;
import org.openslx.virtualization.virtualizer.VirtualizerVirtualBox;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class VBoxSoundCardMeta
{
	public final boolean isPresent;
	public final String value;

	public VBoxSoundCardMeta( boolean present, String val )
	{
		isPresent = present;
		value = val;
	}
}

class VBoxDDAccelMeta
{
	public final boolean isPresent;

	public VBoxDDAccelMeta( boolean present )
	{
		isPresent = present;
	}
}

class VBoxEthernetDevTypeMeta
{
	public final String value;
	public final boolean isPresent;

	public VBoxEthernetDevTypeMeta( boolean present, String val )
	{
		value = val;
		isPresent = present;
	}
}

class VBoxUsbSpeedMeta
{
	public final String value;
	public final int speed;

	public VBoxUsbSpeedMeta( String value, int speed )
	{
		this.value = value;
		this.speed = speed;
	}
}

public class VirtualizationConfigurationVirtualBox
		extends VirtualizationConfiguration<VBoxSoundCardMeta, VBoxDDAccelMeta, VBoxEthernetDevTypeMeta, VBoxUsbSpeedMeta>
{
	/**
	 * File name extension for VirtualBox virtualization configuration files..
	 */
	public static final String FILE_NAME_EXTENSION = "vbox";

	private static final Logger LOGGER = Logger.getLogger( VirtualizationConfigurationVirtualBox.class );

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

	@Override
	public void setSoundCard( org.openslx.virtualization.configuration.VirtualizationConfiguration.SoundCardType type )
	{
		VBoxSoundCardMeta sound = soundCards.get( type );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "enabled",
				Boolean.toString( sound.isPresent ) );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "controller", sound.value );
	}

	@Override
	public VirtualizationConfiguration.SoundCardType getSoundCard()
	{
		// initialize here to type None to avoid all null pointer exceptions thrown for unknown user written sound cards
		VirtualizationConfiguration.SoundCardType returnsct = VirtualizationConfiguration.SoundCardType.NONE;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/AudioAdapter" ).item( 0 );
		if ( !x.hasAttribute( "enabled" )
				|| ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equals( "false" ) ) ) {
			return returnsct;
		} else {
			// extra separate case for the non-existing argument}
			if ( !x.hasAttribute( "controller" ) ) {
				returnsct = VirtualizationConfiguration.SoundCardType.AC;
			} else {
				String controller = x.getAttribute( "controller" );
				VBoxSoundCardMeta soundMeta = null;
				for ( VirtualizationConfiguration.SoundCardType type : VirtualizationConfiguration.SoundCardType
						.values() ) {
					soundMeta = soundCards.get( type );
					if ( soundMeta != null ) {
						if ( controller.equals( soundMeta.value ) ) {
							returnsct = type;
						}
					}
				}
			}
		}
		return returnsct;
	}

	@Override
	public void setDDAcceleration( VirtualizationConfiguration.DDAcceleration type )
	{
		VBoxDDAccelMeta accel = ddacc.get( type );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Display", "accelerate3D",
				Boolean.toString( accel.isPresent ) );
	}

	@Override
	public VirtualizationConfiguration.DDAcceleration getDDAcceleration()
	{
		VirtualizationConfiguration.DDAcceleration returndda = null;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Display" ).item( 0 );
		if ( x.hasAttribute( "accelerate3D" ) ) {
			if ( x.getAttribute( "accelerate3D" ).equals( "true" ) ) {
				returndda = VirtualizationConfiguration.DDAcceleration.ON;
			} else {
				returndda = VirtualizationConfiguration.DDAcceleration.OFF;
			}
		} else {
			returndda = VirtualizationConfiguration.DDAcceleration.OFF;
		}
		return returndda;
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

	@Override
	public void setEthernetDevType( int cardIndex, EthernetDevType type )
	{
		String index = "0";
		VBoxEthernetDevTypeMeta nic = networkCards.get( type );
		// cardIndex is not used yet...maybe later needed for different network cards
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "enabled",
				Boolean.toString( nic.isPresent ) );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "type",
				nic.value );
	}

	@Override
	public VirtualizationConfiguration.EthernetDevType getEthernetDevType( int cardIndex )
	{
		VirtualizationConfiguration.EthernetDevType returnedt = VirtualizationConfiguration.EthernetDevType.NONE;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter" ).item( 0 );
		if ( !x.hasAttribute( "enabled" )
				|| ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equals( "false" ) ) ) {
			return returnedt;
		} else {
			// extra separate case for the non-existing argument}
			if ( !x.hasAttribute( "type" ) ) {
				returnedt = VirtualizationConfiguration.EthernetDevType.PCNETFAST3;
			} else {
				String temp = x.getAttribute( "type" );
				VBoxEthernetDevTypeMeta etherMeta = null;
				for ( VirtualizationConfiguration.EthernetDevType type : VirtualizationConfiguration.EthernetDevType
						.values() ) {
					etherMeta = networkCards.get( type );
					if ( etherMeta != null ) {
						if ( temp.equals( etherMeta.value ) ) {
							returnedt = type;
						}
					}
				}
			}
		}
		return returnedt;
	}

	public void registerVirtualHW()
	{
		// none type needs to have a valid value; it takes the value of AC97; if value is left null or empty vm will not start because value is not valid
		// TODO: Maybe just remove the entire section from the XML? Same for ethernet...
		soundCards.put( VirtualizationConfiguration.SoundCardType.NONE, new VBoxSoundCardMeta( false, "AC97" ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.SOUND_BLASTER, new VBoxSoundCardMeta( true, "SB16" ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.HD_AUDIO, new VBoxSoundCardMeta( true, "HDA" ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.AC, new VBoxSoundCardMeta( true, "AC97" ) );

		ddacc.put( VirtualizationConfiguration.DDAcceleration.OFF, new VBoxDDAccelMeta( false ) );
		ddacc.put( VirtualizationConfiguration.DDAcceleration.ON, new VBoxDDAccelMeta( true ) );

		// none type needs to have a valid value; it takes the value of pcnetcpi2; if value is left null or empty vm will not start because value is not valid
		networkCards.put( VirtualizationConfiguration.EthernetDevType.NONE,
				new VBoxEthernetDevTypeMeta( false, "Am79C970A" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PCNETPCI2,
				new VBoxEthernetDevTypeMeta( true, "Am79C970A" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PCNETFAST3,
				new VBoxEthernetDevTypeMeta( true, "Am79C973" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PRO1000MTD,
				new VBoxEthernetDevTypeMeta( true, "82540EM" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PRO1000TS,
				new VBoxEthernetDevTypeMeta( true, "82543GC" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PRO1000MTS,
				new VBoxEthernetDevTypeMeta( true, "82545EM" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PARAVIRT,
				new VBoxEthernetDevTypeMeta( true, "virtio" ) );

		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.NONE, new VBoxUsbSpeedMeta( null, 0 ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB1_1, new VBoxUsbSpeedMeta( "OHCI", 1 ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB2_0, new VBoxUsbSpeedMeta( "EHCI", 2 ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB3_0, new VBoxUsbSpeedMeta( "XHCI", 3 ) );
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

	@Override
	public void setMaxUsbSpeed( VirtualizationConfiguration.UsbSpeed speed )
	{
		// Wipe existing ones
		config.removeNodes( "/VirtualBox/Machine/Hardware", "USB" );
		if ( speed == null || speed == VirtualizationConfiguration.UsbSpeed.NONE ) {
			// Add marker so we know it's not an old config and we really want no USB
			Element node = config.createNodeRecursive( "/VirtualBox/OpenSLX/USB" );
			if ( node != null ) {
				node.setAttribute( "disabled", "true" );
			}
			return; // NO USB
		}
		Element node = config.createNodeRecursive( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller" );
		VBoxUsbSpeedMeta vboxSpeed = usbSpeeds.get( speed );
		node.setAttribute( "type", vboxSpeed.value );
		node.setAttribute( "name", vboxSpeed.value );
		if ( speed == UsbSpeed.USB2_0 ) {
			// If EHCI (2.0) is selected, VBox adds an OHCI controller too...
			node.setAttribute( "type", "OHCI" );
			node.setAttribute( "name", "OHCI" );
		}
	}

	@Override
	public VirtualizationConfiguration.UsbSpeed getMaxUsbSpeed()
	{
		NodeList nodes = config.findNodes( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller/@type" );
		int maxSpeed = 0;
		VirtualizationConfiguration.UsbSpeed maxItem = VirtualizationConfiguration.UsbSpeed.NONE;
		for ( int i = 0; i < nodes.getLength(); ++i ) {
			if ( nodes.item( i ).getNodeType() != Node.ATTRIBUTE_NODE ) {
				LOGGER.info( "Not ATTRIBUTE type" );
				continue;
			}
			String type = ( (Attr)nodes.item( i ) ).getValue();
			for ( Entry<VirtualizationConfiguration.UsbSpeed, VBoxUsbSpeedMeta> s : usbSpeeds.entrySet() ) {
				if ( s.getValue().speed > maxSpeed && type.equals( s.getValue().value ) ) {
					maxSpeed = s.getValue().speed;
					maxItem = s.getKey();
				}
			}
		}
		return maxItem;
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
