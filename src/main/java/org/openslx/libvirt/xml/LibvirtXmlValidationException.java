package org.openslx.libvirt.xml;

/**
 * An exception of an unsuccessful Libvirt XML validation.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibvirtXmlValidationException extends Exception
{
	/**
	 * Version number for serialization.
	 */
	private static final long serialVersionUID = 2299967599483742777L;

	/**
	 * Creates a validation exception including an error message.
	 * 
	 * @param errorMsg message to describe a specific Libvirt XML error.
	 */
	public LibvirtXmlValidationException( String errorMsg )
	{
		super( errorMsg );
	}
}
