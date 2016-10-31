package org.openslx.util.vm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.util.vm.VmwareConfig.ConfigEntry;

public class VmwareMetaData extends VmMetaData
{

	private static final Logger LOGGER = Logger.getLogger( VmwareMetaData.class );

	private static final Virtualizer virtualizer = new Virtualizer( "vmware", "VMware" );

	private static final Pattern hddKey = Pattern.compile( "^(ide\\d|scsi\\d|sata\\d):?(\\d)?\\.(.*)", Pattern.CASE_INSENSITIVE );

	// Lowercase list of allowed settings for upload (as regex)
	private static final Pattern[] whitelist;

	private final VmwareConfig config;

	// Init static members
	static {
		String[] list = { "^guestos", "^uuid\\.bios", "^config\\.version", "^ehci\\.", "^mks\\.enable3d", "^virtualhw\\.", "^sound\\.", "\\.pcislotnumber$", "^pcibridge",
				"\\.virtualdev$", "^tools\\.syncTime$", "^time\\.synchronize", "^bios\\.bootDelay", "^rtc\\.", "^xhci\\." };
		whitelist = new Pattern[ list.length ];
		for ( int i = 0; i < list.length; ++i ) {
			whitelist[i] = Pattern.compile( list[i].toLowerCase() );
		}
	}

	private final Map<String, Controller> disks = new HashMap<>();

	public VmwareMetaData( List<OperatingSystem> osList, File file ) throws IOException
	{
		super( osList );
		this.config = new VmwareConfig( file );
		init();
	}

	public VmwareMetaData( List<OperatingSystem> osList, byte[] vmxContent, int length )
	{
		super( osList );
		this.config = new VmwareConfig( vmxContent, length ); // still unfiltered 
		init(); // now filtered
	}

	private void init()
	{
		for ( Entry<String, ConfigEntry> entry : config.entrySet() ) {
			handleLoadEntry( entry );
		}
		// if we find this tag, we already went through the hdd's - so we're done.
		if ( config.get( "#SLX_HDD_BUS" ) != null ) {
			return;
		}
		// Now find the HDDs and add to list
		for ( Entry<String, Controller> cEntry : disks.entrySet() ) {
			Controller controller = cEntry.getValue();
			String controllerType = cEntry.getKey();
			if ( !controller.present )
				continue;
			for ( Entry<String, Device> dEntry : controller.devices.entrySet() ) {
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
				}
				hdds.add( new HardDisk( controller.virtualDev, bus, device.filename ) );
			}
		}

