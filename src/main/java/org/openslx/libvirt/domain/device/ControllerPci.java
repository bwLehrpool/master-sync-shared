package org.openslx.libvirt.domain.device;

import org.openslx.libvirt.xml.LibvirtXmlNode;

/**
 * A PCI controller device node in a Libvirt domain XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ControllerPci extends Controller
{
	/**
	 * Creates an empty PCI controller device.
	 */
	public ControllerPci()
	{
		super();
	}

	/**
	 * Creates a PCI controller device representing an existing Libvirt XML PCI controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML PCI controller device element.
	 */
	public ControllerPci( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns model of the PCI controller.
	 * 
	 * @return model of the PCI controller.
	 */
	public Model getModel()
	{
		String model = this.getXmlElementAttributeValue( "model" );
		return Model.fromString( model );
	}

	/**
	 * Sets model for the PCI controller.
	 * 
	 * @param model model for the PCI controller.
	 */
	public void setModel( Model model )
	{
		this.setXmlElementAttributeValue( "model", model.toString() );
	}

	/**
	 * Returns emulated hardware model of the PCI controller.
	 * 
	 * @return emulated hardware model of the PCI controller.
	 */
	public String getModelEmulated()
	{
		return this.getXmlElementAttributeValue( "model", "name" );
	}

	/**
	 * Creates a non-existent PCI controller device as Libvirt XML device element.
	 * 
	 * @param xmlNode Libvirt XML node of the Libvirt XML device that is created.
	 * @return created PCI controller device instance.
	 */
	public static ControllerPci createInstance( LibvirtXmlNode xmlNode )
	{
		return ControllerPci.newInstance( xmlNode );
	}

	/**
	 * Creates a PCI controller device representing an existing Libvirt XML PCI controller device
	 * element.
	 * 
	 * @param xmlNode existing Libvirt XML PCI controller device element.
	 * @return PCI controller device instance.
	 */
	public static ControllerPci newInstance( LibvirtXmlNode xmlNode )
	{
		return new ControllerPci( xmlNode );
	}

	/**
	 * Model of PCI controller device.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	enum Model
	{
		// @formatter:off
		PCI_ROOT                   ( "pci-root" ),
		PCI_BRDIGE                 ( "pci-bridge" ),
		PCIE_ROOT                  ( "pcie-root" ),
		PCI_DMI2BRIDGE             ( "dmi-to-pci-bridge" ),
		PCIE_ROOT_PORT             ( "pcie-root-port" ),
		PCIE_SWITCH_UPSTREAM_PORT  ( "pcie-switch-upstream-port" ),
		PCIE_SWITCH_DOWNSTREAM_PORT( "pcie-switch-downstream-port" ),
		PCI_EXPANDER_BUS           ( "pci-expander-bus" ),
		PCIE_EXPANDER_BUS          ( "pcie-expander-bus" ),
		PCIE2PCI_BRIDGE            ( "pcie-to-pci-bridge" );
		// @formatter:on

		/**
		 * Name of the PCI controller device model.
		 */
		private String model = null;

		/**
		 * Creates PCI controller device model.
		 * 
		 * @param type valid name of the PCI controller device model in a Libvirt domain XML document.
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
		 * Creates PCI controller device model from its name with error check.
		 * 
		 * @param type name of the PCI controller device model in a Libvirt domain XML document.
		 * @return valid PCI controller device model.
		 */
		public static Model fromString( String model )
		{
			for ( Model t : Model.values() ) {
				if ( t.model.equalsIgnoreCase( model ) ) {
					return t;
				}
			}

			return null;
		}
	}
}
