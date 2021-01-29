package org.openslx.libvirt.xml;

/**
 * An exception of a Libvirt XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class LibvirtXmlDocumentException extends Exception
{
	/**
	 * Version number for serialization.
	 */
	private static final long serialVersionUID = -7423926322035713576L;

	/**
	 * Creates an document exception including an error message.
	 * 
	 * @param errorMsg message to describe a specific document error.
	 */
	public LibvirtXmlDocumentException( String errorMsg )
	{
		super( errorMsg );
	}
}
