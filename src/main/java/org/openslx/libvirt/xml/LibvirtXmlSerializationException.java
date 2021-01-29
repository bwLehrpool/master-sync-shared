package org.openslx.libvirt.xml;

/**
 * An exception of an serialization error during Libvirt XML serialization.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibvirtXmlSerializationException extends Exception
{
	/**
	 * Version number for serialization.
	 */
	private static final long serialVersionUID = 7995955592221349949L;

	/**
	 * Creates a XML serialization exception including an error message.
	 * 
	 * @param errorMsg message to describe a specific XML serialization error.
	 */
	public LibvirtXmlSerializationException( String errorMsg )
	{
		super( errorMsg );
	}
}
