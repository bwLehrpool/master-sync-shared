package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

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
	 * File name suffix for virtualization configuration files of the VirtualBox virtualizer.
	 */
	private static final String VIRTUALIZER_CONFIG_FILE_NAME_SUFFIX = "vbox";

	/**
	 * List of supported image formats by the VirtualBox virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.VDI ) );

	/**
	 * Creates a new VirtualBox virtualizer.
	 */
	public VirtualizerVirtualBox()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_VIRTUALBOX,
				VirtualizerVirtualBox.VIRTUALIZER_NAME ), VirtualizerVirtualBox.VIRTUALIZER_CONFIG_FILE_NAME_SUFFIX );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerVirtualBox.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}
}
