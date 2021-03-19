package org.openslx.libvirt.capabilities.cpu;

import java.util.ArrayList;
import java.util.List;

import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.w3c.dom.NodeList;

/**
 * Implementation of the host CPU capabilities as part of the Libvirt capabilities XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Cpu extends LibvirtXmlNode
{
	/**
	 * Creates an empty host CPU capabilities instance.
	 */
	public Cpu()
	{
		super();
	}

	/**
	 * Creates a host CPU capabilities instance representing an existing Libvirt XML host CPU
	 * capabilities element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU capabilities element.
	 */
	public Cpu( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the architecture name of the host CPU.
	 * 
	 * @return architecture name of the host CPU.
	 */
	public String getArch()
	{
		return this.getXmlElementValue( "arch" );
	}

	/**
	 * Returns the model name of the host CPU.
	 * 
	 * @return model name of the host CPU.
	 */
	public String getModel()
	{
		return this.getXmlElementValue( "model" );
	}

	/**
	 * Returns the vendor name of the host CPU.
	 * 
	 * @return vendor name of the host CPU.
	 */
	public String getVendor()
	{
		return this.getXmlElementValue( "vendor" );
	}

	/**
	 * Returns the number of sockets of the host CPU.
	 * 
	 * @return number of sockets of the host CPU.
	 */
	public int getTopologySockets()
	{
		final String numSockets = this.getXmlElementAttributeValue( "topology", "sockets" );
		return Integer.parseInt( numSockets );
	}

	/**
	 * Returns the number of dies of the host CPU.
	 * 
	 * @return number of dies of the host CPU.
	 */
	public int getTopologyDies()
	{
		final String numDies = this.getXmlElementAttributeValue( "topology", "dies" );
		return Integer.parseInt( numDies );
	}

	/**
	 * Returns the number of cores of the host CPU.
	 * 
	 * @return number of cores of the host CPU.
	 */
	public int getTopologyCores()
	{
		final String numCores = this.getXmlElementAttributeValue( "topology", "cores" );
		return Integer.parseInt( numCores );
	}

	/**
	 * Returns the number of threads of the host CPU.
	 * 
	 * @return number of threads of the host CPU.
	 */
	public int getTopologyThreads()
	{
		final String numThreads = this.getXmlElementAttributeValue( "topology", "threads" );
		return Integer.parseInt( numThreads );
	}

	/**
	 * Returns the supported features of the host CPU.
	 * 
	 * @return supported features of the host CPU.
	 */
	public List<Feature> getFeatures()
	{
		final List<Feature> featureList = new ArrayList<Feature>();
		final NodeList featureNodes = this.getXmlNodes( "feature" );

		for ( int i = 0; i < featureNodes.getLength(); i++ ) {
			final LibvirtXmlNode featureNode = new LibvirtXmlNode( this.getXmlDocument(), featureNodes.item( i ) );
			final Feature feature = Feature.newInstance( featureNode );

			if ( feature != null ) {
				featureList.add( feature );
			}
		}

		return featureList;
	}

	/**
	 * Returns the supported memory pages of the host CPU.
	 * 
	 * @return supported memory pages of the host CPU.
	 */
	public List<Pages> getPages()
	{
		final List<Pages> pagesList = new ArrayList<Pages>();
		final NodeList pagesNodes = this.getXmlNodes( "pages" );

		for ( int i = 0; i < pagesNodes.getLength(); i++ ) {
			final LibvirtXmlNode pagesNode = new LibvirtXmlNode( this.getXmlDocument(), pagesNodes.item( i ) );
			final Pages pages = Pages.newInstance( pagesNode );

			if ( pages != null ) {
				pagesList.add( pages );
			}
		}

		return pagesList;
	}

	/**
	 * Creates a host CPU capabilities instance representing an existing Libvirt XML host CPU
	 * capabilities element.
	 * 
	 * @param xmlNode existing Libvirt XML host CPU capabilities element.
	 * @return host CPU capabilities instance.
	 */
	public static Cpu newInstance( LibvirtXmlNode xmlNode )
	{
		return new Cpu( xmlNode );
	}
}
