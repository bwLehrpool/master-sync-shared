package org.openslx.libvirt.domain.device;

/**
 * Union of all known bus types, for generic querying.
 */
public enum BusType
{
	// @formatter:off
	MDEV  ( "mdev" ),
	PCI   ( "pci" ),
	SATA  ( "sata" ),
	PS2   ( "ps2" ),
	VIRTIO( "virtio" ),
	IDE   ( "ide" ),
	FDC   ( "fdc" ),
	SCSI  ( "scsi" ),
	SD    ( "sd" ),
	XEN   ( "xen" ),
	USB   ( "usb" );
	// @formatter:on

	/**
	 * Name of the hostdev device type.
	 */
	private String type = null;

	/**
	 * Creates hostdev device type.
	 * 
	 * @param type valid name of the hostdev device type in a Libvirt domain XML document.
	 */
	BusType( String type )
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return this.type;
	}

	/**
	 * Creates hostdev device type from its name with error check.
	 * 
	 * @param type name of the hostdev device storage in a Libvirt domain XML document.
	 * @return valid hostdev device type.
	 */
	public static BusType fromString( String type )
	{
		for ( BusType t : BusType.values() ) {
			if ( t.type.equalsIgnoreCase( type ) ) {
				return t;
			}
		}

		return null;
	}
}