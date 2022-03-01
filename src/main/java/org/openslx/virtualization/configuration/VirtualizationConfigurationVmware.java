package org.openslx.virtualization.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.Util;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfigurationVmwareFileFormat.ConfigEntry;
import org.openslx.virtualization.hardware.VirtOptionValue;
import org.openslx.virtualization.hardware.ConfigurationGroups;
import org.openslx.virtualization.hardware.Ethernet;
import org.openslx.virtualization.hardware.SoundCard;
import org.openslx.virtualization.hardware.Usb;
import org.openslx.virtualization.virtualizer.VirtualizerVmware;

public class VirtualizationConfigurationVmware extends VirtualizationConfiguration
{
	/**
	 * File name extension for VMware virtualization configuration files.
	 */
	public static final String FILE_NAME_EXTENSION = "vmx";

	private static final Logger LOGGER = LogManager.getLogger( VirtualizationConfigurationVmware.class );

	private static final Pattern HDD_PATTERN = Pattern.compile( "^(ide\\d|scsi\\d|sata\\d|nvme\\d):?(\\d?)\\.(.*)",
			Pattern.CASE_INSENSITIVE );

	/** Lowercase regex of allowed settings for stateless execution */
	private static final Pattern STATELESS_WHITELIST_PATTERN;

	/** Lowercase regex of forbidden settings when uploading (privacy concerns) */
	private static final Pattern PRIVACY_BLACKLIST_PATTERN;

	private final VirtualizationConfigurationVmwareFileFormat config;

	// Init static members
	static {
		// LOWERCASE - Client execution whitelist
		String[] list1 = { "^guestos", "^uuid\\.bios", "^config\\.version", "^ehci[.:]", "^mks\\.enable3d",
				"^virtualhw\\.",
				"^sound[.:]", "\\.pcislotnumber$", "^pcibridge", "\\.virtualdev$", "^tools\\.syncTime$",
				"^time\\.synchronize",
				"^bios\\.bootDelay", "^rtc\\.", "^xhci[.:]", "^usb_xhci[.:]", "\\.deviceType$", "\\.port$", "\\.parent$",
				"^usb[.:]",
				"^firmware", "^hpet", "^vm\\.genid",
				"^svga\\.graphicsMemoryKB$" };
		STATELESS_WHITELIST_PATTERN = Pattern.compile( String.join( "|", list1 ), Pattern.CASE_INSENSITIVE );
		// LOWERCASE - Upload privacy filter
		String[] list2 = { "^displayname$", "^extendedconfigfile$", "^gui\\.", "^nvram$", "^memsize$" };
		PRIVACY_BLACKLIST_PATTERN = Pattern.compile( String.join( "|", list2 ), Pattern.CASE_INSENSITIVE );
	}

	public static enum EthernetType
	{
		NAT( "vmnet1" ), BRIDGED( "vmnet0" ), HOST_ONLY( "vmnet2" );

		public final String vmnet;

		private EthernetType( String vnet )
		{
			this.vmnet = vnet;
		}
	}

	public VirtualizationConfigurationVmware( List<OperatingSystem> osList, File file )
			throws IOException, VirtualizationConfigurationException
	{
		super( new VirtualizerVmware(), osList );
		this.config = new VirtualizationConfigurationVmwareFileFormat( file );
		init();
	}

	public VirtualizationConfigurationVmware( List<OperatingSystem> osList, byte[] vmxContent, int length )
			throws VirtualizationConfigurationException
	{
		super( new VirtualizerVmware(), osList );
		this.config = new VirtualizationConfigurationVmwareFileFormat( vmxContent, length ); // still unfiltered
		init(); // now filtered
	}