		// Add HDD to cleaned vmx
		if ( !hdds.isEmpty() ) {
			HardDisk hdd = hdds.get( 0 );
			addFiltered( "#SLX_HDD_BUS", hdd.bus.toString() );
			if ( hdd.chipsetDriver != null ) {
				addFiltered( "#SLX_HDD_CHIP", hdd.chipsetDriver );
			}
		}
	}

	private void addFiltered( String key, String value )
	{
		config.set( key, value ).filtered( true );
	}

	private boolean isSetAndTrue( String key )
	{
		String value = config.get( key );
		return value != null && value.equalsIgnoreCase( "true" );
	}

	private void handleLoadEntry( Entry<String, ConfigEntry> entry )
	{
		String lowerKey = entry.getKey().toLowerCase();
		// Cleaned vmx construction
		for ( Pattern exp : whitelist ) {
			if ( exp.matcher( lowerKey ).find() ) {
				entry.getValue().filtered( true );
				break;
			}
		}
		//
		// Dig Usable meta data
		String value = entry.getValue().getValue();
		if ( lowerKey.equals( "guestos" ) ) {
			setOs( "vmware", value );
			return;
		}
		if ( lowerKey.equals( "displayname" ) ) {
			displayName = value;
			return;
		}
		Matcher hdd = hddKey.matcher( entry.getKey() );
		if ( hdd.find() ) {
			handleHddEntry( hdd.group( 1 ).toLowerCase(), hdd.group( 2 ), hdd.group( 3 ), value );
		}
	}

	private void handleHddEntry( String controllerStr, String deviceStr, String property, String value )
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

	public boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir )
	{
		DriveBusType bus;
		try {
			bus = DriveBusType.valueOf( config.get( "#SLX_HDD_BUS" ) );
		} catch ( Exception e ) {
			LOGGER.warn( "Unknown bus type: " + config.get( "#SLX_HDD_BUS" ) + ". Cannot add hdd config." );
			return false;
		}
		String chipset = config.get( "#SLX_HDD_CHIP" );
		String prefix;
		switch ( bus ) {
		case IDE:
			prefix = "ide0:0";
			addFiltered( "ide0.present", "TRUE" );
			break;
		case SATA:
			// Cannot happen?... use lsisas1068
		case SCSI:
			prefix = "scsi0:0";
			addFiltered( "scsi0.present", "TRUE" );
			if ( chipset != null ) {
				addFiltered( "scsi0.virtualDev", chipset );
			}
			break;
		default:
			LOGGER.warn( "Unknown HDD bus type: " + bus.toString() );
			return false;
		}
		// Gen
		addFiltered( prefix + ".present", "TRUE" );
		addFiltered( prefix + ".deviceType", "disk" );
		addFiltered( prefix + ".fileName", diskImagePath );
		if ( hddMode != null ) {
			addFiltered( prefix + ".mode", hddMode );
			addFiltered( prefix + ".redo", "" );
			addFiltered( prefix + ".redoLogDir", redoDir );
		}
		return true;
	}

	public boolean addDefaultNat()
	{
		addFiltered( "ethernet0.present", "TRUE" );
		addFiltered( "ethernet0.connectionType", "nat" );
		return true;
	}

	public boolean addEthernet( EthernetType type )
	{
		int index = 0;
		for ( ;; ++index ) {
			if ( config.get( "ethernet" + index + ".present" ) == null )
				break;
		}
		return addEthernet( index, type );
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

	public boolean disableSuspend()
	{
		addFiltered( "suspend.disabled", "TRUE" );
		return true;
	}

	public boolean addDisplayName( String name )
	{
		addFiltered( "displayName", name );
		return true;
	}

	public boolean addRam( int mem )
	{
		addFiltered( "memsize", Integer.toString( mem ) );
		return true;
	}

	public void setOs( String vendorOsId )
	{
		addFiltered( "guestOS", vendorOsId );
		setOs( "vmware", vendorOsId );
	}

	@Override
	public byte[] getFilteredDefinitionArray()
	{
		return config.toString( true, false ).getBytes( StandardCharsets.UTF_8 );
	}

	public byte[] getDefinitionArray()
	{
		return config.toString( false, false ).getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public Virtualizer getVirtualizer()
	{
		return virtualizer;
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

	public static enum EthernetType
	{
		NAT( "vmnet1" ),
		BRIDGED( "vmnet0" ),
		HOST_ONLY( "vmnet2" );

		public final String vmnet;

		private EthernetType( String vnet )
		{
			this.vmnet = vnet;
		}
	}

	@Override
	public void enableUsb( boolean enabled )
	{
		addFiltered( "usb.present", vmBoolean( enabled ) );
		addFiltered( "ehci.present", vmBoolean( enabled ) );
	}

	@Override
	public void applySettingsForLocalEdit()
	{
		addFiltered( "gui.applyHostDisplayScalingToGuest", "FALSE" );
	}

	public String getValue( String key )
	{
		return config.get( key );
	}

	// SOUND
	public static enum SoundCardType
	{
		NONE( false, null, "No card" ),
		DEFAULT( true, null, "Default card" ),
		SOUND_BLASTER( true, "sb16", "Sound Blaster 16" ),
		ES( true, "es1371", "ES 1371" ),
		HD_AUDIO( true, "hdaudio", "Intel Integrated HD Audio" );

		public final boolean isPresent;
		public final String value;
		public final String displayName;

		private SoundCardType( boolean present, String value, String dName )
		{
			this.isPresent = present;
			this.value = value;
			this.displayName = dName;
		}
	}

	public void setSoundCard( SoundCardType type )
	{
		addFiltered( "sound.present", vmBoolean( type.isPresent ) );
		if ( type.value != null ) {
			addFiltered( "sound.virtualDev", type.value );
		} else {
			config.remove( "sound.virtualDev" );
		}
	}

	public SoundCardType getSoundCard()
	{
		if ( isSetAndTrue( "sound.present" ) && isSetAndTrue( "sound.autodetect" ) && config.get( "sound.virtualDev" ) == null ) {
			return SoundCardType.DEFAULT;
		} else if ( isSetAndTrue( "sound.present" ) && isSetAndTrue( "sound.autodetect" ) && config.get( "sound.virtualDev" ).equals( "sb16" ) ) {
			return SoundCardType.SOUND_BLASTER;
		} else if ( isSetAndTrue( "sound.present" ) && isSetAndTrue( "sound.autodetect" ) && config.get( "sound.virtualDev" ).equals( "es1371" ) ) {
			return SoundCardType.ES;
		} else if ( isSetAndTrue( "sound.present" ) && isSetAndTrue( "sound.autodetect" ) && config.get( "sound.virtualDev" ).equals( "hdaudio" ) ) {
			return SoundCardType.HD_AUDIO;
		} else {
			return SoundCardType.NONE;
		}
	}

	// 3DAcceleration
	public static enum DDAcceleration
	{
		OFF( false, "Off" ),
		ON( true, "On" );

		public final boolean isPresent;
		public final String displayName;

		private DDAcceleration( boolean present, String dName )
		{
			this.isPresent = present;
			this.displayName = dName;
		}
	}

	public void setDDAcceleration( DDAcceleration type )
	{
		addFiltered( "mks.enable3d", vmBoolean( type.isPresent ) );
	}

	public DDAcceleration getDDAcceleration()
	{
		if ( isSetAndTrue( "mks.enable3d" ) ) {
			return DDAcceleration.ON;
		} else if ( !isSetAndTrue( "mks.enable3d" ) ) {
			return DDAcceleration.OFF;
		} else
			return DDAcceleration.OFF;
	}

	// Virtual hardware version
	public static enum HWVersion
	{
		NONE( 0, "Bitte korrekter Eintrag wählen!" ),
		THREE( 3, "3" ),
		FOUR( 4, "4" ),
		SIX( 6, "6" ),
		SEVEN( 7, "7" ),
		EIGHT( 8, "8" ),
		NINE( 9, "9" ),
		TEN( 10, "10" ),
		ELEVEN( 11, "11" ),
		TWELVE( 12, "12" );

		public final int version;
		public final String displayName;

		private HWVersion( int vers, String dName )
		{
			this.version = vers;
			this.displayName = dName;
		}
	}

	public void setHWVersion( HWVersion type )
	{
		addFiltered( "virtualHW.version", vmInteger( type.version ) );
	}

	public HWVersion getHWVersion()
	{

		String temp = config.get( "virtualHW.version" );
		if ( temp == null ) {
			return HWVersion.NONE;
		}

		switch ( config.get( "virtualHW.version" ) ) {
		case "3":
			return HWVersion.THREE;
		case "4":
			return HWVersion.FOUR;
		case "6":
			return HWVersion.SIX;
		case "7":
			return HWVersion.SEVEN;
		case "8":
			return HWVersion.EIGHT;
		case "9":
			return HWVersion.NINE;
		case "10":
			return HWVersion.TEN;
		case "11":
			return HWVersion.ELEVEN;
		case "12":
			return HWVersion.TWELVE;
		default:
			return HWVersion.NONE;
		}
	}

	// Virtual network adapter
	public static enum E0VirtDev
	{
		AUTO( null, "auto detect" ),
		AMD( "vlance", "AMD PCnet32" ),
		INTEL( "e1000", "Intel E1000" ),
		VMX( "vmxnet", "VMXnet" );

		public final String value;
		public final String displayName;

		private E0VirtDev( String value, String dName )
		{
			this.value = value;
			this.displayName = dName;
		}
	}

	public void setE0VirtDev( E0VirtDev type )
	{
		if ( type.value != null ) {
			addFiltered( "ethernet0.virtualDev", type.value );
		} else {
			config.remove( "ethernet0.virtualDev" );
		}
	}

	public E0VirtDev getE0VirtDev()
	{

		String temp = config.get( "ethernet0.virtualDev" );
		if ( temp == null ) {
			return E0VirtDev.AUTO;
		}

		switch ( config.get( "ethernet0.virtualDev" ) ) {
		case "vlance":
			return E0VirtDev.AMD;
		case "e1000":
			return E0VirtDev.INTEL;
		case "vmxnet":
			return E0VirtDev.VMX;
		default:
			return E0VirtDev.AUTO;

		}
	}
}
