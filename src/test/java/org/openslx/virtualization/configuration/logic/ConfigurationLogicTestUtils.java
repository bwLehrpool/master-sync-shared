package org.openslx.virtualization.configuration.logic;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.virtualization.configuration.VirtualizationConfiguration;

public class ConfigurationLogicTestUtils
{
	// @formatter:off
	public static final List<OperatingSystem> STUB_OS_LIST = Collections.unmodifiableList( Arrays.asList(
			new OperatingSystem(  1, "Windows 7 (64 Bit)",        null, "AMD64", 196608, 256 ),
			new OperatingSystem(  2, "Windows 8 (32 Bit)",        null, "x86",     4096,  32 ),
			new OperatingSystem(  3, "Windows 8 (64 Bit)",        null, "AMD64", 131072, 256 ),
			new OperatingSystem(  4, "Ubuntu (32 Bit)",           null, "x86",        0,   0 ),
			new OperatingSystem(  5, "Ubuntu (64 Bit)",           null, "AMD64",      0,   0 ),
			new OperatingSystem(  6, "OpenSUSE (32 Bit)",         null, "x86",        0,   0 ),
			new OperatingSystem(  7, "OpenSUSE (64 Bit)",         null, "AMD64",      0,   0 ),
			new OperatingSystem(  8, "Other Linux (32 Bit)",      null, "x86",        0,   0 ),
			new OperatingSystem(  9, "Other Linux (64 Bit)",      null, "AMD64",      0,   0 ),
			new OperatingSystem( 10, "Windows 7 (32 Bit)",        null, "x86",     4096,  32 ),
			new OperatingSystem( 11, "Windows 2000 Professional", null, "x86",     4096,   4 ) ) );
	// @formatter:on

	private static final String REGEX_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	public static VirtualizationConfiguration<?, ?, ?, ?> newVirtualizationConfigurationInstance( File configFile )
	{
		VirtualizationConfiguration<?, ?, ?, ?> config = null;

		try {
			config = VirtualizationConfiguration.getInstance( ConfigurationLogicTestUtils.STUB_OS_LIST, configFile );
		} catch ( IOException e ) {
			fail( "Virtualization configuration file '" + configFile.getName() + "' can not be processed!" );
		}

		if ( config == null ) {
			fail( "Virtualization configuration can not be created from file '" + configFile.getName() + "'" );
		}

		return config;
	}

	public static String readFileToString( File file )
	{
		String content = null;

		try {
			content = FileUtils.readFileToString( file, StandardCharsets.UTF_8 );
		} catch ( IOException e ) {
			fail( "Could not read content of file '" + file.getName() + "'" );
		}

		return content;
	}

	public static boolean isContentEqual( String content1, String content2 )
	{
		final BufferedReader bfrContent1 = new BufferedReader( new StringReader( content1 ) );
		final BufferedReader bfrContent2 = new BufferedReader( new StringReader( content2 ) );
		final List<String> linesContent1 = bfrContent1.lines().collect( Collectors.toList() );
		final List<String> linesContent2 = bfrContent2.lines().collect( Collectors.toList() );

		Collections.sort( linesContent1 );
		Collections.sort( linesContent2 );

		return linesContent1.equals( linesContent2 );
	}

	public static String removeUuid( String content )
	{
		final Pattern patternUuid = Pattern.compile( ConfigurationLogicTestUtils.REGEX_UUID );
		final Matcher matcherUuidContent = patternUuid.matcher( content );

		// replace all UUIDs with the empty String
		return matcherUuidContent.replaceAll( "" );
	}

	public static boolean isVirtualBoxContentEqual( String content1, String content2 )
	{
		// replace all UUIDs with the empty String
		final String filteredContent1 = ConfigurationLogicTestUtils.removeUuid( content1 );
		final String filteredContent2 = ConfigurationLogicTestUtils.removeUuid( content2 );

		return ConfigurationLogicTestUtils.isContentEqual( filteredContent1, filteredContent2 );
	}
}