	private void init()
	{
		Map<String, Controller> disks = new HashMap<>();
		for ( Entry<String, ConfigEntry> entry : config.entrySet() ) {
			handleLoadEntry( entry, disks );
		}
		// Fix accidentally filtered USB config if we see EHCI is present
		if ( isSetAndTrue( "ehci.present" ) && !isSetAndTrue( "usb.present" ) ) {
			addFiltered( "usb.present", "TRUE" );
		}
		// if we find this tag, we already went through the hdd's - so we're done.
		if ( config.get( "#SLX_HDD_BUS" ) != null ) {
			try {
				hdds.add( new HardDisk( config.get( "#SLX_HDD_CHIP" ),
						DriveBusType.valueOf( config.get( "#SLX_HDD_BUS" ) ), "empty" ) );
			} catch ( Exception e ) {
				LOGGER.debug( "Error adding HDD object when parsing #SLX_HDD_BUS. Meta-data will be incorrect.", e );
			}
			return;
		}
		// Now find the HDDs and add to list
		for ( Entry<String, Controller> cEntry : disks.entrySet() ) {
			Controller controller = cEntry.getValue();
			String controllerType = cEntry.getKey();
			if ( !controller.present )
				continue;
			for ( Entry<String, Device> dEntry : controller.devices.entrySet() ) {
				String deviceId = dEntry.getKey();
				Device device = dEntry.getValue();
				if ( !device.present )
					continue; // Not present
				if ( device.deviceType != null && !device.deviceType.toLowerCase().endsWith( "disk" ) )
					continue; // Not a HDD
				DriveBusType bus = null;
				if ( controllerType.startsWith( "ide" ) ) {
					bus = DriveBusType.IDE;
				} else if ( controllerType.startsWith( "scsi" ) ) {
					bus = DriveBusType.SCSI;
				} else if ( controllerType.startsWith( "sata" ) ) {
					bus = DriveBusType.SATA;
				} else if ( controllerType.startsWith( "nvme" ) ) {
					bus = DriveBusType.NVME;
				}
				hdds.add( new HardDisk( controller.virtualDev, bus, device.filename ) );
				// Remove original entries from VMX
				removeEntriesStartingWith( controllerType + ":" + deviceId + "." );
			}
		}
		// TODO check if this machine is in a paused/suspended state
		this.isMachineSnapshot = false;

		// Add HDD to cleaned vmx
		if ( !hdds.isEmpty() ) {
			HardDisk hdd = hdds.get( 0 );
			addFiltered( "#SLX_HDD_BUS", hdd.bus.toString() );
			if ( hdd.chipsetDriver != null ) {
				addFiltered( "#SLX_HDD_CHIP", hdd.chipsetDriver );
			}
		}
	}

	private void removeEntriesStartingWith( String start )
	{
		for ( Iterator<Entry<String, ConfigEntry>> it = config.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, ConfigEntry> entry = it.next();
			if ( entry.getKey().startsWith( start ) ) {
				it.remove();
			}
		}
	}

	private void addFiltered( String key, String value )
	{
		config.set( key, value );
	}

	private boolean isSetAndTrue( String key )
	{
		String value = config.get( key );
		return value != null && value.equalsIgnoreCase( "true" );
	}

	private void handleLoadEntry( Entry<String, ConfigEntry> entry, Map<String, Controller> disks )
	{
		String lowerKey = entry.getKey().toLowerCase();
		// Dig Usable meta data
		String value = entry.getValue().getValue();
		if ( lowerKey.equals( "guestos" ) ) {
			setOs( value );
			return;
		}
		if ( lowerKey.equals( "displayname" ) ) {
			displayName = value;
			return;
		}
		Matcher hdd = HDD_PATTERN.matcher( entry.getKey() );
		if ( hdd.find() ) {
			handleHddEntry( disks, hdd.group( 1 ).toLowerCase(), hdd.group( 2 ), hdd.group( 3 ), value );
		}
	}

