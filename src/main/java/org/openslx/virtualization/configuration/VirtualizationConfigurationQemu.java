package org.openslx.virtualization.configuration;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.libvirt.domain.Domain;
import org.openslx.libvirt.domain.DomainUtils;
import org.openslx.libvirt.domain.device.ControllerUsb;
import org.openslx.libvirt.domain.device.Disk.BusType;
import org.openslx.libvirt.domain.device.Disk.StorageType;
import org.openslx.libvirt.libosinfo.LibOsInfo;
import org.openslx.libvirt.libosinfo.os.Os;
import org.openslx.libvirt.domain.device.DiskCdrom;
import org.openslx.libvirt.domain.device.DiskFloppy;
import org.openslx.libvirt.domain.device.DiskStorage;
import org.openslx.libvirt.domain.device.Graphics;
import org.openslx.libvirt.domain.device.GraphicsSpice;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.domain.device.Sound;
import org.openslx.libvirt.domain.device.Video;
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.openslx.util.LevenshteinDistance;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.virtualizer.VirtualizerQemu;

/**
 * Metadata to describe the hardware type of a QEMU sound card.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class QemuSoundCardMeta
{
	/**
	 * Stores the hardware model of the QEMU sound card.
	 */
	private final Sound.Model model;

	/**
	 * Creates metadata to describe the hardware model of a QEMU sound card.
	 * 
	 * @param model hardware model of the QEMU sound card.
	 */
	public QemuSoundCardMeta( Sound.Model model )
	{
		this.model = model;
	}

	/**
	 * Returns hardware model of the QEMU sound card.
	 * 
	 * @return hardware model of the QEMU sound card.
	 */
	public Sound.Model getModel()
	{
		return this.model;
	}
}

/**
 * Metadata to describe the hardware acceleration state of QEMU virtual graphics.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class QemuDDAccelMeta
{
	/**
	 * Stores state of the hardware acceleration for QEMU virtual graphics.
	 */
	private final boolean enabled;

	/**
	 * Creates metadata to describe the hardware acceleration state of QEMU virtual graphics.
	 * 
	 * @param enabled state of the hardware acceleration for QEMU virtual graphics.
	 */
	public QemuDDAccelMeta( boolean enabled )
	{
		this.enabled = enabled;
	}

	/**
	 * Returns state of the hardware acceleration of QEMU virtual graphics.
	 * 
	 * @return state of the hardware acceleration for QEMU virtual graphics.
	 */
	public boolean isEnabled()
	{
		return this.enabled;
	}
}

/**
 * Metadata to describe the hardware type of a QEMU ethernet device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class QemuEthernetDevTypeMeta
{
	/**
	 * Stores the hardware model of the QEMU ethernet device.
	 */
	private final Interface.Model model;

	/**
	 * Creates metadata to describe the hardware type of a QEMU ethernet device.
	 * 
	 * @param model hardware type of the QEMU ethernet device.
	 */
	public QemuEthernetDevTypeMeta( Interface.Model model )
	{
		this.model = model;
	}

	/**
	 * Returns the hardware type of a QEMU ethernet device.
	 * 
	 * @return hardware type of the QEMU ethernet device.
	 */
	public Interface.Model getModel()
	{
		return this.model;
	}
}

/**
 * Metadata to describe a QEMU USB controller.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class QemuUsbSpeedMeta
{
	/**
	 * Stores the USB speed of the QEMU USB controller.
	 */
	private final int speed;

	/**
	 * Stores the QEMU hardware model of the USB controller.
	 */
	private final ControllerUsb.Model model;

	/**
	 * Creates metadata to describe a QEMU USB controller.
	 * 
	 * @param speed USB speed of the QEMU USB controller.
	 * @param model QEMU hardware model of the USB controller.
	 */
	public QemuUsbSpeedMeta( int speed, ControllerUsb.Model model )
	{
		this.speed = speed;
		this.model = model;
	}

	/**
	 * Returns the speed of the QEMU USB controller.
	 * 
	 * @return speed of the QEMU USB controller.
	 */
	public int getSpeed()
	{
		return this.speed;
	}

	/**
	 * Returns QEMU hardware model of the USB controller.
	 * 
	 * @return hardware model of the QEMU USB controller.
	 */
	public ControllerUsb.Model getModel()
	{
		return this.model;
	}
}

