package org.openslx.libvirt.domain.device;

import java.math.BigInteger;

import org.openslx.libvirt.domain.DomainUtils;
import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A shared memory device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Shmem extends Device
{
	/**
	 * Creates an empty sound device.
	 */
	public Shmem()
	{
		super();
	}

	/**
	 * Creates a shared memory device representing an existing Libvirt XML shared memory device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML shared memory device element.
	 */
	public Shmem( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the model of the shared memory device.
	 * 
	 * @return model of the shared memory device.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model", "type" );
		return Model.fromString( model );
	}

	/**
	 * Sets the model for the shared memory device.
	 * 
	 * @param model model for the shared memory device.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", "type", model.toString() );
	}

	/**
	 * Returns the name of the shared memory device.
	 * 
	 * @return name of the shared memory device.
	 */
	public String getName()
	{
		return this.getXmlElementAttributeValue( "name" );
	}

	/**
	 * Sets the name for the shared memory device.
	 * 
	 * @param name name for the shared memory device.
	 */
	public void setName( String name )
	{
		this.setXmlElementAttributeValue( "name", name );
	}

	/**
	 * Returns the memory size of the shared memory device.
	 * 
	 * @return memory size of the shared memory device in bytes.
	 */
	public BigInteger getSize()
	{
		final String unit = this.getXmlElementAttributeValue( "size", "unit" );
		final String size = this.getXmlElementValue( "size" );

		return DomainUtils.decodeMemory( size, unit );
	}

	/**
	 * Sets the memory size for the shared memory device.
	 * 
	 * @param size memory size for the shared memory device in bytes.
	 */
	public void setSize( BigInteger memory )
	{
		final String unit = "M";
		final String size = DomainUtils.encodeMemory( memory, unit );

		this.setXmlElementAttributeValue( "size", "unit", unit );
		this.setXmlElementValue( "size", size );
	}

	/**
	 * Creates a non-existent shared memory device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created shared memory device instance.
	 */
	public static Shmem createInstance( LibvirtXmlNode xmlNode )
	{
		return Shmem.newInstance( xmlNode );
	}

	/**
	 * Creates a shared memory device representing an existing Libvirt XML shared memory device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML shared memory device element.
	 * @return shared memory device instance.
	 */
	public static Shmem newInstance( LibvirtXmlNode xmlNode )
	{
		return new Shmem( xmlNode );
	}

	/**
	 * Model of shared memory device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		IVSHMEM         ( "ivshmem" ),
		IVSHMEM_PLAIN   ( "ivshmem-plain" ),
		IVSHMEM_DOORBELL( "ivshmem-doorbell" );
		// @formatter:on

		/**
		 * Name of the shared memory device model.
		 */
		private String model;

		/**
		 * Creates shared memory device model.
		 * 
		 * @param type valid name of the shared memory device model in a Libvirt domain XML document.
		 */
		Model( String model )
		{
			this.model = model;
		}

		@Override
		public String toString()
		{
			return this.model;
		}

		/**
		 * Creates shared memory device model from its name with error check.
		 * 
		 * @param model name of the shared memory device model in a Libvirt domain XML document.
		 * @return valid shared memory device model.
		 */
		public static Model fromString( String model )
		{
			for ( Model m : Model.values() ) {
				if ( m.model.equalsIgnoreCase( model ) ) {
					return m;
				}
			}

			return null;
		}
	}
}
