package org.openslx.vm;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.libvirt.domain.Domain;
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
import org.openslx.libvirt.xml.LibvirtXmlDocumentException;
import org.openslx.libvirt.xml.LibvirtXmlSerializationException;
import org.openslx.libvirt.xml.LibvirtXmlValidationException;
import org.openslx.thrifthelper.TConst;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

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
 * Metadata to describe the version of a QEMU virtual machine configuration.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
class QemuHWVersionMeta
{
	/**
	 * Stores the version of a QEMU virtual machine configuration.
	 */
	private final int version;

	/**
	 * Creates metadata to describe the version of a QEMU virtual machine configuration.
	 * 
	 * @param version version of the QEMU virtual machine configuration.
	 */
	public QemuHWVersionMeta( int version )
	{
		this.version = version;
	}

	/**
	 * Returns version of the QEMU virtual machine configuration.
	 * 
	 * @return version of the QEMU virtual machine configuration.
	 */
	public int getVersion()
	{
		return this.version;
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
public class QemuMetaData extends
		VmMetaData<QemuSoundCardMeta, QemuDDAccelMeta, QemuHWVersionMeta, QemuEthernetDevTypeMeta, QemuUsbSpeedMeta>
{
	/**
	 * Default bridge name of the network bridge connected to the LAN.
	 */
	public static final String NETWORK_DEFAULT_BRIDGE = "brBwLehrpool";

	/**
	 * Default network name of the isolated host network (host only).
	 */
	public static final String NETWORK_DEFAULT_HOST_ONLY = "host";

	/**
	 * Default network name of the NAT network.
	 */
	public static final String NETWORK_DEFAULT_NAT = "nat";

	/**
	 * Default physical CDROM drive of the hypervisor host.
	 */
	public static final String CDROM_DEFAULT_PHYSICAL_DRIVE = "/dev/sr0";

	/**
	 * List of supported image formats by the QEMU hypervisor.
	 */
	private static final List<DiskImage.ImageFormat> SUPPORTED_IMAGE_FORMATS = Collections.unmodifiableList(
			Arrays.asList( ImageFormat.QCOW2, ImageFormat.VMDK, ImageFormat.VDI ) );

	/**
	 * Representation of a QEMU hypervisor (managed by Libvirt).
	 */
	private static final Virtualizer VIRTUALIZER = new Virtualizer( TConst.VIRT_QEMU, "QEMU" );

	/**
	 * Libvirt XML configuration file to modify configuration of virtual machine for QEMU.
	 */
	private Domain vmConfig = null;

	/**
	 * Stores current index of added HDD device to the Libvirt XML configuration file.
	 */
	private int vmDeviceIndexHddAdd = 0;

	/**
	 * Stores current index of added CDROM device to the Libvirt XML configuration file.
	 */
	private int vmDeviceIndexCdromAdd = 0;

	/**
	 * Stores current index of added ethernet device to the Libvirt XML configuration file.
	 */
	private int vmDeviceIndexEthernetAdd = 0;

	/**
	 * Creates new virtual machine configuration (managed by Libvirt) for the QEMU hypervisor.
	 * 
	 * @param osList list of operating systems.
	 * @param file image file for the QEMU hypervisor.
	 * @throws UnsupportedVirtualizerFormatException Libvirt XML configuration cannot be processed.
	 */
	public QemuMetaData( List<OperatingSystem> osList, File file ) throws UnsupportedVirtualizerFormatException
	{
		super( osList );

		try {
			// read and parse Libvirt domain XML configuration document
			this.vmConfig = new Domain( file );
		} catch ( LibvirtXmlDocumentException e ) {
			throw new UnsupportedVirtualizerFormatException( e.getLocalizedMessage() );
		} catch ( LibvirtXmlSerializationException e ) {
			throw new UnsupportedVirtualizerFormatException( e.getLocalizedMessage() );
		} catch ( LibvirtXmlValidationException e ) {
			throw new UnsupportedVirtualizerFormatException( e.getLocalizedMessage() );
		}

		// register virtual hardware models for graphical editing of virtual devices (GPU, sound, USB, ...)
		this.registerVirtualHW();

		// set display name of VM
		this.displayName = vmConfig.getName();

		// this property cannot be checked with the Libvirt domain XML configuration
		// to check if machine is in a paused/suspended state, look in the QEMU qcow2 image for snapshots and machine states
		this.isMachineSnapshot = false;

		// add HDDs, SSDs to QEMU metadata
		for ( DiskStorage storageDiskDevice : this.vmConfig.getDiskStorageDevices() ) {
			this.addHddMetaData( storageDiskDevice );
		}
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
		DriveBusType hddChipsetBus = QemuMetaDataUtils.convertBusType( storageDiskDevice.getBusType() );
		String hddImagePath = storageDiskDevice.getStorageSource();

		this.hdds.add( new HardDisk( hddChipsetModel, hddChipsetBus, hddImagePath ) );
	}

	@Override
	public byte[] getFilteredDefinitionArray()
	{
		// remove UUID in Libvirt domain XML configuration
		this.vmConfig.removeUuid();

		// removes all specified boot order entries
		this.vmConfig.removeBootOrder();

		// removes all referenced storage files of all specified CDROMs, Floppy drives and HDDs
		this.vmConfig.removeDiskDevicesStorage();

		// removes all source networks of all specified network interfaces
		this.vmConfig.removeInterfaceDevicesSource();

		// output filtered Libvirt domain XML configuration
		String configuration = this.vmConfig.toString();
		return configuration.getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public List<DiskImage.ImageFormat> getSupportedImageFormats()
	{
		return QemuMetaData.SUPPORTED_IMAGE_FORMATS;
	}

	@Override
	public void applySettingsForLocalEdit()
	{
		// NOT implemented yet
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		return this.addHddTemplate( diskImage.getAbsolutePath(), hddMode, redoDir );
	}

	@Override
	public boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir )
	{
		return this.addHddTemplate( this.vmDeviceIndexHddAdd++, diskImagePath, hddMode, redoDir );
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
		DiskStorage storageDiskDevice = QemuMetaDataUtils.getArrayIndex( storageDiskDevices, index );

		if ( storageDiskDevice == null ) {
			// HDD does not exist, so create new storage (HDD) device
			storageDiskDevice = this.vmConfig.addDiskStorageDevice();
			storageDiskDevice.setReadOnly( false );
			storageDiskDevice.setBusType( BusType.VIRTIO );
			String targetDevName = QemuMetaDataUtils.createAlphabeticalDeviceName( "vd", index );
			storageDiskDevice.setTargetDevice( targetDevName );
			storageDiskDevice.setStorage( StorageType.FILE, diskImagePath );

			// add new created HDD to the metadata of the QemuMetaData object, too
			this.addHddMetaData( storageDiskDevice );
		} else {
			// HDD exists, so update existing storage (HDD) device
			storageDiskDevice.setStorage( StorageType.FILE, diskImagePath );
		}

		return false;
	}

	@Override
	public boolean addDefaultNat()
	{
		return this.addEthernet( EtherType.NAT );
	}

	@Override
	public void setOs( String vendorOsId )
	{
		this.setOs( vendorOsId );
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
		BigInteger memory = BigInteger.valueOf( mem );

		this.vmConfig.setMemory( memory );
		this.vmConfig.setCurrentMemory( memory );

		final boolean isMemorySet = this.vmConfig.getMemory().toString().equals( memory.toString() );
		final boolean isCurrentMemorySet = this.vmConfig.getCurrentMemory().toString().equals( memory.toString() );

		return isMemorySet && isCurrentMemorySet;
	}

	@Override
	public void addFloppy( int index, String image, boolean readOnly )
	{
		ArrayList<DiskFloppy> floppyDiskDevices = this.vmConfig.getDiskFloppyDevices();
		DiskFloppy floppyDiskDevice = QemuMetaDataUtils.getArrayIndex( floppyDiskDevices, index );

		if ( floppyDiskDevice == null ) {
			// floppy device does not exist, so create new floppy device
			floppyDiskDevice = this.vmConfig.addDiskFloppyDevice();
			floppyDiskDevice.setBusType( BusType.FDC );
			String targetDevName = QemuMetaDataUtils.createAlphabeticalDeviceName( "fd", index );
			floppyDiskDevice.setTargetDevice( targetDevName );
			floppyDiskDevice.setReadOnly( readOnly );
			floppyDiskDevice.setStorage( StorageType.FILE, image );
		} else {
			// floppy device exists, so update existing floppy device
			floppyDiskDevice.setReadOnly( readOnly );
			floppyDiskDevice.setStorage( StorageType.FILE, image );
		}
	}

	@Override
	public boolean addCdrom( String image )
	{
		return this.addCdrom( this.vmDeviceIndexCdromAdd++, image );
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
		DiskCdrom cdromDiskDevice = QemuMetaDataUtils.getArrayIndex( cdromDiskDevices, index );

		if ( cdromDiskDevice == null ) {
			// CDROM device does not exist, so create new CDROM device
			cdromDiskDevice = this.vmConfig.addDiskCdromDevice();
			cdromDiskDevice.setBusType( BusType.SATA );
			String targetDevName = QemuMetaDataUtils.createAlphabeticalDeviceName( "sd", index );
			cdromDiskDevice.setTargetDevice( targetDevName );
			cdromDiskDevice.setReadOnly( true );

			if ( image == null ) {
				cdromDiskDevice.setStorage( StorageType.BLOCK, CDROM_DEFAULT_PHYSICAL_DRIVE );
			} else {
				cdromDiskDevice.setStorage( StorageType.FILE, image );
			}
		} else {
			// CDROM device exists, so update existing CDROM device
			cdromDiskDevice.setReadOnly( true );

			if ( image == null ) {
				cdromDiskDevice.setStorage( StorageType.BLOCK, CDROM_DEFAULT_PHYSICAL_DRIVE );
			} else {
				cdromDiskDevice.setStorage( StorageType.FILE, image );
			}
		}

		return false;
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
			soundDeviceType = QemuMetaDataUtils.convertSoundDeviceModel( soundDeviceModel );
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
	public void setHWVersion( HWVersion type )
	{
		// NOT supported by the QEMU hypervisor
	}

	@Override
	public HWVersion getHWVersion()
	{
		// NOT supported by the QEMU hypervisor
		return null;
	}

	@Override
	public void setEthernetDevType( int cardIndex, EthernetDevType type )
	{
		QemuEthernetDevTypeMeta networkDeviceConfig = this.networkCards.get( type );
		ArrayList<Interface> networkDevices = this.vmConfig.getInterfaceDevices();
		Interface networkDevice = QemuMetaDataUtils.getArrayIndex( networkDevices, cardIndex );
		Interface.Model networkDeviceModel = networkDeviceConfig.getModel();

		if ( networkDevice != null ) {
			networkDevice.setModel( networkDeviceModel );
		}
	}

	@Override
	public EthernetDevType getEthernetDevType( int cardIndex )
	{
		ArrayList<Interface> networkDevices = this.vmConfig.getInterfaceDevices();
		Interface networkDevice = QemuMetaDataUtils.getArrayIndex( networkDevices, cardIndex );
		EthernetDevType networkDeviceType = EthernetDevType.NONE;

		if ( networkDevice == null ) {
			// network interface device is not present
			networkDeviceType = EthernetDevType.NONE;
		} else {
			// get model of existing network interface device
			Interface.Model networkDeviceModel = networkDevice.getModel();
			networkDeviceType = QemuMetaDataUtils.convertNetworkDeviceModel( networkDeviceModel );
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
		UsbSpeed maxUsbSpeed = VmMetaData.UsbSpeed.NONE;
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
	public byte[] getDefinitionArray()
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
		return this.addEthernet( this.vmDeviceIndexEthernetAdd++, type );
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
		Interface interfaceDevice = QemuMetaDataUtils.getArrayIndex( interfaceDevices, index );

		final Interface.Model defaultNetworkDeviceModel = defaultNetworkDeviceConfig.getModel();

		if ( interfaceDevice == null ) {
			// network interface device does not exist, so create new network interface device
			switch ( type ) {
			case BRIDGED:
				// add network bridge interface device
				interfaceDevice = this.vmConfig.addInterfaceBridgeDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_BRIDGE );
				break;
			case HOST_ONLY:
				// add network interface device with link to the isolated host network
				interfaceDevice = this.vmConfig.addInterfaceNetworkDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_HOST_ONLY );
				break;
			case NAT:
				// add network interface device with link to the NAT network
				interfaceDevice = this.vmConfig.addInterfaceNetworkDevice();
				interfaceDevice.setModel( defaultNetworkDeviceModel );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_NAT );
				break;
			}
		} else {
			// network interface device exists, so update existing network interface device
			switch ( type ) {
			case BRIDGED:
				interfaceDevice.setType( Interface.Type.BRIDGE );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_BRIDGE );
				break;
			case HOST_ONLY:
				interfaceDevice.setType( Interface.Type.NETWORK );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_HOST_ONLY );
				break;
			case NAT:
				interfaceDevice.setType( Interface.Type.NETWORK );
				interfaceDevice.setSource( QemuMetaData.NETWORK_DEFAULT_NAT );
				break;
			}
		}

