package org.openslx.vm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.device.ControllerUsb;
import org.openslx.libvirt.domain.device.DiskCdrom;
import org.openslx.libvirt.domain.device.DiskFloppy;
import org.openslx.libvirt.domain.device.DiskStorage;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.domain.device.Sound;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.vm.VmMetaData.EtherType;
import org.openslx.vm.VmMetaData.EthernetDevType;
import org.openslx.vm.VmMetaData.SoundCardType;
import org.openslx.vm.VmMetaData.UsbSpeed;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImageTestResources;
import org.openslx.vm.disk.DiskImage.ImageFormat;

public class QemuMetaDataTest
{
	private static Domain getPrivateDomainFromQemuMetaData( QemuMetaData qemuMetadata )
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field privateDomainField = QemuMetaData.class.getDeclaredField( "vmConfig" );
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
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final String displayName = vmConfig.getDisplayName();

		assertEquals( "archlinux", displayName );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@Test
	@DisplayName( "Test machine snapshot state from VM configuration" )
	public void testQemuMetaDataIsMachineSnapshot()
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final boolean isVmSnapshot = vmConfig.isMachineSnapshot();

		assertEquals( false, isVmSnapshot );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@Test
	@DisplayName( "Test supported image formats from VM configuration" )
	public void testQemuMetaDataGetSupportedImageFormats()
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final List<DiskImage.ImageFormat> supportedImageFormats = vmConfig.getSupportedImageFormats();

		assertNotNull( supportedImageFormats );
		assertEquals( 3, supportedImageFormats.size() );
		assertEquals( true, supportedImageFormats
				.containsAll( Arrays.asList( ImageFormat.QCOW2, ImageFormat.VMDK, ImageFormat.VDI ) ) );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@Test
	@DisplayName( "Test output of HDDs from VM configuration" )
	public void testQemuMetaDataGetHdds()
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final List<VmMetaData.HardDisk> hdds = vmConfig.getHdds();

		assertNotNull( hdds );
		assertEquals( 1, hdds.size() );
		assertEquals( "/var/lib/libvirt/images/archlinux.qcow2", hdds.get( 0 ).diskImage );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@Test
	@DisplayName( "Test output of unfiltered VM configuration" )
	public void testQemuMetaDataGetDefinitionArray()
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numberOfDeletedElements = 1;

		final String unfilteredXmlConfig = new String( vmConfig.getDefinitionArray(), StandardCharsets.UTF_8 );
		final String originalXmlConfig = FileUtils.readFileToString( file, StandardCharsets.UTF_8 );

		assertNotNull( unfilteredXmlConfig );

		final int lengthUnfilteredXmlConfig = unfilteredXmlConfig.split( System.lineSeparator() ).length;
		final int lengthOriginalXmlConfig = originalXmlConfig.split( System.lineSeparator() ).length;

		assertEquals( lengthOriginalXmlConfig, lengthUnfilteredXmlConfig + numberOfDeletedElements );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@Test
	@DisplayName( "Test output of filtered VM configuration" )
	public void testQemuMetaDataGetFilteredDefinitionArray()
			throws UnsupportedVirtualizerFormatException, IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numberOfDeletedElements = 2;

		final String filteredXmlConfig = new String( vmConfig.getFilteredDefinitionArray(), StandardCharsets.UTF_8 );
		final String originalXmlConfig = FileUtils.readFileToString( file, StandardCharsets.UTF_8 );

		assertNotNull( filteredXmlConfig );

		final int lengthFilteredXmlConfig = filteredXmlConfig.split( System.lineSeparator() ).length;
		final int lengthOriginalXmlConfig = originalXmlConfig.split( System.lineSeparator() ).length;

