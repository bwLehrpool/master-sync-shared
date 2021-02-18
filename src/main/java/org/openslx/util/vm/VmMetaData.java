package org.openslx.util.vm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;

/**
 * Describes a configured virtual machine. This class is parsed from a machine
 * description, like a *.vmx for VMware machines.
 */
public abstract class VmMetaData<T, U, V, W, X>
{
	private static final Logger LOGGER = Logger.getLogger( VmMetaData.class );

	/*
	 * Helper types
	 */
	protected Map<SoundCardType, T> soundCards = new HashMap<>();
	protected Map<DDAcceleration, U> ddacc = new HashMap<>();
	protected Map<HWVersion, V> hwversion = new HashMap<>();
	protected Map<EthernetDevType, W> networkCards = new HashMap<>();
	protected Map<UsbSpeed, X> usbSpeeds = new HashMap<>();

	/**
	 * Virtual sound cards types
	 */
	public static enum SoundCardType
	{
		NONE( "None" ), DEFAULT( "(default)" ), SOUND_BLASTER( "Sound Blaster 16" ), ES( "ES 1371" ), HD_AUDIO( "Intel Integrated HD Audio" ), AC( "Intel ICH Audio Codec 97" );

		public final String displayName;

		private SoundCardType( String dName )
		{
			this.displayName = dName;
		}
	}

	/**
	 * 3D acceleration types
	 */
	public static enum DDAcceleration
	{
		OFF( "Off" ), ON( "On" );

		public final String displayName;

		private DDAcceleration( String dName )
		{
			this.displayName = dName;
		}
	}

	/**
	 * Virtual hardware version - currently only in use for VMPlayer
	 */
	public static enum HWVersion
	{
		NONE(  "(invalid)" ),
		THREE( "  3 (Workstation 4/5, Player 1)" ),
		FOUR(  "  4 (Workstation 4/5, Player 1/2, Fusion 1)" ),
		SIX(   "  6 (Workstation 6)" ),
		SEVEN( "  7 (Workstation 6.5/7, Player 3, Fusion 2/3)" ),
		EIGHT( "  8 (Workstation 8, Player/Fusion 4)" ),
		NINE( "  9 (Workstation 9, Player/Fusion 5)" ),
		TEN( "10 (Workstation 10, Player/Fusion 6)" ),
		ELEVEN( "11 (Workstation 11, Player/Fusion 7)" ),
		TWELVE( "12 (Workstation/Player 12, Fusion 8)" ),
		FOURTEEN( "14 (Workstation/Player 14, Fusion 10)"),
		FIFTEEN( "15 (Workstation/Player 15, Fusion 11)"),
		FIFTEEN_ONE( "16 (Workstation/Player 15.1, Fusion 11.1)"),
		DEFAULT( "default" );

		public final String displayName;

		private HWVersion( String dName )
		{
			this.displayName = dName;
		}
	}

	/**
	 * Virtual network cards
	 */
	public static enum EthernetDevType
	{
		AUTO( "(default)" ), PCNET32( "AMD PCnet32" ), E1000( "Intel E1000 (PCI)" ), E1000E( "Intel E1000e (PCI-Express)" ), VMXNET( "VMXnet" ), VMXNET3( "VMXnet 3" ), PCNETPCI2(
				"PCnet-PCI II" ), PCNETFAST3( "PCnet-FAST III" ), PRO1000MTD( "Intel PRO/1000 MT Desktop" ), PRO1000TS(
						"Intel PRO/1000 T Server" ), PRO1000MTS( "Intel PRO/1000 MT Server" ), PARAVIRT( "Paravirtualized Network" ), NONE( "No Network Card" );

		public final String displayName;

		private EthernetDevType( String dName )
		{
			this.displayName = dName;
		}
	}

	public static enum UsbSpeed
	{
		NONE( "None" ),
		USB1_1( "USB 1.1" ),
		USB2_0( "USB 2.0" ),
		USB3_0( "USB 3.0" );

		public final String displayName;

		private UsbSpeed( String dName )
		{
			this.displayName = dName;
		}
	}

	public static enum DriveBusType
	{
		SCSI, IDE, SATA;
	}

