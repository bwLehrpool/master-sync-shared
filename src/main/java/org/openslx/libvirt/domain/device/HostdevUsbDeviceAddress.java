package org.openslx.libvirt.domain.device;

/**
 * Representation of an address from an USB device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevUsbDeviceAddress
{
	/**
	 * Minimum value for a valid USB device address component number.
	 */
	private static final int DEVICE_ADDRESS_MIN_VALUE = 0;

	/**
	 * Maximum value for a valid USB device bus number.
	 */
	static final int DEVICE_BUS_MAX_VALUE = 127;

	/**
	 * Maximum value for a valid USB device port number.
	 */
	static final int DEVICE_PORT_MAX_VALUE = 127;

	/**
	 * USB bus of the USB device.
	 */
	final int usbBus;

	/**
	 * USB port of the USB device.
	 */
	final int usbPort;

	public HostdevUsbDeviceAddress( int usbBus, int usbPort ) throws IllegalArgumentException
	{
		HostdevUsbDeviceAddress.validateUsbDeviceAddress( usbBus, "USB bus",
				HostdevUsbDeviceAddress.DEVICE_BUS_MAX_VALUE );
		HostdevUsbDeviceAddress.validateUsbDeviceAddress( usbPort, "USB port",
				HostdevUsbDeviceAddress.DEVICE_PORT_MAX_VALUE );

		this.usbBus = usbBus;
		this.usbPort = usbPort;
	}

	/**
	 * Validates a specified USB device address component (USB bus or port).
	 * 
	 * @param address value of the USB address component.
	 * @param addressName name of the USB address component.
	 * @param upperLimit maximum value for the USB address component
	 * 
	 * @throws IllegalArgumentException
	 */
	private static void validateUsbDeviceAddress( final int address, final String addressName, final int upperLimit )
			throws IllegalArgumentException
	{
		if ( address < HostdevUsbDeviceAddress.DEVICE_ADDRESS_MIN_VALUE ) {
			throw new IllegalArgumentException(
					"The " + addressName + " must be larger or equal than "
							+ HostdevUsbDeviceAddress.DEVICE_ADDRESS_MIN_VALUE );
		} else if ( address > upperLimit ) {
			throw new IllegalArgumentException(
					"The " + addressName + " must be smaller or equal than " + upperLimit );
		}
	}

	/**
	 * Returns the USB bus number.
	 * 
	 * @return USB bus number.
	 */
	public int getUsbBus()
	{
		return this.usbBus;
	}

	/**
	 * Returns the USB port number.
	 * 
	 * @return USB port number.
	 */
	public int getUsbPort()
	{
		return this.usbPort;
	}

	/**
	 * Creates a new USB device address parsed from {@link String}s.
	 * 
	 * @param usbBus textual information containing a decimal USB device bus number as
	 *           {@link String}.
	 * @param usbPort textual information containing a decimal USB device port number as
	 *           {@link String}.
	 * @return USB device address instance.
	 */
	public static HostdevUsbDeviceAddress valueOf( String usbBus, String usbPort )
	{
		HostdevUsbDeviceAddress usbDeviceAddress;

		if ( usbBus == null || usbBus.isEmpty() || usbPort == null || usbPort.isEmpty() ) {
			usbDeviceAddress = null;
		} else {
			try {
				final int parsedUsbBus = Integer.valueOf( usbBus );
				final int parsedUsbPort = Integer.valueOf( usbPort );
				usbDeviceAddress = new HostdevUsbDeviceAddress( parsedUsbBus, parsedUsbPort );
			} catch ( IllegalArgumentException e ) {
				usbDeviceAddress = null;
			}
		}

		return usbDeviceAddress;

	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else {
			// check if USB bus and port are equal
			final HostdevUsbDeviceAddress other = HostdevUsbDeviceAddress.class.cast( obj );
			if ( this.getUsbBus() == other.getUsbBus() && this.getUsbPort() == other.getUsbPort() ) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public String toString()
	{
		return this.getUsbBus() + ":" + this.getUsbPort();
	}
}
