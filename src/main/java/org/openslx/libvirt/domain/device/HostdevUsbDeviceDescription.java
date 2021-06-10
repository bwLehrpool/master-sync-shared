package org.openslx.libvirt.domain.device;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of an USB device description.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevUsbDeviceDescription
{
	/**
	 * Regular expression to parse an USB device vendor and product identifier from a {@link String}.
	 * <p>
	 * The regular expression matches an USB device description if its textual USB device description
	 * is well-formed according to the following examples:
	 * 
	 * <pre>
	 *   1d6b:0002
	 *   0461:4d22
	 *   046d:c312
	 * </pre>
	 */
	private static final String DEVICE_DESCRIPTION_REGEX = "^([a-f0-9]{4}):([a-f0-9]{4})$";

	/**
	 * Minimum value of a valid identifier from an USB device description.
	 */
	private static final int DEVICE_DESCRIPTION_ID_MIN_VALUE = 0x0000;

	/**
	 * Maximum value of a valid identifier from an USB device description.
	 */
	private static final int DEVICE_DESCRIPTION_ID_MAX_VALUE = 0xffff;

	/**
	 * Vendor identifier of the USB device.
	 */
	final int vendorId;

	/**
	 * Product identifier of the USB device.
	 */
	final int productId;

	/**
	 * Creates a new USB device description consisting of a USB vendor and product identifier.
	 * 
	 * @param vendorId vendor identifier of the USB device.
	 * @param productId product identifier of the USB device.
	 * 
	 * @throws throws IllegalArgumentException failed to validate the USB device description
	 *            identifiers.
	 */
	public HostdevUsbDeviceDescription( int vendorId, int productId ) throws IllegalArgumentException
	{
		HostdevUsbDeviceDescription.validateUsbDeviceDescriptionId( "PCI vendor ID", vendorId );
		HostdevUsbDeviceDescription.validateUsbDeviceDescriptionId( "PCI device ID", productId );

		this.vendorId = vendorId;
		this.productId = productId;
	}

	/**
	 * Validates an USB device description ID (an USB vendor or product ID).
	 * 
	 * @param idName name of the USB device description identifier.
	 * @param id value of the USB device description identifier that should be validated.
	 * 
	 * @throws IllegalArgumentException failed to validate the USB device description identifier.
	 */
	private static void validateUsbDeviceDescriptionId( final String idName, final int id )
			throws IllegalArgumentException
	{
		if ( id < HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MIN_VALUE ) {
			throw new IllegalArgumentException(
					"The " + idName + "must be larger or equal than "
							+ HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MIN_VALUE );
		} else if ( id > HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE ) {
			throw new IllegalArgumentException(
					"The " + idName + "must be smaller or equal than "
							+ HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE );
		}
	}

	/**
	 * Returns the USB device vendor identifier.
	 * 
	 * @return USB device vendor identifier.
	 */
	public int getVendorId()
	{
		return this.vendorId;
	}

	/**
	 * Returns the USB vendor identifier as {@link String}.
	 * 
	 * @return USB vendor identifier as {@link String}.
	 */
	public String getVendorIdAsString()
	{
		return String.format( "%04x", HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE & this.getVendorId() );
	}

	/**
	 * Returns the USB device product identifier.
	 * 
	 * @return USB device product identifier.
	 */
	public int getProductId()
	{
		return this.productId;
	}

	/**
	 * Returns the USB device product identifier as {@link String}.
	 * 
	 * @return USB device product identifier as {@link String}.
	 */
	public String getProductIdAsString()
	{
		return String.format( "%04x", HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE & this.getProductId() );
	}

	/**
	 * Creates a new USB device description parsed from a {@link String}.
	 * <p>
	 * The USB device description consists of an USB vendor and product identifier parsed from the
	 * specified {@link String}.
	 * 
	 * @param vendorProductId textual information containing an USB device description as
	 *           {@link String}. The textual USB device description should be well-formed according
	 *           to the defined regular expression {@link #DEVICE_DESCRIPTION_REGEX}.
	 * 
	 * @return USB device description instance.
	 */
	public static HostdevUsbDeviceDescription valueOf( String vendorProductId )
	{
		HostdevUsbDeviceDescription usbDeviceDescription;

		if ( vendorProductId == null || vendorProductId.isEmpty() ) {
			usbDeviceDescription = null;
		} else {
			final Pattern usbDeviceDescPattern = Pattern.compile( HostdevUsbDeviceDescription.DEVICE_DESCRIPTION_REGEX );
			final Matcher usbDeviceDescMatcher = usbDeviceDescPattern.matcher( vendorProductId.toLowerCase() );

			if ( usbDeviceDescMatcher.find() ) {
				final int vendorId = Integer.valueOf( usbDeviceDescMatcher.group( 1 ), 16 );
				final int productId = Integer.valueOf( usbDeviceDescMatcher.group( 2 ), 16 );

				try {
					usbDeviceDescription = new HostdevUsbDeviceDescription( vendorId, productId );
				} catch ( IllegalArgumentException e ) {
					usbDeviceDescription = null;
				}
			} else {
				usbDeviceDescription = null;
			}
		}

		return usbDeviceDescription;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else {
			// check if vendor and product ID are equal
			final HostdevUsbDeviceDescription other = HostdevUsbDeviceDescription.class.cast( obj );
			if ( this.getVendorId() == other.getVendorId() && this.getProductId() == other.getProductId() ) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public String toString()
	{
		return this.getVendorIdAsString() + ":" + this.getProductIdAsString();
	}
}