		return false;
	}

	@Override
	public Virtualizer getVirtualizer()
	{
		return QemuMetaData.VIRTUALIZER;
	}

	@Override
	public boolean tweakForNonPersistent()
	{
		// NOT implemented yet
		return false;
	}

	@Override
	public void registerVirtualHW()
	{
		// @formatter:off
		soundCards.put( VmMetaData.SoundCardType.NONE,          new QemuSoundCardMeta( null ) );
		soundCards.put( VmMetaData.SoundCardType.DEFAULT,       new QemuSoundCardMeta( Sound.Model.ICH9 ) );
		soundCards.put( VmMetaData.SoundCardType.SOUND_BLASTER, new QemuSoundCardMeta( Sound.Model.SB16 ) );
		soundCards.put( VmMetaData.SoundCardType.ES,            new QemuSoundCardMeta( Sound.Model.ES1370 ) );
		soundCards.put( VmMetaData.SoundCardType.AC,            new QemuSoundCardMeta( Sound.Model.AC97 ) );
		soundCards.put( VmMetaData.SoundCardType.HD_AUDIO,      new QemuSoundCardMeta( Sound.Model.ICH9 ) );

		ddacc.put( VmMetaData.DDAcceleration.OFF, new QemuDDAccelMeta( false ) );
		ddacc.put( VmMetaData.DDAcceleration.ON,  new QemuDDAccelMeta( true ) );

		hwversion.put( VmMetaData.HWVersion.DEFAULT, new QemuHWVersionMeta( 0 ) );

		networkCards.put( VmMetaData.EthernetDevType.NONE,      new QemuEthernetDevTypeMeta( null ) );
		networkCards.put( VmMetaData.EthernetDevType.AUTO,      new QemuEthernetDevTypeMeta( Interface.Model.VIRTIO_NET_PCI ) );
		networkCards.put( VmMetaData.EthernetDevType.PCNETPCI2, new QemuEthernetDevTypeMeta( Interface.Model.PCNET ) );
		networkCards.put( VmMetaData.EthernetDevType.E1000,     new QemuEthernetDevTypeMeta( Interface.Model.E1000 ) );
		networkCards.put( VmMetaData.EthernetDevType.E1000E,    new QemuEthernetDevTypeMeta( Interface.Model.E1000E ) );
		networkCards.put( VmMetaData.EthernetDevType.VMXNET3,   new QemuEthernetDevTypeMeta( Interface.Model.VMXNET3 ) );
		networkCards.put( VmMetaData.EthernetDevType.PARAVIRT,  new QemuEthernetDevTypeMeta( Interface.Model.VIRTIO_NET_PCI ) );

		usbSpeeds.put( VmMetaData.UsbSpeed.NONE,   new QemuUsbSpeedMeta( 0, ControllerUsb.Model.NONE ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB1_1, new QemuUsbSpeedMeta( 1, ControllerUsb.Model.ICH9_UHCI1 ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB2_0, new QemuUsbSpeedMeta( 2, ControllerUsb.Model.ICH9_EHCI1 ) );
		usbSpeeds.put( VmMetaData.UsbSpeed.USB3_0, new QemuUsbSpeedMeta( 3, ControllerUsb.Model.QEMU_XHCI ) );
		// @formatter:on
	}
}