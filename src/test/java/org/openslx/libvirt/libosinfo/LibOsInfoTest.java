package org.openslx.libvirt.libosinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.libosinfo.os.Os;
import org.openslx.virtualization.Version;

public class LibOsInfoTest
{
	@Test
	@DisplayName( "Test the lookup of an operating system" )
	public void testOsLookup()
	{
		final String osId = "http://ubuntu.com/ubuntu/20.04";
		final Os os = LibOsInfo.lookupOs( osId );

		assertNotNull( os );

		assertEquals( osId, os.getId() );
		assertEquals( "Ubuntu 20.04", os.getName() );
		assertEquals( "linux", os.getFamily() );
		assertEquals( "ubuntu", os.getDistro() );
		assertEquals( new Version( Short.valueOf( "20" ), Short.valueOf( "04" ) ), os.getVersion() );
	}
}