		assertEquals( lengthOriginalXmlConfig, lengthFilteredXmlConfig + numberOfDeletedElements );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test add HDD to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-hdd.xml" } )
	public void testQemuMetaDataAddHdd( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numHddsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskStorageDevices().size();
		final int numHddsQemuMetaDataBeforeAdd = vmConfig.hdds.size();

		vmConfig.addHddTemplate( diskFile, null, null );

		final int numHddsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskStorageDevices().size();
		final int numHddsQemuMetaDataAfterAdd = vmConfig.hdds.size();

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

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test add CDROM to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-cdrom.xml" } )
	public void testQemuMetaDataAddCdrom( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numCdromsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		vmConfig.addCdrom( 0, diskFile.getAbsolutePath() );

		final int numCdromsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		assertTrue( numCdromsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numCdromsLibvirtDomainXmlAfterAdd > 0 );

		DiskCdrom addedCdromDevice = vmLibvirtDomainConfig.getDiskCdromDevices().get( 0 );
		assertEquals( diskFile.getAbsolutePath(), addedCdromDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test add physical CDROM drive to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-cdrom.xml" } )
	public void testQemuMetaDataAddPhysicalCdromDrive( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numCdromsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		vmConfig.addCdrom( 0, null );

		final int numCdromsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskCdromDevices().size();

		assertTrue( numCdromsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numCdromsLibvirtDomainXmlAfterAdd > 0 );

		DiskCdrom addedCdromDevice = vmLibvirtDomainConfig.getDiskCdromDevices().get( 0 );
		assertEquals( QemuMetaData.CDROM_DEFAULT_PHYSICAL_DRIVE, addedCdromDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test add floppy to VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-floppy.xml" } )
	public void testQemuMetaDataAddFloppy( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File diskFile = DiskImageTestResources.getDiskFile( "image-default.qcow2" );
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numFloppiesLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getDiskFloppyDevices().size();

		vmConfig.addFloppy( 0, diskFile.getAbsolutePath(), true );

		final int numFloppiesLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getDiskFloppyDevices().size();

		assertTrue( numFloppiesLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numFloppiesLibvirtDomainXmlAfterAdd > 0 );

		DiskFloppy addedFloppyDevice = vmLibvirtDomainConfig.getDiskFloppyDevices().get( 0 );
		assertTrue( addedFloppyDevice.isReadOnly() );
		assertEquals( diskFile.getAbsolutePath(), addedFloppyDevice.getStorageSource() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test add CPU core count to VM configuration" )
	@ValueSource( ints = { 2, 4, 6, 8 } )
	public void testQemuMetaDataAddCpuCoreCount( int coreCount )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( "qemu-kvm_default-archlinux-vm.xml" );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		vmConfig.addCpuCoreCount( coreCount );

		assertEquals( coreCount, vmLibvirtDomainConfig.getVCpu() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test get sound card from VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-sound.xml" } )
	public void testQemuMetaDataGetSoundCardType( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		SoundCardType soundCardType = vmConfig.getSoundCard();

		if ( vmLibvirtDomainConfig.getSoundDevices().isEmpty() ) {
			assertEquals( SoundCardType.NONE, soundCardType );
		} else {
			assertEquals( SoundCardType.HD_AUDIO, soundCardType );
		}

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test set sound card in VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-sound.xml" } )
	public void testQemuMetaDataSetSoundCardType( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numSoundDevsLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getSoundDevices().size();

		vmConfig.setSoundCard( SoundCardType.SOUND_BLASTER );

		final int numSoundDevsLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getSoundDevices().size();

		assertTrue( numSoundDevsLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numSoundDevsLibvirtDomainXmlAfterAdd > 0 );

		Sound addedSoundDevice = vmLibvirtDomainConfig.getSoundDevices().get( 0 );
		assertEquals( Sound.Model.SB16, addedSoundDevice.getModel() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test get ethernet device type from VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-nic.xml" } )
	public void testQemuMetaDataGetEthernetDevType( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		EthernetDevType ethernetDeviceType = vmConfig.getEthernetDevType( 0 );

		if ( vmLibvirtDomainConfig.getInterfaceDevices().isEmpty() ) {
			assertEquals( EthernetDevType.NONE, ethernetDeviceType );
		} else {
			assertEquals( EthernetDevType.PARAVIRT, ethernetDeviceType );
		}

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test set ethernet device type in VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-nic.xml" } )
	public void testQemuMetaDataSetEthernetDevType( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		vmConfig.setEthernetDevType( 0, EthernetDevType.E1000E );

		if ( !vmLibvirtDomainConfig.getInterfaceDevices().isEmpty() ) {
			Interface addedEthernetDevice = vmLibvirtDomainConfig.getInterfaceDevices().get( 0 );
			assertEquals( Interface.Model.E1000E, addedEthernetDevice.getModel() );
		}

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test get maximal USB speed from VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-usb.xml" } )
	public void testQemuMetaDataGetMaxUsbSpeed( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		UsbSpeed maxUsbSpeed = vmConfig.getMaxUsbSpeed();

		if ( vmLibvirtDomainConfig.getUsbControllerDevices().isEmpty() ) {
			assertEquals( UsbSpeed.NONE, maxUsbSpeed );
		} else {
			assertEquals( UsbSpeed.USB3_0, maxUsbSpeed );
		}

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}

	@ParameterizedTest
	@DisplayName( "Test set maximal USB speed in VM configuration" )
	@ValueSource( strings = { "qemu-kvm_default-archlinux-vm.xml", "qemu-kvm_default-archlinux-vm-no-usb.xml" } )
	public void testQemuMetaDataSetMaxUsbSpeed( String xmlFileName )
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

		final int numUsbControllersLibvirtDomainXmlBeforeAdd = vmLibvirtDomainConfig.getUsbControllerDevices().size();

		vmConfig.setMaxUsbSpeed( UsbSpeed.USB2_0 );

		final int numUsbControllersLibvirtDomainXmlAfterAdd = vmLibvirtDomainConfig.getUsbControllerDevices().size();

		assertTrue( numUsbControllersLibvirtDomainXmlBeforeAdd >= 0 );
		assertTrue( numUsbControllersLibvirtDomainXmlAfterAdd > 0 );

		ControllerUsb addedUsbControllerDevice = vmLibvirtDomainConfig.getUsbControllerDevices().get( 0 );
		assertEquals( ControllerUsb.Model.ICH9_EHCI1, addedUsbControllerDevice.getModel() );

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
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
			throws UnsupportedVirtualizerFormatException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		File file = LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName );
		QemuMetaData vmConfig = new QemuMetaData( null, file );

		final Domain vmLibvirtDomainConfig = QemuMetaDataTest.getPrivateDomainFromQemuMetaData( vmConfig );

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
			assertEquals( QemuMetaData.NETWORK_BRIDGE_LAN_DEFAULT, addedEthernetDevice.getSource() );
			break;
		case HOST_ONLY:
			assertEquals( Interface.Type.BRIDGE, addedEthernetDevice.getType() );
			assertEquals( Interface.Model.VIRTIO, addedEthernetDevice.getModel() );
			assertEquals( QemuMetaData.NETWORK_BRIDGE_HOST_ONLY_DEFAULT, addedEthernetDevice.getSource() );
			break;
		case NAT:
			assertEquals( Interface.Type.BRIDGE, addedEthernetDevice.getType() );
			assertEquals( Interface.Model.VIRTIO, addedEthernetDevice.getModel() );
			assertEquals( QemuMetaData.NETWORK_BRIDGE_NAT_DEFAULT, addedEthernetDevice.getSource() );
			break;
		}

		assertDoesNotThrow( () -> vmLibvirtDomainConfig.validateXml() );
	}
}
