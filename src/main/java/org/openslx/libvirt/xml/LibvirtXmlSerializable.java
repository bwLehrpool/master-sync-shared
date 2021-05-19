package org.openslx.libvirt.xml;

import java.io.File;
import java.io.InputStream;

import org.xml.sax.InputSource;

/**
 * Serializability of a Libvirt XML document from/to a XML file.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public abstract interface LibvirtXmlSerializable
{
	/**
	 * Serialize Libvirt XML document from {@link String}.
	 * 
	 * @param xml {@link String} containing XML content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	public void fromXml( String xml ) throws LibvirtXmlSerializationException;

	/**
	 * Serialize Libvirt XML document from {@link File}.
	 * 
	 * @param xml {@link File} containing XML content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	public void fromXml( File xml ) throws LibvirtXmlSerializationException;

	/**
	 * Serialize Libvirt XML document from {@link InputStream}.
	 * 
	 * @param xml {@link InputStream} providing XML content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	void fromXml( InputStream xml ) throws LibvirtXmlSerializationException;

	/**
	 * Serialize Libvirt XML document from {@link InputSource}.
	 * 
	 * @param xml {@link InputSource} providing XML content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	public void fromXml( InputSource xml ) throws LibvirtXmlSerializationException;

	/**
	 * Serialize Libvirt XML document to {@link String}.
	 * 
	 * @return XML {@link String} containing Libvirt XML document content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	public String toXml() throws LibvirtXmlSerializationException;

	/**
	 * Serialize Libvirt XML document to {@link File}.
	 * 
	 * @param xml XML {@link File} containing Libvirt XML document content.
	 * 
	 * @throws LibvirtXmlSerializationException serialization of Libvirt XML document failed.
	 */
	public void toXml( File xml ) throws LibvirtXmlSerializationException;
}
