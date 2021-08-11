package org.openslx.libvirt.domain.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HostdevMdevDeviceAddressTest
{
	@Test
	@DisplayName( "Test that a mediated device address instance is parsed from a valid String" )
	public void testHostdevMdevDeviceAddressValueOfValid()
	{
		final UUID deviceAddress = UUID.randomUUID();
		final HostdevMdevDeviceAddress mdevDeviceAddr = HostdevMdevDeviceAddress
				.valueOf( deviceAddress.toString() );

		assertNotNull( mdevDeviceAddr );
		assertEquals( deviceAddress, mdevDeviceAddr.getDeviceAddress() );
		assertEquals( deviceAddress.toString(), mdevDeviceAddr.getDeviceAddressAsString() );
	}

	@Test
	@DisplayName( "Test that no mediated device address instance is parsed from an invalid String" )
	public void testHostdevMdevDeviceAddressValueOfInvalid()
	{
		final HostdevMdevDeviceAddress mdevDeviceAddr = HostdevMdevDeviceAddress.valueOf( "0xaffe" );

		assertNull( mdevDeviceAddr );
	}

	@Test
	@DisplayName( "Test that two mediated device address instances are equal" )
	public void testHostdevMdevDeviceAddressEquals()
	{
		final HostdevMdevDeviceAddress mdevDeviceAddr1 = new HostdevMdevDeviceAddress(
				new UUID( 0xdeadaffe, 0xaffedead ) );
		final HostdevMdevDeviceAddress mdevDeviceAddr2 = new HostdevMdevDeviceAddress(
				new UUID( 0xdeadaffe, 0xaffedead ) );

		assertTrue( mdevDeviceAddr1.equals( mdevDeviceAddr2 ) );
	}

	@Test
	@DisplayName( "Test that two mediated device address instances are not equal" )
	public void testHostdevMdevDeviceAddressNotEquals()
	{
		final HostdevMdevDeviceAddress mdevDeviceAddr1 = new HostdevMdevDeviceAddress(
				new UUID( 0xdeadaffe, 0xaffedead ) );
		final HostdevMdevDeviceAddress mdevDeviceAddr2 = new HostdevMdevDeviceAddress(
				new UUID( 0xaffedead, 0xdeadaffe ) );

		assertFalse( mdevDeviceAddr1.equals( mdevDeviceAddr2 ) );
	}

	@Test
	@DisplayName( "Test that a mediated device address can be dumped to a String" )
	public void testHostdevMdevDeviceAddressToString()
	{
		final UUID deviceAddr = UUID.randomUUID();
		final HostdevMdevDeviceAddress mdevDeviceAddr = new HostdevMdevDeviceAddress( deviceAddr );

		assertEquals( deviceAddr.toString(), mdevDeviceAddr.toString() );
	}
}
