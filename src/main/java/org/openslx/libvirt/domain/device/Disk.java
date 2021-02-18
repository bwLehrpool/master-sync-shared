package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.w3c.dom.Node;

/**
 * A disk (floppy, CDROM, ...) device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Disk extends Device
{
	/**
	 * Creates an empty disk device.
	 */
	public Disk()
	{
		super();
	}

	/**
	 * Creates a disk device representing an existing Libvirt XML disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML disk device element.
	 */
	public Disk( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns storage type of the disk device.
	 * 
	 * @return storage type of underlying source for the disk device.
	 */
	public StorageType getStorageType()
	{
		String storageType = this.getXmlElementAttributeValue( "type" );
		return StorageType.fromString( storageType );
	}

	/**
	 * Sets storage type for the disk device.
	 * 
	 * @param storageType storage type of underlying source for the disk device.
	 * 
	 * @implNote Please call {@link #setStorageSource(String)} after calling this method, otherwise
	 *           the underlying source for the disk device may be invalid.
	 */
	protected void setStorageType( StorageType storageType )
	{
		this.setXmlElementAttributeValue( "type", storageType.toString() );
	}

	/**
	 * Returns underlying source of disk device.
	 * 
	 * @return file path to underlying source of disk device.
	 */
	public String getStorageSource()
	{
		StorageType storageType = this.getStorageType();
		String storageSource = null;

		switch ( storageType ) {
		case FILE:
			storageSource = this.getXmlElementAttributeValue( "source", "file" );
			break;
		case BLOCK:
			storageSource = this.getXmlElementAttributeValue( "source", "bdev" );
			break;
		}

		return storageSource;
	}

	/**
	 * Sets underlying source for disk device.
	 * 
	 * @param source file path to underlying source for disk device.
	 * 
	 * @implNote Please call {@link #setStorageType(StorageType)} before calling this method,
	 *           otherwise the underlying source for the disk device is not set.
	 */
	protected void setStorageSource( String source )
	{
		StorageType storageType = this.getStorageType();

		// remove all attributes from sub-element 'source'
		this.removeXmlElementAttributes( "source" );

		// rewrite specific attribute depending on the storage type
		switch ( storageType ) {
		case FILE:
			this.setXmlElementAttributeValue( "source", "file", source );
			break;
		case BLOCK:
			this.setXmlElementAttributeValue( "source", "bdev", source );
			break;
		}
	}

	/**
	 * Sets storage type and underlying source for disk device.
	 * 
	 * @param storageType storage type of underlying source for the disk device.
	 * @param source file path to underlying source for disk device.
	 */
	public void setStorage( StorageType storageType, String source )
	{
		this.setStorageType( storageType );
		this.setStorageSource( source );
	}

	/**
	 * Removes underlying source of the disk device.
	 * 
	 * @implNote Calling this method will result in an invalid Libvirt domain XML content.
	 */
	public void removeStorage()
	{
		this.removeXmlElement( "source" );
	}

	/**
	 * Removes boot oder entry of the disk device.
	 */
	public void removeBootOrder()
	{
		this.removeXmlElement( "boot" );
	}

	/**
	 * Returns read only state of disk device.
	 * 
	 * @return read only state of disk device.
	 */
	public boolean isReadOnly()
	{
		Node readOnly = this.getXmlElement( "readonly" );

		if ( readOnly == null ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Sets read only state for disk device.
	 * 
	 * @param readOnly state for disk device and its read only functionality.
	 */
	public void setReadOnly( boolean readOnly )
	{
		if ( readOnly ) {
			this.setXmlElement( "readonly" );
		} else {
			this.removeXmlElement( "readonly" );
		}
	}

	/**
	 * Returns bus type of the disk device.
	 * 
	 * @return bus type of the disk device.
	 */
	public BusType getBusType()
	{
		String busType = this.getXmlElementAttributeValue( "target", "bus" );
		return BusType.fromString( busType );
	}

	/**
	 * Sets bus type for the disk device.
	 * 
	 * @param busType bus type for the disk device.
	 */
	public void setBusType( BusType busType )
	{
		this.setXmlElementAttributeValue( "target", "bus", busType.toString() );
	}

	/**
	 * Returns target device of the disk device.
	 * 
	 * @return target device of the disk device.
	 */
	public String getTargetDevice()
	{
		return this.getXmlElementAttributeValue( "target", "dev" );
	}

	/**
	 * Sets target device for the disk device.
	 * 
	 * @param target device for the disk device.
	 */
	public void setTargetDevice( String targetDevice )
	{
		this.setXmlElementAttributeValue( "target", "dev", targetDevice );
	}

	/**
	 * Creates a non-existent disk device as Libvirt XML device element.
	 * 
	 * @param disk disk device that is created.
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created disk device instance.
	 */
	public static Disk createInstance( Disk disk, LibvirtXmlNode xmlNode )
	{
		Disk addedDisk = null;

		if ( disk instanceof DiskCdrom ) {
			xmlNode.setXmlElementAttributeValue( "device", Type.CDROM.toString() );
			addedDisk = DiskCdrom.createInstance( xmlNode );
		} else if ( disk instanceof DiskFloppy ) {
			xmlNode.setXmlElementAttributeValue( "device", Type.FLOPPY.toString() );
			addedDisk = DiskFloppy.createInstance( xmlNode );
		} else if ( disk instanceof DiskStorage ) {
			xmlNode.setXmlElementAttributeValue( "device", Type.STORAGE.toString() );
			addedDisk = DiskStorage.createInstance( xmlNode );
		}

		return addedDisk;
	}

	/**
	 * Creates a disk device representing an existing Libvirt XML disk device element.
	 * 
	 * @param xmlNode existing Libvirt XML disk device element.
	 * @return disk device instance.
	 */
	public static Disk newInstance( LibvirtXmlNode xmlNode )
	{
		Disk deviceDisk = null;
		Type type = Type.fromString( xmlNode.getXmlElementAttributeValue( "device" ) );

		if ( type == null ) {
			return null;
		}

		switch ( type ) {
		case CDROM:
			deviceDisk = DiskCdrom.newInstance( xmlNode );
			break;
		case FLOPPY:
			deviceDisk = DiskFloppy.newInstance( xmlNode );
			break;
		case STORAGE:
			deviceDisk = DiskStorage.newInstance( xmlNode );
			break;
		}

		return deviceDisk;
	}

	/**
	 * Type of disk device.
	 * 
	 * Indicates how a disk is to be exposed to the guest OS.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Type
	{
		// @formatter:off
		CDROM  ( "cdrom" ),
		FLOPPY ( "floppy" ),
		STORAGE( "disk" );
		// @formatter:on

		/**
		 * Name of the disk device type.
		 */
		private String type = null;

		/**
		 * Creates disk device type.
		 * 
		 * @param type valid name of the disk device type in a Libvirt domain XML document.
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
		 * Creates disk device type from its name with error check.
		 * 
		 * @param type name of the disk device type in a Libvirt domain XML document.
		 * @return valid disk device type.
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
	 * Storage type of a disk device.
	 * 
	 * The storage type refers to the underlying source for the disk.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum StorageType
	{
		// @formatter:off
		FILE  ( "file" ),
		BLOCK ( "block" );
		// @formatter:on

		/**
		 * Name of the disk device type.
		 */
		private String storageType = null;

		/**
		 * Creates disk device storage type.
		 * 
		 * @param storageType valid name of the disk device storage type in a Libvirt domain XML
		 *           document.
		 */
		StorageType( String storageType )
		{
			this.storageType = storageType;
		}

		@Override
		public String toString()
		{
			return this.storageType;
		}

		/**
		 * Creates disk device storage type from its name with error check.
		 * 
		 * @param storageType name of the disk device storage type in a Libvirt domain XML document.
		 * @return valid disk device storage type.
		 */
		public static StorageType fromString( String storageType )
		{
			for ( StorageType t : StorageType.values() ) {
				if ( t.storageType.equalsIgnoreCase( storageType ) ) {
					return t;
				}
			}

			return null;
		}
	}

	/**
	 * Bus type (IDE, SATA, ...) of a disk device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum BusType
	{
		// @formatter:off
		IDE   ( "ide" ),
		FDC   ( "fdc" ),
		SATA  ( "sata" ),
		SCSI  ( "scsi" ),
		SD    ( "sd" ),
		USB   ( "usb" ),
		VIRTIO( "virtio" ),
		XEN   ( "xen" );
		// @formatter:on

		/**
		 * Name of the disk device bus type.
		 */
		private String busType = null;

		/**
		 * Creates disk device bus type.
		 * 
		 * @param busType valid name of the disk device bus type in a Libvirt domain XML document.
		 */
		BusType( String busType )
		{
			this.busType = busType;
		}

		@Override
		public String toString()
		{
			return this.busType;
		}

		/**
		 * Creates disk device bus type from its name with error check.
		 * 
		 * @param busType name of the disk device bus type in a Libvirt domain XML document.
		 * @return valid disk device bus type.
		 */
		public static BusType fromString( String busType )
		{
			for ( BusType t : BusType.values() ) {
				if ( t.busType.equalsIgnoreCase( busType ) ) {
					return t;
				}
			}

			return null;
		}
	}
}
