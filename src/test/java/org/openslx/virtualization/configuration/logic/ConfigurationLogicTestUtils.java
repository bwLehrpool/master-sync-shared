package org.openslx.virtualization.configuration.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.xmlunit.assertj.XmlAssert;

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

	private static final String REGEX_UUID = "<(Machine|HardDisk|Image)(.*)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	private static final String REGEX_SOURCE_FILE_PATHS = "(<source.*file=\")([^\"]*)";

	public static VirtualizationConfiguration newVirtualizationConfigurationInstance( File configFile )
	{
		VirtualizationConfiguration config = null;

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

	public static void assertXmlEqual( String expectedXml, String actualXml ) throws AssertionError
	{
		XmlAssert.assertThat( actualXml ).and( expectedXml ).ignoreComments().areIdentical();
	}

	private static String removeSourceFilePaths( String content )
	{
		final Pattern patternSourceFilePaths = Pattern.compile( ConfigurationLogicTestUtils.REGEX_SOURCE_FILE_PATHS );
		final Matcher matcherSourceFilePathsContent = patternSourceFilePaths.matcher( content );

		// replace all source file paths with the empty String
		return matcherSourceFilePathsContent.replaceAll( "$1" );
	}

	private static String removeUuid( String content )
	{
		final Pattern patternUuid = Pattern.compile( ConfigurationLogicTestUtils.REGEX_UUID );
		final Matcher matcherUuidContent = patternUuid.matcher( content );

		// replace all UUIDs with the empty String
		return matcherUuidContent.replaceAll( "<$1$200000000-0000-0000-0000-000000000000" );
	}

	public static void assertXmlLibvirtEqual( String expectedXml, String actualXml ) throws AssertionError
	{
		// replace all source file paths with the empty String
		final String filteredXml1 = ConfigurationLogicTestUtils.removeSourceFilePaths( expectedXml );
		final String filteredXml2 = ConfigurationLogicTestUtils.removeSourceFilePaths( actualXml );

		ConfigurationLogicTestUtils.assertXmlEqual( filteredXml1, filteredXml2 );
	}

	public static void assertXmlVirtualBoxEqual( String expectedXml, String actualXml ) throws AssertionError
	{
		// replace all UUIDs with the zero UUID in the generated XML as it's random
		final String actualXmlFiltered = ConfigurationLogicTestUtils.removeUuid( actualXml );

		ConfigurationLogicTestUtils.assertXmlEqual( expectedXml, actualXmlFiltered );
	}

	public static void assertVmxVmwareEqual( String expectedVmx, String actualVmx ) throws AssertionError
	{
		final BufferedReader bfrVmx1 = new BufferedReader( new StringReader( expectedVmx ) );
		final BufferedReader bfrVmx2 = new BufferedReader( new StringReader( actualVmx ) );
		final List<String> linesVmx1 = bfrVmx1.lines().collect( Collectors.toList() );
		final List<String> linesVmx2 = bfrVmx2.lines().collect( Collectors.toList() );

		// check output size first
		if ( linesVmx1.size() != linesVmx2.size() ) {
			// create list of items that are expected but missing in the actual output
			final List<String> missingItems;
			final String missingItemsDesc;

			if ( linesVmx1.size() > linesVmx2.size() ) {
				missingItemsDesc = "The following items are expected but missing in the actual output";
				missingItems = new ArrayList<String>( linesVmx1 );
				missingItems.removeAll( linesVmx2 );
			} else {
				missingItemsDesc = "The following items are not expected but occuring in the actual output";
				missingItems = new ArrayList<String>( linesVmx2 );
				missingItems.removeAll( linesVmx1 );
			}

			throw new AssertionError( String.format(
					"VMX output size is not satisfied: Expected %d lines, but output has %d lines!\n"
							+ "%s:\n"
							+ "%s",
					linesVmx1.size(), linesVmx2.size(), missingItemsDesc, missingItems ) );
		}

		// check the content of the output line by line
		assertEquals( linesVmx1, linesVmx2 );
	}
}
