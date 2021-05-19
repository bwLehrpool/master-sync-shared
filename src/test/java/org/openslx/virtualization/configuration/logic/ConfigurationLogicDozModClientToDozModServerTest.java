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
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModClientToDozModServer;
import org.openslx.virtualization.configuration.transformation.TransformationException;

public class ConfigurationLogicDozModClientToDozModServerTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-client and a dozmod-server for Libvirt/QEMU configuration" )
	public void testConfigurationLogicDozModClientToDozModServerLibvirt() throws TransformationException
	{
		final String inputConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm.xml";
		final String expectedConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm_transform-privacy.xml";
		final File inputConfig = LibvirtXmlTestResources.getLibvirtXmlFile( inputConfigFileName );
		final File expectedConfig = LibvirtXmlTestResources.getLibvirtXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModClientToDozModServer logic = new ConfigurationLogicDozModClientToDozModServer();

		logic.apply( config, new ConfigurationDataDozModClientToDozModServer() );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-client and a dozmod-server for VirtualBox configuration" )
	public void testConfigurationLogicDozModClientToDozModServerVirtualBox() throws TransformationException
	{
		final String inputConfigFileName = "virtualbox_default-ubuntu.vbox";
		final String expectedConfigFileName = "virtualbox_default-ubuntu_transform-privacy.vbox";
		final File inputConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModClientToDozModServer logic = new ConfigurationLogicDozModClientToDozModServer();

		logic.apply( config, new ConfigurationDataDozModClientToDozModServer() );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );

		// do not validate the VirtualBox configuration afterwards, since the inserted
		// place holders do not match valid primitive values from the XML schema
		//assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-client and a dozmod-server for VMware configuration" )
	public void testConfigurationLogicDozModClientToDozModServerVmware() throws TransformationException
	{
		final String inputConfigFileName = "vmware-player_default-ubuntu.vmx";
		final String expectedConfigFileName = "vmware-player_default-ubuntu_transform-privacy.vmx";
		final File inputConfig = ConfigurationLogicTestResources.getVmwareVmxFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVmwareVmxFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModClientToDozModServer logic = new ConfigurationLogicDozModClientToDozModServer();

		logic.apply( config, new ConfigurationDataDozModClientToDozModServer() );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}
}