	public static class HardDisk
	{
		public final String chipsetDriver;
		public final DriveBusType bus;
		public final String diskImage;

		public HardDisk( String chipsetDriver, DriveBusType bus, String diskImage )
		{
			this.chipsetDriver = chipsetDriver;
			this.bus = bus;
			this.diskImage = diskImage;
		}
	}

	public static enum EtherType
	{
		NAT, BRIDGED, HOST_ONLY;
	}
	/*
	 * Members
	 */

	protected final List<HardDisk> hdds = new ArrayList<>();

	private final List<OperatingSystem> osList;

	private OperatingSystem os = null;

	protected String displayName = null;

	protected boolean isMachineSnapshot;

	/*
	 * Getters for virtual hardware
	 */
	public List<SoundCardType> getSupportedSoundCards()
	{
		ArrayList<SoundCardType> availables = new ArrayList<SoundCardType>( soundCards.keySet() );
		Collections.sort( availables );
		return availables;
	}

	public List<DDAcceleration> getSupportedDDAccs()
	{
		ArrayList<DDAcceleration> availables = new ArrayList<DDAcceleration>( ddacc.keySet() );
		Collections.sort( availables );
		return availables;
	}

	public List<HWVersion> getSupportedHWVersions()
	{
		ArrayList<HWVersion> availables = new ArrayList<HWVersion>( hwversion.keySet() );
		Collections.sort( availables );
		return availables;
	}

	public List<EthernetDevType> getSupportedEthernetDevices()
	{
		ArrayList<EthernetDevType> availables = new ArrayList<EthernetDevType>( networkCards.keySet() );
		Collections.sort( availables );
		return availables;
	}

	public List<UsbSpeed> getSupportedUsbSpeeds()
	{
		ArrayList<UsbSpeed> availables = new ArrayList<>( usbSpeeds.keySet() );
		Collections.sort( availables );
		return availables;
	}

	/**
	 * Get operating system of this VM.
	 */
	public OperatingSystem getOs()
	{
		return os;
	}

	/**
	 * Get all hard disks of this VM.
	 */
	public List<HardDisk> getHdds()
	{
		return Collections.unmodifiableList( hdds );
	}

	/**
	 * Get display name of VM.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/*
	 * Getter for isMachineSnapshot
	 */
	public boolean isMachineSnapshot()
	{
		return isMachineSnapshot;
	}

	/**
	 * This method should return a minimal representation of the input meta data.
	 * The representation is platform dependent, and should be stripped of all
	 * non-essential configuration, such as CD/DVD/FLoppy drives, serial or parallel
	 * ports, shared folders, or anything else that could be considered sensible
	 * information (absolute paths containing the local user's name).
	 */
	public abstract byte[] getFilteredDefinitionArray();

	public final ByteBuffer getFilteredDefinition()
	{
		return ByteBuffer.wrap( getFilteredDefinitionArray() );
	}

	/*
	 * Methods
	 */

	public VmMetaData( List<OperatingSystem> osList )
	{
		this.osList = osList;
	}

	/**
	 * Called from subclass to set the OS. If the OS cannot be determined from the
	 * given parameters, it will not be set.
	 *
	 * @param virtId
	 *           virtualizer, eg "vmware" for VMware
	 * @param virtOsId
	 *           the os identifier used by the virtualizer, eg. windows7-64 for
	 *           64bit Windows 7 on VMware
	 */
	protected final void setOs( String virtId, String virtOsId )
	{
		OperatingSystem lazyMatch = null;
		for ( OperatingSystem os : osList ) {
			if ( os.getVirtualizerOsId() == null )
				continue;
			for ( Entry<String, String> entry : os.getVirtualizerOsId().entrySet() ) {
				if ( !entry.getValue().equals( virtOsId ) )
					continue;
				if ( entry.getKey().equals( virtId ) ) {
					this.os = os;
					return;
				} else {
					lazyMatch = os;
				}
			}
		}
		this.os = lazyMatch;
	}

	/**
	 * Returns list of image formats supported by the VM's hypervisor.
	 * 
	 * @return list of image formats.
	 */
	public abstract List<DiskImage.ImageFormat> getSupportedImageFormats();
	
