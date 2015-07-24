package org.openslx.util.vm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
import org.openslx.util.Util;

public class VmwareMetaData extends VmMetaData
{

	private static final Logger LOGGER = Logger.getLogger( VmwareMetaData.class );

	private static final Virtualizer virtualizer = new Virtualizer( "vmware", "VMware" );

	private static final Pattern hddKey = Pattern.compile( "^(ide\\d|scsi\\d|sata\\d):?(\\d)?\\.(.*)", Pattern.CASE_INSENSITIVE );

	// Lowercase list of allowed settings for upload (as regex)
	private static final Pattern[] whitelist;

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

	private final StringBuilder cleanedConfig = new StringBuilder( 200 );

	public VmwareMetaData( List<OperatingSystem> osList, File file ) throws IOException
	{
		super( osList );
		int todo = (int)Math.min( 100000, file.length() );
		int offset = 0;
		byte[] data = new byte[ todo ];
		FileInputStream fr = null;
		try {
			fr = new FileInputStream( file );
			while ( todo > 0 ) {
				int ret = fr.read( data, offset, todo );
				if ( ret <= 0 )
					break;
				todo -= ret;
				offset += ret;
			}
		} finally {
			Util.safeClose( fr );
		}
		init( data, offset );
	}

	public VmwareMetaData( List<OperatingSystem> osList, byte[] vmxContent, int length )
	{
		super( osList );
		init( vmxContent, length );
	}

	private void init( byte[] vmxContent, int length )
	{
		String csName = detectCharset( new ByteArrayInputStream( vmxContent, 0, length ) );
		Charset cs = null;
		try {
			cs = Charset.forName( csName );
		} catch ( Exception e ) {
			LOGGER.warn( "Could not instantiate charset " + csName, e );
		}
		if ( cs == null )
			cs = StandardCharsets.ISO_8859_1;
		try {
			loadVmx( new ByteArrayInputStream( vmxContent, 0, length ), cs );
		} catch ( IOException e ) {
			LOGGER.warn( "Exception when loading vmx from byte array (how!?)", e );
		}
	}

	private void loadVmx( InputStream is, Charset cs ) throws IOException
	{
		LOGGER.info( "Loading VMX with charset " + cs.name() );
		// Prepare filtered vmx
		addFiltered( ".encoding", "UTF-8" );
		// Read line-by-line
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader( is, cs ) );
			String line;
			while ( ( line = reader.readLine() ) != null ) {
				KeyValuePair entry = parse( line );
				if ( entry != null )
					handleLoadEntry( entry );
			}
		} finally {
			Util.safeClose( reader );
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
				if ( device.deviceType != null && !device.deviceType.endsWith( "disk" ) )
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
		cleanedConfig.append( key ).append( " = \"" ).append( value ).append( "\"\n" );
	}

	private void handleLoadEntry( KeyValuePair entry )
	{
		String lowerKey = entry.key.toLowerCase();
		// Cleaned vmx construction
		for ( Pattern exp : whitelist ) {
			if ( exp.matcher( lowerKey ).find() ) {
				addFiltered( entry.key, entry.value );
				break;
			}
		}
		//
		// Dig Usable meta data
		if ( lowerKey.equals( "guestos" ) ) {
			setOs( "vmware", entry.value );
			return;
		}
		if ( lowerKey.equals( "displayname" ) ) {
			displayName = entry.value;
			return;
		}
		Matcher hdd = hddKey.matcher( entry.key );
		if ( hdd.find() ) {
			handleHddEntry( hdd.group( 1 ).toLowerCase(), hdd.group( 2 ), hdd.group( 3 ), entry.value );
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

	private String detectCharset( InputStream is )
	{
		BufferedReader csDetectReader = null;
		try {
			csDetectReader = new BufferedReader( new InputStreamReader( is, StandardCharsets.ISO_8859_1 ) );
			String line;
			while ( ( line = csDetectReader.readLine() ) != null ) {
				KeyValuePair entry = parse( line );
				if ( entry == null )
					continue;
				if ( entry.key.equals( ".encoding" ) ) {
					return entry.value;
				}
			}
		} catch ( Exception e ) {
			LOGGER.warn( "Could not detect charset, fallback to latin1", e );
		} finally {
			Util.safeClose( csDetectReader );
		}
		// Dumb fallback
		return "ISO-8859-1";
	}

	private static final Pattern settingMatcher = Pattern.compile( "^\\s*([a-z0-9\\.\\:]+)\\s*=\\s*\"(.*)\"\\s*$", Pattern.CASE_INSENSITIVE );

	private KeyValuePair parse( String line )
	{
		Matcher matcher = settingMatcher.matcher( line );
		if ( !matcher.matches() )
			return null;
		return new KeyValuePair(
				matcher.group( 1 ), matcher.group( 2 ) );
				
	}

	@Override
	public ByteBuffer getFilteredDefinition()
	{
		return ByteBuffer.wrap( cleanedConfig.toString().getBytes( StandardCharsets.UTF_8 ) );
	}

	@Override
	public Virtualizer getVirtualizer()
	{
		return virtualizer;
	}

	// ###############################

	private static class KeyValuePair
	{
		public final String key;
		public final String value;

		public KeyValuePair( String key, String value )
		{
			this.key = key;
			this.value = value;
		}
	}

	private static class ConfigEntry
	{
		private String value;
		private boolean forFiltered;
		private boolean forGenerated;

		public ConfigEntry( String value )
		{
			this.value = value;
		}

		public ConfigEntry filtered( boolean set )
		{
			this.forFiltered = set;
			return this;
		}

		public ConfigEntry generated( boolean set )
		{
			this.forGenerated = set;
			return this;
		}

	}

	private static class ConfigEntries
	{
		private Map<String, ConfigEntry> entries = new HashMap<>();

		public ConfigEntry set( String key, String value )
		{
			return entries.put( key, new ConfigEntry( value ) );
		}

		public ConfigEntry set( KeyValuePair entry )
		{
			return set( entry.key, entry.value );
		}

		public String get( boolean filteredRequired, boolean generatedRequired )
		{
			StringBuilder sb = new StringBuilder( 300 );
			for ( Entry<String, ConfigEntry> entry : entries.entrySet() ) {
				ConfigEntry value = entry.getValue();
				if ( ( !filteredRequired || value.forFiltered ) &&
						( !generatedRequired || value.forGenerated ) ) {
					sb.append( entry.getKey() );
					sb.append( " = \"" );
					sb.append( entry.getValue() );
					sb.append( "\"\n" );
				}
			}
			return sb.toString();
		}
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
