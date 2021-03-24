package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A file system device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class FileSystem extends Device
{
	/**
	 * Creates an empty file system device.
	 */
	public FileSystem()
	{
		super();
	}

	/**
	 * Creates a file system device representing an existing Libvirt XML file system device element.
	 * 
	 * @param xmlNode existing Libvirt XML file system device element.
	 */
	public FileSystem( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns access mode of the file system device.
	 * 
	 * @return access mode of the file system device.
	 */
	public AccessMode getAccessMode()
	{
		final String mode = this.getXmlElementAttributeValue( "accessmode" );
		return AccessMode.fromString( mode );
	}

	/**
	 * Sets access mode for the file system device.
	 * 
	 * @param mode access mode for the file system device.
	 */
	public void setAccessMode( AccessMode mode )
	{
		this.setXmlElementAttributeValue( "accessmode", mode.toString() );
	}

	/**
	 * Returns type of the file system device.
	 * 
	 * @return type of the file system device.
	 */
	public Type getType()
	{
		final String type = this.getXmlElementAttributeValue( "type" );
		return Type.fromString( type );
	}

	/**
	 * Sets type for the file system device.
	 * 
	 * @param type type for the file system device.
	 */
	public void setType( Type type )
	{
		this.setXmlElementAttributeValue( "type", type.toString() );
	}

	/**
	 * Returns source of the file system device.
	 * 
	 * @return source of the file system device.
	 */
	public String getSource()
	{
		final Type type = this.getType();
		String source = null;

		switch ( type ) {
		case BIND:
			source = this.getXmlElementAttributeValue( "source", "dir" );
			break;
		case BLOCK:
			source = this.getXmlElementAttributeValue( "source", "dev" );
			break;
		case FILE:
			source = this.getXmlElementAttributeValue( "source", "file" );
			break;
		case MOUNT:
			source = this.getXmlElementAttributeValue( "source", "dir" );
			break;
		case RAM:
			source = this.getXmlElementAttributeValue( "source", "usage" );
			break;
		case TEMPLATE:
			source = this.getXmlElementAttributeValue( "source", "name" );
			break;
		}

		return source;
	}

	/**
	 * Sets source for the file system device.
	 * 
	 * @param source source for the file system device.
	 */
	public void setSource( String source )
	{
		Type type = this.getType();

		// remove all attributes from sub-element 'source'
		this.removeXmlElementAttributes( "source" );

		switch ( type ) {
		case BIND:
			this.setXmlElementAttributeValue( "source", "dir", source );
			break;
		case BLOCK:
			this.setXmlElementAttributeValue( "source", "dev", source );
			break;
		case FILE:
			this.setXmlElementAttributeValue( "source", "file", source );
			break;
		case MOUNT:
			this.setXmlElementAttributeValue( "source", "dir", source );
			break;
		case RAM:
			this.setXmlElementAttributeValue( "source", "usage", source );
			break;
		case TEMPLATE:
			this.setXmlElementAttributeValue( "source", "name", source );
			break;
		}
	}

	/**
	 * Returns target of the file system device.
	 * 
	 * @return target of the file system device.
	 */
	public String getTarget()
	{
		return this.getXmlElementAttributeValue( "target", "dir" );
	}

	/**
	 * Sets target for the file system device.
	 * 
	 * @param target target for the file system device.
	 */
	public void setTarget( String target )
	{
		this.setXmlElementAttributeValue( "target", "dir", target );
	}

	/**
	 * Creates a non-existent file system device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created file system device instance.
	 */
	public static FileSystem createInstance( LibvirtXmlNode xmlNode )
	{
		return FileSystem.newInstance( xmlNode );
	}

	/**
	 * Creates a file system device representing an existing Libvirt XML file system device element.
	 * 
	 * @param xmlNode existing Libvirt XML file system device element.
	 * @return file system device instance.
	 */
	public static FileSystem newInstance( LibvirtXmlNode xmlNode )
	{
		return new FileSystem( xmlNode );
	}

	/**
	 * Access mode for the file system device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum AccessMode
	{
		// @formatter:off
		PASSTHROUGH( "passthrough" ),
		MAPPED     ( "mapped" ),
		SQUASH     ( "squash" );
		// @formatter:on

		/**
		 * Name of the file system device access mode.
		 */
		private String mode;

		/**
		 * Creates file system device access mode.
		 * 
		 * @param mode valid name of the file system device access mode in a Libvirt domain XML
		 *           document.
		 */
		AccessMode( String mode )
		{
			this.mode = mode;
		}

		@Override
		public String toString()
		{
			return this.mode;
		}

		/**
		 * Creates file system device access mode from its name with error check.
		 * 
		 * @param mode name of the file system device access mode in a Libvirt domain XML document.
		 * @return valid file system device access mode.
		 */
		public static AccessMode fromString( String mode )
		{
			for ( AccessMode a : AccessMode.values() ) {
				if ( a.mode.equalsIgnoreCase( mode ) ) {
					return a;
				}
			}

			return null;
		}
	}

	/**
	 * Type of file system device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Type
	{
		// @formatter:off
		MOUNT   ( "mount" ),
		TEMPLATE( "template" ),
		FILE    ( "file" ),
		BLOCK   ( "block" ),
		RAM     ( "ram" ),
		BIND    ( "bind" );
		// @formatter:on

		/**
		 * Name of the file system device type.
		 */
		private String type;

		/**
		 * Creates file system device type.
		 * 
		 * @param type valid name of the file system device type in a Libvirt domain XML document.
		 */
		Type( String type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type;
		}

		/**
		 * Creates file system device type from its name with error check.
		 * 
		 * @param type name of the file system device type in a Libvirt domain XML document.
		 * @return valid file system device type.
		 */
		public static Type fromString( String type )
		{
			for ( Type t : Type.values() ) {
				if ( t.type.equalsIgnoreCase( type ) ) {
					return t;
				}
			}

			return null;
		}
	}
}
