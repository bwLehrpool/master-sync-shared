package org.openslx.libvirt.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.domain.Domain.CpuCheck;
import org.openslx.libvirt.domain.Domain.CpuMode;
import org.openslx.libvirt.domain.Domain.OsType;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;

public class DomainTest
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
			String errorMsg = new String( "Cannot prepare requested Libvirt domain XML file from the resources folder" );
			fail( errorMsg );
		}

		return domain;
	}

	@Test
	@DisplayName( "Get VM type from libvirt XML file" )
	public void testGetType()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( Domain.Type.KVM.toString(), vm.getType().toString() );
	}

	@Test
	@DisplayName( "Set VM type from libvirt XML file" )
	public void testSetType()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setType( Domain.Type.QEMU );
		assertEquals( Domain.Type.QEMU.toString(), vm.getType().toString() );
	}

	@Test
	@DisplayName( "Get VM name from libvirt XML file" )
	public void testGetName()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "ubuntu-20-04", vm.getName() );
	}

	@Test
	@DisplayName( "Set VM name in libvirt XML file" )
	public void testSetName()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setName( "ubuntu-18-04" );
		assertEquals( "ubuntu-18-04", vm.getName() );
	}

	@Test
	@DisplayName( "Get VM title from libvirt XML file" )
	public void testGetTitle()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "Ubuntu 20.04", vm.getTitle() );
	}

	@Test
	@DisplayName( "Set VM title in libvirt XML file" )
	public void testSetTitle()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setTitle( "Ubuntu 18.04" );
		assertEquals( "Ubuntu 18.04", vm.getTitle() );
	}

	@Test
	@DisplayName( "Get VM description from libvirt XML file" )
	public void testGetDescription()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "Ubuntu 20.04 desktop installation", vm.getDescription() );
	}

	@Test
	@DisplayName( "Set VM description in libvirt XML file" )
	public void testSetDescription()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setDescription( "Ubuntu 18.04 server installation" );
		assertEquals( "Ubuntu 18.04 server installation", vm.getDescription() );
	}

	@Test
	@DisplayName( "Get VM libosinfo operating system identifier in libvirt XML file" )
	public void testGetLibOsInfoOsId()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "http://ubuntu.com/ubuntu/20.04", vm.getLibOsInfoOsId() );
	}

	@Test
	@DisplayName( "Get VM UUID from libvirt XML file" )
	public void testGetUuid()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "8dc5433c-0228-49e4-b019-fa2b606aa544", vm.getUuid() );
	}

	@Test
	@DisplayName( "Set VM UUID in libvirt XML file" )
	public void testSetUuid()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setUuid( "5ab08167-3d95-400e-ac83-e6af8d150971" );
		assertEquals( "5ab08167-3d95-400e-ac83-e6af8d150971", vm.getUuid() );
	}

	@Test
	@DisplayName( "Get VM memory from libvirt XML file" )
	public void testGetMemory()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( new BigInteger( "4294967296" ).toString(), vm.getMemory().toString() );
	}

	@Test
	@DisplayName( "Set VM memory in libvirt XML file" )
	public void testSetMemory()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setMemory( new BigInteger( "12073740288" ) );
		assertEquals( new BigInteger( "12073740288" ).toString(), vm.getMemory().toString() );
	}

	@Test
	@DisplayName( "Get current VM memory from libvirt XML file" )
	public void testGetCurrentMemory()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( new BigInteger( "4294967296" ).toString(), vm.getCurrentMemory().toString() );
	}

	@Test
	@DisplayName( "Set current VM memory in libvirt XML file" )
	public void testSetCurrentMemory()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCurrentMemory( new BigInteger( "8087237632" ) );
		assertEquals( new BigInteger( "8087237632" ).toString(), vm.getCurrentMemory().toString() );
	}

	@Test
	@DisplayName( "Get VM number of vCpus from libvirt XML file" )
	public void testGetVCpu()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 2, vm.getVCpu() );
	}

	@Test
	@DisplayName( "Set VM number of vCpus in libvirt XML file" )
	public void testSetVCpu()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setVCpu( 4 );
		assertEquals( 4, vm.getVCpu() );
	}

	@Test
	@DisplayName( "Get VM's OS type from libvirt XML file" )
	public void testGetOsType()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( OsType.HVM.toString(), vm.getOsType().toString() );
	}

	@Test
	@DisplayName( "Set VM's OS type in libvirt XML file" )
	public void testSetOsType()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsType( OsType.XEN );
		assertEquals( OsType.XEN.toString(), vm.getOsType().toString() );
	}

	@Test
	@DisplayName( "Get VM's OS architecture from libvirt XML file" )
	public void testGetOsArch()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "x86_64", vm.getOsArch() );
	}

	@Test
	@DisplayName( "Set VM's OS architecture in libvirt XML file" )
	public void testSetOsArch()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsArch( "aarch" );
		assertEquals( "aarch", vm.getOsArch() );
	}

	@Test
	@DisplayName( "Get VM's OS machine from libvirt XML file" )
	public void testGetOsMachine()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "pc-q35-5.1", vm.getOsMachine() );
	}

	@Test
	@DisplayName( "Set VM's OS machine in libvirt XML file" )
	public void testSetOsMachine()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsMachine( "pc" );
		assertEquals( "pc", vm.getOsMachine() );
	}

	@Test
	@DisplayName( "Get VM's OS loader from libvirt XML file" )
	public void testGetOsLoader()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_uefi.xml" );
		assertEquals( "/usr/share/edk2-ovmf/x64/OVMF_CODE.fd", vm.getOsLoader() );
	}

	@Test
	@DisplayName( "Set VM's OS loader in libvirt XML file" )
	public void testSetOsLoader()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_uefi.xml" );
		vm.setOsLoader( "/usr/share/qemu/edk2-x86_64-code.fd" );
		assertEquals( "/usr/share/qemu/edk2-x86_64-code.fd", vm.getOsLoader() );
	}

	@Test
	@DisplayName( "Get VM's OS Nvram from libvirt XML file" )
	public void testGetOsNvram()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_uefi.xml" );
		assertEquals( "/var/lib/libvirt/nvram/guest_VARS.fd", vm.getOsNvram() );
	}

	@Test
	@DisplayName( "Set VM's OS Nvram in libvirt XML file" )
	public void testSetOsNvram()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_uefi.xml" );
		vm.setOsNvram( "/tmp/nvram-tmp/tmp_VARS.fd" );
		assertEquals( "/tmp/nvram-tmp/tmp_VARS.fd", vm.getOsNvram() );
	}

	@Test
	@DisplayName( "Get VM CPU model from libvirt XML file" )
	public void testGetCpuModel()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getCpuModel() );
	}

	@Test
	@DisplayName( "Set VM CPU model in libvirt XML file" )
	public void testSetCpuModel()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuModel( "core2duo" );
		assertEquals( "core2duo", vm.getCpuModel() );
	}

	@Test
	@DisplayName( "Get VM CPU mode from libvirt XML file" )
	public void testGetCpuModelMode()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( CpuMode.HOST_MODEL.toString(), vm.getCpuMode().toString() );
	}

	@Test
	@DisplayName( "Set VM CPU mode in libvirt XML file" )
	public void testSetCpuModelMode()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuMode( CpuMode.HOST_PASSTHROUGH );
		assertEquals( CpuMode.HOST_PASSTHROUGH.toString(), vm.getCpuMode().toString() );
	}

	@Test
	@DisplayName( "Get VM CPU check from libvirt XML file" )
	public void testGetCpuCheck()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( CpuCheck.PARTIAL.toString(), vm.getCpuCheck().toString() );
	}

	@Test
	@DisplayName( "Set VM CPU check in libvirt XML file" )
	public void testSetCpuCheck()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuCheck( CpuCheck.NONE );
		assertEquals( CpuCheck.NONE.toString(), vm.getCpuCheck().toString() );
	}

	@Test
	@DisplayName( "Get VM CPU dies from libvirt XML file" )
	public void testGetCpuDies()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		assertEquals( 2, vm.getCpuDies() );
	}

	@Test
	@DisplayName( "Set VM CPU dies in libvirt XML file" )
	public void testSetCpuDies()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		vm.setCpuDies( 3 );
		assertEquals( 3, vm.getCpuDies() );
	}

	@Test
	@DisplayName( "Get VM CPU sockets from libvirt XML file" )
	public void testGetCpuSockets()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		assertEquals( 3, vm.getCpuSockets() );
	}

	@Test
	@DisplayName( "Set VM CPU sockets in libvirt XML file" )
	public void testSetCpuSockets()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		vm.setCpuSockets( 2 );
		assertEquals( 2, vm.getCpuSockets() );
	}

	@Test
	@DisplayName( "Get VM CPU cores from libvirt XML file" )
	public void testGetCpuCores()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		assertEquals( 4, vm.getCpuCores() );
	}

	@Test
	@DisplayName( "Set VM CPU cores in libvirt XML file" )
	public void testSetCpuCores()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		vm.setCpuCores( 8 );
		assertEquals( 8, vm.getCpuCores() );
	}

	@Test
	@DisplayName( "Get VM CPU threads from libvirt XML file" )
	public void testGetCpuThreads()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		assertEquals( 1, vm.getCpuThreads() );
	}

	@Test
	@DisplayName( "Set VM CPU threads in libvirt XML file" )
	public void testSetCpuThreads()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_cpu-topology.xml" );
		vm.setCpuThreads( 2 );
		assertEquals( 2, vm.getCpuThreads() );
	}

	@Test
	@DisplayName( "Get VM emulator binary from libvirt XML file" )
	public void testGetDevicesEmulator()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "/usr/bin/qemu-system-x86_64", vm.getDevicesEmulator() );
	}

	@Test
	@DisplayName( "Set VM emulator binary in libvirt XML file" )
	public void testSetDevicesEmulator()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setDevicesEmulator( "/usr/bin/qemu-system-i386" );
		assertEquals( "/usr/bin/qemu-system-i386", vm.getDevicesEmulator() );
	}

	@Test
	@DisplayName( "Get all VM devices from libvirt XML file" )
	public void testGetDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 24, vm.getDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM controller devices from libvirt XML file" )
	public void testGetControllerDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 14, vm.getControllerDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM disk devices from libvirt XML file" )
	public void testGetDiskDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 3, vm.getDiskDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM file system devices from libvirt XML file" )
	public void testGetFileSystemDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 0, vm.getFileSystemDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM hostdev devices from libvirt XML file" )
	public void testGetHostdevDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 0, vm.getHostdevDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM interface devices from libvirt XML file" )
	public void testGetInterfaceDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getInterfaceDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM graphic devices from libvirt XML file" )
	public void testGetGraphicDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getGraphicDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM parallel port devices from libvirt XML file" )
	public void testGetParallelDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 0, vm.getParallelDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM serial port devices from libvirt XML file" )
	public void testGetSerialDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getSerialDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM sound devices from libvirt XML file" )
	public void testGetSoundDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getSoundDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM video devices from libvirt XML file" )
	public void testGetVideoDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getVideoDevices().size() );
	}

	@Test
	@DisplayName( "Get all Redir devices from libvirt XML file" )
	public void testGetRedirDevices()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 2, vm.getRedirectDevices().size() );
	}

	@Test
	@DisplayName( "Get all QEMU command line arguments from libvirt XML file" )
	public void testGetQemuCmdlnArguments()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm_qemu-cmdln.xml" );
		assertEquals( 2, vm.getQemuCmdlnArguments().size() );
	}

	@Test
	@DisplayName( "Set QEMU command line arguments in libvirt XML file" )
	public void testAddQemuCmdlnArguments()
	{
		Domain vm = DomainTest.getDomain( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 0, vm.getQemuCmdlnArguments().size() );

		vm.addQemuCmdlnArgument( "-set" );
		vm.addQemuCmdlnArgument( "device.hostdev0.x-igd-opregion=on" );

		assertEquals( 2, vm.getQemuCmdlnArguments().size() );
	}
}
