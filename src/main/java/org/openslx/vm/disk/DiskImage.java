package org.openslx.vm.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.function.Predicate;

import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;

/**
 * Disk image for virtual machines.
 * 
 * @implNote This class is the abstract base class to implement various specific disk images (like
 *           QCOW2 or VMDK).
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public abstract class DiskImage
{
	/**
	 * Stores the image file of the disk.
	 */
	private RandomAccessFile diskImage = null;

	/**
	 * Creates a new disk image from an existing image file with a known disk image format.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * 
	 * @throws FileNotFoundException cannot find specified disk image file.
	 * @throws IOException cannot access the content of the disk image file.
	 * 
	 * @implNote Do not use this constructor to create a new disk image from an image file with
	 *           unknown disk image format. Instead, use the factory method
	 *           {@link #newInstance(File)} to probe unknown disk image files before creation.
	 */
	public DiskImage( File diskImage ) throws FileNotFoundException, IOException
	{
		final RandomAccessFile diskFile = new RandomAccessFile( diskImage, "r" );
		this.diskImage = diskFile;
	}

	/**
	 * Creates a new disk image from an existing image file with a known disk image format.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * 
	 * @implNote Do not use this constructor to create a new disk image from an image file with
	 *           unknown disk image format. Instead, use the factory method
	 *           {@link #newInstance(File)} to probe unknown disk image files before creation.
	 */
	public DiskImage( RandomAccessFile diskImage )
	{
		this.diskImage = diskImage;
	}

	/**
	 * Returns the disk image file.
	 * 
	 * @return the disk image file.
	 */
	protected RandomAccessFile getDiskImage()
	{
		return this.diskImage;
	}

	/**
	 * Checks whether disk image is standalone and do not depend on other files (e.g. snapshot
	 * files).
	 * 
	 * @return state whether disk image is standalone or not.
	 * 
	 * @throws DiskImageException unable to check if disk image is standalone.
	 */
	public abstract boolean isStandalone() throws DiskImageException;

	/**
	 * Checks whether disk image is compressed.
	 * 
	 * @return state whether disk image is compressed or not.
	 * 
	 * @throws DiskImageException unable to check whether disk image is compressed.
	 */
	public abstract boolean isCompressed() throws DiskImageException;

	/**
	 * Checks whether disk image is a snapshot.
	 * 
	 * @return state whether disk image is a snapshot or not.
	 * 
	 * @throws DiskImageException unable to check whether disk image is a snapshot.
	 */
	public abstract boolean isSnapshot() throws DiskImageException;

	/**
	 * Returns the version of the disk image format.
	 * 
	 * @return version of the disk image format.
	 * 
	 * @throws DiskImageException unable to obtain version of the disk image format.
	 */
	public abstract int getVersion() throws DiskImageException;

	/**
	 * Returns the disk image description.
	 * 
	 * @return description of the disk image.
	 * 
	 * @throws DiskImageException unable to obtain description of the disk image.
	 */
	public abstract String getDescription() throws DiskImageException;

	/**
	 * Returns the format of the disk image.
	 * 
	 * @return format of the disk image.
	 */
	public abstract ImageFormat getFormat();

	/**
	 * Creates a new disk image from an existing image file with an unknown disk image format.
	 * 
	 * @param diskImage file to a disk storing the image content.
	 * @return concrete disk image instance.
	 * 
	 * @throws FileNotFoundException cannot find specified disk image file.
	 * @throws IOException cannot access the content of the disk image file.
	 * @throws DiskImageException disk image file has an invalid and unknown disk image format.
	 */
	public static DiskImage newInstance( File diskImage ) throws FileNotFoundException, IOException, DiskImageException
	{
		final RandomAccessFile diskFile = new RandomAccessFile( diskImage, "r" );
		final DiskImage diskImageInstance;

		if ( DiskImageQcow2.probe( diskFile ) ) {
			diskImageInstance = new DiskImageQcow2( diskFile );
		} else if ( DiskImageVdi.probe( diskFile ) ) {
			diskImageInstance = new DiskImageVdi( diskFile );
		} else if ( DiskImageVmdk.probe( diskFile ) ) {
			diskImageInstance = new DiskImageVmdk( diskFile );
		} else {
			final String errorMsg = new String( "File '" + diskImage.getAbsolutePath() + "' is not a valid disk image!" );
			throw new DiskImageException( errorMsg );
		}

		return diskImageInstance;
	}

	/**
	 * Format of a disk image.
	 * 
	 * @author Manuel Bentele
	 * @version 1.0
	 */
	public enum ImageFormat
	{
		// @formatter:off
		NONE ( "none" ),
		QCOW2( "qcow2" ),
		VDI  ( "vdi" ),
		VMDK ( "vmdk" );
		// @formatter:on

		/**
		 * Stores filename extension of the disk image format.
		 */
		public final String extension;

		/**
		 * Create new disk image format.
		 * 
		 * @param extension filename extension of the disk image format.
		 */
		ImageFormat( String extension )
		{
			this.extension = extension;
		}

		/**
		 * Returns filename extension of the disk image.
		 * 
		 * @return filename extension of the disk image.
		 */
		public String getExtension()
		{
			return this.extension;
		}

		/**
		 * Checks if the disk image format is supported by a virtualizer.
		 * 
		 * @param supportedImageTypes list of supported disk image formats of a virtualizer.
		 * @return <code>true</code> if image type is supported by the virtualizer; otherwise
		 *         <code>false</code>.
		 */
		public boolean isSupportedbyVirtualizer( List<ImageFormat> supportedImageFormats )
		{
			Predicate<ImageFormat> matchDiskFormat = supportedImageFormat -> supportedImageFormat.toString()
					.equalsIgnoreCase( this.toString() );
			return supportedImageFormats.stream().anyMatch( matchDiskFormat );
		}

		/**
		 * Returns default (preferred) disk image format for the specified virtualizer.
		 * 
		 * @param virt virtualizer for that the default disk image should be determined.
		 * @return default (preferred) disk image format.
		 */
		public static ImageFormat defaultForVirtualizer( Virtualizer virt )
		{
			if ( virt == null ) {
				return null;
			} else {
				return ImageFormat.defaultForVirtualizer( virt.virtId );
			}
		}

		/**
		 * Returns default (preferred) disk image format for the specified virtualizer.
		 * 
		 * @param virtId ID of a virtualizer for that the default disk image should be determined.
		 * @return default (preferred) disk image format.
		 */
		public static ImageFormat defaultForVirtualizer( String virtId )
		{
			ImageFormat imgFormat = null;

			if ( TConst.VIRT_DOCKER.equals( virtId ) ) {
				imgFormat = NONE;
			} else if ( TConst.VIRT_QEMU.equals( virtId ) ) {
				imgFormat = QCOW2;
			} else if ( TConst.VIRT_VIRTUALBOX.equals( virtId ) ) {
				imgFormat = VDI;
			} else if ( TConst.VIRT_VMWARE.equals( virtId ) ) {
				imgFormat = VMDK;
			}

			return imgFormat;
		}

		@Override
		public String toString()
		{
			return this.getExtension();
		}
	}
}
