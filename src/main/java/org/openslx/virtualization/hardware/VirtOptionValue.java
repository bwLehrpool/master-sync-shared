package org.openslx.virtualization.hardware;

public abstract class VirtOptionValue
{

	protected final String id;

	protected final String displayName;

	public VirtOptionValue( String id, String displayName )
	{
		this.id = id;
		this.displayName = displayName;
	}

	public String getId()
	{
		return this.id;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public abstract void apply();
	
	public abstract boolean isActive();

	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj instanceof VirtOptionValue ) {
			VirtOptionValue other = ( (VirtOptionValue)obj );
			return other.id == this.id || ( other.id != null && other.id.equals( this.id ) );
		}
		return false;
	}

}
