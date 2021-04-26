package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.Version;
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
	 * List of supported versions of the QEMU virtualizer.
	 */
	private static final List<Version> VIRTUALIZER_SUPPORTED_VERSIONS = Collections.unmodifiableList(
			Arrays.asList(
					new Version( Short.valueOf( "2" ), Short.valueOf( "1" ), "QEMU 2.1" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "4" ), "QEMU 2.4" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "5" ), "QEMU 2.5" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "6" ), "QEMU 2.6" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "7" ), "QEMU 2.7" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "8" ), "QEMU 2.8" ),
					new Version( Short.valueOf( "2" ), Short.valueOf( "9" ), "QEMU 2.9" ),
					new Version( Short.valueOf( "3" ), Short.valueOf( "0" ), "QEMU 3.0" ),
					new Version( Short.valueOf( "3" ), Short.valueOf( "1" ), "QEMU 3.1" ),
					new Version( Short.valueOf( "4" ), Short.valueOf( "0" ), "QEMU 4.0" ),
					new Version( Short.valueOf( "4" ), Short.valueOf( "1" ), "QEMU 4.1" ),
					new Version( Short.valueOf( "4" ), Short.valueOf( "2" ), "QEMU 4.2" ) ) );

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

	@Override
	public List<Version> getSupportedVersions()
	{
		return VirtualizerQemu.VIRTUALIZER_SUPPORTED_VERSIONS;
	}
}
