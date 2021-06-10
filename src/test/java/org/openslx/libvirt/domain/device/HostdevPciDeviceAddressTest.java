package org.openslx.libvirt.domain.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HostdevPciDeviceAddressTest
{
	@Test
	@DisplayName( "Test that a PCI device address instance is not created if invalid values are specified" )
	public void testHostdevPciDeviceAddressInstanceInvalid()
	{
		assertThrows( IllegalArgumentException.class,
				() -> new HostdevPciDeviceAddress( Integer.MIN_VALUE, 0x03, 0x0a, 0x7 ) );
		assertThrows( IllegalArgumentException.class,
				() -> new HostdevPciDeviceAddress( 0x72ab, 0x0c, 0x1a, Integer.MAX_VALUE ) );
	}

	@Test
	@DisplayName( "Test that a PCI device address instance is parsed from a valid String" )
	public void testHostdevPciDeviceAddressValueOfValid()
	{
		final HostdevPciDeviceAddress pciDeviceAddr = HostdevPciDeviceAddress.valueOf( "002b:2a:1f.1" );

		assertNotNull( pciDeviceAddr );
		assertEquals( 0x002b, pciDeviceAddr.getPciDomain() );
		assertEquals( "002b", pciDeviceAddr.getPciDomainAsString() );
		assertEquals( 0x2a, pciDeviceAddr.getPciBus() );
		assertEquals( "2a", pciDeviceAddr.getPciBusAsString() );
		assertEquals( 0x1f, pciDeviceAddr.getPciDevice() );
		assertEquals( "1f", pciDeviceAddr.getPciDeviceAsString() );
		assertEquals( 0x1, pciDeviceAddr.getPciFunction() );
		assertEquals( "1", pciDeviceAddr.getPciFunctionAsString() );
	}

	@Test
	@DisplayName( "Test that no PCI device address instance is parsed from an invalid String" )
	public void testHostdevPciDeviceAddressValueOfInvalid()
	{
		final HostdevPciDeviceAddress pciDeviceAddr = HostdevPciDeviceAddress.valueOf( "0000b2ac1f31" );

		assertNull( pciDeviceAddr );
	}

	@Test
	@DisplayName( "Test that two PCI device address instances are equal" )
	public void testHostdevPciDeviceAddressEquals()
	{
		final HostdevPciDeviceAddress pciDeviceAddr1 = new HostdevPciDeviceAddress( 0x0000, 0x2a, 0x1f, 0x1 );
		final HostdevPciDeviceAddress pciDeviceAddr2 = new HostdevPciDeviceAddress( 0x0000, 0x2a, 0x1f, 0x1 );

		assertTrue( pciDeviceAddr1.equals( pciDeviceAddr2 ) );
	}

	@Test
	@DisplayName( "Test that two PCI device address instances are not equal" )
	public void testHostdevPciDeviceAddressNotEquals()
	{
		final HostdevPciDeviceAddress pciDeviceAddr1 = new HostdevPciDeviceAddress( 0x0000, 0x2a, 0x1f, 0x1 );
		final HostdevPciDeviceAddress pciDeviceAddr2 = new HostdevPciDeviceAddress( 0x0000, 0x2a, 0x1f, 0x2 );

		assertFalse( pciDeviceAddr1.equals( pciDeviceAddr2 ) );
	}

	@Test
	@DisplayName( "Test that a PCI device address can be dumped to a String" )
	public void testHostdevPciDeviceAddressToString()
	{
		final HostdevPciDeviceAddress pciDeviceAddr = new HostdevPciDeviceAddress( 0x0000, 0x2a, 0x1f, 0x1 );

		assertEquals( "0000:2a:1f.1", pciDeviceAddr.toString() );
	}
}
