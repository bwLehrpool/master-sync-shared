package org.openslx.virtualization.disk;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.openslx.virtualization.Version;

/**
 * VDI disk image for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskImageVdi extends DiskImage
{
	/**
	 * Big endian representation of the little endian VDI magic bytes (signature).
	 */
	private static final int VDI_MAGIC = 0x7f10dabe;
	/**
	 * Just 16 null-bytes
	 */
	private static final byte[] ZERO_UUID = new byte[16];

	/**
	 * Creates a new VDI disk image from an existing VDI image file.
	 * 
	 * @param diskImage file to a VDI disk storing the image content.
	 */
	DiskImageVdi( RandomAccessFile diskImage )
	{
		super( diskImage );
	}

	/**
	 * Probe specified file with unknown format to be a VDI disk image file.
	 * 
	 * @param diskImage file with unknown format that should be probed.
	 * @return state whether file is a VDI disk image or not.
	 * 
	 * @throws DiskImageException cannot probe specified file with unknown format.
	 */
	public static boolean probe( RandomAccessFile diskImage ) throws DiskImageException
	{
		final boolean isVdiImageFormat;

		// goto the beginning of the disk image to read the magic bytes
		// skip first 64 bytes (opening tag)
		final int diskImageMagic = DiskImageUtils.readInt( diskImage, 64 );

		// check if disk image's magic bytes can be found
		if ( diskImageMagic == DiskImageVdi.VDI_MAGIC ) {
			isVdiImageFormat = true;
		} else {
			isVdiImageFormat = false;
		}

		return isVdiImageFormat;
	}

	@Override
	public boolean isStandalone() throws DiskImageException
	{
		// VDI does not seem to support split VDI files, so VDI files are always standalone
		return true;
	}

	@Override
	public boolean isCompressed() throws DiskImageException
	{
		// compression is done by sparsifying the disk files, there is no flag for it
		return false;
	}

	@Override
	public boolean isSnapshot() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();

		// if parent UUID is set, the VDI file is a snapshot
		byte[] parentUuid = DiskImageUtils.readBytesAsArray( diskFile, 440, 16 );

		return !Arrays.equals( ZERO_UUID, parentUuid );
	}

	@Override
	public Version getVersion() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();

		final short vdiVersionMajor = Short.reverseBytes( DiskImageUtils.readShort( diskFile, 68 ) );
		final short vdiVersionMinor = Short.reverseBytes( DiskImageUtils.readShort( diskFile, 70 ) );

		return new Version( vdiVersionMajor, vdiVersionMinor );
	}

	@Override
	public String getDescription() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		byte[] data = DiskImageUtils.readBytesAsArray( diskFile, 84, 256 );
		// This will replace invalid chars. Maybe use CharsetDecoder and fall back to latin1 on error
		return new String( data, StandardCharsets.UTF_8 );
	}

	@Override
	public ImageFormat getFormat()
	{
		return ImageFormat.VDI;
	}
}
