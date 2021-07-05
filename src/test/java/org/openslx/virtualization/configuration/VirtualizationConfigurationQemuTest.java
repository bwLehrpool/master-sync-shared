package org.openslx.virtualization.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.device.DiskCdrom;
import org.openslx.libvirt.domain.device.DiskFloppy;
import org.openslx.libvirt.domain.device.DiskStorage;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.ConfigurableOptionGroup;
import org.openslx.virtualization.configuration.VirtualizationConfiguration.EtherType;
import org.openslx.virtualization.configuration.logic.ConfigurationLogicTestUtils;
import org.openslx.virtualization.disk.DiskImage;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;
import org.openslx.virtualization.disk.DiskImageTestResources;
import org.openslx.virtualization.hardware.ConfigurationGroups;

public class VirtualizationConfigurationQemuTest
{
	public static final List<OperatingSystem> STUB_OS_LIST = ConfigurationLogicTestUtils.STUB_OS_LIST;

	private static Domain getPrivateDomainFromQemuMetaData( VirtualizationConfigurationQemu qemuMetadata )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field privateDomainField = VirtualizationConfigurationQemu.class.getDeclaredField( "vmConfig" );
		privateDomainField.setAccessible( true );
		return Domain.class.cast( privateDomainField.get( qemuMetadata ) );
	}

	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@Test
	@DisplayName( "Test display name from VM configuration" )
	public void testQemuMetaDataGetDisplayName()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final String displayName = vmConfig.getDisplayName();

		assertEquals( "archlinux", displayName );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test machine snapshot state from VM configuration" )
	public void testQemuMetaDataIsMachineSnapshot()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final boolean isVmSnapshot = vmConfig.isMachineSnapshot();

		assertEquals( false, isVmSnapshot );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test supported image formats from VM configuration" )
	public void testQemuMetaDataGetSupportedImageFormats()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final List<DiskImage.ImageFormat> supportedImageFormats = vmConfig.getVirtualizer().getSupportedImageFormats();

		assertNotNull( supportedImageFormats );
		assertEquals( 3, supportedImageFormats.size() );
		assertEquals( true, supportedImageFormats
				.containsAll( Arrays.asList( ImageFormat.QCOW2, ImageFormat.VMDK, ImageFormat.VDI ) ) );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test output of detected 32-bit OS from VM configuration" )
	public void testQemuMetaDataGetOs32Bit()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		final File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-ubuntu-20-04-vm_i686.xml" );
		final VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu(
				VirtualizationConfigurationQemuTest.STUB_OS_LIST, file );

		final OperatingSystem os = vmConfig.getOs();

		assertNotNull( os );
		assertEquals( VirtualizationConfigurationQemuTest.STUB_OS_LIST.get( 3 ), os );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test output of detected 64-bit OS from VM configuration" )
	public void testQemuMetaDataGetOs64Bit()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		final File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		final VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu(
				VirtualizationConfigurationQemuTest.STUB_OS_LIST, file );

		final OperatingSystem os = vmConfig.getOs();

		assertNotNull( os );
		assertEquals( VirtualizationConfigurationQemuTest.STUB_OS_LIST.get( 4 ), os );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test output of HDDs from VM configuration" )
	public void testQemuMetaDataGetHdds()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final List<VirtualizationConfiguration.HardDisk> hdds = vmConfig.getHdds();

		assertNotNull( hdds );
		assertEquals( 1, hdds.size() );
		assertEquals( "/var/lib/libvirt/images/archlinux.qcow2", hdds.get( 0 ).diskImage );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@Test
	@DisplayName( "Test output of unfiltered VM configuration" )
	public void testQemuMetaDataGetDefinitionArray()
			throws VirtualizationConfigurationException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final String unfilteredXmlConfig = new String( vmConfig.getConfigurationAsByteArray(), StandardCharsets.UTF_8 );
		final String originalXmlConfig = FileUtils.readFileToString( file, StandardCharsets.UTF_8 );

		assertNotNull( unfilteredXmlConfig );

		final int lengthUnfilteredXmlConfig = unfilteredXmlConfig.split( System.lineSeparator() ).length;
		final int lengthOriginalXmlConfig = originalXmlConfig.split( System.lineSeparator() ).length;

		assertEquals( lengthOriginalXmlConfig, lengthUnfilteredXmlConfig );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test add HDD to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-hdd.xml" } )
	public void testQemuMetaDataAddHdd( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numHddsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskStorageDevices().size();
		final int numHddsQemuMetaDataBeforeAdd = vmConfig.getHdds().size();

		vmConfig.addHddTemplate( diskFile, null, null );

		final int numHddsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskStorageDevices().size();
		final int numHddsQemuMetaDataAfterAdd = vmConfig.getHdds().size();

		assertTrue( numHddsLibvirtDomainXmlBeforeAdd == numHddsQemuMetaDataBeforeAdd );
		assertTrue( numHddsLibvirtDomainXmlAfterAdd == numHddsQemuMetaDataAfterAdd );
		assertTrue( numHddsQemuMetaDataBeforeAdd >= 0 );
		assertTrue( numHddsQemuMetaDataAfterAdd > 0 );

		if ( numHddsQemuMetaDataBeforeAdd >= 1 ) {
			// update existing HDD in the Libvirt XML config, but do not add a new HDD
			assertEquals( numHddsQemuMetaDataBeforeAdd, numHddsQemuMetaDataAfterAdd );
		} else {
			// numHddsQemuMetaDataBeforeAdd == 0
			// add a HDD to the Libvirt XML config, since there was no HDD available
			assertEquals( numHddsQemuMetaDataBeforeAdd + 1, numHddsQemuMetaDataAfterAdd );
		}

		DiskStorage addedStorageDevice = vmLibvirtDomainConfig.getDiskStorageDevices().get( 0 );
		assertEquals( diskFile.getAbsolutePath(), addedStorageDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test add CDROM to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-cdrom.xml" } )
	public void testQemuMetaDataAddCdrom( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numCdromsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		vmConfig.addCdrom( 0, diskFile.getAbsolutePath() );

		final int numCdromsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		assertTrue( numCdromsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numCdromsLibvirtDomainXmlAfterAdd > 0 );

		DiskCdrom addedCdromDevice = vmLibvirtDomainConfig.getDiskCdromDevices().get( 0 );
		assertEquals( diskFile.getAbsolutePath(), addedCdromDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test add physical CDROM drive to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-cdrom.xml" } )
	public void testQemuMetaDataAddPhysicalCdromDrive( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numCdromsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		vmConfig.addCdrom( 0, null );

		final int numCdromsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		assertTrue( numCdromsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numCdromsLibvirtDomainXmlAfterAdd > 0 );

		DiskCdrom addedCdromDevice = vmLibvirtDomainConfig.getDiskCdromDevices().get( 0 );
		assertEquals( VirtualizationConfigurationQemu.CDROM_DEFAULT_PHYSICAL_DRIVE, addedCdromDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test add floppy to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-floppy.xml" } )
	public void testQemuMetaDataAddFloppy( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numFloppiesLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskFloppyDevices().size();

		vmConfig.addFloppy( 0, diskFile.getAbsolutePath(), true );

		final int numFloppiesLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskFloppyDevices().size();

		assertTrue( numFloppiesLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numFloppiesLibvirtDomainXmlAfterAdd > 0 );

		DiskFloppy addedFloppyDevice = vmLibvirtDomainConfig.getDiskFloppyDevices().get( 0 );
		assertTrue( addedFloppyDevice.isReadOnly() );
		assertEquals( diskFile.getAbsolutePath(), addedFloppyDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test add CPU core count to VM configuration" )
	@ValueSource( ints = { 2, 4, 6, 8 } )
	public void testQemuMetaDataAddCpuCoreCount( int coreCount )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		vmConfig.addCpuCoreCount( coreCount );

		assertEquals( coreCount, vmLibvirtDomainConfig.getVCpu() );

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test get ethernet device type from VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-nic.xml" } )
	public void testQemuMetaDataGetEthernetDevType( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		List<ConfigurableOptionGroup> groups = vmConfig.getConfigurableOptions();

		for ( ConfigurableOptionGroup group : groups ) {
			if ( group.groupIdentifier != ConfigurationGroups.NIC_MODEL )
				continue;
			if ( vmLibvirtDomainConfig.getInterfaceDevices().isEmpty() ) {
				assertEquals( null, group.getSelected() );
			} else {
				assertEquals( Interface.Model.VIRTIO.toString(), group.getSelected().getId() );
			}
		}

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	static Stream<Arguments> configAndEthernetTypeProvider()
	{
		return Stream.of(
				arguments( "qemu-kvm_default-archlinux-vm.xml", EtherType.BRIDGED ),
				arguments( "qemu-kvm_default-archlinux-vm.xml", EtherType.HOST_ONLY ),
				arguments( "qemu-kvm_default-archlinux-vm.xml", EtherType.NAT ),
				arguments( "qemu-kvm_default-archlinux-vm-no-usb.xml", EtherType.BRIDGED ),
				arguments( "qemu-kvm_default-archlinux-vm-no-usb.xml", EtherType.HOST_ONLY ),
				arguments( "qemu-kvm_default-archlinux-vm-no-usb.xml", EtherType.NAT ) );
	}

	@ParameterizedTest
	@DisplayName( "Test add ethernet device to VM configuration" )
	@MethodSource( "configAndEthernetTypeProvider" )
	public void testQemuMetaDataAddEthernet( String xmlFileName, EtherType ethernetType )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numEthernetDevsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getInterfaceDevices().size();

		vmConfig.addEthernet( ethernetType );

		final int numEthernetDevsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getInterfaceDevices().size();

		assertTrue( numEthernetDevsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numEthernetDevsLibvirtDomainXmlAfterAdd > 0 );

		Interface addedEthernetDevice = vmLibvirtDomainConfig.getInterfaceDevices().get( 0 );
		switch ( ethernetType ) {
		case BRIDGED:
			assertEquals( Interface.Type.BRIDGE, addedEthernetDevice.getType() );
			assertEquals( Interface.Model.VIRTIO, addedEthernetDevice.getModel() );
			assertEquals( VirtualizationConfigurationQemu.NETWORK_BRIDGE_LAN_DEFAULT, addedEthernetDevice.getSource() );
			break;
		case HOST_ONLY:
			assertEquals( Interface.Type.BRIDGE, addedEthernetDevice.getType() );
			assertEquals( Interface.Model.VIRTIO, addedEthernetDevice.getModel() );
			assertEquals( VirtualizationConfigurationQemu.NETWORK_BRIDGE_HOST_ONLY_DEFAULT,
					addedEthernetDevice.getSource() );
			break;
		case NAT:
			assertEquals( Interface.Type.BRIDGE, addedEthernetDevice.getType() );
			assertEquals( Interface.Model.VIRTIO, addedEthernetDevice.getModel() );
			assertEquals( VirtualizationConfigurationQemu.NETWORK_BRIDGE_NAT_DEFAULT, addedEthernetDevice.getSource() );
			break;
		}

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test get virtualizer HW version from VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm-old-os.xml", "qemu-kvm_default-archlinux-vm-no-os.xml" } )
	public void testQemuMetaDataGetVirtualizerVersion( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final Version machineVersion = vmConfig.getVirtualizerVersion();

		if ( vmLibvirtDomainConfig.getOsMachine() == null ) {
			assertNull( machineVersion );
		} else {
			assertEquals( new Version( Short.valueOf( "3" ), Short.valueOf( "1" ) ), machineVersion );
		}

		assertDoesNotThrow( () -> vmConfig.validate() );
	}

	@ParameterizedTest
	@DisplayName( "Test set virtualizer HW version in VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm-old-os.xml", "qemu-kvm_default-archlinux-vm-no-os.xml" } )
	public void testQemuMetaDataSetVirtualizerVersion( String xmlFileName )
			throws VirtualizationConfigurationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		VirtualizationConfigurationQemu vmConfig = new VirtualizationConfigurationQemu( null, file );

		final Domain vmLibvirtDomainConfig = VirtualizationConfigurationQemuTest
				.getPrivateDomainFromQemuMetaData( vmConfig );

		final String originalOsMachine = vmLibvirtDomainConfig.getOsMachine();
		if ( originalOsMachine != null ) {
			assertEquals( "pc-q35-3.1", originalOsMachine );
		}

		final Version modifiedVersion = new Version( Short.valueOf( "4" ), Short.valueOf( "1" ) );
		vmConfig.setVirtualizerVersion( modifiedVersion );

		final String modifiedOsMachine = vmLibvirtDomainConfig.getOsMachine();
		if ( modifiedOsMachine == null ) {
			assertNull( vmConfig.getVirtualizerVersion() );
		} else {
			assertEquals( modifiedVersion, vmConfig.getVirtualizerVersion() );
			assertEquals( "pc-q35-4.1", modifiedOsMachine );
		}

		assertDoesNotThrow( () -> vmConfig.validate() );
	}
}
