package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.disk.DiskImage;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;

/**
 * Representation of the VirtualBox virtualizer for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizerVirtualBox extends Virtualizer
{
	/**
	 * Name of the VirtualBox virtualizer.
	 */
	private static final String VIRTUALIZER_NAME = "VirtualBox";

	/**
	 * List of supported image formats by the VirtualBox virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.VDI ) );

	/**
	 * List of supported version of the VirtualBox virtualizer.
	 */
	private static final List<Version> VIRTUALIZER_SUPPORTED_VERSIONS = null;

	/**
	 * Creates a new VirtualBox virtualizer.
	 */
	public VirtualizerVirtualBox()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_VIRTUALBOX,
				VirtualizerVirtualBox.VIRTUALIZER_NAME ) );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerVirtualBox.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}

	@Override
	public List<Version> getSupportedVersions()
	{
		return VirtualizerVirtualBox.VIRTUALIZER_SUPPORTED_VERSIONS;
	}
}
