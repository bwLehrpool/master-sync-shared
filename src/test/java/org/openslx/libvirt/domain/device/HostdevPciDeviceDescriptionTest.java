package org.openslx.libvirt.domain.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HostdevPciDeviceDescriptionTest
{
	@Test
	@DisplayName( "Test that a PCI device description instance is not created if invalid values are specified" )
	public void testHostdevPciDeviceDescriptionInstanceInvalid()
	{
		assertThrows( IllegalArgumentException.class,
				() -> new HostdevPciDeviceDescription( Integer.MIN_VALUE, 0x293a ) );
		assertThrows( IllegalArgumentException.class,
				() -> new HostdevPciDeviceDescription( 0x8086, Integer.MAX_VALUE ) );
	}

	@Test
	@DisplayName( "Test that a PCI device description instance is parsed from a valid String" )
	public void testHostdevPciDeviceDescriptionValueOfValid()
	{
		final HostdevPciDeviceDescription pciDeviceDesc = HostdevPciDeviceDescription.valueOf( "8086:293a" );

		assertNotNull( pciDeviceDesc );
		assertEquals( 0x8086, pciDeviceDesc.getVendorId() );
		assertEquals( "8086", pciDeviceDesc.getVendorIdAsString() );
		assertEquals( 0x293a, pciDeviceDesc.getDeviceId() );
		assertEquals( "293a", pciDeviceDesc.getDeviceIdAsString() );
	}

	@Test
	@DisplayName( "Test that no PCI device description instance is parsed from an invalid String" )
	public void testHostdevPciDeviceDescriptionValueOfInvalid()
	{
		final HostdevPciDeviceDescription pciDeviceDesc = HostdevPciDeviceDescription.valueOf( "bba93e215" );

		assertNull( pciDeviceDesc );
	}

	@Test
	@DisplayName( "Test that two PCI device description instances are equal" )
	public void testHostdevPciDeviceDescriptionEquals()
	{
		final HostdevPciDeviceDescription pciDeviceDesc1 = new HostdevPciDeviceDescription( 0x8086, 0x293a );
		final HostdevPciDeviceDescription pciDeviceDesc2 = new HostdevPciDeviceDescription( 0x8086, 0x293a );

		assertTrue( pciDeviceDesc1.equals( pciDeviceDesc2 ) );
	}

	@Test
	@DisplayName( "Test that two PCI device description instances are not equal" )
	public void testHostdevPciDeviceDescriptionNotEquals()
	{
		final HostdevPciDeviceDescription pciDeviceDesc1 = new HostdevPciDeviceDescription( 0x8086, 0x293a );
		final HostdevPciDeviceDescription pciDeviceDesc2 = new HostdevPciDeviceDescription( 0x293a, 0x8086 );

		assertFalse( pciDeviceDesc1.equals( pciDeviceDesc2 ) );
	}

	@Test
	@DisplayName( "Test that a PCI device description can be dumped to a String" )
	public void testHostdevPciDeviceDescriptionToString()
	{
		final HostdevPciDeviceDescription pciDeviceDesc = new HostdevPciDeviceDescription( 0x00b1, 0x293a );

		assertEquals( "00b1:293a", pciDeviceDesc.toString() );
	}
}
