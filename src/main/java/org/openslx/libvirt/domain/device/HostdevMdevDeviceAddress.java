package org.openslx.libvirt.domain.device;

import java.util.UUID;

/**
 * Representation of an address from a mediated device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevMdevDeviceAddress
{
	/**
	 * Address of the hostdev mediated device.
	 */
	private final UUID deviceAddress;

	/**
	 * Creates a new mediated device address and sets the address information to the default address
	 * {@code 00000000-0000-0000-0000-000000000000}.
	 */
	public HostdevMdevDeviceAddress()
	{
		this( new UUID( 0, 0 ) );
	}

	/**
	 * Creates a new mediated device address consisting of a specified mediated device address.
	 * 
	 * @param mdevDeviceAddress mediated device address.
	 */
	public HostdevMdevDeviceAddress( UUID mdevDeviceAddress )
	{
		this.deviceAddress = mdevDeviceAddress;
	}

	/**
	 * Returns the address of the mediated device.
	 * 
	 * @return address of the mediated device.
	 */
	public UUID getDeviceAddress()
	{
		return this.deviceAddress;
	}

	/**
	 * Returns the address of the mediated device as {@code String}.
	 * 
	 * @return address of the mediated device as {@code String}.
	 */
	public String getDeviceAddressAsString()
	{
		return this.getDeviceAddress().toString();
	}

	/**
	 * Creates a new mediated device address parsed from a {@link String}.
	 * 
	 * @param mdevDeviceAddress textual information containing a mediated device address as
	 *           {@link String}. The textual mediated device address should be well-formed according
	 *           to the string representation as described in {@link UUID#toString}.
	 * 
	 * @return mediated device address instance.
	 */
	public static HostdevMdevDeviceAddress valueOf( String mdevDeviceAddress )
	{
		HostdevMdevDeviceAddress parsedMdevDeviceAddress = null;

		try {
			final UUID deviceAddress = UUID.fromString( mdevDeviceAddress );
			parsedMdevDeviceAddress = new HostdevMdevDeviceAddress( deviceAddress );
		} catch ( IllegalArgumentException e ) {
			parsedMdevDeviceAddress = null;
		}

		return parsedMdevDeviceAddress;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else {
			// check if MDEV device addresses are equal
			final HostdevMdevDeviceAddress other = HostdevMdevDeviceAddress.class.cast( obj );
			return other.getDeviceAddress().equals( this.getDeviceAddress() );
		}
	}

	@Override
	public String toString()
	{
		return this.getDeviceAddressAsString();
	}
}
