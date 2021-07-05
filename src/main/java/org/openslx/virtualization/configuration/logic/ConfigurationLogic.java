package org.openslx.virtualization.configuration.logic;

import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.transformation.TransformationGeneric;

/**
 * Generic transformation logic for virtualization configurations.
 * <p>
 * This transformation logic represents an encapsulated transformation logic to transform
 * virtualization configurations as part of server or client implementations.
 * 
 * @author Manuel Bentele
 * @version 1.0
 * 
 * @param <T> type of configuration data used as input arguments for a transformation.
 */
public abstract class ConfigurationLogic<T> extends TransformationGeneric<VirtualizationConfiguration, T>
{
	/**
	 * Creates a new generic transformation logic for virtualization configurations.
	 * 
	 * @param name generic transformation logic name.
	 */
	public ConfigurationLogic( String name )
	{
		super( name );
	}
}
