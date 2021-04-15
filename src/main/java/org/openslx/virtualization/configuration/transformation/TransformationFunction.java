package org.openslx.virtualization.configuration.transformation;

/**
 * Represents a transformation operation that transforms (alters) a given configuration with
 * specified input arguments and returns no result.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the configuration which will be transformed.
 * @param <R> type of input arguments for the transformation.
 */
@FunctionalInterface
public interface TransformationFunction<T, R>
{
	/**
	 * Transforms a given configuration with the specified input arguments.
	 * 
	 * @param config configuration which will be transformed.
	 * @param args input arguments for the transformation.
	 * 
	 * @throws TransformationException transformation of the configuration failed.
	 */
	public void transform( T config, R args ) throws TransformationException;

	/**
	 * Applies the transformation function {@link #transform(Object, Object)} to the given
	 * configuration and specified input arguments.
	 * 
	 * @param config configuration which will be transformed.
	 * @param args input arguments for the transformation.
	 * 
	 * @throws TransformationException transformation of the configuration failed.
	 */
	public default void apply( T config, R args ) throws TransformationException
	{
		this.transform( config, args );
	}
}
