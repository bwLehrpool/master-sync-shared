package org.openslx.libvirt.domain.device;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a slot address from a PCI device.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevPciDeviceAddress
{
	/**
	 * Regular expression to parse a PCI device address from a {@link String}.
	 * <p>
	 * The regular expression matches a PCI device address if its textual PCI device address is
	 * well-formed according to the following examples:
	 * 
	 * <pre>
	 *   0000:00:02.3
	 *   0000:01:00.0
	 *   0000:00:1f.3
	 * </pre>
	 */
	private static final String DEVICE_ADDRESS_REGEX = "^([a-f0-9]{4}):([a-f0-9]{2}):([a-f0-9]{2})\\.([0-7]{1})$";

	/**
	 * Minimum value for a valid PCI device address component number.
	 */
	private static final int DEVICE_ADDRESS_MIN_VALUE = 0;

	/**
	 * Maximum value for a valid PCI device domain number.
	 */
	private static final int DEVICE_ADDRESS_DOMAIN_MAX_VALUE = 0xffff;

	/**
	 * Maximum value for a valid PCI device bus number.
	 */
	private static final int DEVICE_ADDRESS_BUS_MAX_VALUE = 0xff;

	/**
	 * Maximum value for a valid PCI device device number.
	 */
	private static final int DEVICE_ADDRESS_DEVICE_MAX_VALUE = 0x1f;

	/**
	 * Maximum value for a valid PCI device function number.
	 */
	private static final int DEVICE_ADDRESS_FUNCTION_MAX_VALUE = 0x7;

	/**
	 * Domain number of the PCI device address.
	 */
	final int pciDomain;

	/**
	 * Bus number of the PCI device address.
	 */
	final int pciBus;

	/**
	 * Device number of the PCI device address.
	 */
	final int pciDevice;

	/**
	 * Function number of the PCI device address.
	 */
	final int pciFunction;

	/**
	 * Creates a new PCI device address and sets the address information to the default
	 * address {@code 0000:00:00.0}.
	 */
	public HostdevPciDeviceAddress()
	{
		this( 0, 0, 0, 0 );
	}

	/**
	 * Creates a new PCI device address consisting of a specified PCI bus, device, and function.
	 * <p>
	 * The domain of the PCI device address is set to the default number {@code 0000}.
	 * 
	 * @param pciBus number of the PCI bus.
	 * @param pciDevice number of the PCI device.
	 * @param pciFunction number of the PCI function.
	 * 
	 * @throws IllegalArgumentException failed to validate the PCI bus, device and function.
	 */
	public HostdevPciDeviceAddress( int pciBus, int pciDevice, int pciFunction ) throws IllegalArgumentException
	{
		this( 0, pciBus, pciDevice, pciFunction );
	}

	/**
	 * Creates a new PCI device address consisting of a specified PCI domain, bus, device, and
	 * function.
	 * 
	 * @param pciDomain number of the PCI domain.
	 * @param pciBus number of the PCI bus.
	 * @param pciDevice number of the PCI device.
	 * @param pciFunction number of the PCI function.
	 * 
	 * @throws IllegalArgumentException failed to validate the PCI domain, bus, device and function.
	 */
	public HostdevPciDeviceAddress( int pciDomain, int pciBus, int pciDevice, int pciFunction )
			throws IllegalArgumentException
	{
		HostdevPciDeviceAddress.validatePciDeviceAddress( pciDomain, "PCI domain",
				HostdevPciDeviceAddress.DEVICE_ADDRESS_DOMAIN_MAX_VALUE );
		HostdevPciDeviceAddress.validatePciDeviceAddress( pciBus, "PCI bus",
				HostdevPciDeviceAddress.DEVICE_ADDRESS_BUS_MAX_VALUE );
		HostdevPciDeviceAddress.validatePciDeviceAddress( pciDevice, "PCI device",
				HostdevPciDeviceAddress.DEVICE_ADDRESS_DEVICE_MAX_VALUE );
		HostdevPciDeviceAddress.validatePciDeviceAddress( pciFunction, "PCI function",
				HostdevPciDeviceAddress.DEVICE_ADDRESS_FUNCTION_MAX_VALUE );

		this.pciDomain = pciDomain;
		this.pciBus = pciBus;
		this.pciDevice = pciDevice;
		this.pciFunction = pciFunction;
	}

	/**
	 * Validates a specified PCI address component (PCI domain, bus, device or function).
	 * 
	 * @param address value of the PCI address component.
	 * @param addressName name of the PCI address component.
	 * @param upperLimit maximum value for the PCI address component
	 * 
	 * @throws IllegalArgumentException
	 */
	private static void validatePciDeviceAddress( final int address, final String addressName, final int upperLimit )
			throws IllegalArgumentException
	{
		if ( address < HostdevPciDeviceAddress.DEVICE_ADDRESS_MIN_VALUE ) {
			throw new IllegalArgumentException(
					"The " + addressName + " address must be larger or equal than "
							+ HostdevPciDeviceAddress.DEVICE_ADDRESS_MIN_VALUE );
		} else if ( address > upperLimit ) {
			throw new IllegalArgumentException(
					"The " + addressName + " address must be smaller or equal than " + upperLimit );
		}
	}

	/**
	 * Returns the PCI domain.
	 * 
	 * @return PCI domain.
	 */
	public int getPciDomain()
	{
		return this.pciDomain;
	}

	/**
	 * Returns the PCI domain as {@link String}.
	 * 
	 * @return PCI domain as {@link String}.
	 */
	public String getPciDomainAsString()
	{
		return String.format( "%04x", HostdevPciDeviceAddress.DEVICE_ADDRESS_DOMAIN_MAX_VALUE & this.getPciDomain() );
	}

	/**
	 * Returns the PCI bus.
	 * 
	 * @return PCI bus.
	 */
	public int getPciBus()
	{
		return this.pciBus;
	}

	/**
	 * Returns the PCI bus as {@link String}.
	 * 
	 * @return PCI bus as {@link String}.
	 */
	public String getPciBusAsString()
	{
		return String.format( "%02x", HostdevPciDeviceAddress.DEVICE_ADDRESS_BUS_MAX_VALUE & this.getPciBus() );
	}

	/**
	 * Returns the PCI device.
	 * 
	 * @return PCI device.
	 */
	public int getPciDevice()
	{
		return this.pciDevice;
	}

	/**
	 * Returns the PCI device as {@link String}.
	 * 
	 * @return PCI device as {@link String}.
	 */
	public String getPciDeviceAsString()
	{
		return String.format( "%02x", HostdevPciDeviceAddress.DEVICE_ADDRESS_DEVICE_MAX_VALUE & this.getPciDevice() );
	}

	/**
	 * Returns the PCI function.
	 * 
	 * @return PCI function.
	 */
	public int getPciFunction()
	{
		return this.pciFunction;
	}

	/**
	 * Returns the PCI function as {@link String}.
	 * 
	 * @return PCI function as {@link String}.
	 */
	public String getPciFunctionAsString()
	{
		return String.format( "%01x", HostdevPciDeviceAddress.DEVICE_ADDRESS_FUNCTION_MAX_VALUE & this.getPciFunction() );
	}

	/**
	 * Creates a new PCI device address parsed from a {@link String}.
	 * <p>
	 * The PCI device address consists of a PCI domain, bus, device and function parsed from the
	 * specified {@link String}.
	 * 
	 * @param pciDeviceAddress textual information containing a PCI device address as {@link String}.
	 *           The textual PCI device address should be well-formed according to the defined
	 *           regular expression {@link #DEVICE_ADDRESS_REGEX}.
	 * 
	 * @return PCI device address instance.
	 */
	public static HostdevPciDeviceAddress valueOf( String pciDeviceAddress )
	{
		HostdevPciDeviceAddress parsedPciDeviceAddress;

		if ( pciDeviceAddress == null || pciDeviceAddress.isEmpty() ) {
			parsedPciDeviceAddress = null;
		} else {
			final Pattern pciDeviceAddressPattern = Pattern.compile( HostdevPciDeviceAddress.DEVICE_ADDRESS_REGEX );
			final Matcher pciDeviceAddressMatcher = pciDeviceAddressPattern.matcher( pciDeviceAddress.toLowerCase() );

			if ( pciDeviceAddressMatcher.find() ) {
				final int pciDomain = Integer.valueOf( pciDeviceAddressMatcher.group( 1 ), 16 );
				final int pciBus = Integer.valueOf( pciDeviceAddressMatcher.group( 2 ), 16 );
				final int pciDevice = Integer.valueOf( pciDeviceAddressMatcher.group( 3 ), 16 );
				final int pciFunction = Integer.valueOf( pciDeviceAddressMatcher.group( 4 ), 16 );

				try {
					parsedPciDeviceAddress = new HostdevPciDeviceAddress( pciDomain, pciBus, pciDevice, pciFunction );
				} catch ( IllegalArgumentException e ) {
					parsedPciDeviceAddress = null;
				}
			} else {
				parsedPciDeviceAddress = null;
			}
		}

		return parsedPciDeviceAddress;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == null ) {
			return false;
		} else if ( this.getClass() != obj.getClass() ) {
			return false;
		} else {
			// check if PCI domain, bus, device and function are equal
			final HostdevPciDeviceAddress other = HostdevPciDeviceAddress.class.cast( obj );
			if ( this.getPciDomain() == other.getPciDomain() && this.getPciBus() == other.getPciBus()
					&& this.getPciDevice() == other.getPciDevice() && this.getPciFunction() == other.getPciFunction() ) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public String toString()
	{
		return this.getPciDomainAsString() + ":" + this.getPciBusAsString() + ":" + this.getPciDeviceAsString() + "."
				+ this.getPciFunctionAsString();
	}
}
