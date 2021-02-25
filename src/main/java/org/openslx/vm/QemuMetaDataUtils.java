package org.openslx.vm;

import java.util.ArrayList;

import org.openslx.libvirt.domain.device.Disk;
import org.openslx.libvirt.domain.device.Interface;
import org.openslx.libvirt.domain.device.Disk.BusType;
import org.openslx.vm.VmMetaData.DriveBusType;
import org.openslx.vm.VmMetaData.EthernetDevType;
import org.openslx.vm.VmMetaData.SoundCardType;
import org.openslx.libvirt.domain.device.Sound;

/**
 * Collection of utils to convert data types from bwLehrpool to Libvirt and vice versa.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class QemuMetaDataUtils
{
	/**
	 * Converts a Libvirt disk device bus type to a VM metadata driver bus type.
	 * 
	 * @param busType Libvirt disk device bus type.
	 * @return VM metadata bus type of the disk drive.
	 */
	public static DriveBusType convertBusType( Disk.BusType busType )
	{
		DriveBusType type = null;

		switch ( busType ) {
		case IDE:
			type = DriveBusType.IDE;
			break;
		case SATA:
			type = DriveBusType.SATA;
			break;
		case SCSI:
			type = DriveBusType.SCSI;
			break;
		default:
			type = null;
			break;
		}

		return type;
	}

	/**
	 * Converts a VM metadata driver bus type to a Libvirt disk device bus type.
	 * 
	 * @param busType VM metadata bus type of the disk drive.
	 * @return Libvirt disk device bus type.
	 */
	public static Disk.BusType convertBusType( DriveBusType busType )
	{
		Disk.BusType type = null;

		switch ( busType ) {
		case IDE:
			type = BusType.IDE;
			break;
		case SATA:
			type = BusType.SATA;
			break;
		case SCSI:
			type = BusType.SCSI;
			break;
		}

		return type;
	}

	/**
	 * Converts a Libvirt sound device model to a VM metadata sound card type.
	 * 
	 * @param soundDeviceModel Libvirt sound device model.
	 * @return VM metadata sound card type.
	 */
	public static SoundCardType convertSoundDeviceModel( Sound.Model soundDeviceModel )
	{
		SoundCardType type = SoundCardType.NONE;

		switch ( soundDeviceModel ) {
		case AC97:
			type = SoundCardType.AC;
			break;
		case ES1370:
			type = SoundCardType.ES;
			break;
		case ICH6:
			type = SoundCardType.HD_AUDIO;
			break;
		case ICH9:
			type = SoundCardType.HD_AUDIO;
			break;
		case SB16:
			type = SoundCardType.SOUND_BLASTER;
			break;
		}

		return type;
	}

	/**
	 * Converts a Libvirt network device model to a VM metadata ethernet device type.
	 * 
	 * @param soundDeviceModel Libvirt network device model.
	 * @return VM metadata ethernet device type.
	 */
	public static EthernetDevType convertNetworkDeviceModel( Interface.Model networkDeviceModel )
	{
		EthernetDevType type = EthernetDevType.NONE;

		switch ( networkDeviceModel ) {
		case E1000:
			type = EthernetDevType.E1000;
			break;
		case E1000E:
			type = EthernetDevType.E1000E;
			break;
		case PCNET:
			type = EthernetDevType.PCNETPCI2;
			break;
		case VIRTIO:
			type = EthernetDevType.PARAVIRT;
			break;
		case VIRTIO_NET_PCI:
			type = EthernetDevType.PARAVIRT;
			break;
		case VIRTIO_NET_PCI_NON_TRANSITIONAL:
			type = EthernetDevType.PARAVIRT;
			break;
		case VIRTIO_NET_PCI_TRANSITIONAL:
			type = EthernetDevType.PARAVIRT;
			break;
		case VMXNET3:
			type = EthernetDevType.VMXNET3;
			break;
		default:
			type = EthernetDevType.AUTO;
			break;
		}

		return type;
	}

	/**
	 * Returns an item from a given {@link ArrayList}.
	 * 
	 * The item is selected by a given index. If the item is not available within the
	 * {@link ArrayList}, <code>null</code> is returned.
	 * 
	 * @param <T> type of the {@link ArrayList}.
	 * @param array {@link ArrayList} of type <code>T</code>.
	 * @param index selects the item from the {@link ArrayList}.
	 * @return selected item of the {@link ArrayList}.
	 */
	public static <T> T getArrayIndex( ArrayList<T> array, int index )
	{
		T ret;

		try {
			ret = array.get( index );
		} catch ( IndexOutOfBoundsException e ) {
			ret = null;
		}

		return ret;
	}

	/**
	 * Creates an alphabetical device name constructed from a device prefix and a device number.
	 * 
	 * @param devicePrefix prefix of the constructed device name.
	 * @param deviceNumber number of the device.
	 * @return alphabetical device name.
	 */
	public static String createAlphabeticalDeviceName( String devicePrefix, int deviceNumber )
	{
		if ( deviceNumber < 0 || deviceNumber >= ( 'z' - 'a' ) ) {
			String errorMsg = new String( "Device number is out of range to be able to create a valid device name." );
			throw new IllegalArgumentException( errorMsg );
		}

		return devicePrefix + ( 'a' + deviceNumber );
	}
}
