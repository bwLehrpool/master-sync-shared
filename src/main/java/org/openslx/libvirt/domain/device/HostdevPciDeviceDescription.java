package org.openslx.libvirt.domain.device;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a PCI device description.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevPciDeviceDescription extends Hostdev
{
	/**
	 * Regular expression to parse a PCI vendor and device identifier from a {@link String}.
	 * <p>
	 * The regular expression matches a PCI device description if its textual PCI device description
	 * is well-formed according to the following examples:
	 * 
	 * <pre>
	 *   8086:a170
	 *   10ec:8168
	 *   8086:15b7
	 * </pre>
	 */
	private static final String DEVICE_DESCRIPTION_REGEX = "^([a-f0-9]{4}):([a-f0-9]{4})$";

	/**
	 * Minimum value of a valid identifier from a PCI device description.
	 */
	private static final int DEVICE_DESCRIPTION_ID_MIN_VALUE = 0x0000;

	/**
	 * Maximum value of a valid identifier from a PCI device description.
	 */
	private static final int DEVICE_DESCRIPTION_ID_MAX_VALUE = 0xffff;

	/**
	 * Vendor identifier of the PCI device.
	 */
	final int vendorId;

	/**
	 * Device identifier of the PCI device.
	 */
	final int deviceId;

	/**
	 * Creates a new PCI device description consisting of a PCI vendor and device identifier.
	 * 
	 * @param vendorId vendor identifier of the PCI device.
	 * @param deviceId device identifier of the PCI device.
	 * 
	 * @throws throws IllegalArgumentException failed to validate the PCI device description
	 *            identifiers.
	 */
	public HostdevPciDeviceDescription( int vendorId, int deviceId ) throws IllegalArgumentException
	{
		HostdevPciDeviceDescription.validatePciDeviceDescriptionId( "PCI vendor ID", vendorId );
		HostdevPciDeviceDescription.validatePciDeviceDescriptionId( "PCI device ID", deviceId );

		this.vendorId = vendorId;
		this.deviceId = deviceId;
	}

	/**
	 * Validates a PCI device description ID (a PCI vendor or device ID).
	 * 
	 * @param idName name of the PCI device description identifier.
	 * @param id value of the PCI device description identifier that should be validated.
	 * 
	 * @throws IllegalArgumentException failed to validate the PCI device description identifier.
	 */
	private static void validatePciDeviceDescriptionId( final String idName, final int id )
			throws IllegalArgumentException
	{
		if ( id < HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MIN_VALUE ) {
			throw new IllegalArgumentException(
					"The " + idName + "must be larger or equal than "
							+ HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MIN_VALUE );
		} else if ( id > HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE ) {
			throw new IllegalArgumentException(
					"The " + idName + "must be smaller or equal than "
							+ HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE );
		}
	}

	/**
	 * Returns the PCI vendor identifier.
	 * 
	 * @return PCI vendor identifier.
	 */
	public int getVendorId()
	{
		return this.vendorId;
	}

	/**
	 * Returns the PCI vendor identifier as {@link String}.
	 * 
	 * @return PCI vendor identifier as {@link String}.
	 */
	public String getVendorIdAsString()
	{
		return String.format( "%04x", HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE & this.getVendorId() );
	}

	/**
	 * Returns the PCI device identifier.
	 * 
	 * @return PCI device identifier.
	 */
	public int getDeviceId()
	{
		return this.deviceId;
	}

	/**
	 * Returns the PCI device identifier as {@link String}.
	 * 
	 * @return PCI device identifier as {@link String}.
	 */
	public String getDeviceIdAsString()
	{
		return String.format( "%04x", HostdevPciDeviceDescription.DEVICE_DESCRIPTION_ID_MAX_VALUE & this.getDeviceId() );
	}

	/**
	 * Creates a new PCI device description parsed from a {@link String}.
	 * <p>
	 * The PCI device description consists of a PCI vendor and device identifier parsed from the
	 * specified {@link String}.
	 * 
	 * @param vendorDeviceId textual information containing a PCI device description as
	 *           {@link String}. The textual PCI device description should be well-formed according
	 *           to the defined regular expression {@link #DEVICE_DESCRIPTION_REGEX}.
	 * 
	 * @return PCI device description instance.
	 */
	public static HostdevPciDeviceDescription valueOf( String vendorDeviceId )
	{
		final HostdevPciDeviceDescription pciDeviceDescription;

		if ( vendorDeviceId == null || vendorDeviceId.isEmpty() ) {
			pciDeviceDescription = null;
		} else {
			final Pattern pciDeviceDescPattern = Pattern.compile( HostdevPciDeviceDescription.DEVICE_DESCRIPTION_REGEX );
			final Matcher pciDeviceDescMatcher = pciDeviceDescPattern.matcher( vendorDeviceId.toLowerCase() );

			if ( pciDeviceDescMatcher.find() ) {
				final int vendorId = Integer.valueOf( pciDeviceDescMatcher.group( 1 ), 16 );
				final int deviceId = Integer.valueOf( pciDeviceDescMatcher.group( 2 ), 16 );

				pciDeviceDescription = new HostdevPciDeviceDescription( vendorId, deviceId );
			} else {
				pciDeviceDescription = null;
			}
		}

		return pciDeviceDescription;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else {
			// check if vendor and device ID are equal
			final HostdevPciDeviceDescription other = HostdevPciDeviceDescription.class.cast( obj );
			if ( this.getVendorId() == other.getVendorId() && this.getDeviceId() == other.getDeviceId() ) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public String toString()
	{
		return this.getVendorIdAsString() + ":" + this.getDeviceIdAsString();
	}
}
