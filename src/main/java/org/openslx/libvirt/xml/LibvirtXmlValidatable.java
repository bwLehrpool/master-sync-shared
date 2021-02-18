package org.openslx.libvirt.xml;

/**
 * Validatability of Libvirt XML document.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public abstract interface LibvirtXmlValidatable
{
	/**
	 * Validates the XML document's content and report error if document is not a valid Libvirt XML
	 * document.
	 * 
	 * @throws LibvirtXmlValidationException XML content is not a valid Libvirt XML.
	 */
	public void validateXml() throws LibvirtXmlValidationException;
}
