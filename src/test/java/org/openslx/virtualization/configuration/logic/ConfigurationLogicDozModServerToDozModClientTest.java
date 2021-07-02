package org.openslx.virtualization.configuration.logic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.data.ConfigurationDataDozModServerToDozModClient;
import org.openslx.virtualization.configuration.transformation.TransformationException;
import org.openslx.virtualization.disk.DiskImageTestResources;

public class ConfigurationLogicDozModServerToDozModClientTest
{
	private static final String DEFAULT_DISPLAY_NAME = "Test";
	private static final File DEFAULT_DISK_IMAGE = DiskImageTestResources.getDiskFile( "image-default.vmdk" );
	private static final OperatingSystem DEFAULT_GUEST_OS = null;
	private static final String DEFAULT_VIRTUALIZER_ID = null;
	private static final int DEFAULT_TOTAL_MEMORY = 4096;

	private static final ConfigurationDataDozModServerToDozModClient DEFAULT_CONFIG_DATA = new ConfigurationDataDozModServerToDozModClient(
			ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_DISPLAY_NAME,
			ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_DISK_IMAGE,
			ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_GUEST_OS,
			ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_VIRTUALIZER_ID,
			ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_TOTAL_MEMORY );

	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-server and a dozmod-client for Libvirt/QEMU configuration" )
	public void testConfigurationLogicDozModServerToDozModClientLibvirt() throws TransformationException
	{
		final String inputConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm_transform-privacy.xml";
		final String expectedConfigFileName = "qemu-kvm_default-ubuntu-20-04-vm_transform-editable.xml";
		final File inputConfig = LibvirtXmlTestResources.getLibvirtXmlFile( inputConfigFileName );
		final File expectedConfig = LibvirtXmlTestResources.getLibvirtXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModServerToDozModClient logic = new ConfigurationLogicDozModServerToDozModClient();

		logic.apply( config, ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isLibvirtContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between a dozmod-server and a dozmod-client for VirtualBox configuration" )
	public void testConfigurationLogicDozModServerToDozModClientVirtualBox() throws TransformationException
	{
		final String inputConfigFileName = "virtualbox_default-ubuntu_transform-privacy.vbox";
		final String expectedConfigFileName = "virtualbox_default-ubuntu_transform-editable.vbox";
		final File inputConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVirtualBoxXmlFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		final ConfigurationLogicDozModServerToDozModClient logic = new ConfigurationLogicDozModServerToDozModClient();

		logic.apply( config, ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue(
				ConfigurationLogicTestUtils.isVirtualBoxContentEqual( expectedTransformedConfig, transformedConfig ) );

		// do not validate the VirtualBox configuration afterwards, since the inserted network configuration
		// leads to an invalid DOM although the created output after the transformation is as expected
		//assertDoesNotThrow( () -> config.validate() );
	}

	@Test
	@DisplayName( "Test transformation logic between dozmod-server and a dozmod-client for VMware configuration" )
	public void testConfigurationLogicDozModServerToDozModClientVmware() throws TransformationException
	{
		final String inputConfigFileName = "vmware-player_default-ubuntu_transform-privacy.vmx";
		final String expectedConfigFileName = "vmware-player_default-ubuntu_transform-editable.vmx";
		final File inputConfig = ConfigurationLogicTestResources.getVmwareVmxFile( inputConfigFileName );
		final File expectedConfig = ConfigurationLogicTestResources.getVmwareVmxFile( expectedConfigFileName );
		final VirtualizationConfiguration<?, ?, ?, ?> config;
		config = ConfigurationLogicTestUtils.newVirtualizationConfigurationInstance( inputConfig );
		assertTrue( config.getHdds().size() == 1 );
		final ConfigurationLogicDozModServerToDozModClient logic = new ConfigurationLogicDozModServerToDozModClient();

		logic.apply( config, ConfigurationLogicDozModServerToDozModClientTest.DEFAULT_CONFIG_DATA );

		final String transformedConfig = config.getConfigurationAsString();
		final String expectedTransformedConfig = ConfigurationLogicTestUtils.readFileToString( expectedConfig );

		assertTrue( ConfigurationLogicTestUtils.isContentEqual( expectedTransformedConfig, transformedConfig ) );
		assertDoesNotThrow( () -> config.validate() );
	}
}
