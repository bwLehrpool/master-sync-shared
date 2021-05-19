package org.openslx.virtualization.disk;

/**
 * An exception for faulty disk image handling.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskImageException extends Exception
{
	/**
	 * Version number for serialization.
	 */
	private static final long serialVersionUID = 5464286488698331909L;

	/**
	 * Creates a disk image exception including an error message.
	 * 
	 * @param errorMsg message to describe a disk image error.
	 */
	public DiskImageException( String errorMsg )
	{
		super( errorMsg );
	}
}
