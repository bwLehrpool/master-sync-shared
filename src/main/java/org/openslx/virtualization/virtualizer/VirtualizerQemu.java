package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

/**
 * Representation of the QEMU virtualizer for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizerQemu extends Virtualizer
{
	/**
	 * Name of the QEMU virtualizer.
	 */
	private static final String VIRTUALIZER_NAME = "QEMU";

	/**
	 * List of supported image formats by the QEMU virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.QCOW2, ImageFormat.VMDK, ImageFormat.VDI ) );

	/**
	 * Creates a new QEMU virtualizer.
	 */
	public VirtualizerQemu()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_QEMU, VirtualizerQemu.VIRTUALIZER_NAME ) );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerQemu.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}
}