	/**
	 * Apply config options that are desired when locally editing a VM. for vmware,
	 * this disables automatic DPI scaling of the guest.
	 */
	public abstract void applySettingsForLocalEdit();

	/**
	 * Returns a VmMetaData instance of the given machine description given as file
	 *
	 * @param osList List of supported operating systems
	 * @param file VM's machine description file to get the metadata instance from
	 * @return VmMetaData object representing the relevant parts of the given machine description
	 */
	public static VmMetaData<?, ?, ?, ?, ?> getInstance( List<OperatingSystem> osList, File file )
			throws IOException
	{
		try {
			return new VmwareMetaData( osList, file );
		} catch ( UnsupportedVirtualizerFormatException e ) {
			LOGGER.info( "Not a VMware file", e );
		}
		try {
			return new VboxMetaData( osList, file );
		} catch ( UnsupportedVirtualizerFormatException e ) {
			LOGGER.info( "Not a VirtualBox file", e );
		}
		try {
			return new QemuMetaData( osList, file );
		} catch ( Exception e ) {
			LOGGER.info( "Not a Qemu file", e );
		}
		try {
			return new DockerMetaDataDummy(osList, file);
		} catch ( Exception e ) {
			LOGGER.info( "Not a tar.gz file, for docker container", e );
		}
		LOGGER.error( "Could not detect any known virtualizer format" );
		return null;
	}

	/**
	 * Returns a VmMetaData instance of the given machine description given as a byte array
	 *
	 * @param osList List of supported operating systems
	 * @param vmContent VM's machine description as byte array (e.g. stored in DB)
	 * @param length length of the byte array given as vmContent
	 * @return VmMetaData object representing the relevant parts of the given machine description
	 * @throws IOException
	 */
	public static VmMetaData<?, ?, ?, ?, ?> getInstance( List<OperatingSystem> osList, byte[] vmContent, int length ) throws IOException
	{
		Map<String, Exception> exceptions = new HashMap<>();
		try {
			return new VmwareMetaData( osList, vmContent, length );
		} catch ( UnsupportedVirtualizerFormatException e ) {
			exceptions.put( "Not a VMware file", e );
		}
		try {
			return new VboxMetaData( osList, vmContent, length );
		} catch ( UnsupportedVirtualizerFormatException e ) {
			exceptions.put( "Not a VirtualBox file", e );
		}
		try {
			return new DockerMetaDataDummy(osList, vmContent, length);
		} catch (UnsupportedVirtualizerFormatException e) {
			exceptions.put( "Not tar.gz file for DockerMetaDataDummy ", e);
		}
		// TODO QEmu -- hack above expects qcow2 file, so we can't do anything here yet
		LOGGER.error( "Could not detect any known virtualizer format" );
		for ( Entry<String, Exception> e : exceptions.entrySet() ) {
			LOGGER.error( e.getKey(), e.getValue() );
		}
		return null;
	}

	public abstract boolean addHddTemplate( File diskImage, String hddMode, String redoDir );

	public abstract boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir );

	public abstract boolean addDefaultNat();

	public abstract void setOs( String vendorOsId );

	public abstract boolean addDisplayName( String name );

	public abstract boolean addRam( int mem );

	public abstract void addFloppy( int index, String image, boolean readOnly );

	public abstract boolean addCdrom( String image );

	public abstract boolean addCpuCoreCount( int nrOfCores );

	public abstract void setSoundCard( SoundCardType type );

	public abstract SoundCardType getSoundCard();

	public abstract void setDDAcceleration( DDAcceleration type );

	public abstract DDAcceleration getDDAcceleration();

	public abstract void setHWVersion( HWVersion type );

	public abstract HWVersion getHWVersion();

	public abstract void setEthernetDevType( int cardIndex, EthernetDevType type );

	public abstract EthernetDevType getEthernetDevType( int cardIndex );

	public abstract void setMaxUsbSpeed( UsbSpeed speed );

	public abstract UsbSpeed getMaxUsbSpeed();

	public abstract byte[] getDefinitionArray();

	public abstract boolean addEthernet( EtherType type );

	public abstract Virtualizer getVirtualizer();

	public abstract boolean tweakForNonPersistent();

	/**
	 * Function used to register virtual devices
	 */
	public abstract void registerVirtualHW();
}
