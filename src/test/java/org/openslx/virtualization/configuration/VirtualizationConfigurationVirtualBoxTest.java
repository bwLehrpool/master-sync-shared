package org.openslx.virtualization.configuration;

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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;

public class VirtualizationConfigurationVirtualBoxTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		Configurator.setRootLevel( Level.OFF );
	}

	@ParameterizedTest
	@DisplayName( "Test version from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetConfigurationVersion( String name, String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		assertEquals( configVersion, vmConfig.getConfigurationVersion() );
	}

	@ParameterizedTest
	@DisplayName( "Test display name from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetDisplayName( String name, String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final String displayName = vmConfig.getDisplayName();

		assertEquals( VirtualizationConfigurationVirtualBoxTest.getVmName( name, configVersion ), displayName );
	}

	@ParameterizedTest
	@DisplayName( "Test machine snapshot state from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxIsMachineSnapshot( String name, String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final boolean isVmSnapshot = vmConfig.isMachineSnapshot();

		assertFalse( isVmSnapshot );
	}

	@ParameterizedTest
	@DisplayName( "Test supported image formats from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetSupportedImageFormats( String name, String configFileName,
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
	}

	@ParameterizedTest
	@DisplayName( "Test output of HDDs from VM configuration" )
	@MethodSource( "configAndVersionProvider" )
	public void testVirtualizationConfigurationVirtualBoxGetHdds( String name, String configFileName,
			Version configVersion )
			throws IOException, VirtualizationConfigurationException
	{
		final File configFile = VirtualizationConfigurationTestResources.getVirtualBoxXmlFile( configFileName );
		final VirtualizationConfigurationVirtualBox vmConfig = new VirtualizationConfigurationVirtualBox( null,
				configFile );

		final List<VirtualizationConfiguration.HardDisk> hdds = vmConfig.getHdds();

		final String imageFileName = VirtualizationConfigurationVirtualBoxTest.getVmName( name, configVersion ) + ".vdi";

		assertNotNull( hdds );
		assertEquals( 1, hdds.size() );
		assertEquals( imageFileName, hdds.get( 0 ).diskImage );
	}

	static String getVmName( String name, Version version )
	{
		return name + "_" + version.toString().replace( '.', '-' );
	}

	static Stream<Arguments> configAndVersionProvider()
	{
		return Stream.of(
				arguments( "ubuntu", "virtualbox_default-ubuntu_v1-15.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "15" ) ) ),
				arguments( "ubuntu", "virtualbox_default-ubuntu_v1-16.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "16" ) ) ),
				arguments( "ubuntu", "virtualbox_default-ubuntu_v1-17.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "17" ) ) ),
				arguments( "ubuntu", "virtualbox_default-ubuntu_v1-18.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "18" ) ) ),
				arguments( "windows-7", "virtualbox_default-windows-7_v1-15.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "15" ) ) ),
				arguments( "windows-7", "virtualbox_default-windows-7_v1-16.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "16" ) ) ),
				arguments( "windows-7", "virtualbox_default-windows-7_v1-17.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "17" ) ) ),
				arguments( "windows-7", "virtualbox_default-windows-7_v1-18.vbox",
						new Version( Short.valueOf( "1" ), Short.valueOf( "18" ) ) ) );
	}
}
