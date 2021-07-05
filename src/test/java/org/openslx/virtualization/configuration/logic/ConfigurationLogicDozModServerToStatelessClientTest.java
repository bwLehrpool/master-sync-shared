package org.openslx.virtualization.configuration.logic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModServerToStatelessClient;
import org.openslx.virtualization.configuration.transformation.TransformationException;

public class ConfigurationLogicDozModServerToStatelessClientTest
{
	private static final String DEFAULT_DISPLAY_NAME = "Test";
	private static final String DEFAULT_OS_ID = null;
	private static final boolean DEFAULT_HAS_USB_ACCESS = true;

	private static final ConfigurationDataDozModServerToStatelessClient DEFAULT_CONFIG_DATA = new ConfigurationDataDozModServerToStatelessClient(
			ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_DISPLAY_NAME,
			ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_OS_ID,
			ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_HAS_USB_ACCESS );

	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-server and a stateless client for Libvirt/QEMU configuration" )
	public void testConfigurationLogicDozModServerToStatelessClientLibvirt() throws TransformationException
	{
		final String inputConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm_transform-privacy.xml";
		final String expectedConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm_transform-non-persistent.xml";
		final File inputConfig = LibvirtXmlTestResources.getLibvirtXmlFile( inputConfigFileName );
		final File expectedConfig = LibvirtXmlTestResources.getLibvirtXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModServerToStatelessClient logic = new ConfigurationLogicDozModServerToStatelessClient();

		logic.apply( config, ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-server and a stateless client for VirtualBox configuration" )
	public void testConfigurationLogicDozModServerToStatelessClientVirtualBox() throws TransformationException
	{
		final String inputConfigFileName = "virtualbox_default-ubuntu_transform-privacy.vbox";
		final String expectedConfigFileName = "virtualbox_default-ubuntu_transform-non-persistent.vbox";
		final File inputConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModServerToStatelessClient logic = new ConfigurationLogicDozModServerToStatelessClient();

		logic.apply( config, ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue(
				ConfigurationLogicTestUtils.isVirtualBoxContentEqual( expectedTransformedConfig, transformedConfig ) );

		// do not validate the VirtualBox configuration afterwards, since the inserted
		// place holders do not match valid primitive values from the XML schema
		//assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between dozmod-server and a stateless client for VMware configuration" )
	public void testConfigurationLogicDozModServerToStatelessClientVmware() throws TransformationException
	{
		final String inputConfigFileName = "vmware-player_default-ubuntu_transform-privacy.vmx";
		final String expectedConfigFileName = "vmware-player_default-ubuntu_transform-non-persistent.vmx";
		final File inputConfig = ConfigurationLogicTestResources.getVmwareVmxFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVmwareVmxFile( expectedConfigFileName );
		final VirtualizationConfiguration config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		assertTrue( config.getHdds().size() == 1 );
		final ConfigurationLogicDozModServerToStatelessClient logic = new ConfigurationLogicDozModServerToStatelessClient();

		logic.apply( config, ConfigurationLogicDozModServerToStatelessClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}
}
