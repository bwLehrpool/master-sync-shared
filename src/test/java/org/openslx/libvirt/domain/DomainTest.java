package org.openslx.libvirt.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	private Domain newDomainInstance( String xmlFileName )
	{
		Domain domain = null;

		try {
			domain = new Domain( LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName ) );
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
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( Domain.Type.KVM.toString(), vm.getType().toString() );
	}

	@Test
	@DisplayName( "Set VM type from libvirt XML file" )
	public void testSetType()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setType( Domain.Type.QEMU );
		assertEquals( Domain.Type.QEMU.toString(), vm.getType().toString() );
	}

	@Test
	@DisplayName( "Get VM name from libvirt XML file" )
	public void testGetName()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "ubuntu-20-04", vm.getName() );
	}

	@Test
	@DisplayName( "Set VM name in libvirt XML file" )
	public void testSetName()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setName( "ubuntu-18-04" );
		assertEquals( "ubuntu-18-04", vm.getName() );
	}

	@Test
	@DisplayName( "Get VM title from libvirt XML file" )
	public void testGetTitle()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "Ubuntu 20.04", vm.getTitle() );
	}

	@Test
	@DisplayName( "Set VM title in libvirt XML file" )
	public void testSetTitle()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setTitle( "Ubuntu 18.04" );
		assertEquals( "Ubuntu 18.04", vm.getTitle() );
	}

	@Test
	@DisplayName( "Get VM description from libvirt XML file" )
	public void testGetDescription()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "Ubuntu 20.04 desktop installation", vm.getDescription() );
	}

	@Test
	@DisplayName( "Set VM description in libvirt XML file" )
	public void testSetDescription()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setDescription( "Ubuntu 18.04 server installation" );
		assertEquals( "Ubuntu 18.04 server installation", vm.getDescription() );
	}

	@Test
	@DisplayName( "Get VM UUID from libvirt XML file" )
	public void testGetUuid()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "8dc5433c-0228-49e4-b019-fa2b606aa544", vm.getUuid() );
	}

	@Test
	@DisplayName( "Set VM UUID in libvirt XML file" )
	public void testSetUuid()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setUuid( "5ab08167-3d95-400e-ac83-e6af8d150971" );
		assertEquals( "5ab08167-3d95-400e-ac83-e6af8d150971", vm.getUuid() );
	}

	@Test
	@DisplayName( "Get VM memory from libvirt XML file" )
	public void testGetMemory()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( new BigInteger( "4294967296" ).toString(), vm.getMemory().toString() );
	}

	@Test
	@DisplayName( "Set VM memory in libvirt XML file" )
	public void testSetMemory()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setMemory( new BigInteger( "12073740288" ) );
		assertEquals( new BigInteger( "12073740288" ).toString(), vm.getMemory().toString() );
	}

	@Test
	@DisplayName( "Get current VM memory from libvirt XML file" )
	public void testGetCurrentMemory()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( new BigInteger( "4294967296" ).toString(), vm.getCurrentMemory().toString() );
	}

	@Test
	@DisplayName( "Set current VM memory in libvirt XML file" )
	public void testSetCurrentMemory()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCurrentMemory( new BigInteger( "8087237632" ) );
		assertEquals( new BigInteger( "8087237632" ).toString(), vm.getCurrentMemory().toString() );
	}

	@Test
	@DisplayName( "Get VM number of vCpus from libvirt XML file" )
	public void testGetVCpu()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 2, vm.getVCpu() );
	}

	@Test
	@DisplayName( "Set VM number of vCpus in libvirt XML file" )
	public void testSetVCpu()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setVCpu( 4 );
		assertEquals( 4, vm.getVCpu() );
	}

	@Test
	@DisplayName( "Get VM's OS type from libvirt XML file" )
	public void testGetOsType()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( OsType.HVM.toString(), vm.getOsType().toString() );
	}

	@Test
	@DisplayName( "Set VM's OS type in libvirt XML file" )
	public void testSetOsType()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsType( OsType.XEN );
		assertEquals( OsType.XEN.toString(), vm.getOsType().toString() );
	}

	@Test
	@DisplayName( "Get VM's OS architecture from libvirt XML file" )
	public void testGetOsArch()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "x86_64", vm.getOsArch() );
	}

	@Test
	@DisplayName( "Set VM's OS architecture in libvirt XML file" )
	public void testSetOsArch()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsArch( "aarch" );
		assertEquals( "aarch", vm.getOsArch() );
	}

	@Test
	@DisplayName( "Get VM's OS machine from libvirt XML file" )
	public void testGetOsMachine()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( "pc-q35-5.1", vm.getOsMachine() );
	}

	@Test
	@DisplayName( "Set VM's OS machine in libvirt XML file" )
	public void testSetOsMachine()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setOsMachine( "pc" );
		assertEquals( "pc", vm.getOsMachine() );
	}

	@Test
	@DisplayName( "Get VM CPU model from libvirt XML file" )
	public void testGetCpuModel()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertNull( vm.getCpuModel() );
	}

	@Test
	@DisplayName( "Set VM CPU model in libvirt XML file" )
	public void testSetCpuModel()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuModel( "core2duo" );
		assertEquals( "core2duo", vm.getCpuModel() );
	}

	@Test
	@DisplayName( "Get VM CPU mode from libvirt XML file" )
	public void testGetCpuModelMode()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( CpuMode.HOST_MODEL.toString(), vm.getCpuMode().toString() );
	}

	@Test
	@DisplayName( "Set VM CPU mode in libvirt XML file" )
	public void testSetCpuModelMode()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuMode( CpuMode.HOST_PASSTHROUGH );
		assertEquals( CpuMode.HOST_PASSTHROUGH.toString(), vm.getCpuMode().toString() );
	}

	@Test
	@DisplayName( "Get VM CPU check from libvirt XML file" )
	public void testGetCpuCheck()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( CpuCheck.PARTIAL.toString(), vm.getCpuCheck().toString() );
	}

	@Test
	@DisplayName( "Set VM CPU check in libvirt XML file" )
	public void testSetCpuCheck()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		vm.setCpuCheck( CpuCheck.NONE );
		assertEquals( CpuCheck.NONE.toString(), vm.getCpuCheck().toString() );
	}

	@Test
	@DisplayName( "Get all VM devices from libvirt XML file" )
	public void testGetDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 21, vm.getDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM controller devices from libvirt XML file" )
	public void testGetControllerDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 14, vm.getControllerDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM disk devices from libvirt XML file" )
	public void testGetDiskDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 3, vm.getDiskDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM hostdev devices from libvirt XML file" )
	public void testGetHostdevDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 0, vm.getHostdevDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM interface devices from libvirt XML file" )
	public void testGetInterfaceDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getInterfaceDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM graphic devices from libvirt XML file" )
	public void testGetGraphicDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getGraphicDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM sound devices from libvirt XML file" )
	public void testGetSoundDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getSoundDevices().size() );
	}

	@Test
	@DisplayName( "Get all VM video devices from libvirt XML file" )
	public void testGetVideoDevices()
	{
		Domain vm = this.newDomainInstance( "qemu-kvm_default-ubuntu-20-04-vm.xml" );
		assertEquals( 1, vm.getVideoDevices().size() );
	}
}
