package org.openslx.virtualization.configuration.transformation;

/**
 * Represents a transformation that transforms (alters) a given configuration with specified input
 * arguments.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the configuration which will be transformed.
 * @param <R> type of input arguments for the transformation.
 */
public abstract class Transformation<T, R> implements TransformationFunction<T, R>
{
	/**
	 * Name of the transformation.
	 */
	private final String name;

	/**
	 * State of the transformation.
	 */
	private boolean enabled;

	/**
	 * Creates a transformation.
	 * 
	 * @param name comprehensible name for the transformation.
	 */
	public Transformation( String name )
	{
		this.name = name;
		this.setEnabled( true );
	}

	/**
	 * Returns the name of the transformation.
	 * 
	 * @return name of the transformation.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns the state of the transformation.
	 * 
	 * @return state of the transformation.
	 */
	public boolean isEnabled()
	{
		return this.enabled;
	}

	/**
	 * Sets the state for the transformation.
	 * 
	 * @param enabled state for the transformation.
	 */
	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}
}