	private void handleHddEntry( Map<String, Controller> disks, String controllerStr, String deviceStr, String property, String value )
	{
		Controller controller = disks.get( controllerStr );
		if ( controller == null ) {
			controller = new Controller();
			disks.put( controllerStr, controller );
		}
		if ( deviceStr == null || deviceStr.isEmpty() ) {
			// Controller property
			if ( property.equalsIgnoreCase( "present" ) ) {
				controller.present = Boolean.parseBoolean( value );
			} else if ( property.equalsIgnoreCase( "virtualDev" ) ) {
				controller.virtualDev = value;
			}
			return;
		}
		// Device property
		Device device = controller.devices.get( deviceStr );
		if ( device == null ) {
			device = new Device();
			controller.devices.put( deviceStr, device );
		}
		if ( property.equalsIgnoreCase( "deviceType" ) ) {
			device.deviceType = value;
		} else if ( property.equalsIgnoreCase( "filename" ) ) {
			device.filename = value;
		} else if ( property.equalsIgnoreCase( "present" ) ) {
			device.present = Boolean.parseBoolean( value );
		}
	}

	@Override
	public boolean addEmptyHddTemplate()
	{
		return this.addHddTemplate( "%VM_DISK_PATH%", "%VM_DISK_MODE%", "%VM_DISK_REDOLOGDIR%" );
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		return addHddTemplate( diskImage.getName(), hddMode, redoDir );
	}

