package org.openslx.virtualization.configuration.transformation;

/**
 * An exception of a transformation error.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class TransformationException extends Exception
{
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 7293420658901349154L;

	/**
	 * Creates a transformation exception including an error message.
	 * 
	 * @param errorMsg message to describe the exception.
	 */
	public TransformationException( String errorMsg )
	{
		super( errorMsg );
	}
}
