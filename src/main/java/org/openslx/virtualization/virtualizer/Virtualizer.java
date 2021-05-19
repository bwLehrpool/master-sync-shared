package org.openslx.virtualization.virtualizer;

import java.util.List;

import org.openslx.virtualization.Version;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;

/**
 * Representation of a virtualization system.
 * 
 * The virtualization system can be for example a virtual machine hypervisor (like VirtualBox), a
 * container for operating systems or applications (like LXC or Docker), or a runtime environment
 * (like Wine).
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public abstract class Virtualizer
{
	/**
	 * Internal data representation for the virtualizer.
	 */
	protected final org.openslx.bwlp.thrift.iface.Virtualizer internalVirtualizer;

	/**
	 * Creates a new virtualizer.
	 * 
	 * @param internalVirtualizer internal data representation for the new virtualizer.
	 */
	public Virtualizer( org.openslx.bwlp.thrift.iface.Virtualizer internalVirtualizer )
	{
		this.internalVirtualizer = internalVirtualizer;
	}

	/**
	 * Returns the identifier of the virtualizer.
	 * 
	 * @return identifier of the virtualizer.
	 */
	public String getId()
	{
		return this.internalVirtualizer.getVirtId();
	}

	/**
	 * Returns the name of the virtualizer.
	 * 
	 * @return name of the virtualizer.
	 */
	public String getName()
	{
		return this.internalVirtualizer.getVirtName();
	}

	/**
	 * Returns a list of supported disk image formats by the virtualizer.
	 * 
	 * @return list of supported disk image formats by the virtualizer.
	 */
	public abstract List<ImageFormat> getSupportedImageFormats();

	/**
	 * Returns a list of supported versions of the virtualizer.
	 * 
	 * @return list of supported versions of the virtualizer.
	 */
	public abstract List<Version> getSupportedVersions();
}
