package org.openslx.virtualization.configuration.transformation;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * A transformation manager is a class to manage several transformations and their application.
 * 
 * Transformations can be registered at the transformation manager. The transformation manager has
 * the ability to apply all registered transformations on a given configuration and specified input
 * arguments.
 * 
 * @author Manuel Bentele
 * @version 1.0
 *
 * @param <T> type of the configuration which will be transformed by all transformations.
 * @param <R> type of input arguments for all transformations.
 */
public class TransformationManager<T, R>
{
	/**
	 * List of registered transformations.
	 */
	private ArrayList<Transformation<T, R>> transformations;

	/**
	 * Reference to the configuration that will be transformed (altered).
	 */
	private T config;

	/**
	 * Reference to the input arguments for all registered transformations.
	 */
	private R args;

	/**
	 * Logger instance to log messages.
	 */
	private static final Logger LOGGER = Logger.getLogger( TransformationManager.class );

	/**
	 * Create a transformation manager.
	 * 
	 * @param config configuration which will be transformed.
	 * @param args input arguments for all registered transformations.
	 */
	public TransformationManager( T config, R args )
	{
		this.transformations = new ArrayList<Transformation<T, R>>();
		this.config = config;
		this.args = args;
	}

	/**
	 * Registers and enables a transformation.
	 * 
	 * @param transformation existing transformation that will be registered and enabled.
	 */
	public void register( Transformation<T, R> transformation )
	{
		this.register( transformation, true );
	}

	/**
	 * Registers a transformation and sets its state.
	 * 
	 * @param transformation existing transformation that will be registered.
	 * @param enabled state for the existing transformation that will be set.
	 */
	public void register( Transformation<T, R> transformation, boolean enabled )
	{
		LOGGER.debug( "Register transformation '" + transformation.getName() + "' and "
				+ ( enabled ? "enable" : "do not enable" ) + " it" );

		transformation.setEnabled( enabled );
		this.transformations.add( transformation );
	}

	/**
	 * Registers a transformation function as a new transformation and enables the registered
	 * transformation.
	 * 
	 * @param name comprehensible name for the transformation.
	 * @param function transformation operation for the transformation.
	 */
	public void register( String name, TransformationFunction<T, R> function )
	{
		this.register( name, function, true );
	}

	/**
	 * Registers a transformation function as a new transformation and sets the state of the
	 * registered transformation.
	 * 
	 * @param name comprehensible name for the transformation.
	 * @param function transformation operation for the transformation.
	 * @param enabled state for the transformation.
	 */
	public void register( String name, TransformationFunction<T, R> function, boolean enabled )
	{
		this.register( new Transformation<T, R>( name ) {
			@Override
			public void transform( T document, R args ) throws TransformationException
			{
				function.apply( document, args );
			}
		}, enabled );
	}

	/**
	 * Applies all registered transformations, whose state is set to <code>enabled</code>, to the
	 * referenced configuration and input arguments.
	 * 
	 * @throws TransformationException transformation of the configuration failed.
	 */
	public void transform() throws TransformationException
	{
		for ( Transformation<T, R> transformation : this.transformations ) {
			LOGGER.debug( "Apply transformation '" + transformation.getName() + "'" );
			try {
				transformation.apply( this.config, this.args );
			} catch ( TransformationException e ) {
				final String errorMsg = new String(
						"Error in configuration filter '" + transformation.getName() + "': " + e.getLocalizedMessage() );
				throw new TransformationException( errorMsg );
			}
		}
	}

	/**
	 * Returns a human readable summary of all registered transformations.
	 * 
	 * @return human readable summary of all registered transformations.
	 */
	private String showTransformations()
	{
		String transformationSummary = new String();
		final int maxFilterNumCharacters = ( this.transformations.size() + 1 ) / 10;

		for ( int i = 0; i < this.transformations.size(); i++ ) {
			final Transformation<T, R> transformation = this.transformations.get( i );
			final String paddedNumber = String.format( "%-" + maxFilterNumCharacters + "s", i + 1 );
			final String transformationState = transformation.isEnabled() ? "[ active ]" : "[inactive]";
			transformationSummary += paddedNumber + ": " + transformationState + " ";
			transformationSummary += transformation.getName() + System.lineSeparator();
		}

		return transformationSummary;
	}

	@Override
	public String toString()
	{
		return this.showTransformations();
	}
}
