package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.disk.DiskImage;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;

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
	 * List of supported image formats by the Docker virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.NONE ) );

	/**
	 * List of supported versions of the Docker virtualizer.
	 */
	private static final List<Version> VIRTUALIZER_SUPPORTED_VERSIONS = null;

	/**
	 * Creates a new Docker virtualizer.
	 */
	public VirtualizerDocker()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_DOCKER, VirtualizerDocker.VIRTUALIZER_NAME ) );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerDocker.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}

	@Override
	public List<Version> getSupportedVersions()
	{
		return VirtualizerDocker.VIRTUALIZER_SUPPORTED_VERSIONS;
	}
}
