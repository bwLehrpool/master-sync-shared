package org.openslx.firmware;

/**
 * An exception of a firmware-related error.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class FirmwareException extends Exception
{
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = -5932122856822258867L;

	/**
	 * Creates a firmware exception including an error message.
	 * 
	 * @param errorMsg message to describe the exception.
	 */
	public FirmwareException( String errorMsg )
	{
		super( errorMsg );
	}
}
