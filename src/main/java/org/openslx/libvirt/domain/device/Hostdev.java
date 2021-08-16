package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev device node in a Libvirt domain XML document for PCI, USB, ... passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Hostdev extends Device
{
	/**
	 * Creates an empty hostdev device.
	 */
	public Hostdev()
	{
		super();
	}

	/**
	 * Creates a hostdev device representing an existing Libvirt XML hostdev device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev device element.
	 */
	public Hostdev( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Checks if hostdev device is managed.
	 * 
	 * If {@link #isManaged()} returns <code>true</code> the hostdev device is detached from the
	 * host before being passed on to the guest and reattached to the host after the guest exits.
	 * 
	 * @return state whether hostdev device is managed.
	 */
	public boolean isManaged()
	{
		return this.getXmlElementAttributeValueAsBool( "managed" );
	}

	/**
	 * Sets state whether hostdev device is managed.
	 * 
	 * If the <code>managed</code> parameter is set to <code>true</code> the hostdev device is
	 * detached from the host before being passed on to the guest and reattached to the host after
	 * the guest exits.
	 * 
	 * @param managed state whether hostdev device is managed or not.
	 */
	public void setManaged( boolean managed )
	{
		this.setXmlElementAttributeValueYesNo( "managed", managed );
	}

	/**
	 * Removes boot oder entry of the hostdev device.
	 */
	public void removeBootOrder()
	{
		this.removeXmlElement( "boot" );
	}

	/**
	 * Creates a non-existent hostdev device as Libvirt XML device element.
	 * 
	 * @param hostdev hostdev device that is created.
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created hostdev device instance.
	 */
	public static Hostdev createInstance( Hostdev hostdev, LibvirtXmlNode xmlNode )
	{
		Hostdev addedHostdev = null;

		xmlNode.setXmlElementAttributeValue( "mode", "subsystem" );

		if ( hostdev instanceof HostdevMdev ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.MDEV.toString() );
			addedHostdev = HostdevMdev.createInstance( xmlNode );
		} else if ( hostdev instanceof HostdevPci ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.PCI.toString() );
			addedHostdev = HostdevPci.createInstance( xmlNode );
		} else if ( hostdev instanceof HostdevUsb ) {
			xmlNode.setXmlElementAttributeValue( "type", Type.USB.toString() );
			addedHostdev = HostdevUsb.createInstance( xmlNode );
		}

		return addedHostdev;
	}

	/**
	 * Creates a hostdev device representing an existing Libvirt XML hostdev device element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev device element.
	 * @return hostdev device instance.
	 */
	public static Hostdev newInstance( LibvirtXmlNode xmlNode )
	{
		Hostdev deviceHostdev = null;
		Type type = Type.fromString( xmlNode.getXmlElementAttributeValue( "type" ) );

		if ( type == null ) {
			return null;
		}

		switch ( type ) {
		case MDEV:
			deviceHostdev = HostdevMdev.newInstance( xmlNode );
			break;
		case PCI:
			deviceHostdev = HostdevPci.newInstance( xmlNode );
			break;
		case USB:
			deviceHostdev = HostdevUsb.newInstance( xmlNode );
			break;
		}

		return deviceHostdev;
	}

	/**
	 * Type of hostdev device subsystem passthrough.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Type
	{
		// @formatter:off
		MDEV( "mdev" ),
		PCI ( "pci" ),
		USB ( "usb" );
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
		 * Creates hostdev device type from its name with error check.
		 * 
		 * @param type name of the hostdev device storage in a Libvirt domain XML document.
		 * @return valid hostdev device type.
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
}