/**
 * Virtual machine configuration (managed by Libvirt) for the QEMU hypervisor.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizationConfigurationQemu extends
		VirtualizationConfiguration<QemuSoundCardMeta, QemuDDAccelMeta, QemuEthernetDevTypeMeta, QemuUsbSpeedMeta>
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
				final LevenshteinDistance distance = new LevenshteinDistance( 1, 1, 1 );
				int smallestDistance = Integer.MAX_VALUE;

				// get name of the OS and combine it with the optional available architecture
				final String osLookupOsName = osLookup.getName() + " " + this.vmConfig.getOsArch();

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
		// removes all specified boot order entries
		this.vmConfig.removeBootOrder();

		// removes all source networks of all specified network interfaces
		this.vmConfig.removeInterfaceDevicesSource();
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
		return this.addEthernet( EtherType.NAT );
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

	@Override
	public void setSoundCard( SoundCardType type )
	{
		QemuSoundCardMeta soundDeviceConfig = this.soundCards.get( type );
		ArrayList<Sound> soundDevices = this.vmConfig.getSoundDevices();
		Sound.Model soundDeviceModel = soundDeviceConfig.getModel();

		if ( soundDevices.isEmpty() ) {
			// create new sound device with 'soundDeviceModel' hardware
			Sound soundDevice = this.vmConfig.addSoundDevice();
			soundDevice.setModel( soundDeviceModel );
		} else {
			// update sound device model type of existing sound devices
			for ( Sound soundDevice : soundDevices ) {
				soundDevice.setModel( soundDeviceModel );
			}
		}
	}

	@Override
	public SoundCardType getSoundCard()
	{
		ArrayList<Sound> soundDevices = this.vmConfig.getSoundDevices();
		SoundCardType soundDeviceType = SoundCardType.DEFAULT;

		if ( soundDevices.isEmpty() ) {
			// the VM configuration does not contain a sound card device
			soundDeviceType = SoundCardType.NONE;
		} else {
			// the VM configuration at least one sound card device, so return the type of the first one
			Sound.Model soundDeviceModel = soundDevices.get( 0 ).getModel();
			soundDeviceType = VirtualizationConfigurationQemuUtils.convertSoundDeviceModel( soundDeviceModel );
		}

		return soundDeviceType;
	}

	@Override
	public void setDDAcceleration( DDAcceleration type )
	{
		QemuDDAccelMeta accelerationConfig = this.ddacc.get( type );
		ArrayList<Graphics> graphicDevices = this.vmConfig.getGraphicDevices();
		ArrayList<Video> videoDevices = this.vmConfig.getVideoDevices();
		final boolean accelerationEnabled = accelerationConfig.isEnabled();

		boolean acceleratedGraphicsAvailable = false;

		if ( graphicDevices.isEmpty() ) {
			// add new graphics device with enabled acceleration to VM configuration
			GraphicsSpice graphicSpiceDevice = this.vmConfig.addGraphicsSpiceDevice();
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
				Video videoDevice = this.vmConfig.addVideoDevice();
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
	public DDAcceleration getDDAcceleration()
	{
		ArrayList<Graphics> graphicsDevices = this.vmConfig.getGraphicDevices();
		ArrayList<Video> videoDevices = this.vmConfig.getVideoDevices();
		DDAcceleration accelerationState = DDAcceleration.OFF;

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
			accelerationState = DDAcceleration.ON;
		} else {
			accelerationState = DDAcceleration.OFF;
		}

		return accelerationState;
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

	@Override
	public void setEthernetDevType( int cardIndex, EthernetDevType type )
	{
		QemuEthernetDevTypeMeta networkDeviceConfig = this.networkCards.get( type );
		ArrayList<Interface> networkDevices = this.vmConfig.getInterfaceDevices();
		Interface networkDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( networkDevices, cardIndex );
		Interface.Model networkDeviceModel = networkDeviceConfig.getModel();

		if ( networkDevice != null ) {
			networkDevice.setModel( networkDeviceModel );
		}
	}

	@Override
	public EthernetDevType getEthernetDevType( int cardIndex )
	{
		ArrayList<Interface> networkDevices = this.vmConfig.getInterfaceDevices();
		Interface networkDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( networkDevices, cardIndex );
		EthernetDevType networkDeviceType = EthernetDevType.NONE;

		if ( networkDevice == null ) {
			// network interface device is not present
			networkDeviceType = EthernetDevType.NONE;
		} else {
			// get model of existing network interface device
			Interface.Model networkDeviceModel = networkDevice.getModel();
			networkDeviceType = VirtualizationConfigurationQemuUtils.convertNetworkDeviceModel( networkDeviceModel );
		}

		return networkDeviceType;
	}

	@Override
	public void setMaxUsbSpeed( UsbSpeed speed )
	{
		QemuUsbSpeedMeta usbControllerConfig = this.usbSpeeds.get( speed );
		ArrayList<ControllerUsb> usbControllerDevices = this.vmConfig.getUsbControllerDevices();
		ControllerUsb.Model usbControllerModel = usbControllerConfig.getModel();

		if ( usbControllerDevices.isEmpty() ) {
			// add new USB controller with specified speed 'usbControllerModel'
			ControllerUsb usbControllerDevice = this.vmConfig.addControllerUsbDevice();
			usbControllerDevice.setModel( usbControllerModel );
		} else {
			// update model of all USB controller devices to support the maximum speed
			for ( ControllerUsb usbControllerDevice : usbControllerDevices ) {
				usbControllerDevice.setModel( usbControllerModel );
			}
		}
	}

	@Override
	public UsbSpeed getMaxUsbSpeed()
	{
		ArrayList<ControllerUsb> usbControllerDevices = this.vmConfig.getUsbControllerDevices();
		UsbSpeed maxUsbSpeed = VirtualizationConfiguration.UsbSpeed.NONE;
		int maxUsbSpeedNumeric = 0;

		for ( ControllerUsb usbControllerDevice : usbControllerDevices ) {
			ControllerUsb.Model usbControllerModel = usbControllerDevice.getModel();

			for ( Entry<UsbSpeed, QemuUsbSpeedMeta> usbSpeedEntry : this.usbSpeeds.entrySet() ) {
				QemuUsbSpeedMeta usbSpeed = usbSpeedEntry.getValue();
				if ( usbSpeed.getSpeed() > maxUsbSpeedNumeric && usbSpeed.getModel() == usbControllerModel ) {
					maxUsbSpeed = usbSpeedEntry.getKey();
					maxUsbSpeedNumeric = usbSpeed.getSpeed();
				}
			}
		}

		return maxUsbSpeed;
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
		QemuEthernetDevTypeMeta defaultNetworkDeviceConfig = this.networkCards.get( EthernetDevType.AUTO );
		ArrayList<Interface> interfaceDevices = this.vmConfig.getInterfaceDevices();
		Interface interfaceDevice = VirtualizationConfigurationQemuUtils.getArrayIndex( interfaceDevices, index );

		final Interface.Model defaultNetworkDeviceModel = defaultNetworkDeviceConfig.getModel();

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
		// @formatter:off
		soundCards.put( VirtualizationConfiguration.SoundCardType.NONE,          new QemuSoundCardMeta( null ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.DEFAULT,       new QemuSoundCardMeta( Sound.Model.ICH9 ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.SOUND_BLASTER, new QemuSoundCardMeta( Sound.Model.SB16 ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.ES,            new QemuSoundCardMeta( Sound.Model.ES1370 ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.AC,            new QemuSoundCardMeta( Sound.Model.AC97 ) );
		soundCards.put( VirtualizationConfiguration.SoundCardType.HD_AUDIO,      new QemuSoundCardMeta( Sound.Model.ICH9 ) );

		ddacc.put( VirtualizationConfiguration.DDAcceleration.OFF, new QemuDDAccelMeta( false ) );
		ddacc.put( VirtualizationConfiguration.DDAcceleration.ON,  new QemuDDAccelMeta( true ) );

		networkCards.put( VirtualizationConfiguration.EthernetDevType.NONE,      new QemuEthernetDevTypeMeta( null ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.AUTO,      new QemuEthernetDevTypeMeta( Interface.Model.VIRTIO_NET_PCI ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PCNETPCI2, new QemuEthernetDevTypeMeta( Interface.Model.PCNET ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.E1000,     new QemuEthernetDevTypeMeta( Interface.Model.E1000 ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.E1000E,    new QemuEthernetDevTypeMeta( Interface.Model.E1000E ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.VMXNET3,   new QemuEthernetDevTypeMeta( Interface.Model.VMXNET3 ) );
		networkCards.put( VirtualizationConfiguration.EthernetDevType.PARAVIRT,  new QemuEthernetDevTypeMeta( Interface.Model.VIRTIO_NET_PCI ) );

		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.NONE,   new QemuUsbSpeedMeta( 0, ControllerUsb.Model.NONE ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB1_1, new QemuUsbSpeedMeta( 1, ControllerUsb.Model.ICH9_UHCI1 ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB2_0, new QemuUsbSpeedMeta( 2, ControllerUsb.Model.ICH9_EHCI1 ) );
		usbSpeeds.put( VirtualizationConfiguration.UsbSpeed.USB3_0, new QemuUsbSpeedMeta( 3, ControllerUsb.Model.QEMU_XHCI ) );
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
