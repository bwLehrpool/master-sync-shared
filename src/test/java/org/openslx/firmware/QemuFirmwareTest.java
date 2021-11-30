package org.openslx.firmware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class QemuFirmwareTest
{
	@Test
	@DisplayName( "Test parsing of valid QEMU firmware specification file" )
	public void testQemuFirmwareFromFwSpecValid()
	{
		final File fwSpecFile = QemuFirmwareTestResources.getQemuFirmwareSpecFile( "60-edk2-x86_64.json" );

		final QemuFirmware firmware = QemuFirmware.fromFwSpec( fwSpecFile );

		assertNotNull( firmware );
		assertEquals( "UEFI firmware for x86_64", firmware.getDescription() );
		assertEquals( 1, firmware.getInterfaceTypes().size() );
		assertEquals( "uefi", firmware.getInterfaceTypes().get( 0 ) );
		assertEquals( "/usr/share/qemu/edk2-x86_64-code.fd", firmware.getMapping().getExecutable().getFileName() );
		assertEquals( "/usr/share/qemu/edk2-i386-vars.fd", firmware.getMapping().getNvramTemplate().getFileName() );
		assertEquals( 1, firmware.getTargets().size() );
		assertEquals( "x86_64", firmware.getTargets().get( 0 ).getArchitecture() );
		assertEquals( 2, firmware.getTargets().get( 0 ).getMachines().size() );
		assertEquals( "pc-i440fx-*", firmware.getTargets().get( 0 ).getMachines().get( 0 ) );
		assertEquals( "pc-q35-*", firmware.getTargets().get( 0 ).getMachines().get( 1 ) );
		assertEquals( 3, firmware.getFeatures().size() );
		assertEquals( 0, firmware.getTags().size() );
	}

	@Test
	@DisplayName( "Test parsing of invalid QEMU firmware specification file" )
	public void testQemuFirmwareFromFwSpecInvalid()
	{
		final QemuFirmware firmware = QemuFirmware.fromFwSpec( null );

		assertNull( firmware );
	}
}
