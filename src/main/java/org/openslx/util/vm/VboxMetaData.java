package org.openslx.util.vm;

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
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.vm.VboxConfig.PlaceHolder;
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

class VBoxHWVersionMeta
{
	public final int version;

	public VBoxHWVersionMeta( int vers )
	{
		version = vers;
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

public class VboxMetaData extends VmMetaData<VBoxSoundCardMeta, VBoxDDAccelMeta, VBoxHWVersionMeta, VBoxEthernetDevTypeMeta, VBoxUsbSpeedMeta>
{
	private static final Logger LOGGER = Logger.getLogger( VboxMetaData.class );

	private static final Virtualizer virtualizer = new Virtualizer( TConst.VIRT_VIRTUALBOX, "VirtualBox" );

	private final VboxConfig config;

	public static enum EthernetType
	{
		NAT( "vboxnet1" ), BRIDGED( "vboxnet0" ), HOST_ONLY( "vboxnet2" );

		public final String vnet;

		private EthernetType( String vnet )
		{
			this.vnet = vnet;
		}
	}

	public VboxMetaData( List<OperatingSystem> osList, File file ) throws IOException, UnsupportedVirtualizerFormatException
	{
		super( osList );
		this.config = new VboxConfig( file );
		init();
	}

	public VboxMetaData( List<OperatingSystem> osList, byte[] vmContent, int length ) throws IOException, UnsupportedVirtualizerFormatException
	{
		super( osList );
		this.config = new VboxConfig( vmContent, length );
		init();
	}

	private void init()
	{
		registerVirtualHW();
		displayName = config.getDisplayName();
		setOs( config.getOsName() );
		this.isMachineSnapshot = config.isMachineSnapshot();
		for ( HardDisk hardDisk : config.getHdds() ) {
			hdds.add( hardDisk );
		}
	}

	@Override
	public Virtualizer getVirtualizer()
	{
		return virtualizer;
	}

	@Override
	public void applySettingsForLocalEdit()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] getDefinitionArray()
	{
		return config.toString( false ).getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public byte[] getFilteredDefinitionArray()
	{
		return config.toString( false ).getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public boolean addHddTemplate( String diskImage, String hddMode, String redoDir )
	{
		config.changeAttribute( "/VirtualBox/Machine/MediaRegistry/HardDisks/HardDisk[@location='" + PlaceHolder.HDDLOCATION.toString() + "']", "location", diskImage );
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
		config.changeAttribute( "/VirtualBox/Machine/StorageControllers/StorageController/AttachedDevice/Image", "uuid", vboxUUid );

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
		if ( config.addNewNode( "/VirtualBox/Machine/Hardware/Network/Adapter", "NAT" ) == null ) {
			LOGGER.error( "Failed to set network adapter to NAT." );
			return false;
		}
		return config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter", "MACAddress", "080027B86D12" );
	}

	@Override
	public void setOs( String vendorOsId )
	{
		config.changeAttribute( "/VirtualBox/Machine", "OSType", vendorOsId );
		setOs( TConst.VIRT_VIRTUALBOX, vendorOsId );
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
		NodeList matches = (NodeList)config.findNodes( "/VirtualBox/Machine/StorageControllers/StorageController[@name='Floppy']" );
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
			floppyImage.setAttribute( "uuid", VboxConfig.PlaceHolder.FLOPPYUUID.toString() );
			// register the image in the media registry
			Element floppyImages = (Element)config.addNewNode( "/VirtualBox/Machine/MediaRegistry", "FloppyImages" );
			if ( floppyImages == null ) {
				LOGGER.error( "Failed to add <FloppyImages> to media registry." );
				return;
			}
			Element floppyImageReg = (Element)config.addNewNode( "/VirtualBox/Machine/MediaRegistry/FloppyImages", "Image" );
			if ( floppyImageReg == null ) {
				LOGGER.error( "Failed to add <Image> to floppy images in the media registry." );
				return;
			}
			floppyImageReg.setAttribute( "uuid", VboxConfig.PlaceHolder.FLOPPYUUID.toString() );
			floppyImageReg.setAttribute( "location", VboxConfig.PlaceHolder.FLOPPYLOCATION.toString() );
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
	public void setSoundCard( org.openslx.util.vm.VmMetaData.SoundCardType type )
	{
		VBoxSoundCardMeta sound = soundCards.get( type );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "enabled", Boolean.toString( sound.isPresent ) );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/AudioAdapter", "controller", sound.value );
	}

	@Override
	public VmMetaData.SoundCardType getSoundCard()
	{
		// initialize here to type None to avoid all null pointer exceptions thrown for unknown user written sound cards
		VmMetaData.SoundCardType returnsct = VmMetaData.SoundCardType.NONE;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/AudioAdapter" ).item( 0 );
		if ( !x.hasAttribute( "enabled" ) || ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equals( "false" ) ) ) {
			return returnsct;
		} else {
			// extra separate case for the non-existing argument}
			if ( !x.hasAttribute( "controller" ) ) {
				returnsct = VmMetaData.SoundCardType.AC;
			} else {
				String controller = x.getAttribute( "controller" );
				VBoxSoundCardMeta soundMeta = null;
				for ( VmMetaData.SoundCardType type : VmMetaData.SoundCardType.values() ) {
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
	public void setDDAcceleration( VmMetaData.DDAcceleration type )
	{
		VBoxDDAccelMeta accel = ddacc.get( type );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Display", "accelerate3D", Boolean.toString( accel.isPresent ) );
	}

	@Override
	public VmMetaData.DDAcceleration getDDAcceleration()
	{
		VmMetaData.DDAcceleration returndda = null;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Display" ).item( 0 );
		if ( x.hasAttribute( "accelerate3D" ) ) {
			if ( x.getAttribute( "accelerate3D" ).equals( "true" ) ) {
				returndda = VmMetaData.DDAcceleration.ON;
			} else {
				returndda = VmMetaData.DDAcceleration.OFF;
			}
		} else {
			returndda = VmMetaData.DDAcceleration.OFF;
		}
		return returndda;
	}

	/**
	 * Function does nothing for Virtual Box;
	 * Virtual Box accepts per default only one hardware version and is hidden from the user
	 */
	@Override
	public void setHWVersion( HWVersion type )
	{
	}

	@Override
	public VmMetaData.HWVersion getHWVersion()
	{
		// Virtual Box uses only one virtual hardware version and can't be changed
		return VmMetaData.HWVersion.DEFAULT;
	}

	@Override
	public void setEthernetDevType( int cardIndex, EthernetDevType type )
	{
		String index = "0";
		VBoxEthernetDevTypeMeta nic = networkCards.get( type );
		// cardIndex is not used yet...maybe later needed for different network cards
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "enabled", Boolean.toString( nic.isPresent ) );
		config.changeAttribute( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='" + index + "']", "type", nic.value );
	}

	@Override
	public VmMetaData.EthernetDevType getEthernetDevType( int cardIndex )
	{
		VmMetaData.EthernetDevType returnedt = VmMetaData.EthernetDevType.NONE;
		Element x = (Element)config.findNodes( "/VirtualBox/Machine/Hardware/Network/Adapter" ).item( 0 );
		if ( !x.hasAttribute( "enabled" ) || ( x.hasAttribute( "enabled" ) && x.getAttribute( "enabled" ).equals( "false" ) ) ) {
			return returnedt;
		} else {
			// extra separate case for the non-existing argument}
			if ( !x.hasAttribute( "type" ) ) {
				returnedt = VmMetaData.EthernetDevType.PCNETFAST3;
			} else {
				String temp = x.getAttribute( "type" );
				VBoxEthernetDevTypeMeta etherMeta = null;
				for ( VmMetaData.EthernetDevType type : VmMetaData.EthernetDevType.values() ) {
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
		soundCards.put( VmMetaData.SoundCardType.NONE, new VBoxSoundCardMeta( false, "AC97" ) );
		soundCards.put( VmMetaData.SoundCardType.SOUND_BLASTER, new VBoxSoundCardMeta( true, "SB16" ) );
		soundCards.put( VmMetaData.SoundCardType.HD_AUDIO, new VBoxSoundCardMeta( true, "HDA" ) );
		soundCards.put( VmMetaData.SoundCardType.AC, new VBoxSoundCardMeta( true, "AC97" ) );

		ddacc.put( VmMetaData.DDAcceleration.OFF, new VBoxDDAccelMeta( false ) );
		ddacc.put( VmMetaData.DDAcceleration.ON, new VBoxDDAccelMeta( true ) );

		hwversion.put( VmMetaData.HWVersion.DEFAULT, new VBoxHWVersionMeta( 0 ) );

		// none type needs to have a valid value; it takes the value of pcnetcpi2; if value is left null or empty vm will not start because value is not valid
		networkCards.put( VmMetaData.EthernetDevType.NONE, new VBoxEthernetDevTypeMeta( false, "Am79C970A" ) );
		networkCards.put( VmMetaData.EthernetDevType.PCNETPCI2, new VBoxEthernetDevTypeMeta( true, "Am79C970A" ) );
		networkCards.put( VmMetaData.EthernetDevType.PCNETFAST3, new VBoxEthernetDevTypeMeta( true, "Am79C973" ) );
		networkCards.put( VmMetaData.EthernetDevType.PRO1000MTD, new VBoxEthernetDevTypeMeta( true, "82540EM" ) );
		networkCards.put( VmMetaData.EthernetDevType.PRO1000TS, new VBoxEthernetDevTypeMeta( true, "82543GC" ) );
		networkCards.put( VmMetaData.EthernetDevType.PRO1000MTS, new VBoxEthernetDevTypeMeta( true, "82545EM" ) );
		networkCards.put( VmMetaData.EthernetDevType.PARAVIRT, new VBoxEthernetDevTypeMeta( true, "virtio" ) );
		
		usbSpeeds.put( VmMetaData.UsbSpeed.NONE, new VBoxUsbSpeedMeta( null, 0 ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB1_1, new VBoxUsbSpeedMeta( "OHCI", 1 ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB2_0, new VBoxUsbSpeedMeta( "EHCI", 2 ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB3_0, new VBoxUsbSpeedMeta( "XHCI", 3 ) );
	}

	@Override
	public boolean addEthernet( VmMetaData.EtherType type )
	{
		Node hostOnlyInterfaceNode = config.addNewNode( "/VirtualBox/Machine/Hardware/Network/Adapter[@slot='0']", "HostOnlyInterface" );
		if ( hostOnlyInterfaceNode == null ) {
			LOGGER.error( "Failed to create node for HostOnlyInterface." );
			return false;
		}
		return config.addAttributeToNode( hostOnlyInterfaceNode, "name", EthernetType.valueOf( type.name() ).vnet );
	}

	@Override
	public boolean disableSuspend()
	{
		// TODO how??
		// short answer is: you can't
		// https://forums.virtualbox.org/viewtopic.php?f=6&t=77169
		// https://forums.virtualbox.org/viewtopic.php?f=8&t=80338
		return true;
	}

	@Override
	public void setMaxUsbSpeed( VmMetaData.UsbSpeed speed )
	{
		// Wipe existing ones
		config.removeNodes( "/VirtualBox/Machine/Hardware", "USB" );
		if ( speed == null || speed == VmMetaData.UsbSpeed.NONE ) {
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
	public VmMetaData.UsbSpeed getMaxUsbSpeed()
	{
		NodeList nodes = config.findNodes( "/VirtualBox/Machine/Hardware/USB/Controllers/Controller/@type" );
		int maxSpeed = 0;
		VmMetaData.UsbSpeed maxItem = VmMetaData.UsbSpeed.NONE;
		for ( int i = 0; i < nodes.getLength(); ++i ) {
			if ( nodes.item( i ).getNodeType() != Node.ATTRIBUTE_NODE ) {
				LOGGER.info( "Not ATTRIBUTE type" );
				continue;
			}
			String type = ((Attr)nodes.item( i )).getValue();
			for ( Entry<VmMetaData.UsbSpeed, VBoxUsbSpeedMeta> s : usbSpeeds.entrySet() ) {
				if ( s.getValue().speed > maxSpeed && type.equals( s.getValue().value ) ) {
					maxSpeed = s.getValue().speed;
					maxItem = s.getKey();
				}
			}
		}
		return maxItem;
	}
}
