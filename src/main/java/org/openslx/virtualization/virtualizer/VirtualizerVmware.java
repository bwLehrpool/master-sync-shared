package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

/**
 * Representation of the VMware virtualizer for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizerVmware extends Virtualizer
{
	/**
	 * Name of the VMware virtualizer.
	 */
	private static final String VIRTUALIZER_NAME = "VMware";

	/**
	 * List of supported image formats by the VMware virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.VMDK ) );

	/**
	 * Creates a new VMware virtualizer.
	 */
	public VirtualizerVmware()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_VMWARE, VirtualizerVmware.VIRTUALIZER_NAME ) );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerVmware.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}
}
