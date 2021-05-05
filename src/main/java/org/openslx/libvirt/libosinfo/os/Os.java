package org.openslx.libvirt.libosinfo.os;

import org.openslx.libvirt.xml.LibvirtXmlNode;
import org.openslx.virtualization.Version;

/**
 * A operating system node in a libosinfo XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class Os extends LibvirtXmlNode
{
	/**
	 * Creates an empty operating system.
	 */
	public Os()
	{
		super();
	}

	/**
	 * Creates a operating system representing an existing libosinfo XML operating system element.
	 * 
	 * @param xmlNode existing libosinfo XML operating system element.
	 */
	public Os( LibvirtXmlNode xmlNode )
	{
		super( xmlNode );
	}

	/**
	 * Returns the identifier of the operating system.
	 * 
	 * @return identifier of the operating system.
	 */
	public String getId()
	{
		return this.getXmlElementAttributeValue( "id" );
	}

	/**
	 * Returns the name of the operating system.
	 * 
	 * @return name of the operating system.
	 */
	public String getName()
	{
		return this.getXmlElementValue( "name" );
	}

	/**
	 * Returns the version of the operating system.
	 * 
	 * @return version of the operating system.
	 */
	public Version getVersion()
	{
		final String version = this.getXmlElementValue( "version" );
		return Version.valueOf( version );
	}

	/**
	 * Returns the system family of the operating system.
	 * 
	 * @return system family of the operating system.
	 */
	public String getFamily()
	{
		return this.getXmlElementValue( "family" );
	}

	/**
	 * Returns the distribution name of the operating system.
	 * 
	 * @return distribution name of the operating system.
	 */
	public String getDistro()
	{
		return this.getXmlElementValue( "distro" );
	}

	/**
	 * Creates a operating system representing an existing libosinfo XML operating system element.
	 * 
	 * @param xmlNode existing libosinfo XML operating system element.
	 * @return libosinfo XML operating system instance.
	 */
	public static Os newInstance( LibvirtXmlNode node )
	{
		return new Os( node );
	}
}
