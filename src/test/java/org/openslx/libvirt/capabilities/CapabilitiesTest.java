package org.openslx.libvirt.capabilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.libvirt.capabilities.cpu.Cpu;
import org.openslx.libvirt.capabilities.cpu.Feature;
import org.openslx.libvirt.capabilities.cpu.Pages;
import org.openslx.libvirt.capabilities.guest.Domain;
import org.openslx.libvirt.capabilities.guest.Guest;
import org.openslx.libvirt.capabilities.guest.Machine;
import org.openslx.libvirt.domain.Domain.OsType;
import org.openslx.libvirt.domain.Domain.Type;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlTestResources;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;

public class CapabilitiesTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		Configurator.setRootLevel( Level.OFF );
	}

	private Capabilities newCapabilitiesInstance( String xmlFileName )
	{
		Capabilities caps = null;

		try {
			caps = new Capabilities( LibvirtXmlTestResources.getLibvirtXmlFile( xmlFileName ) );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
			final String errorMsg = new String(
					"Cannot prepare requested Libvirt capabilities XML file from the resources folder" );
			fail( errorMsg );
		}

		return caps;
	}

	@Test
	@DisplayName( "Get host UUID from libvirt XML capabilities file" )
	public void testGetHostUuid()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		assertEquals( "9b2f12af-1fba-444c-b72b-9cbc43fb3ca5", caps.getHostUuid() );
	}

	@Test
	@DisplayName( "Get host CPU from libvirt XML capabilities file" )
	public void testGetHostCpu()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNotNull( hostCpu );
		assertEquals( "x86_64", hostCpu.getArch() );
		assertEquals( "Skylake-Client-IBRS", hostCpu.getModel() );
		assertEquals( "Intel", hostCpu.getVendor() );
		assertEquals( 1, hostCpu.getTopologySockets() );
		assertEquals( 1, hostCpu.getTopologyDies() );
		assertEquals( 4, hostCpu.getTopologyCores() );
		assertEquals( 1, hostCpu.getTopologyThreads() );
	}

	@Test
	@DisplayName( "Get non-existent host CPU from libvirt XML capabilities file" )
	public void testGetHostCpuNonExistent()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-cpu.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNull( hostCpu );
	}

	@Test
	@DisplayName( "Get host CPU features from libvirt XML capabilities file" )
	public void testGetHostCpuFeatures()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNotNull( hostCpu );

		final List<Feature> hostCpuFeatures = hostCpu.getFeatures();
		assertNotNull( hostCpuFeatures );
		assertEquals( 25, hostCpuFeatures.size() );

		final Feature hostCpuFeature = hostCpuFeatures.get( 9 );
		assertNotNull( hostCpuFeature );
		assertEquals( "vmx", hostCpuFeature.getName() );
	}

	@Test
	@DisplayName( "Get empty host CPU features from libvirt XML capabilities file" )
	public void testGetHostCpuFeaturesEmpty()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-cpu-features.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNotNull( hostCpu );

		final List<Feature> hostCpuFeatures = hostCpu.getFeatures();
		assertNotNull( hostCpuFeatures );
		assertEquals( 0, hostCpuFeatures.size() );
	}

	@Test
	@DisplayName( "Get host CPU pages from libvirt XML capabilities file" )
	public void testGetHostCpuPages()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNotNull( hostCpu );

		final List<Pages> hostCpuPages = hostCpu.getPages();
		assertNotNull( hostCpuPages );
		assertEquals( 3, hostCpuPages.size() );

		final Pages hostCpuPage = hostCpuPages.get( 2 );
		assertNotNull( hostCpuPage );
		assertEquals( new BigInteger( "1073741824" ).toString(), hostCpuPage.getSize().toString() );
	}

	@Test
	@DisplayName( "Get empty host CPU pages from libvirt XML capabilities file" )
	public void testGetHostCpuPagesEmpty()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-cpu-pages.xml" );
		final Cpu hostCpu = caps.getHostCpu();

		assertNotNull( hostCpu );

		final List<Pages> hostCpuPages = hostCpu.getPages();
		assertNotNull( hostCpuPages );
		assertEquals( 0, hostCpuPages.size() );
	}

	@Test
	@DisplayName( "Get host IOMMU support from libvirt XML capabilities file" )
	public void testGetHostIommuSupport()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		assertEquals( true, caps.hasHostIommuSupport() );
	}

	@Test
	@DisplayName( "Get non-existent host IOMMU support from libvirt XML capabilities file" )
	public void testGetHostIommuSupportNonExistent()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-iommu.xml" );

		assertEquals( false, caps.hasHostIommuSupport() );
	}

	@Test
	@DisplayName( "Get guests from libvirt XML capabilities file" )
	public void testGetGuests()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 3 );
		assertNotNull( guest );
		assertEquals( OsType.HVM.toString(), guest.getOsType().toString() );
		assertEquals( "aarch64", guest.getArchName() );
		assertEquals( 64, guest.getArchWordSize() );
		assertEquals( "/usr/bin/qemu-system-aarch64", guest.getArchEmulator() );
	}

	@Test
	@DisplayName( "Get empty guests from libvirt XML capabilities file" )
	public void testGetGuestsEmpty()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-guests.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 0, guests.size() );
	}

	@Test
	@DisplayName( "Get guest machines from libvirt XML capabilities file" )
	public void testGetGuestMachines()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 3 );
		assertNotNull( guest );

		final List<Machine> guestMachines = guest.getArchMachines();
		assertNotNull( guestMachines );
		assertEquals( 89, guestMachines.size() );

		final Machine guestMachine = guestMachines.get( 5 );
		assertNotNull( guestMachine );
		assertNull( guestMachine.getCanonicalMachine() );
		assertEquals( 2, guestMachine.getMaxCpus() );
		assertEquals( "nuri", guestMachine.getName() );
	}

	@Test
	@DisplayName( "Get empty guest machines from libvirt XML capabilities file" )
	public void testGetGuestMachinesEmpty()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-guest-machines.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 3 );
		assertNotNull( guest );

		final List<Machine> guestMachines = guest.getArchMachines();
		assertNotNull( guestMachines );
		assertEquals( 0, guestMachines.size() );
	}

	@Test
	@DisplayName( "Get canonical guest machine from libvirt XML capabilities file" )
	public void testGetGuestMachineCanonical()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 3 );
		assertNotNull( guest );

		final List<Machine> guestMachines = guest.getArchMachines();
		assertNotNull( guestMachines );
		assertEquals( 89, guestMachines.size() );

		final Machine guestMachine = guestMachines.get( 29 );
		assertNotNull( guestMachine );
		assertEquals( "virt-5.2", guestMachine.getCanonicalMachine() );
		assertEquals( 512, guestMachine.getMaxCpus() );
		assertEquals( "virt", guestMachine.getName() );
	}

	@Test
	@DisplayName( "Get guest machine domains from libvirt XML capabilities file" )
	public void testGetGuestMachineDomains()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_default.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 5 );
		assertNotNull( guest );

		final List<Domain> guestDomains = guest.getArchDomains();
		assertNotNull( guestDomains );
		assertEquals( 2, guestDomains.size() );

		final Domain guestDomain = guestDomains.get( 1 );
		assertNotNull( guestDomain );
		assertEquals( Type.KVM, guestDomain.getType() );
	}

	@Test
	@DisplayName( "Get empty guest machine domains from libvirt XML capabilities file" )
	public void testGetGuestMachineDomainsEmpty()
	{
		final Capabilities caps = this.newCapabilitiesInstance( "qemu-kvm_capabilities_no-guest-machines.xml" );

		final List<Guest> guests = caps.getGuests();
		assertNotNull( guests );
		assertEquals( 26, guests.size() );

		final Guest guest = guests.get( 3 );
		assertNotNull( guest );

		final List<Domain> guestDomains = guest.getArchDomains();
		assertNotNull( guestDomains );
		assertEquals( 0, guestDomains.size() );
	}
}
