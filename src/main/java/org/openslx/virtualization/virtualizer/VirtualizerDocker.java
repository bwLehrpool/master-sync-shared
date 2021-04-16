package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

/**
 * Representation of the Docker virtualizer for application containers.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizerDocker extends Virtualizer
{
	/**
	 * Name of the Docker virtualizer.
	 */
	private static final String VIRTUALIZER_NAME = "Docker";

	/**
	 * File name suffix for virtualization configuration files of the Docker virtualizer.
	 */
	private static final String VIRTUALIZER_CONFIG_FILE_NAME_SUFFIX = null;

	/**
	 * List of supported image formats by the Docker virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.NONE ) );

	/**
	 * Creates a new Docker virtualizer.
	 */
	public VirtualizerDocker()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_DOCKER, VirtualizerDocker.VIRTUALIZER_NAME ),
				VirtualizerDocker.VIRTUALIZER_CONFIG_FILE_NAME_SUFFIX );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerDocker.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}
}
