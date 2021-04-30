package org.openslx.virtualization.configuration.transformation;

/**
 * Represents a specific transformation that transforms (alters) a given configuration with
 * specified input arguments. The specific transformation depends on external states of a
 * specified virtualizer.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the configuration which will be transformed.
 * @param <R> type of input arguments for the transformation.
 * @param <H> type of the external input source.
 */
public abstract class TransformationSpecific<T, R, H> extends Transformation<T, R>
{
	/**
	 * Reference to virtualizer to query external states.
	 */
	private final H virtualizer;

	/**
	 * Create a specific transformation.
	 * 
	 * @param name comprehensible name for the transformation.
	 * @param virtualizer initialized virtualizer.
	 */
	public TransformationSpecific( String name, H virtualizer )
	{
		super( name );

		this.virtualizer = virtualizer;
	}

	/**
	 * Returns the referenced virtualizer of the transformation.
	 * 
	 * @return referenced virtualizer of the transformation.
	 */
	public H getVirtualizer()
	{
		return this.virtualizer;
	}
}
