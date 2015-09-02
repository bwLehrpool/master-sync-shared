package org.openslx.util.vm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openslx.util.Util;

public class VmwareConfig
{

	private static final Logger LOGGER = Logger.getLogger( VmwareConfig.class );

	private Map<String, ConfigEntry> entries = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );

	public VmwareConfig()
	{
		// (void)
	}

	public VmwareConfig( File file ) throws IOException
	{
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

	public VmwareConfig( InputStream is ) throws IOException
	{
		int todo = Math.max( 4000, Math.min( 100000, is.available() ) );
		int offset = 0;
		byte[] data = new byte[ todo ];
		while ( todo > 0 ) {
			int ret = is.read( data, offset, todo );
			if ( ret <= 0 )
				break;
			todo -= ret;
			offset += ret;
		}
		init( data, offset );
	}

	public VmwareConfig( byte[] vmxContent, int length )
	{
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
			BufferedReader reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( vmxContent, 0, length ), cs ) );
			String line;
			while ( ( line = reader.readLine() ) != null ) {
				KeyValuePair entry = parse( line );
				if ( entry != null ) {
					set( entry.key, unescape( entry.value ) );
				}
			}
		} catch ( IOException e ) {
			LOGGER.warn( "Exception when loading vmx from byte array (how!?)", e );
		}
	}

	private String unescape( String value )
	{
		String ret = value;
		if ( ret.contains( "|22" ) ) {
			ret = ret.replace( "|22", "\"" );
		}
		if ( ret.contains( "|7C" ) ) {
			ret.replace( "|7C", "|" );
		}
		return ret;
	}

	private String detectCharset( InputStream is )
	{
		try {
			BufferedReader csDetectReader = new BufferedReader( new InputStreamReader( is, StandardCharsets.ISO_8859_1 ) );
			String line;
			while ( ( line = csDetectReader.readLine() ) != null ) {
				KeyValuePair entry = parse( line );
				if ( entry == null )
					continue;
				if ( entry.key.equals( ".encoding" ) || entry.key.equals( "encoding" ) ) {
					return entry.value;
				}
			}
		} catch ( Exception e ) {
			LOGGER.warn( "Could not detect charset, fallback to latin1", e );
		}
		// Dumb fallback
		return "ISO-8859-1";
	}

	public Set<Entry<String, ConfigEntry>> entrySet()
	{
		return entries.entrySet();
	}

	private static final Pattern settingMatcher1 = Pattern.compile( "^\\s*(#?[a-z0-9\\.\\:_]+)\\s*=\\s*\"(.*)\"\\s*$",
			Pattern.CASE_INSENSITIVE );
	private static final Pattern settingMatcher2 = Pattern.compile( "^\\s*(#?[a-z0-9\\.\\:_]+)\\s*=\\s*([^\"]*)\\s*$",
			Pattern.CASE_INSENSITIVE );

	private KeyValuePair parse( String line )
	{
		Matcher matcher = settingMatcher1.matcher( line );
		if ( !matcher.matches() ) {
			matcher = settingMatcher2.matcher( line );
		}
		if ( !matcher.matches() ) {
			return null;
		}
		return new KeyValuePair(
				matcher.group( 1 ), matcher.group( 2 ) );

	}

	public ConfigEntry set( String key, String value, boolean replace )
	{
		if ( !replace && entries.containsKey( key ) )
			return null;
		ConfigEntry ce = new ConfigEntry( value );
		entries.put( key, ce );
		return ce;
	}

	public ConfigEntry set( String key, String value )
	{
		return set( key, value, true );
	}

	public ConfigEntry set( KeyValuePair entry )
	{
		return set( entry.key, entry.value );
	}

	public String get( String key )
	{
		ConfigEntry ce = entries.get( key );
		if ( ce == null )
			return null;
		return ce.value;
	}

	public String toString( boolean filteredRequired, boolean generatedRequired )
	{
		set( ".encoding", "UTF-8" );
		StringBuilder sb = new StringBuilder( 300 );
		for ( Entry<String, ConfigEntry> entry : entries.entrySet() ) {
			ConfigEntry value = entry.getValue();
			if ( ( !filteredRequired || value.forFiltered ) &&
					( !generatedRequired || value.forGenerated ) ) {
				sb.append( entry.getKey() );
				sb.append( " = \"" );
				sb.append( value.getEscaped() );
				sb.append( "\"\n" );
			}
		}
		return sb.toString();
	}

	@Override
	public String toString()
	{
		return toString( false, false );
	}

	public static class ConfigEntry
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

		public String getEscaped()
		{
			String ret = value;
			if ( ret.contains( "|" ) ) {
				ret = ret.replace( "|", "|7C" );
			}
			if ( ret.contains( "\"" ) ) {
				ret = ret.replace( "\"", "|22" );
			}
			return ret;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue( String value )
		{
			this.value = value;
		}

	}

}
