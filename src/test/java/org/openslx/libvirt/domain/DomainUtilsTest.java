package org.openslx.libvirt.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.domain.device.HostdevPciDeviceAddress;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;

/**
 * Unit tests for the {@link DomainUtils} PCI helper methods.
 *
 * @author Test
 * @version 1.0
 */
public class DomainUtilsTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		Configurator.setRootLevel( Level.OFF );
	}

	public static Domain getDomain( String xmlFileName )
	{
		Domain domain = null;

		try {
			domain = new Domain( LibvirtXmlTestResources.getLibvirtXmlStream( xmlFileName ) );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
			String errorMsg = "Cannot prepare requested Libvirt domain XML file from the resources folder";
			throw new RuntimeException( errorMsg, e );
		}

		return domain;
	}

	// ============================================================================
	// Tests for buildPciSlotUsageMap()
	// ============================================================================

	@Test
	@DisplayName( "buildPciSlotUsageMap - slots 0 and 1 are pre-marked as reserved" )
	public void testBuildPciSlotUsageMap_reservedSlots()
	{
		Domain domain = DomainTest.getDomain( "qemu-kvm_default-archlinux-vm.xml" );
		int[] inUse = DomainUtils.buildPciSlotUsageMap( domain );

		assertEquals( Integer.MAX_VALUE, inUse[ 0 ], "Slot 0 should be reserved" );
		assertEquals( Integer.MAX_VALUE, inUse[ 1 ], "Slot 1 should be reserved" );
	}

	@Test
	@DisplayName( "buildPciSlotUsageMap - detects PCI devices on primary bus from ubuntu VM" )
	public void testBuildPciSlotUsageMap_detectsPrimaryBusDevices()
	{
		Domain domain = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		int[] inUse = DomainUtils.buildPciSlotUsageMap( domain );

		// The ubuntu VM has several devices on bus 0x00:
		// - USB controllers at slot 0x1d (29)
		// - SATA controller at slot 0x1f (31)
		// - PCIE root ports at slot 0x02 (2)
		// - Sound at slot 0x1b (27)
		// - Video at slot 0x01 (1) - but slot 1 is reserved

		// Slot 2 should be marked (PCIE root ports use it)
		assertTrue( inUse[ 2 ] != 0, "Slot 2 should be marked as used (PCIE root ports)" );

		// Slot 27 (0x1b) should be marked (sound device)
		assertTrue( inUse[ 27 ] != 0, "Slot 27 should be marked as used (sound device)" );

		// Slot 29 (0x1d) should be marked (USB controllers)
		assertTrue( inUse[ 29 ] != 0, "Slot 29 should be marked as used (USB controllers)" );

		// Slot 31 (0x1f) should be marked (SATA controller)
		assertTrue( inUse[ 31 ] != 0, "Slot 31 should be marked as used (SATA controller)" );
	}

	@Test
	@DisplayName( "buildPciSlotUsageMap - ignores devices on non-primary bus" )
	public void testBuildPciSlotUsageMap_ignoresNonPrimaryBus()
	{
		Domain domain = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_mdev.xml" );
		int[] inUse = DomainUtils.buildPciSlotUsageMap( domain );

		// The mdev hostdev device is at bus 0x07, slot 0x00 - should be ignored
		// because it's not on the primary bus (bus 0)
		// Slot 0 should only be marked as reserved, not because of the mdev device
		assertEquals( Integer.MAX_VALUE, inUse[ 0 ] );
	}

	// ============================================================================
	// Tests for findFreePciDeviceSlot()
	// ============================================================================

	@Test
	@DisplayName( "findFreePciDeviceSlot - returns first free slot for fresh map" )
	public void testFindFreePciDeviceSlot_returnsFirstFreeSlot()
	{
		// Use a VM with minimal PCI devices on bus 0
		Domain domain = DomainTest.getDomain( "qemu-kvm_default-archlinux-vm.xml" );
		int[] inUse = DomainUtils.buildPciSlotUsageMap( domain );

		// archlinux VM has slot 0x02 used by PCIE root ports, slot 0x1b (27) by sound, slot 0x1f (31) by SATA
		// First free slot should be 3 (after reserved 0, 1 and used slot 2)
		// 3-arg constructor: (bus, device, function), so (5, 0, 0) creates domain=0, bus=5, device=0
		HostdevPciDeviceAddress testAddress = new HostdevPciDeviceAddress( 5, 0, 0 );
		int slot = DomainUtils.findFreePciDeviceSlot( inUse, testAddress );

		assertEquals( 3, slot, "First free slot should be 3 (after reserved 0, 1 and used slot 2)" );
	}

	@Test
	@DisplayName( "findFreePciDeviceSlot - reuses slot if same address already assigned" )
	public void testFindFreePciDeviceSlot_reusesExistingAddress()
	{
		// Create a fresh map with only reserved slots
		int[] inUse = new int[ 64 ];
		inUse[ 0 ] = Integer.MAX_VALUE;
		inUse[ 1 ] = Integer.MAX_VALUE;
		// Slot 2 is free

		// 3-arg constructor: (bus, device, function), so this creates domain=0, bus=3, device=5, function=0
		HostdevPciDeviceAddress testAddress = new HostdevPciDeviceAddress( 3, 5, 0 );
		// lookup must match: domain=0, bus=3, device=5
		int lookup = ( 0 << 16 ) | ( 3 << 8 ) | 5;
		inUse[ 10 ] = lookup; // Already assigned at slot 10

		int slot = DomainUtils.findFreePciDeviceSlot( inUse, testAddress );

		assertEquals( 10, slot, "Should reuse the existing slot for the same address" );
	}

	@Test
	@DisplayName( "findFreePciDeviceSlot - marks slot as used after assignment" )
	public void testFindFreePciDeviceSlot_marksSlotAsUsed()
	{
		// Create a fresh map with only reserved slots
		int[] inUse = new int[ 64 ];
		inUse[ 0 ] = Integer.MAX_VALUE;
		inUse[ 1 ] = Integer.MAX_VALUE;

		// 3-arg constructor: (bus, device, function), so this creates domain=0, bus=5, device=0, function=0
		HostdevPciDeviceAddress testAddress = new HostdevPciDeviceAddress( 5, 0, 0 );
		// lookup = (0 << 16) | (5 << 8) | 0 = 1280
		int expectedLookup = ( 0 << 16 ) | ( 5 << 8 ) | 0;

		int slot = DomainUtils.findFreePciDeviceSlot( inUse, testAddress );

		assertEquals( 2, slot, "First free slot should be 2" );
		assertEquals( expectedLookup, inUse[ 2 ], "Slot should be marked with the lookup value" );
	}

	@Test
	@DisplayName( "findFreePciDeviceSlot - finds next free slot when first is taken" )
	public void testFindFreePciDeviceSlot_findsNextFreeSlot()
	{
		// Create a fresh map with only reserved slots
		int[] inUse = new int[ 64 ];
		inUse[ 0 ] = Integer.MAX_VALUE;
		inUse[ 1 ] = Integer.MAX_VALUE;

		// First call: (bus=5, device=0, fn=0) should return slot 2
		HostdevPciDeviceAddress addr1 = new HostdevPciDeviceAddress( 5, 0, 0 );
		int slot1 = DomainUtils.findFreePciDeviceSlot( inUse, addr1 );
		assertEquals( 2, slot1, "First free slot should be 2" );

		// Second call: (bus=6, device=0, fn=0) should return slot 3 (next free)
		HostdevPciDeviceAddress addr2 = new HostdevPciDeviceAddress( 6, 0, 0 );
		int slot2 = DomainUtils.findFreePciDeviceSlot( inUse, addr2 );
		assertEquals( 3, slot2, "Second free slot should be 3" );
	}

	@Test
	@DisplayName( "findFreePciDeviceSlot - throws exception when no slots available" )
	public void testFindFreePciDeviceSlot_throwsWhenFull()
	{
		// Create a completely full map
		int[] inUse = new int[ 64 ];
		for ( int i = 0; i < inUse.length; i++ ) {
			inUse[ i ] = Integer.MAX_VALUE;
		}

		HostdevPciDeviceAddress testAddress = new HostdevPciDeviceAddress( 0, 5, 0 );

		assertThrows( IllegalStateException.class,
				() -> DomainUtils.findFreePciDeviceSlot( inUse, testAddress ),
				"Should throw IllegalStateException when no slots are available" );
	}

	// ============================================================================
	// Integration test with real VM configuration
	// ============================================================================

	@Test
	@DisplayName( "PCI slot assignment - ubuntu VM with multiple assignments" )
	public void testPciSlotAssignmentWithUbuntuVm()
	{
		Domain domain = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		int[] inUse = DomainUtils.buildPciSlotUsageMap( domain );

		// Simulate adding a GPU passthrough device (domain=1, bus=2, device=3, function=0)
		HostdevPciDeviceAddress gpuAddress = new HostdevPciDeviceAddress( 0x0001, 0x02, 0x03, 0 );
		int gpuSlot = DomainUtils.findFreePciDeviceSlot( inUse, gpuAddress );

		// Should get a free slot (not conflicting with existing devices)
		assertTrue( gpuSlot > 1, "GPU slot should be > 1 (reserved slots)" );
		assertTrue( inUse[ gpuSlot ] != 0, "GPU slot should be marked as used" );

		// Add audio device for the GPU (different device number, same bus)
		// Note: lookup only uses domain:bus:device, not function
		HostdevPciDeviceAddress audioAddress = new HostdevPciDeviceAddress( 0x0001, 0x02, 0x04, 0 );
		int audioSlot = DomainUtils.findFreePciDeviceSlot( inUse, audioAddress );

		// Audio should get a different slot than GPU
		assertTrue( audioSlot != gpuSlot, "Audio and GPU should have different slots" );
		assertTrue( inUse[ audioSlot ] != 0, "Audio slot should be marked as used" );
	}

	@Test
	@DisplayName( "PCI slot assignment - same device gets same slot on re-assignment" )
	public void testPciSlotAssignment_sameDeviceReused()
	{
		// Create a fresh map with only reserved slots
		int[] inUse = new int[ 64 ];
		inUse[ 0 ] = Integer.MAX_VALUE;
		inUse[ 1 ] = Integer.MAX_VALUE;

		// 3-arg constructor: (bus, device, function), so (2, 3, 0) creates domain=0, bus=2, device=3
		HostdevPciDeviceAddress deviceAddress = new HostdevPciDeviceAddress( 2, 3, 0 );

		// First assignment
		int slot1 = DomainUtils.findFreePciDeviceSlot( inUse, deviceAddress );
		assertEquals( 2, slot1, "First assignment should get slot 2" );

		// Second assignment of same device should get same slot
		int slot2 = DomainUtils.findFreePciDeviceSlot( inUse, deviceAddress );
		assertEquals( slot1, slot2, "Same device should get the same slot on re-assignment" );
	}
}
