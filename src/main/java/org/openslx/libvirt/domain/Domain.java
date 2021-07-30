package org.openslx.libvirt.domain;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openslx.libvirt.domain.device.Device;
import org.openslx.libvirt.domain.device.Controller;
import org.openslx.libvirt.domain.device.ControllerFloppy;
import org.openslx.libvirt.domain.device.ControllerIde;
import org.openslx.libvirt.domain.device.ControllerPci;
import org.openslx.libvirt.domain.device.ControllerSata;
import org.openslx.libvirt.domain.device.ControllerScsi;
import org.openslx.libvirt.domain.device.ControllerUsb;
import org.openslx.libvirt.domain.device.Disk;
import org.openslx.libvirt.domain.device.DiskCdrom;
import org.openslx.libvirt.domain.device.DiskFloppy;
import org.openslx.libvirt.domain.device.DiskStorage;
import org.openslx.libvirt.domain.device.FileSystem;
import org.openslx.libvirt.domain.device.Graphics;
import org.openslx.libvirt.domain.device.GraphicsSpice;
import org.openslx.libvirt.domain.device.GraphicsVnc;
import org.openslx.libvirt.domain.device.Hostdev;
import org.openslx.libvirt.domain.device.HostdevPci;
import org.openslx.libvirt.domain.device.HostdevUsb;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.domain.device.InterfaceBridge;
import org.openslx.libvirt.domain.device.InterfaceNetwork;
import org.openslx.libvirt.domain.device.Parallel;
import org.openslx.libvirt.domain.device.Serial;
import org.openslx.libvirt.domain.device.Shmem;
import org.openslx.libvirt.domain.device.Sound;
import org.openslx.libvirt.domain.device.Video;
import org.openslx.libvirt.xml.LibvirtXmlDocument;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.openslx.libvirt.xml.LibvirtXmlResources;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Implementation of the Libvirt domain XML document.
 * 
 * The Libvirt domain XML document is used to describe virtual machines and their configurations.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Domain extends LibvirtXmlDocument
{
	/**
	 * Creates Libvirt domain XML document from {@link String} providing Libvirt domain XML content.
	 * 
	 * @param xml {@link String} with Libvirt domain XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the domain XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid domain XML document.
	 */
	public Domain( String xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "domain.rng" ) );
	}

	/**
	 * Creates Libvirt domain XML document from {@link File} containing Libvirt domain XML content.
	 * 
	 * @param xml existing {@link File} containing Libvirt domain XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the domain XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid domain XML document.
	 */
	public Domain( File xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "domain.rng" ) );
	}

	/**
	 * Creates Libvirt domain XML document from {@link InputStream} providing Libvirt domain XML
	 * content.
	 * 
	 * @param xml {@link InputStream} providing Libvirt domain XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the domain XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid domain XML document.
	 */
	public Domain( InputStream xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "domain.rng" ) );
	}

	/**
	 * Creates Libvirt domain XML document from {@link InputSource} providing Libvirt domain XML
	 * content.
	 * 
	 * @param xml {@link InputSource} providing Libvirt domain XML content.
	 * 
	 * @throws LibvirtXmlDocumentException creation of XML context failed.
	 * @throws LibvirtXmlSerializationException serialization of the domain XML content failed.
	 * @throws LibvirtXmlValidationException XML content is not a valid domain XML document.
	 */
	public Domain( InputSource xml )
			throws LibvirtXmlDocumentException, LibvirtXmlSerializationException, LibvirtXmlValidationException
	{
		super( xml, LibvirtXmlResources.getLibvirtRng( "domain.rng" ) );
	}

	/**
	 * Types of hypervisors specifiable for a virtual machine in the Libvirt domain XML document.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Type
	{
		// @formatter:off
		QEMU  ( "qemu" ),
		KQEMU ( "kqemu" ),
		KVM   ( "kvm" ),
		XEN   ( "xen" ),
		LXC   ( "lxc" ),
		UML   ( "uml" ),
		OPENVZ( "openvz" ),
		TEST  ( "test" ),
		VMWARE( "vmware" ),
		HYPERV( "hyperv" ),
		VBOX  ( "vbox" ),
		PHYP  ( "phyp" ),
		VZ    ( "vz" ),
		BHYVE ( "bhyve" );
		// @formatter:on

		/**
		 * Name of the hypervisor in a Libvirt domain XML document.
		 */
		private String type;

		/**
		 * Creates a hypervisor type.
		 * 
		 * @param type valid name of the hypervisor in a Libvirt domain XML document.
		 */
		Type( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates a hypervisor type from its name with error check.
		 * 
		 * @param type name of the hypervisor in the Libvirt domain XML document.
		 * @return valid hypervisor type.
		 */
		public static Type fromString( String type )
		{
			for ( Type t : Type.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Returns hypervisor type defined in the Libvirt domain XML document.
	 * 
	 * @return hypervisor type.
	 */
	public Type getType()
	{
		String typeValue = this.getRootXmlNode().getXmlElementAttributeValue( null, "type" );
		return Type.fromString( typeValue );
	}

	/**
	 * Sets hypervisor type in Libvirt domain XML document.
	 * 
	 * @param type hypervisor type for Libvirt domain XML document.
	 */
	public void setType( Type type )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( null, "type", type.toString() );
	}

	/**
	 * Returns virtual machine name defined in the Libvirt domain XML document.
	 * 
	 * @return name of the virtual machine.
	 */
	public String getName()
	{
		return this.getRootXmlNode().getXmlElementValue( "name" );
	}

	/**
	 * Sets virtual machine name in the Libvirt domain XML document.
	 * 
	 * @param name virtual machine name for Libvirt domain XML document.
	 */
	public void setName( String name )
	{
		this.getRootXmlNode().setXmlElementValue( "name", name );
	}

	/**
	 * Returns virtual machine title defined in the Libvirt domain XML document.
	 * 
	 * @return title of the virtual machine.
	 */
	public String getTitle()
	{
		return this.getRootXmlNode().getXmlElementValue( "title" );
	}

	/**
	 * Sets virtual machine title in the Libvirt domain XML document.
	 * 
	 * @param title virtual machine title for Libvirt domain XML document.
	 */
	public void setTitle( String title )
	{
		this.getRootXmlNode().setXmlElementValue( "title", title );
	}

	/**
	 * Returns virtual machine description defined in the Libvirt domain XML document.
	 * 
	 * @return description of virtual machine.
	 */
	public String getDescription()
	{
		return this.getRootXmlNode().getXmlElementValue( "description" );
	}

	/**
	 * Sets virtual machine description in the Libvirt domain XML document.
	 * 
	 * @param description virtual machine description for Libvirt domain XML document.
	 */
	public void setDescription( String description )
	{
		this.getRootXmlNode().setXmlElementValue( "description", description );
	}

	/**
	 * Returns the libosinfo operating system identifier.
	 * 
	 * @return libosinfo operating system identifier.
	 */
	public String getLibOsInfoOsId()
	{
		return this.getRootXmlNode()
				.getXmlElementAttributeValue( "metadata/*[local-name()='libosinfo']/*[local-name()='os']", "id" );
	}

	/**
	 * Returns the state of the Hyper-V vendor identifier feature.
	 * 
	 * @return state of the Hyper-V vendor identifier feature.
	 */
	public boolean isFeatureHypervVendorIdStateOn()
	{
		return this.getRootXmlNode().getXmlElementAttributeValueAsBool( "features/hyperv/vendor_id", "state" );
	}

	/**
	 * Sets the state of the Hyper-V vendor identifier feature.
	 * 
	 * @param on state for the Hyper-V vendor identifier feature.
	 */
	public void setFeatureHypervVendorIdState( boolean on )
	{
		this.getRootXmlNode().setXmlElementAttributeValueOnOff( "features/hyperv/vendor_id", "state", on );
	}

	/**
	 * Returns the value of the Hyper-V vendor identifier feature.
	 * 
	 * @return value of the Hyper-V vendor identifier feature.
	 */
	public String getFeatureHypervVendorIdValue()
	{
		return this.getRootXmlNode().getXmlElementAttributeValue( "features/hyperv/vendor_id", "value" );
	}

	/**
	 * Sets the value of the Hyper-V vendor identifier feature.
	 * 
	 * @param value value for the Hyper-V vendor identifier feature.
	 */
	public void setFeatureHypervVendorIdValue( String value )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "features/hyperv/vendor_id", "value", value );
	}

	/**
	 * Returns the state of the KVM hidden feature.
	 * 
	 * @return state of the KVM hidden feature.
	 */
	public boolean isFeatureKvmHiddenStateOn()
	{
		return this.getRootXmlNode().getXmlElementAttributeValueAsBool( "features/kvm/hidden", "state" );
	}

	/**
	 * Sets the state of the KVM hidden feature.
	 * 
	 * @param on state for the KVM hidden feature.
	 */
	public void setFeatureKvmHiddenState( boolean on )
	{
		this.getRootXmlNode().setXmlElementAttributeValueOnOff( "features/kvm/hidden", "state", on );
	}

	/**
	 * Returns virtual machine UUID defined in the Libvirt domain XML document.
	 * 
	 * @return UUID of virtual machine.
	 */
	public String getUuid()
	{
		return this.getRootXmlNode().getXmlElementValue( "uuid" );
	}

	/**
	 * Sets virtual machine UUID in the Libvirt domain XML document.
	 * 
	 * @param uuid virtual machine UUID for Libvirt domain XML document.
	 */
	public void setUuid( String uuid )
	{
		this.getRootXmlNode().setXmlElementValue( "uuid", uuid );
	}

	/**
	 * Removes virtual machine UUID in the Libvirt domain XML document.
	 */
	public void removeUuid()
	{
		this.getRootXmlNode().removeXmlElement( "uuid" );
	}

	/**
	 * Returns virtual machine memory defined in the Libvirt domain XML document.
	 * 
	 * @return memory of virtual machine.
	 */
	public BigInteger getMemory()
	{
		String memValue = this.getRootXmlNode().getXmlElementValue( "memory" );
		String memUnit = this.getRootXmlNode().getXmlElementAttributeValue( "memory", "unit" );
		return DomainUtils.decodeMemory( memValue, memUnit );
	}

	/**
	 * Sets virtual machine memory in the Libvirt domain XML document.
	 * 
	 * @param memory virtual machine memory in the Libvirt domain XML document.
	 */
	public void setMemory( BigInteger memory )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "memory", "unit", "KiB" );
		this.getRootXmlNode().setXmlElementValue( "memory", DomainUtils.encodeMemory( memory, "KiB" ) );
	}

	/**
	 * Returns current virtual machine memory defined in the Libvirt domain XML document.
	 * 
	 * @return current memory of virtual machine.
	 */
	public BigInteger getCurrentMemory()
	{
		String memValue = this.getRootXmlNode().getXmlElementValue( "currentMemory" );
		String memUnit = this.getRootXmlNode().getXmlElementAttributeValue( "currentMemory", "unit" );
		return DomainUtils.decodeMemory( memValue, memUnit );
	}

	/**
	 * Set current virtual machine memory in the Libvirt domain XML document.
	 * 
	 * @param currentMemory current virtual machine memory in the Libvirt domain XML document.
	 */
	public void setCurrentMemory( BigInteger currentMemory )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "currentMemory", "unit", "KiB" );
		this.getRootXmlNode().setXmlElementValue( "currentMemory", DomainUtils.encodeMemory( currentMemory, "KiB" ) );
	}

	/**
	 * Returns number of virtual machine CPUs defined in the Libvirt domain XML document.
	 * 
	 * @return number of CPUs of the virtual machine.
	 */
	public int getVCpu()
	{
		String number = this.getRootXmlNode().getXmlElementValue( "vcpu" );
		return Integer.parseUnsignedInt( number );
	}

	/**
	 * Set number of virtual machine CPUs in the Libvirt domain XML document.
	 * 
	 * @param number virtual machine CPUs.
	 */
	public void setVCpu( int number )
	{
		this.getRootXmlNode().setXmlElementValue( "vcpu", Integer.toString( number ) );
	}

	/**
	 * Returns OS type defined in the Libvirt domain XML document.
	 * 
	 * @return OS type of the virtual machine.
	 */
	public OsType getOsType()
	{
		final String osType = this.getRootXmlNode().getXmlElementValue( "os/type" );
		return OsType.fromString( osType );
	}

	/**
	 * Set OS type in the Libvirt domain XML document.
	 * 
	 * @param type OS type for the virtual machine.
	 */
	public void setOsType( OsType type )
	{
		this.getRootXmlNode().setXmlElementValue( "os/type", type.toString() );
	}

	/**
	 * Returns OS architecture defined in the Libvirt domain XML document.
	 * 
	 * @return OS architecture of the virtual machine.
	 */
	public String getOsArch()
	{
		return this.getRootXmlNode().getXmlElementAttributeValue( "os/type", "arch" );
	}

	/**
	 * Set OS architecture in the Libvirt domain XML document.
	 * 
	 * @param arch OS architecture for the virtual machine.
	 */
	public void setOsArch( String arch )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "os/type", "arch", arch );
	}

	/**
	 * Returns OS machine defined in the Libvirt domain XML document.
	 * 
	 * @return OS machine of the virtual machine.
	 */
	public String getOsMachine()
	{
		return this.getRootXmlNode().getXmlElementAttributeValue( "os/type", "machine" );
	}

	/**
	 * Set OS machine in the Libvirt domain XML document.
	 * 
	 * @param machine OS machine for the virtual machine.
	 */
	public void setOsMachine( String machine )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "os/type", "machine", machine );
	}

	/**
	 * Operating system types specifiable for a virtual machine in the Libvirt domain XML document.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum OsType
	{
		// @formatter:off
		XEN  ( "xen" ),
		LINUX( "linux" ),
		HVM  ( "hvm" ),
		EXE  ( "exe" ),
		UML  ( "uml" );
		// @formatter:on

		/**
		 * Name of the OS type in a Libvirt domain XML document.
		 */
		private final String osType;

		/**
		 * Creates an OS type.
		 * 
		 * @param osType valid name of the OS type in the Libvirt domain XML document.
		 */
		OsType( String osType )
		{
			this.osType = osType;
		}

		@Override
		public String toString()
		{
			return this.osType;
		}

		/**
		 * Creates an OS type from its name with error check.
		 * 
		 * @param osType name of the OS type in the Libvirt domain XML document.
		 * @return valid OS type.
		 */
		public static OsType fromString( String osType )
		{
			for ( OsType t : OsType.values() ) {
				if ( t.osType.equalsIgnoreCase( osType ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Returns virtual machine CPU model defined in the Libvirt domain XML document.
	 * 
	 * @return CPU model of virtual machine.
	 */
	public String getCpuModel()
	{
		return this.getRootXmlNode().getXmlElementValue( "cpu/model" );
	}

	/**
	 * Sets virtual machine CPU model in the Libvirt domain XML document.
	 * 
	 * @param model virtual machine CPU model.
	 */
	public void setCpuModel( String model )
	{
		this.getRootXmlNode().setXmlElementValue( "cpu/model", model );
	}

	/**
	 * CPU modes specifiable for a virtual machine in the Libvirt domain XML document.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum CpuMode
	{
		// @formatter:off
		CUSTOM          ( "custom" ),
		HOST_MODEL      ( "host-model" ),
		HOST_PASSTHROUGH( "host-passthrough" );
		// @formatter:on

		/**
		 * Name of the CPU mode in a Libvirt domain XML document.
		 */
		private String cpuMode;

		/**
		 * Creates a CPU mode.
		 * 
		 * @param mode valid name of the CPU mode in the Libvirt domain XML document.
		 */
		CpuMode( String mode )
		{
			this.cpuMode = mode;
		}

		@Override
		public String toString()
		{
			return this.cpuMode;
		}

		/**
		 * Creates a CPU mode from its name with error check.
		 * 
		 * @param mode name of the CPU mode in the Libvirt domain XML document.
		 * @return valid CPU mode.
		 */
		public static CpuMode fromString( String mode )
		{
			for ( CpuMode t : CpuMode.values() ) {
				if ( t.cpuMode.equalsIgnoreCase( mode ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Returns virtual machine CPU mode defined in the Libvirt domain XML document.
	 * 
	 * @return CPU mode of the virtual machine.
	 */
	public CpuMode getCpuMode()
	{
		String cpuMode = this.getRootXmlNode().getXmlElementAttributeValue( "cpu", "mode" );
		return CpuMode.fromString( cpuMode );
	}

	/**
	 * Sets virtual machine CPU mode in the Libvirt domain XML document.
	 * 
	 * @param mode virtual machine CPU mode.
	 */
	public void setCpuMode( CpuMode mode )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "cpu", "mode", mode.toString() );
	}

	/**
	 * CPU checks specifiable for a virtual machine in the Libvirt domain XML document.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum CpuCheck
	{
		// @formatter:off
		NONE   ( "none" ),
		PARTIAL( "partial" ),
		FULL   ( "full" );
		// @formatter:on

		/**
		 * Name of the CPU check in the Libvirt domain XML document.
		 */
		private String cpuCheck;

		/**
		 * Creates a CPU check.
		 * 
		 * @param check valid name of the CPU check in the Libvirt domain XML document.
		 */
		CpuCheck( String check )
		{
			this.cpuCheck = check;
		}

		@Override
		public String toString()
		{
			return this.cpuCheck;
		}

		/**
		 * Creates a CPU check from its name with error check.
		 * 
		 * @param check name of the CPU check in the Libvirt domain XML document.
		 * @return valid CPU check.
		 */
		public static CpuCheck fromString( String check )
		{
			for ( CpuCheck t : CpuCheck.values() ) {
				if ( t.cpuCheck.equalsIgnoreCase( check ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Returns virtual machine CPU check defined in the Libvirt domain XML document.
	 * 
	 * @return CPU check of the virtual machine.
	 */
	public CpuCheck getCpuCheck()
	{
		String cpuCheck = this.getRootXmlNode().getXmlElementAttributeValue( "cpu", "check" );
		return CpuCheck.fromString( cpuCheck );
	}

	/**
	 * Sets virtual machine CPU check in the Libvirt domain XML document.
	 * 
	 * @param check virtual machine CPU check.
	 */
	public void setCpuCheck( CpuCheck check )
	{
		this.getRootXmlNode().setXmlElementAttributeValue( "cpu", "check", check.toString() );
	}

	/**
	 * Returns the file name of the emulator binary defined in the Libvirt domain XML document.
	 * 
	 * @return file name of the emulator binary.
	 */
	public String getDevicesEmulator()
	{
		return this.getRootXmlNode().getXmlElementValue( "devices/emulator" );
	}

	/**
	 * Sets the file name of the emulator binary in the Libvirt domain XML document.
	 * 
	 * @param emulator file name of the emulator binary.
	 */
	public void setDevicesEmulator( String emulator )
	{
		this.getRootXmlNode().setXmlElementValue( "devices/emulator", emulator );
	}

	/**
	 * Returns virtual machine devices defined in the Libvirt domain XML document.
	 * 
	 * @return devices of the virtual machine.
	 */
	public ArrayList<Device> getDevices()
	{
		ArrayList<Device> devices = new ArrayList<Device>();
		Node devicesNode = this.getRootXmlNode().getXmlElement( "devices" );

		if ( devicesNode != null ) {

			NodeList devicesElements = devicesNode.getChildNodes();

			for ( int i = 0; i < devicesElements.getLength(); i++ ) {
				final Node childNode = devicesElements.item( i );
				if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
					LibvirtXmlNode deviceNode = new LibvirtXmlNode( this.getRootXmlNode().getXmlDocument(), childNode );
					Device device = Device.newInstance( deviceNode );

					if ( device != null ) {
						devices.add( device );
					}
				}
			}
		}

		return devices;
	}

	/**
	 * Filter list of virtual machine devices of type {@link Device} and cast filtered instances to
	 * more specific device type <code>R</code>.
	 * 
	 * @param <R> specific device type for filtering and casting.
	 * @param cls specific device type's class.
	 * @param devices list of virtual machines devices.
	 * @return filtered list of virtual machines devices of type <code>R</code>.
	 */
	private static <R> ArrayList<R> filterDevices( Class<R> cls, ArrayList<Device> devices )
	{
		Predicate<Device> byFilter = device -> cls.isInstance( device );
		Function<Device, R> castFunction = device -> cls.cast( device );

		return devices.stream().filter( byFilter ).map( castFunction )
				.collect( Collectors.toCollection( ArrayList::new ) );
	}

	/**
	 * Returns list of virtual machine controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine controller devices.
	 */
	public ArrayList<Controller> getControllerDevices()
	{
		return Domain.filterDevices( Controller.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine floppy controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine floppy controller devices.
	 */
	public ArrayList<ControllerFloppy> getFloppyControllerDevices()
	{
		return Domain.filterDevices( ControllerFloppy.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine IDE controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine IDE controller devices.
	 */
	public ArrayList<ControllerIde> getIdeControllerDevices()
	{
		return Domain.filterDevices( ControllerIde.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine floppy controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine floppy controller devices.
	 */
	public ArrayList<ControllerPci> getPciControllerDevices()
	{
		return Domain.filterDevices( ControllerPci.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine SATA controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine SATA controller devices.
	 */
	public ArrayList<ControllerSata> getSataControllerDevices()
	{
		return Domain.filterDevices( ControllerSata.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine SCSI controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine SCSI controller devices.
	 */
	public ArrayList<ControllerScsi> getScsiControllerDevices()
	{
		return Domain.filterDevices( ControllerScsi.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine USB controller devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine USB controller devices.
	 */
	public ArrayList<ControllerUsb> getUsbControllerDevices()
	{
		return Domain.filterDevices( ControllerUsb.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine disk devices specified in the Libvirt domain XML document.
	 * 
	 * @return list of virtual machine disk devices.
	 */
	public ArrayList<Disk> getDiskDevices()
	{
		return Domain.filterDevices( Disk.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine disk CDROM devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine disk CDROM devices.
	 */
	public ArrayList<DiskCdrom> getDiskCdromDevices()
	{
		return Domain.filterDevices( DiskCdrom.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine disk floppy devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine disk floppy devices.
	 */
	public ArrayList<DiskFloppy> getDiskFloppyDevices()
	{
		return Domain.filterDevices( DiskFloppy.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine disk storage devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine disk storage devices.
	 */
	public ArrayList<DiskStorage> getDiskStorageDevices()
	{
		return Domain.filterDevices( DiskStorage.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine file system devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine file system devices.
	 */
	public ArrayList<FileSystem> getFileSystemDevices()
	{
		return Domain.filterDevices( FileSystem.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine hostdev devices specified in the Libvirt domain XML document.
	 * 
	 * @return list of virtual machine hostdev devices.
	 */
	public ArrayList<Hostdev> getHostdevDevices()
	{
		return Domain.filterDevices( Hostdev.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine PCI hostdev devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine PCI hostdev devices.
	 */
	public ArrayList<HostdevPci> getHostdevPciDevices()
	{
		return Domain.filterDevices( HostdevPci.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine USB hostdev devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine USB hostdev devices.
	 */
	public ArrayList<HostdevUsb> getHostdevUsbDevices()
	{
		return Domain.filterDevices( HostdevUsb.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine network interface devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine network interface devices.
	 */
	public ArrayList<Interface> getInterfaceDevices()
	{
		return Domain.filterDevices( Interface.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine graphic devices specified in the Libvirt domain XML document.
	 * 
	 * @return list of virtual machine graphic devices.
	 */
	public ArrayList<Graphics> getGraphicDevices()
	{
		return Domain.filterDevices( Graphics.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine parallel port devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine parallel port devices.
	 */
	public ArrayList<Parallel> getParallelDevices()
	{
		return Domain.filterDevices( Parallel.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine serial port devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine serial port devices.
	 */
	public ArrayList<Serial> getSerialDevices()
	{
		return Domain.filterDevices( Serial.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine shared memory devices specified in the Libvirt domain XML
	 * document.
	 * 
	 * @return list of virtual machine shared memory devices.
	 */
	public ArrayList<Shmem> getShmemDevices()
	{
		return Domain.filterDevices( Shmem.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine sound devices specified in the Libvirt domain XML document.
	 * 
	 * @return list of virtual machine sound devices.
	 */
	public ArrayList<Sound> getSoundDevices()
	{
		return Domain.filterDevices( Sound.class, this.getDevices() );
	}

	/**
	 * Returns list of virtual machine video devices specified in the Libvirt domain XML document.
	 * 
	 * @return list of virtual machine video devices.
	 */
	public ArrayList<Video> getVideoDevices()
	{
		return Domain.filterDevices( Video.class, this.getDevices() );
	}

	/**
	 * Adds a virtual machine device to the Libvirt domain XML document.
	 *
	 * @param device virtual machine device that is added to the Libvirt domain XML document.
	 * @return reference to the added device for configuration purposes if creation was successful.
	 */
	public Device addDevice( Device device )
	{
		Device addedDevice = null;

		if ( device != null ) {
			Node devicesNode = this.getRootXmlNode().getXmlElement( "devices" );

			if ( devicesNode != null ) {
				LibvirtXmlNode parentDevicesNode = null;
				parentDevicesNode = new LibvirtXmlNode( this.getRootXmlNode().getXmlDocument(), devicesNode );
				addedDevice = Device.createInstance( device, parentDevicesNode );
			}
		}

		return addedDevice;
	}

	/**
	 * Adds a virtual machine controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added controller device if creation was successful.
	 */
	public Controller addControllerDevice()
	{
		return Controller.class.cast( this.addDevice( new Controller() ) );
	}

	/**
	 * Adds a virtual machine floppy controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added floppy controller device if creation was successful.
	 */
	public ControllerFloppy addControllerFloppyDevice()
	{
		return ControllerFloppy.class.cast( this.addDevice( new ControllerFloppy() ) );
	}

	/**
	 * Adds a virtual machine IDE controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added IDE controller device if creation was successful.
	 */
	public ControllerIde addControllerIdeDevice()
	{
		return ControllerIde.class.cast( this.addDevice( new ControllerIde() ) );
	}

	/**
	 * Adds a virtual machine PCI controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added PCI controller device if creation was successful.
	 */
	public ControllerPci addControllerPciDevice()
	{
		return ControllerPci.class.cast( this.addDevice( new ControllerPci() ) );
	}

	/**
	 * Adds a virtual machine SATA controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added SATA controller device if creation was successful.
	 */
	public ControllerSata addControllerSataDevice()
	{
		return ControllerSata.class.cast( this.addDevice( new ControllerSata() ) );
	}

	/**
	 * Adds a virtual machine SCSI controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added SCSI controller device if creation was successful.
	 */
	public ControllerScsi addControllerScsiDevice()
	{
		return ControllerScsi.class.cast( this.addDevice( new ControllerScsi() ) );
	}

	/**
	 * Adds a virtual machine USB controller device to the Libvirt domain XML document.
	 *
	 * @return reference to the added USB controller device if creation was successful.
	 */
	public ControllerUsb addControllerUsbDevice()
	{
		return ControllerUsb.class.cast( this.addDevice( new ControllerUsb() ) );
	}

	/**
	 * Adds a virtual machine disk device to the Libvirt domain XML document.
	 *
	 * @return reference to the added disk device if creation was successful.
	 */
	public Disk addDiskDevice()
	{
		return Disk.class.cast( this.addDevice( new Disk() ) );
	}

	/**
	 * Adds a virtual machine CDROM disk device to the Libvirt domain XML document.
	 *
	 * @return reference to the added CDROM disk device if creation was successful.
	 */
	public DiskCdrom addDiskCdromDevice()
	{
		return DiskCdrom.class.cast( this.addDevice( new DiskCdrom() ) );
	}

	/**
	 * Adds a virtual machine floppy disk device to the Libvirt domain XML document.
	 *
	 * @return reference to the added floppy disk device if creation was successful.
	 */
	public DiskFloppy addDiskFloppyDevice()
	{
		return DiskFloppy.class.cast( this.addDevice( new DiskFloppy() ) );
	}

	/**
	 * Adds a virtual machine storage disk device to the Libvirt domain XML document.
	 *
	 * @return reference to the added storage disk device if creation was successful.
	 */
	public DiskStorage addDiskStorageDevice()
	{
		return DiskStorage.class.cast( this.addDevice( new DiskStorage() ) );
	}

	/**
	 * Adds a virtual machine file system device to the Libvirt domain XML document.
	 *
	 * @return reference to the added file system device if creation was successful.
	 */
	public FileSystem addFileSystemDevice()
	{
		return FileSystem.class.cast( this.addDevice( new FileSystem() ) );
	}

	/**
	 * Adds a virtual machine hostdev device to the Libvirt domain XML document.
	 *
	 * @return reference to the added hostdev device if creation was successful.
	 */
	public Hostdev addHostdevDevice()
	{
		return Hostdev.class.cast( this.addDevice( new Hostdev() ) );
	}

	/**
	 * Adds a virtual machine PCI hostdev device to the Libvirt domain XML document.
	 *
	 * @return reference to the added PCI hostdev device if creation was successful.
	 */
	public HostdevPci addHostdevPciDevice()
	{
		return HostdevPci.class.cast( this.addDevice( new HostdevPci() ) );
	}

	/**
	 * Adds a virtual machine USB hostdev device to the Libvirt domain XML document.
	 *
	 * @return reference to the added USB hostdev device if creation was successful.
	 */
	public HostdevUsb addHostdevUsbDevice()
	{
		return HostdevUsb.class.cast( this.addDevice( new HostdevUsb() ) );
	}

	/**
	 * Adds a virtual machine network device to the Libvirt domain XML document.
	 *
	 * @return reference to the added network device if creation was successful.
	 */
	public Interface addInterfaceDevice()
	{
		return Interface.class.cast( this.addDevice( new Interface() ) );
	}

	/**
	 * Adds a virtual machine network bridge interface device to the Libvirt domain XML document.
	 *
	 * @return reference to the added network interface device if creation was successful.
	 */
	public InterfaceBridge addInterfaceBridgeDevice()
	{
		return InterfaceBridge.class.cast( this.addDevice( new InterfaceBridge() ) );
	}

	/**
	 * Adds a virtual machine network interface device to the Libvirt domain XML document.
	 *
	 * @return reference to the added network interface device if creation was successful.
	 */
	public InterfaceNetwork addInterfaceNetworkDevice()
	{
		return InterfaceNetwork.class.cast( this.addDevice( new InterfaceNetwork() ) );
	}

	/**
	 * Adds a virtual machine graphics device to the Libvirt domain XML document.
	 *
	 * @return reference to the added graphics device if creation was successful.
	 */
	public Graphics addGraphicsDevice()
	{
		return Graphics.class.cast( this.addDevice( new Graphics() ) );
	}

	/**
	 * Adds a virtual machine SPICE graphics device to the Libvirt domain XML document.
	 *
	 * @return reference to the added SPICE graphics device if creation was successful.
	 */
	public GraphicsSpice addGraphicsSpiceDevice()
	{
		return GraphicsSpice.class.cast( this.addDevice( new GraphicsSpice() ) );
	}

	/**
	 * Adds a virtual machine VNC graphics device to the Libvirt domain XML document.
	 *
	 * @return reference to the added VNC graphics device if creation was successful.
	 */
	public GraphicsVnc addGraphicsVncDevice()
	{
		return GraphicsVnc.class.cast( this.addDevice( new GraphicsVnc() ) );
	}

	/**
	 * Adds a virtual machine parallel port device to the Libvirt domain XML document.
	 *
	 * @return reference to the added parallel port device if creation was successful.
	 */
	public Parallel addParallelDevice()
	{
		return Parallel.class.cast( this.addDevice( new Parallel() ) );
	}

	/**
	 * Adds a virtual machine serial port device to the Libvirt domain XML document.
	 *
	 * @return reference to the added serial port device if creation was successful.
	 */
	public Serial addSerialDevice()
	{
		return Serial.class.cast( this.addDevice( new Serial() ) );
	}

	/**
	 * Adds a virtual machine shared memory device to the Libvirt domain XML document.
	 *
	 * @return reference to the added shared memory device if creation was successful.
	 */
	public Shmem addShmemDevice()
	{
		return Shmem.class.cast( this.addDevice( new Shmem() ) );
	}

	/**
	 * Adds a virtual machine sound device to the Libvirt domain XML document.
	 *
	 * @return reference to the added sound device if creation was successful.
	 */
	public Sound addSoundDevice()
	{
		return Sound.class.cast( this.addDevice( new Sound() ) );
	}

	/**
	 * Adds a virtual machine video device to the Libvirt domain XML document.
	 *
	 * @return reference to the added video device if creation was successful.
	 */
	public Video addVideoDevice()
	{
		return Video.class.cast( this.addDevice( new Video() ) );
	}

	/**
	 * Removes boot oder entries in the Libvirt domain XML document.
	 */
	public void removeBootOrder()
	{
		// remove boot order entries of all disk devices
		for ( Disk diskDevice : this.getDiskDevices() ) {
			diskDevice.removeBootOrder();
		}

		// remove boot order entries of all network interface devices
		for ( Interface interfaceDevice : this.getInterfaceDevices() ) {
			interfaceDevice.removeBootOrder();
		}

		// remove boot order entries of all hostdev devices
		for ( Hostdev hostdevDevice : this.getHostdevDevices() ) {
			hostdevDevice.removeBootOrder();
		}

		// remove boot oder entries under the 'os' element
		this.getRootXmlNode().removeXmlElement( "os/boot" );
	}

	/**
	 * Removes underlying source for all disk devices in the Libvirt domain XML document.
	 * 
	 * @implNote Calling this method will result in an invalid Libvirt domain XML document.
	 */
	public void removeDiskDevicesStorage()
	{
		for ( Disk diskDevice : this.getDiskDevices() ) {
			diskDevice.removeStorage();
		}
	}

	/**
	 * Removes network source for all interface devices in the Libvirt domain XML document.
	 */
	public void removeInterfaceDevicesSource()
	{
		for ( Interface interfaceDevice : this.getInterfaceDevices() ) {
			// set empty source to preserve the XML attribute (to prevent XML validation errors)
			interfaceDevice.setSource( "" );
		}
	}
}
