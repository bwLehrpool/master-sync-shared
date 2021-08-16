package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A hostdev mediated device node in a Libvirt domain XML document for mediated device passthrough.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class HostdevMdev extends Hostdev implements HostdevAddressableSource<HostdevMdevDeviceAddress>
{
	/**
	 * Creates an empty hostdev mediated device.
	 */
	public HostdevMdev()
	{
		super();
	}

	/**
	 * Creates a hostdev mediated device representing an existing Libvirt XML hostdev mediated device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev mediated device element.
	 */
	public HostdevMdev( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Checks whether the hostdev mediated device display is on or off.
	 * 
	 * @return state whether the hostdev mediated device display is on or off.
	 */
	public boolean isDisplayOn()
	{
		return this.getXmlElementAttributeValueAsBool( "display" );
	}

	/**
	 * Sets the state of the hostdev mediated device display.
	 * 
	 * @param on state whether the hostdev mediated device display is on or off.
	 */
	public void setDisplayOn( boolean on )
	{
		this.setXmlElementAttributeValueOnOff( "display", on );
	}

	/**
	 * Checks whether the hostdev mediated device memory framebuffer is on or off.
	 * 
	 * @return state whether the hostdev mediated device memory framebuffer is on or off.
	 */
	public boolean isMemoryFramebufferOn()
	{
		return this.getXmlElementAttributeValueAsBool( "ramfb" );
	}

	/**
	 * Sets the state of the hostdev mediated device memory framebuffer.
	 * 
	 * @param on state whether the hostdev mediated device memory framebuffer is on or off.
	 */
	public void setMemoryFramebufferOn( boolean on )
	{
		this.setXmlElementAttributeValueOnOff( "ramfb", on );
	}

	/**
	 * Returns the hostdev mediated device model.
	 * 
	 * @return hostdev mediated device model.
	 */
	public Model getModel()
	{
		final String model = this.getXmlElementAttributeValue( "model" );
		return Model.fromString( model );
	}

	/**
	 * Sets the hostdev mediated device model.
	 * 
	 * @param model hostdev mediated device model that is set.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", model.toString() );
	}

	@Override
	public HostdevMdevDeviceAddress getSource()
	{
		final String mdevDeviceAddress = this.getXmlElementAttributeValue( "source/address", "uuid" );
		return HostdevMdevDeviceAddress.valueOf( mdevDeviceAddress );
	}

	@Override
	public void setSource( HostdevMdevDeviceAddress source )
	{
		this.setXmlElementAttributeValue( "source/address", "uuid", source.getDeviceAddressAsString() );
	}

	/**
	 * Creates a non-existent hostdev mediated device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created hostdev mediated device instance.
	 */
	public static HostdevMdev createInstance( LibvirtXmlNode xmlNode )
	{
		return HostdevMdev.newInstance( xmlNode );
	}

	/**
	 * Creates a hostdev mediated device representing an existing Libvirt XML hostdev mediated device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML hostdev mediated device element.
	 * @return hostdev mediated device instance.
	 */
	public static HostdevMdev newInstance( LibvirtXmlNode xmlNode )
	{
		return new HostdevMdev( xmlNode );
	}

	/**
	 * Model for hostdev mediated device passthrough.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum Model
	{
		// @formatter:off
		VFIO_PCI( "vfio-pci" ),
		VFIO_CCW( "vfio-ccw" ),
		VFIO_AP ( "vfio-ap" );
		// @formatter:on

		/**
		 * Name of the hostdev mediated device model.
		 */
		private String model = null;

		/**
		 * Creates hostdev mediated device model.
		 * 
		 * @param type valid name of the hostdev mediated device model in a Libvirt domain XML
		 *           document.
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
		 * Creates hostdev mediated device model from its name with error check.
		 * 
		 * @param model name of the hostdev mediated device model in a Libvirt domain XML document.
		 * @return valid hostdev mediated device model.
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
