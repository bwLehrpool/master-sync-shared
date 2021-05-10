package org.openslx.virtualization.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openslx.virtualization.Version;
import org.openslx.vm.disk.DiskImage.ImageFormat;

public class VirtualizationConfigurationVirtualBoxTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@ParameterizedTest
	@DisplayName( "Test version from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetConfigurationVersion( String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		assertEquals( configVersion, vmConfig.getConfigurationVersion() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test display name from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetDisplayName( String configFileName, Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final String displayName = vmConfig.getDisplayName();

		assertEquals( VirtualizationConfigurationVirtualBoxTest.getVmName( configVersion ), displayName );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test machine snapshot state from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxIsMachineSnapshot( String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final boolean isVmSnapshot = vmConfig.isMachineSnapshot();

		assertFalse( isVmSnapshot );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test supported image formats from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetSupportedImageFormats( String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final List<ImageFormat> supportedImageFormats = vmConfig.getVirtualizer().getSupportedImageFormats();

		assertNotNull( supportedImageFormats );
		assertEquals( 1, supportedImageFormats.size() );
		assertTrue( supportedImageFormats.containsAll( Arrays.asList( ImageFormat.VDI ) ) );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test output of HDDs from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetHdds( String configFileName, Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final List<VirtualizationConfiguration.HardDisk> hdds = vmConfig.getHdds();

		final String imageFileName = VirtualizationConfigurationVirtualBoxTest.getVmName( configVersion ) + ".vdi";

		assertNotNull( hdds );
		assertEquals( 1, hdds.size() );
		assertEquals( imageFileName, hdds.get( 0 ).diskImage );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	static String getVmName( Version version )
	{
		return "ubuntu_" + version.toString().replace( '.', '-' );
	}

	static Stream<Arguments> configAndVersionProvider()
	{
		return Stream.of(
				arguments( "virtualbox_default-ubuntu_v1-15.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "15" ) ) ),
				arguments( "virtualbox_default-ubuntu_v1-16.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "16" ) ) ),
				arguments( "virtualbox_default-ubuntu_v1-17.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "17" ) ) ),
				arguments( "virtualbox_default-ubuntu_v1-18.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "18" ) ) ) );
	}
}
