package org.openslx.vm.disk;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Utilities to parse disk image format elements and control versions of disk images.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskImageUtils
{
	/**
	 * Returns the size of a specified disk image file.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @return size of the disk image file in bytes.
	 * 
	 * @throws DiskImageException unable to obtain the size of the disk image file.
	 */
	public static long getImageSize( RandomAccessFile diskImage ) throws DiskImageException
	{
		final long imageSize;

		try {
			imageSize = diskImage.length();
		} catch ( IOException e ) {
			throw new DiskImageException( e.getLocalizedMessage() );
		}

		return imageSize;
	}

	/**
	 * Reads two bytes ({@link Short}) at a given <code>offset</code> from the specified disk image
	 * file.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @param offset offset in bytes for reading the two bytes.
	 * @return value of the two bytes from the disk image file as {@link Short}.
	 * 
	 * @throws DiskImageException unable to read two bytes from the disk image file.
	 */
	public static short readShort( RandomAccessFile diskImage, long offset ) throws DiskImageException
	{
		final long imageSize = DiskImageUtils.getImageSize( diskImage );
		short value = 0;

		if ( imageSize >= ( offset + Short.BYTES ) ) {
			try {
				diskImage.seek( offset );
				value = diskImage.readShort();
			} catch ( IOException e ) {
				throw new DiskImageException( e.getLocalizedMessage() );
			}
		}

		return value;
	}

	/**
	 * Reads four bytes ({@link Integer}) at a given <code>offset</code> from the specified disk
	 * image file.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @param offset offset in bytes for reading the four bytes.
	 * @return value of the four bytes from the disk image file as {@link Integer}.
	 * 
	 * @throws DiskImageException unable to read four bytes from the disk image file.
	 */
	public static int readInt( RandomAccessFile diskImage, long offset ) throws DiskImageException
	{
		final long imageSize = DiskImageUtils.getImageSize( diskImage );
		int value = 0;

		if ( imageSize >= ( offset + Integer.BYTES ) ) {
			try {
				diskImage.seek( offset );
				value = diskImage.readInt();
			} catch ( IOException e ) {
				throw new DiskImageException( e.getLocalizedMessage() );
			}
		}

		return value;
	}

	/**
	 * Reads eight bytes ({@link Long}) at a given <code>offset</code> from the specified disk image
	 * file.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @param offset offset in bytes for reading the eight bytes.
	 * @return value of the eight bytes from the disk image file as {@link Long}.
	 * 
	 * @throws DiskImageException unable to read eight bytes from the disk image file.
	 */
	public static long readLong( RandomAccessFile diskImage, long offset ) throws DiskImageException
	{
		final long imageSize = DiskImageUtils.getImageSize( diskImage );
		long value = 0;

		if ( imageSize >= ( offset + Long.BYTES ) ) {
			try {
				diskImage.seek( offset );
				value = diskImage.readLong();
			} catch ( IOException e ) {
				throw new DiskImageException( e.getLocalizedMessage() );
			}
		}

		return value;
	}

	/**
	 * Reads a variable number of bytes (<code>numBytes</code>) at a given <code>offset</code> from the specified disk image file.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @param offset offset in bytes for reading <code>numBytes</code> bytes.
	 * @param numBytes number of bytes to read at <code>offset</code>.
	 * @return read bytes from the disk image file as {@link String}.
	 * 
	 * @throws DiskImageException unable to read two bytes from the disk image file.
	 */
	public static String readBytesAsString( RandomAccessFile diskImage, long offset, int numBytes )
			throws DiskImageException
	{
		final long imageSize = DiskImageUtils.getImageSize( diskImage );
		byte values[] = {};

		if ( imageSize >= ( offset + numBytes ) ) {
			try {
				diskImage.seek( offset );
				values = new byte[ numBytes ];
				diskImage.readFully( values );
			} catch ( IOException e ) {
				throw new DiskImageException( e.getLocalizedMessage() );
			}
		}

		return new String( values );
	}
}
