package org.openslx.firmware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.DomainTest;

public class QemuFirmwareUtilTest
{
	@Test
	@DisplayName( "Test that lookup of OS loader from Libvirt domain file succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValid() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();
		final Domain config = DomainTest
				.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_transform-non-persistent_uefi.xml" );

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath, config.getOsLoader(),
				config.getOsArch(), config.getOsMachine() );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of OS loader from Archlinux path succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValidArchlinux() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				"/usr/share/edk2-ovmf/x64/OVMF_CODE.fd", "x86_64", "pc-q35-5.0" );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of OS loader from Debian path succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValidDebian() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				"/usr/share/OVMF/OVMF_CODE.fd", "x86_64", "pc-q35-5.0" );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of OS loader from CentOS path succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValidCentOs() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				"/usr/share/edk2/ovmf/OVMF_CODE.cc.fd", "x86_64", "pc-q35-5.0" );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of OS loader from OpenSUSE path succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValidOpenSuse() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				"/usr/share/qemu/ovmf-x86_64-4m-code.bin", "x86_64", "pc-q35-5.0" );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of OS loader from Ubuntu path succeeds" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderValidUbuntu() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		final String targetOsLoader = QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				"/usr/share/OVMF/OVMF_CODE_4M.fd", "x86_64", "pc-q35-5.0" );

		assertEquals( Paths.get( "/usr/share/qemu/edk2-x86_64-code.fd" ).toString(), targetOsLoader );
	}

	@Test
	@DisplayName( "Test that lookup of non-existent OS loader for non-existent architecture fails" )
	public void testQemuFirmwareUtilLookupTargetOsLoaderInvalid() throws FirmwareException
	{
		final String fwSpecPath = QemuFirmwareTestResources.getQemuFirmwareSpecPath();

		assertThrows( FirmwareException.class, () -> QemuFirmwareUtil.lookupTargetOsLoader( fwSpecPath,
				Paths.get( "/non/existent/loader.fd" ).toString(), "x87", "pc-q35-6.0" ) );
	}
}
