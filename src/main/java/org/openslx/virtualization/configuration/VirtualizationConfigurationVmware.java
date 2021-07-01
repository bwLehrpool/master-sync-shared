package org.openslx.virtualization.configuration;

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
import org.openslx.thrifthelper.TConst;
import org.openslx.util.Util;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfigurationVmwareFileFormat.ConfigEntry;
import org.openslx.virtualization.virtualizer.VirtualizerVmware;

class VmWareSoundCardMeta
{
	public final boolean isPresent;
	public final String value;

	public VmWareSoundCardMeta( boolean present, String val )
	{
		isPresent = present;
		value = val;
	}
}

class VmWareDDAccelMeta
{
	public final boolean isPresent;

	public VmWareDDAccelMeta( boolean present )
	{
		isPresent = present;
	}
}

class VmWareEthernetDevTypeMeta
{
	public final String value;

	public VmWareEthernetDevTypeMeta( String val )
	{
		value = val;
	}
}

class VmwareUsbSpeed
{
	public final String keyName;
	public final int speedNumeric;

	public VmwareUsbSpeed( int speed, String key )
	{
		this.keyName = key == null ? null : ( key + ".present" );
		this.speedNumeric = speed;
	}
}

public class VirtualizationConfigurationVmware extends
		VirtualizationConfiguration<VmWareSoundCardMeta, VmWareDDAccelMeta, VmWareEthernetDevTypeMeta, VmwareUsbSpeed>
{
	/**
	 * File name extension for VMware virtualization configuration files.
	 */
	public static final String FILE_NAME_EXTENSION = "vmx";

	private static final Logger LOGGER = Logger.getLogger( VirtualizationConfigurationVmware.class );

	private static final Pattern hddKey = Pattern.compile( "^(ide\\d|scsi\\d|sata\\d|nvme\\d):?(\\d)?\\.(.*)",
			Pattern.CASE_INSENSITIVE );

	// Lowercase list of allowed settings for upload (as regex)
	private static final Pattern[] whitelist;

	private final VirtualizationConfigurationVmwareFileFormat config;

	// Init static members
	static {
		String[] list = { "^guestos", "^uuid\\.bios", "^config\\.version", "^ehci[.:]", "^mks\\.enable3d",
				"^virtualhw\\.",
				"^sound[.:]", "\\.pcislotnumber$", "^pcibridge", "\\.virtualdev$", "^tools\\.syncTime$",
				"^time\\.synchronize",
				"^bios\\.bootDelay", "^rtc\\.", "^xhci[.:]", "^usb_xhci[.:]", "\\.deviceType$", "\\.port$", "\\.parent$",
				"^usb[.:]",
				"^firmware", "^hpet", "^vm\\.genid" };
		whitelist = new Pattern[ list.length ];
		for ( int i = 0; i < list.length; ++i ) {
			whitelist[i] = Pattern.compile( list[i].toLowerCase() );
		}
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

	private final Map<String, Controller> disks = new HashMap<>();

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
		for ( Entry<String, ConfigEntry> entry : config.entrySet() ) {
			handleLoadEntry( entry );
		}
		// Fix accidentally filtered USB config if we see EHCI is present
		if ( isSetAndTrue( "ehci.present" ) && !isSetAndTrue( "usb.present" ) ) {
			addFiltered( "usb.present", "TRUE" );
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
				} else if ( controllerType.startsWith( "nvme" ) ) {
					bus = DriveBusType.NVME;
				}
				hdds.add( new HardDisk( controller.virtualDev, bus, device.filename ) );
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
			setOs( value );
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
		case SATA:
			// Cannot happen?... use lsisas1068
			prefix = "scsi0";
			chipset = "lsisas1068";
			break;
		case IDE:
		case SCSI:
		case NVME:
			prefix = bus.name().toLowerCase() + "0";
			break;
		default:
			LOGGER.warn( "Unknown HDD bus type: " + bus.toString() );
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
		addFiltered( "suspend.disabled", "TRUE" );
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
		return config.toString( false, false ).getBytes( StandardCharsets.UTF_8 );
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

	@Override
	public void transformEditable() throws VirtualizationConfigurationException
	{
		addFiltered( "gui.applyHostDisplayScalingToGuest", "FALSE" );
	}

	public String getValue( String key )
	{
		return config.get( key );
	}

	public void setSoundCard( VirtualizationConfiguration.SoundCardType type )
	{
		VmWareSoundCardMeta soundCardMeta = soundCards.get( type );
		addFiltered( "sound.present", vmBoolean( soundCardMeta.isPresent ) );
		if ( soundCardMeta.value != null ) {
			addFiltered( "sound.virtualDev", soundCardMeta.value );
		} else {
			config.remove( "sound.virtualDev" );
		}
	}

	public VirtualizationConfiguration.SoundCardType getSoundCard()
	{
		if ( !isSetAndTrue( "sound.present" ) || !isSetAndTrue( "sound.autodetect" ) ) {
			return VirtualizationConfiguration.SoundCardType.NONE;
		}
		String current = config.get( "sound.virtualDev" );
		if ( current != null ) {
			VmWareSoundCardMeta soundCardMeta = null;
			for ( VirtualizationConfiguration.SoundCardType type : VirtualizationConfiguration.SoundCardType.values() ) {
				soundCardMeta = soundCards.get( type );
				if ( soundCardMeta != null ) {
					if ( current.equals( soundCardMeta.value ) ) {
						return type;
					}
				}
			}
		}
		return VirtualizationConfiguration.SoundCardType.DEFAULT;
	}

	public void setDDAcceleration( VirtualizationConfiguration.DDAcceleration type )
	{
		VmWareDDAccelMeta ddaMeta = ddacc.get( type );
		addFiltered( "mks.enable3d", vmBoolean( ddaMeta.isPresent ) );
	}

	public VirtualizationConfiguration.DDAcceleration getDDAcceleration()
	{
		if ( isSetAndTrue( "mks.enable3d" ) ) {
			return VirtualizationConfiguration.DDAcceleration.ON;
		} else {
			return VirtualizationConfiguration.DDAcceleration.OFF;
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

	public void setEthernetDevType( int cardIndex, VirtualizationConfiguration.EthernetDevType type )
	{
		VmWareEthernetDevTypeMeta ethernetDevTypeMeta = networkCards.get( type );
		if ( ethernetDevTypeMeta.value != null ) {
			addFiltered( "ethernet" + cardIndex + ".virtualDev", ethernetDevTypeMeta.value );
		} else {
			config.remove( "ethernet" + cardIndex + ".virtualDev" );
		}
	}

	public VirtualizationConfiguration.EthernetDevType getEthernetDevType( int cardIndex )
	{
		String temp = config.get( "ethernet" + cardIndex + ".virtualDev" );
		if ( temp != null ) {
			VmWareEthernetDevTypeMeta ethernetDevTypeMeta = null;
			for ( VirtualizationConfiguration.EthernetDevType type : VirtualizationConfiguration.EthernetDevType
					.values() ) {
				ethernetDevTypeMeta = networkCards.get( type );
				if ( ethernetDevTypeMeta == null ) {
					continue;
				}
				if ( temp.equals( ethernetDevTypeMeta.value ) ) {
					return type;
				}
			}
		}
		return VirtualizationConfiguration.EthernetDevType.AUTO;
	}

	@Override
	public void setMaxUsbSpeed( VirtualizationConfiguration.UsbSpeed newSpeed )
	{
		if ( newSpeed == null ) {
			newSpeed = VirtualizationConfiguration.UsbSpeed.NONE;
		}
		VmwareUsbSpeed newSpeedMeta = usbSpeeds.get( newSpeed );
		if ( newSpeedMeta == null ) {
			throw new RuntimeException( "USB Speed " + newSpeed.name() + " not registered with VMware" );
		}
		for ( VmwareUsbSpeed meta : usbSpeeds.values() ) {
			if ( meta == null )
				continue; // Should not happen
			if ( meta.keyName == null )
				continue; // "No USB" has no config entry, obviously
			if ( meta.speedNumeric <= newSpeedMeta.speedNumeric ) {
				// Enable desired speed class, plus all lower ones
				addFiltered( meta.keyName, "TRUE" );
			} else {
				// This one is higher – remove
				config.remove( meta.keyName );
			}
		}
		// VMware 14+ needs this to use USB 3.0 devices at USB 3.0 ports in VMs configured for < 3.0
		if ( newSpeedMeta.speedNumeric > 0 && newSpeedMeta.speedNumeric < 3 ) {
			addFiltered( "usb.mangleUsb3Speed", "TRUE" );
		}
	}

	@Override
	public VirtualizationConfiguration.UsbSpeed getMaxUsbSpeed()
	{
		int max = 0;
		VirtualizationConfiguration.UsbSpeed maxEnum = VirtualizationConfiguration.UsbSpeed.NONE;
		for ( Entry<VirtualizationConfiguration.UsbSpeed, VmwareUsbSpeed> entry : usbSpeeds.entrySet() ) {
			VmwareUsbSpeed v = entry.getValue();
			if ( v.speedNumeric > max && isSetAndTrue( v.keyName ) ) {
				max = v.speedNumeric;
				maxEnum = entry.getKey();
			}
		}
		return maxEnum;
	}

	@Override
	public boolean addCpuCoreCount( int numCores )
	{
		// TODO actually add the cpu core count to the machine description
		return true;
	}

	@Override
	public void transformPrivacy() throws VirtualizationConfigurationException
	{
	}

	public void registerVirtualHW()
	{
		soundCards.put( VirtualizationConfiguration.SoundCardType.NONE, new VmWareSoundCardMeta( false, null ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.DEFAULT, new VmWareSoundCardMeta( true, null ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.SOUND_BLASTER,
				new VmWareSoundCardMeta( true, "sb16" ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.ES, new VmWareSoundCardMeta( true, "es1371" ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.HD_AUDIO, new VmWareSoundCardMeta( true, "hdaudio" ) );

		ddacc.put( VirtualizationConfiguration.DDAcceleration.OFF, new VmWareDDAccelMeta( false ) );
		ddacc.put( VirtualizationConfiguration.DDAcceleration.ON, new VmWareDDAccelMeta( true ) );

		networkCards.put( VirtualizationConfiguration.EthernetDevType.AUTO, new VmWareEthernetDevTypeMeta( null ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PCNET32,
				new VmWareEthernetDevTypeMeta( "vlance" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.E1000, new VmWareEthernetDevTypeMeta( "e1000" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.E1000E, new VmWareEthernetDevTypeMeta( "e1000e" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.VMXNET, new VmWareEthernetDevTypeMeta( "vmxnet" ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.VMXNET3,
				new VmWareEthernetDevTypeMeta( "vmxnet3" ) );

		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.NONE, new VmwareUsbSpeed( 0, null ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB1_1, new VmwareUsbSpeed( 1, "usb" ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB2_0, new VmwareUsbSpeed( 2, "ehci" ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB3_0, new VmwareUsbSpeed( 3, "usb_xhci" ) );
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
