package org.openslx.virtualization.configuration;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.DomainUtils;
import org.openslx.libvirt.domain.device.ControllerUsb;
import org.openslx.libvirt.domain.device.Disk.BusType;
import org.openslx.libvirt.domain.device.Disk.StorageType;
import org.openslx.libvirt.domain.device.DiskCdrom;
import org.openslx.libvirt.domain.device.DiskFloppy;
import org.openslx.libvirt.domain.device.DiskStorage;
import org.openslx.libvirt.domain.device.Graphics;
import org.openslx.libvirt.domain.device.GraphicsSpice;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.domain.device.Sound;
import org.openslx.libvirt.domain.device.Video;
import org.openslx.libvirt.libosinfo.LibOsInfo;
import org.openslx.libvirt.libosinfo.os.Os;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.openslx.util.LevenshteinDistance;
import org.openslx.util.Util;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.hardware.VirtOptionValue;
import org.openslx.virtualization.hardware.ConfigurationGroups;
import org.openslx.virtualization.hardware.Ethernet;
import org.openslx.virtualization.hardware.SoundCard;
import org.openslx.virtualization.hardware.Usb;
import org.openslx.virtualization.virtualizer.VirtualizerQemu;

/**
 * Virtual machine configuration (managed by Libvirt) for the QEMU hypervisor.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizationConfigurationQemu extends VirtualizationConfiguration
{
	/**
	 * Name of the network bridge for the LAN.
	 */
	public static final String NETWORK_BRIDGE_LAN_DEFAULT = "br0";

	/**
	 * Name of the network bridge for the default NAT network.
	 */
	public static final String NETWORK_BRIDGE_NAT_DEFAULT = "nat1";

	/**
	 * Name of the network for the isolated host network (host only).
	 */
	public static final String NETWORK_BRIDGE_HOST_ONLY_DEFAULT = "vsw2";

	/**
	 * Default physical CDROM drive of the hypervisor host.
	 */
	public static final String CDROM_DEFAULT_PHYSICAL_DRIVE = "/dev/sr0";

	/**
	 * File name extension for QEMU (Libvirt) virtualization configuration files.
	 */
	public static final String FILE_NAME_EXTENSION = "xml";

	/**
	 * Libvirt XML configuration file to modify configuration of virtual machine for QEMU.
	 */
	private Domain vmConfig = null;

	/**
	 * Creates new virtual machine configuration (managed by Libvirt) for the QEMU hypervisor.
	 * 
	 * @param osList list of operating systems.
	 * @param file image file for the QEMU hypervisor.
	 * 
	 * @throws VirtualizationConfigurationException Libvirt XML configuration cannot be processed.
	 */
	public VirtualizationConfigurationQemu( List<OperatingSystem> osList, File file )
			throws VirtualizationConfigurationException
	{
		super( new VirtualizerQemu(), osList );

		try {
			// read and parse Libvirt domain XML configuration document
			this.vmConfig = new Domain( file );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
			throw new VirtualizationConfigurationException( e.getLocalizedMessage() );
		}

		// parse VM config and initialize fields of QemuMetaData class
		this.parseVmConfig();
	}

	/**
	 * Creates new virtual machine configuration (managed by Libvirt) for the QEMU hypervisor.
	 * 
	 * @param osList list of operating systems.
	 * @param vmContent file content for the QEMU hypervisor.
	 * @param length number of bytes of the file content.
	 * 
	 * @throws VirtualizationConfigurationException Libvirt XML configuration cannot be processed.
	 */
	public VirtualizationConfigurationQemu( List<OperatingSystem> osList, byte[] vmContent, int length )
			throws VirtualizationConfigurationException
	{
		super( new VirtualizerQemu(), osList );

		try {
			// read and parse Libvirt domain XML configuration document
			this.vmConfig = new Domain( new String( vmContent ) );
		} catch ( LibvirtXmlDocumentException | LibvirtXmlSerializationException | LibvirtXmlValidationException e ) {
			throw new VirtualizationConfigurationException( e.getLocalizedMessage() );
		}

		// parse VM config and initialize fields of QemuMetaData class
		this.parseVmConfig();
	}

	/**
	 * Parses Libvirt domain XML configuration to initialize QEMU metadata.
	 */
	private void parseVmConfig()
	{
		// set display name of VM
		this.displayName = vmConfig.getName();

		// this property cannot be checked with the Libvirt domain XML configuration
		// to check if machine is in a paused/suspended state, look in the QEMU qcow2 image for snapshots and machine states
		this.isMachineSnapshot = false;

		// add HDDs, SSDs to QEMU metadata
		for ( DiskStorage storageDiskDevice : this.vmConfig.getDiskStorageDevices() ) {
			this.addHddMetaData( storageDiskDevice );
		}

		// detect the operating system from the optional embedded libosinfo metadata
		this.setOs( this.vmConfig.getLibOsInfoOsId() );
	}

	/**
	 * Adds an existing and observed storage disk device to the HDD metadata.
	 * 
	 * @param storageDiskDevice existing and observed storage disk that should be added to the
	 *           metadata.
	 */
	private void addHddMetaData( DiskStorage storageDiskDevice )
	{
		String hddChipsetModel = null;
		DriveBusType hddChipsetBus = VirtualizationConfigurationQemuUtils
				.convertBusType( storageDiskDevice.getBusType() );
		String hddImagePath = storageDiskDevice.getStorageSource();

		this.hdds.add( new HardDisk( hddChipsetModel, hddChipsetBus, hddImagePath ) );
	}

	/**
	 * Detects the operating system by the specified libosinfo operating system identifier.
	 * 
	 * @param osId libosinfo operating system identifier.
	 */
	private OperatingSystem detectOperatingSystem( String osId )
	{
		OperatingSystem os = null;

		if ( osId != null && !osId.isEmpty() ) {
			// lookup operating system identifier in the libosinfo database
			final Os osLookup = LibOsInfo.lookupOs( osId );

			// check if entry in the database was found
			if ( osLookup != null ) {
				// operating system entry was found
				// so determine OpenSLX OS name with the smallest distance to the libosinfo OS name
				final LevenshteinDistance distance = new LevenshteinDistance( 2, 1, 1 );
				int smallestDistance = Integer.MAX_VALUE;

				// get name of the OS and combine it with the optional available architecture
				String osLookupOsName = osLookup.getName();
				final int osArchSize = VirtualizationConfigurationQemuUtils.getOsArchSize( this.vmConfig.getOsArch() );

				if ( osArchSize > 0 ) {
					// append architecture size in bit if information is available from the specified architecture
					osLookupOsName += " (" + osArchSize + " Bit)";
				}

				for ( final OperatingSystem osCandidate : this.osList ) {
					final int currentDistance = distance.calculateDistance( osLookupOsName, osCandidate.getOsName() );

					if ( currentDistance < smallestDistance ) {
						// if the distance is smaller save the current distance and operating system as best candidate
						smallestDistance = currentDistance;
						os = osCandidate;
					}
				}
			}
		}

		return os;
	}

	@Override
	public void transformEditable() throws VirtualizationConfigurationException
	{
	}

	@Override
	public boolean addEmptyHddTemplate()
	{
		return this.addHddTemplate( new String(), null, null );
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		return this.addHddTemplate( diskImage.getAbsolutePath(), hddMode, redoDir );
	}

	@Override
	public boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir )
	{
		int index = this.vmConfig.getDiskStorageDevices().size() - 1;
		index = ( index > 0 ) ? index : 0;
		return this.addHddTemplate( index, diskImagePath, hddMode, redoDir );
	}

	/**
	 * Adds hard disk drive (HDD) to the QEMU virtual machine configuration.
	 * 
	 * @param index current index of HDD to be added to the virtual machine configuration.
	 * @param diskImagePath path to the virtual disk image for the HDD.
	 * @param hddMode operation mode of the HDD.
	 * @param redoDir directory for the redo log if an independent non-persistent
	 *           <code>hddMode</code> is set.
	 * @return result state of adding the HDD.
	 */
	public boolean addHddTemplate( int index, String diskImagePath, String hddMode, String redoDir )
	{
		ArrayList<DiskStorage> storageDiskDevices = this.vmConfig.getDiskStorageDevices();
		DiskStorage storageDiskDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( storageDiskDevices, index );

		if ( storageDiskDevice == null ) {
			// HDD does not exist, so create new storage (HDD) device
			storageDiskDevice = this.vmConfig.addDiskStorageDevice();
			storageDiskDevice.setReadOnly( false );
			storageDiskDevice.setBusType( BusType.VIRTIO );
			String targetDevName = VirtualizationConfigurationQemuUtils.createAlphabeticalDeviceName( "vd", index );
			storageDiskDevice.setTargetDevice( targetDevName );

			if ( diskImagePath == null || diskImagePath.isEmpty() ) {
				storageDiskDevice.removeStorage();
			} else {
				storageDiskDevice.setStorage( StorageType.FILE, diskImagePath );
			}

			// add new created HDD to the metadata of the QemuMetaData object, too
			this.addHddMetaData( storageDiskDevice );
		} else {
			// HDD exists, so update existing storage (HDD) device
			if ( diskImagePath == null || diskImagePath.isEmpty() ) {
				storageDiskDevice.removeStorage();
			} else {
				storageDiskDevice.setStorage( StorageType.FILE, diskImagePath );
			}
		}

		return true;
	}

	@Override
	public boolean addDefaultNat()
	{
		// since network interface was not filtered during VM upload,
		// do not add or configure any network interface here
		return true;
	}

	@Override
	public void setOs( String vendorOsId )
	{
		final OperatingSystem os = this.detectOperatingSystem( vendorOsId );
		this.setOs( os );
	}

	@Override
	public boolean addDisplayName( String name )
	{
		this.vmConfig.setName( name );

		final boolean statusName = this.vmConfig.getName().equals( name );

		return statusName;
	}

	@Override
	public boolean addRam( int mem )
	{
		// convert given memory in MiB to memory in bytes for Libvirt XML Domain API functions
		final BigInteger memory = DomainUtils.decodeMemory( Integer.toString( mem ), "MiB" );

		this.vmConfig.setMemory( memory );
		this.vmConfig.setCurrentMemory( memory );

		final boolean isMemorySet = this.vmConfig.getMemory().equals( memory );
		final boolean isCurrentMemorySet = this.vmConfig.getCurrentMemory().equals( memory );

		return isMemorySet && isCurrentMemorySet;
	}

	@Override
	public void addFloppy( int index, String image, boolean readOnly )
	{
		ArrayList<DiskFloppy> floppyDiskDevices = this.vmConfig.getDiskFloppyDevices();
		DiskFloppy floppyDiskDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( floppyDiskDevices, index );

		if ( floppyDiskDevice == null ) {
			// floppy device does not exist, so create new floppy device
			floppyDiskDevice = this.vmConfig.addDiskFloppyDevice();
			floppyDiskDevice.setBusType( BusType.FDC );
			String targetDevName = VirtualizationConfigurationQemuUtils.createAlphabeticalDeviceName( "fd", index );
			floppyDiskDevice.setTargetDevice( targetDevName );
			floppyDiskDevice.setReadOnly( readOnly );

			if ( image == null || image.isEmpty() ) {
				floppyDiskDevice.removeStorage();
			} else {
				floppyDiskDevice.setStorage( StorageType.FILE, image );
			}
		} else {
			// floppy device exists, so update existing floppy device
			floppyDiskDevice.setReadOnly( readOnly );

			if ( image == null || image.isEmpty() ) {
				floppyDiskDevice.removeStorage();
			} else {
				floppyDiskDevice.setStorage( StorageType.FILE, image );
			}
		}
	}

	@Override
	public boolean addCdrom( String image )
	{
		int index = this.vmConfig.getDiskCdromDevices().size() - 1;
		index = ( index > 0 ) ? index : 0;
		return this.addCdrom( index, image );
	}

	/**
	 * Adds CDROM drive to the QEMU virtual machine configuration.
	 * 
	 * @param index current index of CDROM drive to be added to the virtual machine configuration.
	 * @param image path to a virtual image that will be inserted as CDROM into the drive.
	 * @return result state of adding the CDROM drive.
	 */
	public boolean addCdrom( int index, String image )
	{
		ArrayList<DiskCdrom> cdromDiskDevices = this.vmConfig.getDiskCdromDevices();
		DiskCdrom cdromDiskDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( cdromDiskDevices, index );

		if ( cdromDiskDevice == null ) {
			// CDROM device does not exist, so create new CDROM device
			cdromDiskDevice = this.vmConfig.addDiskCdromDevice();
			cdromDiskDevice.setBusType( BusType.SATA );
			String targetDevName = VirtualizationConfigurationQemuUtils.createAlphabeticalDeviceName( "sd", index );
			cdromDiskDevice.setTargetDevice( targetDevName );
			cdromDiskDevice.setReadOnly( true );

			if ( image == null ) {
				cdromDiskDevice.setStorage( StorageType.BLOCK, CDROM_DEFAULT_PHYSICAL_DRIVE );
			} else {
				if ( image.isEmpty() ) {
					cdromDiskDevice.removeStorage();
				} else {
					cdromDiskDevice.setStorage( StorageType.FILE, image );
				}
			}
		} else {
			// CDROM device exists, so update existing CDROM device
			cdromDiskDevice.setReadOnly( true );

			if ( image == null ) {
				cdromDiskDevice.setStorage( StorageType.BLOCK, CDROM_DEFAULT_PHYSICAL_DRIVE );
			} else {
				if ( image.isEmpty() ) {
					cdromDiskDevice.removeStorage();
				} else {
					cdromDiskDevice.setStorage( StorageType.FILE, image );
				}
			}
		}

		return true;
	}

	@Override
	public boolean addCpuCoreCount( int nrOfCores )
	{
		this.vmConfig.setVCpu( nrOfCores );

		boolean isVCpuSet = this.vmConfig.getVCpu() == nrOfCores;

		return isVCpuSet;
	}
	
	class QemuGfxType extends VirtOptionValue
	{

		public QemuGfxType( String id, String displayName )
		{
			super( id, displayName );
		}

		@Override
		public void apply()
		{
			ArrayList<Graphics> graphicDevices = vmConfig.getGraphicDevices();
			ArrayList<Video> videoDevices = vmConfig.getVideoDevices();
			final boolean accelerationEnabled = this.id.equals( "true" );

			boolean acceleratedGraphicsAvailable = false;

			if ( graphicDevices.isEmpty() ) {
				// add new graphics device with enabled acceleration to VM configuration
				GraphicsSpice graphicSpiceDevice = vmConfig.addGraphicsSpiceDevice();
				graphicSpiceDevice.setOpenGl( true );
				acceleratedGraphicsAvailable = true;
			} else {
				// enable graphic acceleration of existing graphics devices
				for ( Graphics graphicDevice : graphicDevices ) {
					// set hardware acceleration for SPICE based graphics output
					// other graphic devices do not support hardware acceleration
					if ( graphicDevice instanceof GraphicsSpice ) {
						GraphicsSpice.class.cast( graphicDevice ).setOpenGl( true );
						acceleratedGraphicsAvailable = true;
					}
				}
			}

			// only configure hardware acceleration of video card(s) if graphics with hardware acceleration is available
			if ( acceleratedGraphicsAvailable ) {
				if ( videoDevices.isEmpty() ) {
					// add new video device with enabled acceleration to VM configuration
					Video videoDevice = vmConfig.addVideoDevice();
					videoDevice.setModel( Video.Model.VIRTIO );
					videoDevice.set2DAcceleration( true );
					videoDevice.set3DAcceleration( true );
				} else {
					// enable graphic acceleration of existing graphics and video devices
					for ( Video videoDevice : videoDevices ) {
						// set hardware acceleration for Virtio GPUs
						// other GPUs do not support hardware acceleration
						if ( videoDevice.getModel() == Video.Model.VIRTIO ) {
							videoDevice.set2DAcceleration( accelerationEnabled );
							videoDevice.set3DAcceleration( accelerationEnabled );
						}
					}
				}
			}
		}

		@Override
		public boolean isActive()
		{
			ArrayList<Graphics> graphicsDevices = vmConfig.getGraphicDevices();
			ArrayList<Video> videoDevices = vmConfig.getVideoDevices();

			boolean acceleratedGraphicsAvailable = false;
			boolean acceleratedVideoDevAvailable = false;

			// search for hardware accelerated graphics
			for ( Graphics graphicDevice : graphicsDevices ) {
				// only SPICE based graphic devices support hardware acceleration
				if ( graphicDevice instanceof GraphicsSpice ) {
					acceleratedGraphicsAvailable = true;
					break;
				}
			}

			// search for hardware accelerated video devices
			for ( Video videoDevice : videoDevices ) {
				// only Virtio based video devices support hardware acceleration
				if ( videoDevice.getModel() == Video.Model.VIRTIO ) {
					acceleratedVideoDevAvailable = true;
					break;
				}
			}

			// hardware acceleration is available if at least one accelerated graphics and video device is available
			if ( acceleratedGraphicsAvailable && acceleratedVideoDevAvailable ) {
				return this.id.equals( "true" );
			} else {
				return this.id.equals( "false" );
			}
		}
		
	}

	@Override
	public void setVirtualizerVersion( Version type )
	{
		if ( type != null ) {
			final String osMachine = this.vmConfig.getOsMachine();
			final String osMachineName = VirtualizationConfigurationQemuUtils.getOsMachineName( osMachine );

			if ( osMachineName != null && !osMachineName.isEmpty() ) {
				final String modifiedOsMachineVersion = VirtualizationConfigurationQemuUtils.getOsMachineVersion( type );
				final String modifiedOsMachine = VirtualizationConfigurationQemuUtils.getOsMachine( osMachineName,
						modifiedOsMachineVersion );
				this.vmConfig.setOsMachine( modifiedOsMachine );
			}
		}
	}

	@Override
	public Version getVirtualizerVersion()
	{
		final String osMachine = this.vmConfig.getOsMachine();
		final Version uncheckedVersion = VirtualizationConfigurationQemuUtils.getOsMachineVersion( osMachine );
		final Version checkedVersion;

		if ( uncheckedVersion == null ) {
			checkedVersion = null;
		} else {
			checkedVersion = Version.getInstanceByMajorMinorFromVersions( uncheckedVersion.getMajor(),
					uncheckedVersion.getMinor(), this.getVirtualizer().getSupportedVersions() );
		}

		return checkedVersion;
	}

	class QemuNicModel extends VirtOptionValue
	{
		
		private final int cardIndex;

		public QemuNicModel( int cardIndex, Interface.Model model, String displayName )
		{
			super( model.toString(), displayName ); // XXX: toString/fromString would disappear if
			this.cardIndex = cardIndex; // this were AbstractConfigurableOption<T extends Enum<T>>
		}

		@Override
		public void apply()
		{
			ArrayList<Interface> networkDevices = vmConfig.getInterfaceDevices();
			Interface networkDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( networkDevices, cardIndex );

			if ( networkDevice != null ) {
				networkDevice.setModel( Interface.Model.fromString( id ) );
			}
		}

		@Override
		public boolean isActive()
		{
			ArrayList<Interface> networkDevices = vmConfig.getInterfaceDevices();
			Interface networkDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( networkDevices, cardIndex );

			if ( networkDevice == null ) {
				// network interface device is not present
				return Util.isEmptyString( this.id ); // XXX: would be more explicit with enum.NONE
			}
			// get model of existing network interface device
			Interface.Model networkDeviceModel = networkDevice.getModel();
			if ( networkDeviceModel == null ) {
				return Util.isEmptyString( this.id ); // see above
			}
			// Success
			return networkDeviceModel.toString().equals( this.id ); // XXX: enum would allow simple ==
		}
	}
	
	class QemuSoundCardModel extends VirtOptionValue
	{

		public QemuSoundCardModel( Sound.Model id, String displayName )
		{
			super( id.toString(), displayName );
		}

		@Override
		public void apply()
		{
			ArrayList<Sound> soundDevices = vmConfig.getSoundDevices();
			Sound.Model soundDeviceModel = Sound.Model.fromString( this.id );

			if ( soundDevices.isEmpty() ) {
				// create new sound device with 'soundDeviceModel' hardware
				Sound soundDevice = vmConfig.addSoundDevice();
				soundDevice.setModel( soundDeviceModel );
			} else {
				// update sound device model type of existing sound devices
				for ( Sound soundDevice : soundDevices ) {
					soundDevice.setModel( soundDeviceModel );
				}
			}
		}

		@Override
		public boolean isActive()
		{
			ArrayList<Sound> soundDevices = vmConfig.getSoundDevices();

			if ( soundDevices.isEmpty() ) {
				// the VM configuration does not contain a sound card device
				return Util.isEmptyString( this.id );
			}
			// the VM configuration at least one sound card device, so return the type of the first one
			Sound.Model soundDeviceModel = soundDevices.get( 0 ).getModel();
			return soundDeviceModel != null && soundDeviceModel.toString().equals( this.id );
		}
		
	}
	
	class QemuUsbSpeed extends VirtOptionValue
	{

		public QemuUsbSpeed( ControllerUsb.Model id, String displayName )
		{
			super( id.toString(), displayName );
		}

		@Override
		public void apply()
		{
			ArrayList<ControllerUsb> usbControllerDevices = vmConfig.getUsbControllerDevices();
			ControllerUsb.Model usbControllerModel = ControllerUsb.Model.fromString( this.id );

			if ( usbControllerDevices.isEmpty() ) {
				// add new USB controller with specified speed 'usbControllerModel'
				ControllerUsb usbControllerDevice = vmConfig.addControllerUsbDevice();
				usbControllerDevice.setModel( usbControllerModel );
			} else {
				// update model of all USB controller devices to support the maximum speed
				for ( ControllerUsb usbControllerDevice : usbControllerDevices ) {
					usbControllerDevice.setModel( usbControllerModel );
				}
			}
		}

		@Override
		public boolean isActive()
		{
			ArrayList<ControllerUsb> usbControllerDevices = vmConfig.getUsbControllerDevices();
			String maxUsbSpeed = null;
			int maxUsbSpeedNumeric = 0;

			for ( ControllerUsb usbControllerDevice : usbControllerDevices ) {
				ControllerUsb.Model usbControllerModel = usbControllerDevice.getModel();

				// TODO Need something to map from chip to usb speed. But this is conceptually broken anyways since
				// it's modeled after vmware, where you only cannot configure different controllers at the same time
				// anyways XXX
				if ( usbControllerModel.toString().equals( this.id ) )
					return true;
			}

			return false;
		}
		
	}

	@Override
	public byte[] getConfigurationAsByteArray()
	{
		String configuration = this.vmConfig.toString();

		if ( configuration == null ) {
			return null;
		} else {
			// append newline at the end of the XML content to match the structure of an original Libvirt XML file
			configuration += System.lineSeparator();
			return configuration.getBytes( StandardCharsets.UTF_8 );
		}
	}

	@Override
	public boolean addEthernet( EtherType type )
	{
		int index = this.vmConfig.getInterfaceDevices().size() - 1;
		index = ( index > 0 ) ? index : 0;
		return this.addEthernet( index, type );
	}

	/**
	 * Adds an ethernet card to the QEMU virtual machine configuration.
	 * 
	 * @param index current index of the ethernet card to be added to the virtual machine
	 *           configuration.
	 * @param type card model of the ethernet card.
	 * @return result state of adding the ethernet card.
	 */
	public boolean addEthernet( int index, EtherType type )
	{
		ArrayList<Interface> interfaceDevices = this.vmConfig.getInterfaceDevices();
		Interface interfaceDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( interfaceDevices, index );

		final Interface.Model defaultNetworkDeviceModel = Interface.Model.VIRTIO_NET_PCI;

		if ( interfaceDevice == null ) {
			// network interface device does not exist, so create new network interface device
			switch ( type ) {
			case BRIDGED:
				// add network bridge interface device
				interfaceDevice = this.vmConfig.addInterfaceBridgeDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_LAN_DEFAULT );
				break;
			case HOST_ONLY:
				// add network interface device with link to the isolated host network
				interfaceDevice = this.vmConfig.addInterfaceBridgeDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_HOST_ONLY_DEFAULT );
				break;
			case NAT:
				// add network interface device with link to the NAT network
				interfaceDevice = this.vmConfig.addInterfaceBridgeDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_NAT_DEFAULT );
				break;
			}
		} else {
			// network interface device exists, so update existing network interface device
			switch ( type ) {
			case BRIDGED:
				interfaceDevice.setType( Interface.Type.BRIDGE );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_LAN_DEFAULT );
				break;
			case HOST_ONLY:
				interfaceDevice.setType( Interface.Type.BRIDGE );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_HOST_ONLY_DEFAULT );
				break;
			case NAT:
				interfaceDevice.setType( Interface.Type.BRIDGE );
				interfaceDevice.setSource( VirtualizationConfigurationQemu.NETWORK_BRIDGE_NAT_DEFAULT );
				break;
			}
		}

		return true;
	}

	@Override
	public void transformNonPersistent() throws VirtualizationConfigurationException
	{
		// NOT implemented yet
	}

	@Override
	public void transformPrivacy() throws VirtualizationConfigurationException
	{
		// removes all referenced storage files of all specified CDROMs, Floppy drives and HDDs
		this.vmConfig.removeDiskDevicesStorage();
	}

	@Override
	public void registerVirtualHW()
	{
		// XXX Add missing qemu-only types/models
		List<VirtOptionValue> list;
		// @formatter:off
		list = new ArrayList<>();
		//list.add( new QemuSoundCardModel( Sound.Model.NONE, SoundCard.NONE ) ); // XXX TODO
		list.add( new QemuSoundCardModel( Sound.Model.ICH9, SoundCard.DEFAULT ) );
		list.add( new QemuSoundCardModel( Sound.Model.SB16, SoundCard.SOUND_BLASTER ) );
		list.add( new QemuSoundCardModel( Sound.Model.ES1370, SoundCard.ES ) );
		list.add( new QemuSoundCardModel( Sound.Model.AC97, SoundCard.AC ) );
		list.add( new QemuSoundCardModel( Sound.Model.ICH9, SoundCard.HD_AUDIO ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.SOUND_CARD_MODEL, list ) );

		list = new ArrayList<>();
		// XXX This would greatly benefit from having more meaningful options for qemu instead of on/off
		list.add( new QemuGfxType( "false", "langsam" ) );
		list.add( new QemuGfxType( "true", "3D OpenGL" ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.GFX_TYPE, list ) );

		list = new ArrayList<>();
		// XXX Represent NONE; can add missing models now with new approach (add human readable strings)
		list.add( new QemuNicModel( 0, Interface.Model.VIRTIO,         Ethernet.AUTO ) );
		list.add( new QemuNicModel( 0, Interface.Model.PCNET,          Ethernet.PCNETPCI2 ) );
		list.add( new QemuNicModel( 0, Interface.Model.E1000,          Ethernet.E1000 ) );
		list.add( new QemuNicModel( 0, Interface.Model.E1000E,         Ethernet.E1000E ) );
		list.add( new QemuNicModel( 0, Interface.Model.VMXNET3,        Ethernet.VMXNET3 ) );
		list.add( new QemuNicModel( 0, Interface.Model.VIRTIO_NET_PCI, Ethernet.PARAVIRT ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.NIC_MODEL, list ) );

		list = new ArrayList<>();
		list.add( new QemuUsbSpeed( ControllerUsb.Model.NONE, Usb.NONE ) );
		list.add( new QemuUsbSpeed( ControllerUsb.Model.ICH9_UHCI1, Usb.USB1_1 ) );
		list.add( new QemuUsbSpeed( ControllerUsb.Model.ICH9_EHCI1, Usb.USB2_0 ) );
		list.add( new QemuUsbSpeed( ControllerUsb.Model.QEMU_XHCI, Usb.USB3_0 ) );
		configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.USB_SPEED, list ) );
		// @formatter:on
	}

	@Override
	public String getFileNameExtension()
	{
		return VirtualizationConfigurationQemu.FILE_NAME_EXTENSION;
	}

	@Override
	public void validate() throws VirtualizationConfigurationException
	{
		try {
			this.vmConfig.validateXml();
		} catch ( LibvirtXmlValidationException e ) {
			throw new VirtualizationConfigurationException( e.getLocalizedMessage() );
		}
	}
}
