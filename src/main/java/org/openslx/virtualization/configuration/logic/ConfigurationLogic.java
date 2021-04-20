package org.openslx.virtualization.configuration.logic;

import org.openslx.virtualization.configuration.VirtualizationConfiguration;
import org.openslx.virtualization.configuration.transformation.TransformationGeneric;

public abstract class ConfigurationLogic<T> extends TransformationGeneric<VirtualizationConfiguration<?, ?, ?, ?, ?>, T>
{
	public ConfigurationLogic( String name )
	{
		super( name );
	}
}
