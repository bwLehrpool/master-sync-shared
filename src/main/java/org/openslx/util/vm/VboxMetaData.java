package org.openslx.util.vm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

public class VboxMetaData extends VmMetaData<VBoxSoundCardMeta, VBoxDDAccelMeta, VBoxHWVersionMeta, VBoxEthernetDevTypeMeta>
{
	private static final Logger LOGGER = Logger.getLogger( VboxMetaData.class );

	private static final Virtualizer virtualizer = new Virtualizer( "virtualbox", "VirtualBox" );

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

		this.config.init();
		displayName = config.getDisplayName();
		setOs( "virtualbox", config.getOsName() );

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
	public void enableUsb( boolean enabled )
	{
		if ( !enabled ) {
			config.disableUsb();
		} else {
			config.enableUsb();
		}
	}

	@Override
	public void applySettingsForLocalEdit()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] getFilteredDefinitionArray()
	{
		return config.toString().getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public boolean addHddTemplate( String diskImage, String hddMode, String redoDir )
	{
		config.changeAttribute( "HardDisk", "location", diskImage );
		config.changeAttribute( "Machine", "snapshotFolder", redoDir );
		return true;
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		String diskImagePath = diskImage.getName();
		config.changeAttribute( "HardDisk", "location", diskImagePath );

		UUID newhdduuid = UUID.randomUUID();

		// patching the new uuid in the vbox config file here
		String vboxUUid = "{" + newhdduuid.toString() + "}";
		config.changeAttribute( "HardDisk", "uuid", vboxUUid );
		config.changeAttribute( "Image", "uuid", vboxUUid );

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
		config.changeAttribute( "Machine", "uuid", machineUUid );
		return true;
	}

	@Override
	public boolean addDefaultNat()
	{
		config.addNewNode( "Adapter", "NAT", true );
		config.changeAttribute( "Adapter", "MACAddress", "080027B86D12" );
		return true;
	}

	@Override
	public void setOs( String vendorOsId )
	{
		config.changeAttribute( "Machine", "OSType", vendorOsId );
		setOs( "virtualbox", vendorOsId );
	}

	@Override
	public boolean addDisplayName( String name )
	{
		config.changeAttribute( "Machine", "name", name );
		return true;
	}

	@Override
	public boolean addRam( int mem )
	{
		config.changeAttribute( "Memory", "RAMSize", Integer.toString( mem ) );
		return true;
	}

	@Override
	public void addFloppy( int index, String image, boolean readOnly )
	{

		Node somenode = config.findNode( "StorageController", "name", "Floppy" );
		if ( somenode == null ) {
			Element controller = (Element)config.addNewNode( "StorageControllers", "StorageController", false );
			controller.setAttribute( "name", "Floppy" );
			controller.setAttribute( "type", "I82078" );
			controller.setAttribute( "PortCount", "1" );
			controller.setAttribute( "useHostIOCache", "true" );
			controller.setAttribute( "Bootable", "true" );
		}

		Element attachedDev = null;

		if ( image == null ) {
			attachedDev = (Element)config.addNewNode( "StorageController", "AttachedDevice", true, "name", "Floppy" );
			LOGGER.warn( "Floppy controller has no image attached" );
		} else {
			attachedDev = (Element)config.addNewNode( "StorageController", "AttachedDevice", false, "name", "Floppy" );

			Element imageTag = (Element)config.addNewNode( "AttachedDevice", "Image", true, "type", "Floppy" );
			imageTag.setAttribute( "uuid", VboxConfig.PlaceHolder.FLOPPYUUID.holderName() );
			config.addNewNode( "MediaRegistry", "FloppyImages", false );
			Element floppyImageTag = (Element)config.addNewNode( "FloppyImages", "Image", true );
			floppyImageTag.setAttribute( "uuid", VboxConfig.PlaceHolder.FLOPPYUUID.holderName() );
			floppyImageTag.setAttribute( "location", VboxConfig.PlaceHolder.FLOPPYLOCATION.holderName() );
		}

		attachedDev.setAttribute( "type", "Floppy" );
		attachedDev.setAttribute( "hotpluggable", "false" );
		attachedDev.setAttribute( "port", "0" );
		attachedDev.setAttribute( "device", Integer.toString( index ) );
	}

	@Override
	public boolean addCdrom( String image )
	{
		return false;
	}

	@Override
	public boolean addCpuCoreCount( int nrOfCores )
	{
		config.changeAttribute( "CPU", "count", Integer.toString( nrOfCores ) );
		return true;
	}

	@Override
	public void setSoundCard( org.openslx.util.vm.VmMetaData.SoundCardType type )
	{
		VBoxSoundCardMeta sound = soundCards.get( type );

		config.changeAttribute( "AudioAdapter", "enabled", Boolean.toString( sound.isPresent ) );
		config.changeAttribute( "AudioAdapter", "controller", sound.value );
	}

	@Override
	public VmMetaData.SoundCardType getSoundCard()
	{
		// initialize here to type None to avoid all null pointer exceptions thrown for unknown user written sound cards
		VmMetaData.SoundCardType returnsct = VmMetaData.SoundCardType.NONE;
		Element x = (Element)config.findNodes( "AudioAdapter" ).item( 0 );
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
		config.changeAttribute( "Display", "accelerate3D", Boolean.toString( accel.isPresent ) );
	}

	@Override
	public VmMetaData.DDAcceleration getDDAcceleration()
	{
		VmMetaData.DDAcceleration returndda = null;
		Element x = (Element)config.findNodes( "Display" ).item( 0 );
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

	@Override
	/**
	 * Function does nothing for Virtual Box;
	 * Virtual Box accepts per default only one hardware version and is hidden from the user
	 */
	public void setHWVersion( HWVersion type )
	{
	}

	@Override
	public VmMetaData.HWVersion getHWVersion()
	{
		VmMetaData.HWVersion returnhwv = null;
		// Virtual Box uses only one virtual hardware version and can't be changed
		returnhwv = VmMetaData.HWVersion.DEFAULT;
		return returnhwv;
	}

	@Override
	public void setEthernetDevType( int cardIndex, EthernetDevType type )
	{
		String index = "0";
		VBoxEthernetDevTypeMeta networkc = networkCards.get( type );
		// cardIndex is not used yet...maybe later needed for different network cards
		config.changeAttribute( "Adapter", "enabled", Boolean.toString( networkc.isPresent ), "slot", index );
		config.changeAttribute( "Adapter", "type", networkc.value, "slot", index );
	}

	@Override
	public VmMetaData.EthernetDevType getEthernetDevType( int cardIndex )
	{
		VmMetaData.EthernetDevType returnedt = VmMetaData.EthernetDevType.NONE;
		Element x = (Element)config.findNodes( "Adapter" ).item( 0 );
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

	@Override
	public byte[] getDefinitionArray()
	{
		return config.toString().getBytes( StandardCharsets.UTF_8 );
	}

	public void registerVirtualHW()
	{
		// none type needs to have a valid value; it takes the value of AC97; if value is left null or empty vm will not start because value is not valid
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
	}

	@Override
	public boolean addEthernet( VmMetaData.EtherType type )
	{
		Node hostOnlyInterfaceNode = config.addNewNode( "Adapter", "HostOnlyInterface", true, "slot", "0" );
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
}