	@Override
	public boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir )
	{
		if ( diskImagePath.isEmpty() ) {
			LOGGER.error( "Empty disk image path given!" );
			return false;
		}

		if ( hdds.isEmpty() ) {
			LOGGER.warn( "No HDDs found in configuration" );
			return false;
		}

		HardDisk hdd = hdds.get( 0 );

		String chipset = hdd.chipsetDriver;
		String prefix;
		switch ( hdd.bus ) {
		case SATA:
			// Cannot happen?... use lsisas1068
			prefix = "scsi0";
			chipset = "lsisas1068";
			break;
		case IDE:
		case SCSI:
		case NVME:
			prefix = hdd.bus.name().toLowerCase() + "0";
			break;
		default:
			LOGGER.warn( "Unknown HDD bus type: " + hdd.bus.toString() );
			return false;
		}
		// Gen
		addFiltered( prefix + ".present", "TRUE" );
		if ( chipset != null ) {
			addFiltered( prefix + ".virtualDev", chipset );
		}
		addFiltered( prefix + ":0.present", "TRUE" );
		addFiltered( prefix + ":0.deviceType", "disk" );
		addFiltered( prefix + ":0.fileName", diskImagePath );
		if ( hddMode != null ) {
			addFiltered( prefix + ":0.mode", hddMode );
			addFiltered( prefix + ":0.redo", "" );
			addFiltered( prefix + ":0.redoLogDir", redoDir );
		}
		config.remove( "#SLX_HDD_BUS" );
		config.remove( "#SLX_HDD_CHIP" );
		return true;
	}

	public boolean addDefaultNat()
	{
		addFiltered( "ethernet0.present", "TRUE" );
		addFiltered( "ethernet0.connectionType", "nat" );
		return true;
	}

	public boolean addEthernet( VirtualizationConfiguration.EtherType type )
	{
		boolean ret = false;
		int index = 0;
		for ( ;; ++index ) {
			if ( config.get( "ethernet" + index + ".present" ) == null )
				break;
		}
		switch ( type ) {
		case NAT:
			ret = addEthernet( index, EthernetType.NAT );
			break;
		case BRIDGED:
			ret = addEthernet( index, EthernetType.BRIDGED );
			break;
		case HOST_ONLY:
			ret = addEthernet( index, EthernetType.HOST_ONLY );
			break;
		default:
			// Should not come to this...
			break;
		}
		return ret;
	}

	public boolean addEthernet( int index, EthernetType type )
	{
		String ether = "ethernet" + index;
		addFiltered( ether + ".present", "TRUE" );
		addFiltered( ether + ".connectionType", "custom" );
		addFiltered( ether + ".vnet", type.vmnet );
		if ( config.get( ether + ".virtualDev" ) == null ) {
			String dev = config.get( "ethernet0.virtualDev" );
			if ( dev != null ) {
				addFiltered( ether + ".virtualDev", dev );
			}
		}
		return true;
	}

	public void addFloppy( int index, String image, boolean readOnly )
	{
		String pre = "floppy" + index;
		addFiltered( pre + ".present", "TRUE" );
		if ( image == null ) {
			addFiltered( pre + ".startConnected", "FALSE" );
			addFiltered( pre + ".fileType", "device" );
			config.remove( pre + ".fileName" );
			config.remove( pre + ".readonly" );
			addFiltered( pre + ".autodetect", "TRUE" );
		} else {
			addFiltered( pre + ".startConnected", "TRUE" );
			addFiltered( pre + ".fileType", "file" );
			addFiltered( pre + ".fileName", image );
			addFiltered( pre + ".readonly", vmBoolean( readOnly ) );
			config.remove( pre + ".autodetect" );
		}
	}

	public boolean addCdrom( String image )
	{
		for ( String port : new String[] { "ide0:0", "ide0:1", "ide1:0", "ide1:1", "scsi0:1" } ) {
			if ( !isSetAndTrue( port + ".present" ) ) {
				addFiltered( port + ".present", "TRUE" );
				if ( image == null ) {
					addFiltered( port + ".autodetect", "TRUE" );
					addFiltered( port + ".deviceType", "cdrom-raw" );
					config.remove( port + ".fileName" );
				} else {
					config.remove( port + ".autodetect" );
					addFiltered( port + ".deviceType", "cdrom-image" );
					addFiltered( port + ".fileName", image );
				}
				return true;
			}
		}
		return false;
	}

	private static String vmBoolean( boolean var )
	{
		return Boolean.toString( var ).toUpperCase();
	}

	private static String vmInteger( int val )
	{
		return Integer.toString( val );
	}

	@Override
	public void transformNonPersistent() throws VirtualizationConfigurationException
	{
		// Cleaned vmx construction
		for ( Iterator<Entry<String, ConfigEntry>> it = config.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, ConfigEntry> elem = it.next();
			if ( !STATELESS_WHITELIST_PATTERN.matcher( elem.getKey() ).find() ) {
				it.remove();
			}
		}
		addFiltered( "suspend.disabled", "TRUE" );
	}

	@Override
	public void transformEditable() throws VirtualizationConfigurationException
	{
		addFiltered( "gui.applyHostDisplayScalingToGuest", "FALSE" );
		// This is for a very old bug: Check we have at lerast USB 2.0, as
		// a buggy dmsd removed all USB controllers
		List<ConfigurableOptionGroup> groups = getConfigurableOptions();
		for ( ConfigurableOptionGroup group : groups ) {
			if ( group.groupIdentifier != ConfigurationGroups.USB_SPEED )
				continue;
			int currentSpeed = 0;
			VirtOptionValue twoPointOh = null;
			for ( VirtOptionValue option : group.availableOptions ) {
				int s = Util.parseInt( option.getId(), 0 );
				if ( option.isActive() && s > currentSpeed ) {
					currentSpeed = s;
				}
				if ( s == 2 ) {
					twoPointOh = option;
				}
			}
			if ( currentSpeed < 3 && twoPointOh != null ) {
				twoPointOh.apply();
			}
		}
	}

	@Override
	public void transformPrivacy() throws VirtualizationConfigurationException
	{
		for ( Iterator<Entry<String, ConfigEntry>> it = config.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, ConfigEntry> elem = it.next();
			String key = elem.getKey();
			String value = elem.getValue().getValue();
			if ( key.endsWith( ".fileName" ) && !value.startsWith( "-" )
					&& ( value.contains( "." ) || value.contains( "/" ) || value.contains( "\\" ) ) ) {
				it.remove();
			} else if ( PRIVACY_BLACKLIST_PATTERN.matcher( key ).find() ) {
				it.remove();
			}
		}
	}

	@Override
	public boolean addDisplayName( String name )
	{
		addFiltered( "displayName", name );
		return true;
	}

	@Override
	public boolean addRam( int mem )
	{
		addFiltered( "memsize", Integer.toString( mem ) );
		return true;
	}

	public void setOs( String vendorOsId )
	{
		addFiltered( "guestOS", vendorOsId );

		final OperatingSystem os = VirtualizationConfigurationUtils.getOsOfVirtualizerFromList( this.osList,
				TConst.VIRT_VMWARE, vendorOsId );
		this.setOs( os );
	}

	public byte[] getConfigurationAsByteArray()
	{
		return config.toString().getBytes( StandardCharsets.UTF_8 );
	}

	private static class Device
	{
		public boolean present = false;
		public String deviceType = null;
		public String filename = null;

		@Override
		public String toString()
		{
			return filename + " is " + deviceType + " (present: " + present + ")";
		}
	}

	private static class Controller
	{
		public boolean present = true; // Seems to be implicit, seen at least for IDE...
		public String virtualDev = null;
		Map<String, Device> devices = new HashMap<>();

		@Override
		public String toString()
		{
			return virtualDev + " is (present: " + present + "): " + devices.toString();
		}
	}

	public String getValue( String key )
	{
		return config.get( key );
	}
	
	class VmwareNoSoundCard extends VirtOptionValue
	{

		public VmwareNoSoundCard( String displayName )
		{
			super( "", displayName );
		}

		@Override
		public void apply()
		{
			addFiltered( "sound.present", vmBoolean( false ) );
			config.remove( "sound.virtualDev" );
		}

		@Override
		public boolean isActive()
		{
			return !isSetAndTrue( "sound.present" );
		}

	}

	class VmWareSoundCardModelNone extends VirtOptionValue
	{

		public VmWareSoundCardModelNone( String displayName )
		{
			super( "none", displayName );
		}

		@Override
		public void apply()
		{
			addFiltered( "sound.present", vmBoolean( false ) );
			addFiltered( "sound.autodetect", vmBoolean( false ) );
			config.remove( "sound.virtualDev" );
		}

		@Override
		public boolean isActive()
		{
			return !isSetAndTrue( "sound.present" );
		}

	}

	class VmWareSoundCardModel extends VirtOptionValue
	{

		public VmWareSoundCardModel( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			addFiltered( "sound.present", vmBoolean( true ) );
			addFiltered( "sound.autodetect", vmBoolean( true ) );
			addFiltered( "sound.virtualDev", this.id );
		}

		@Override
		public boolean isActive()
		{
			return isSetAndTrue( "sound.present" ) && isSetAndTrue( "sound.autodetect" )
					&& this.id.equals( config.get( "sound.virtualDev" ) );
		}

	}

	class VmWareAccel3D extends VirtOptionValue
	{

		public VmWareAccel3D( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			addFiltered( "mks.enable3d", this.id );
		}

		@Override
		public boolean isActive()
		{
			return Boolean.parseBoolean( this.id ) == isSetAndTrue( "mks.enable3d" );
		}

	}

	public void setVirtualizerVersion( Version type )
	{
		addFiltered( "virtualHW.version", vmInteger( type.getMajor() ) );
	}

	public Version getVirtualizerVersion()
	{
		final short major = Integer.valueOf( Util.parseInt( config.get( "virtualHW.version" ), -1 ) ).shortValue();
		return Version.getInstanceByMajorFromVersions( major, this.getVirtualizer().getSupportedVersions() );
	}
	
	class VmwareNicModel extends VirtOptionValue
	{

		private final int cardIndex;
		
		public VmwareNicModel( int cardIndex, String id, String displayName )
		{
			super( id, displayName );
			this.cardIndex = cardIndex;
		}

		@Override
		public void apply()
		{
			if ( Util.isEmptyString( id ) ) {
				config.remove( "ethernet" + cardIndex + ".virtualDev" );
			} else {
				addFiltered( "ethernet" + cardIndex + ".virtualDev", id );
			}
		}

		@Override
		public boolean isActive()
		{
			String temp = config.get( "ethernet" + cardIndex + ".virtualDev" );
			if ( temp == null )
				return Util.isEmptyString( this.id );
			return temp.equals( this.id );
		}

	}
	
	class VmWareUsbSpeed extends VirtOptionValue
	{
		private final String[] SPEED = { null, "usb", "ehci", "usb_xhci" };
		private final int speed;

		public VmWareUsbSpeed( int speed, String displayName )
		{
			super( Integer.toString( speed ), displayName );
			this.speed = speed;
		}

		@Override
		public void apply()
		{
			// XXX TODO This sucks, qnd
			for ( int i = 1; i < SPEED.length; ++i ) {
				String key = SPEED[i] + ".present";
				if ( i <= speed ) {
					// Enable desired speed class, plus all lower ones
					addFiltered( key, "TRUE" );
				} else {
					config.remove( key );
				}
			}
			// VMware 14+ needs this to use USB 3.0 devices at USB 3.0 ports in VMs configured for < 3.0
			if ( speed > 0 && speed < 3 ) {
				addFiltered( "usb.mangleUsb3Speed", "TRUE" );
			}
		}

		@Override
		public boolean isActive()
		{
			int max = 0;
			for ( int i = 1; i < SPEED.length; ++i ) {
				if ( isSetAndTrue( SPEED[i] + ".present" ) ) {
					max = i;
				}
			}
			return speed == max;
		}
		
	}

	@Override
	public boolean addCpuCoreCount( int numCores )
	{
		addFiltered( "numvcpus", vmInteger( numCores ) );
		return true;
	}

	public void registerVirtualHW()
	{
		List<VirtOptionValue> list;
		list = new ArrayList<>();
		list.add( new VmWareSoundCardModelNone( SoundCard.NONE ) );
		list.add( new VmWareSoundCardModel( "", SoundCard.DEFAULT ) );
		list.add( new VmWareSoundCardModel( "sb16", SoundCard.SOUND_BLASTER ) );
		list.add( new VmWareSoundCardModel( "es1371", SoundCard.ES ) );
		list.add( new VmWareSoundCardModel( "hdaudio", SoundCard.HD_AUDIO ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.SOUND_CARD_MODEL, list ) );

		list = new ArrayList<>();
		list.add( new VmWareAccel3D( "FALSE", "2D" ) );
		list.add( new VmWareAccel3D( "TRUE", "3D" ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.GFX_TYPE, list ) );
		
		list = new ArrayList<>();
		list.add( new VmwareNicModel( 0, "", Ethernet.AUTO ) );
		list.add( new VmwareNicModel( 0, "vlance", Ethernet.PCNET32 ) );
		list.add( new VmwareNicModel( 0, "e1000", Ethernet.E1000 ) );
		list.add( new VmwareNicModel( 0, "e1000e", Ethernet.E1000E ) );
		list.add( new VmwareNicModel( 0, "vmxnet", Ethernet.VMXNET ) );
		list.add( new VmwareNicModel( 0, "vmxnet3", Ethernet.VMXNET3 ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.NIC_MODEL, list ) );

		list = new ArrayList<>();
		list.add( new VmWareUsbSpeed( 0, Usb.NONE ) );
		list.add( new VmWareUsbSpeed( 1, Usb.USB1_1 ) );
		list.add( new VmWareUsbSpeed( 2, Usb.USB2_0 ) );
		list.add( new VmWareUsbSpeed( 3, Usb.USB3_0 ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.USB_SPEED, list ) );
	}

	@Override
	public String getFileNameExtension()
	{
		return VirtualizationConfigurationVmware.FILE_NAME_EXTENSION;
	}

	@Override
	public void validate() throws VirtualizationConfigurationException
	{
	}
}
