package org.openslx.virtualization.configuration.transformation;

/**
 * Represents a generic transformation that transforms (alters) a given configuration with specified
 * input arguments. The generic transformation does not depend on any external states of a
 * virtualizer.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the configuration which will be transformed.
 * @param <R> type of input arguments for the transformation.
 */
public abstract class TransformationGeneric<T, R> extends Transformation<T, R>
{
	/**
	 * Create a generic transformation.
	 * 
	 * @param name comprehensible name for the transformation.
	 */
	public TransformationGeneric( String name )
	{
		super( name );
	}
}
