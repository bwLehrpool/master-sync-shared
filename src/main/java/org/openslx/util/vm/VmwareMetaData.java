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
		String[] list = { "^guestos", "^uuid\\.bios", "^config\\.version", "^ehci\\.",
				"mks\\.", "virtualhw\\.", "^sound\\.", "\\.pcislotnumber$", "^pcibridge", "\\.virtualdev$" };
		whitelist = new Pattern[ list.length ];
		for ( int i = 0; i < list.length; ++i ) {
			whitelist[i] = Pattern.compile( list[i] );
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
		this.config = new VmwareConfig( vmxContent, length );
		init();
	}

	private void init()
	{
		for ( Entry<String, ConfigEntry> entry : config.entrySet() ) {
			handleLoadEntry( entry );
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

	public boolean addHddTemplate( String diskImagePath )
	{
		DriveBusType bus;
		try {
			bus = DriveBusType.valueOf( config.get( "#SLX_HDD_BUS" ) );
		} catch ( Exception e ) {
			LOGGER.warn( "Unknown bus type: " + config.get( "#SLX_HDD_BUS" ) + ". Cannot add hdd config." );
			return false;
		}
		String chipset = config.get( "#SLX_HDD_CHIP" );
		switch ( bus ) {
		case IDE:
			addFiltered( "ide0.present", "TRUE" );
			addFiltered( "ide0:0.present", "TRUE" );
			addFiltered( "ide0:0.deviceType", "disk" );
			addFiltered( "ide0:0.fileName", diskImagePath );
			return true;
		case SATA:
			// Cannot happen?... use lsisas1068
		case SCSI:
			addFiltered( "scsi0.present", "TRUE" );
			addFiltered( "scsi0:0.present", "TRUE" );
			addFiltered( "scsi0:0.deviceType", "disk" );
			addFiltered( "scsi0:0.fileName", diskImagePath );
			if ( chipset != null ) {
				addFiltered( "scsi0.virtualDev", chipset );
			}
			return true;
		default:
			LOGGER.warn( "Unknown HDD bus type: " + bus.toString() );
			return false;
		}
	}

	public boolean addEthernetNat()
	{
		addFiltered( "ethernet0.present", "TRUE" );
		addFiltered( "ethernet0.connectionType", "nat" );
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

}
