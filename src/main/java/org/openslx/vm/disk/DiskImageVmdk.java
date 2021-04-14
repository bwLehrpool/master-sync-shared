package org.openslx.vm.disk;

import java.io.RandomAccessFile;

import org.openslx.util.Util;
import org.openslx.vm.UnsupportedVirtualizerFormatException;
import org.openslx.vm.VmwareConfig;

/**
 * VMDK (sparse extent) disk image for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskImageVmdk extends DiskImage
{
	/**
	 * Big endian representation of the little endian magic bytes <code>KDMV</code>.
	 */
	private static final int VMDK_MAGIC = 0x4b444d56;

	/**
	 * Size of a VMDK disk image data cluster in bytes.
	 */
	private static final int VMDK_SECTOR_SIZE = 512;

	/**
	 * Default hardware version of a VMDK disk image.
	 */
	private static final int VMDK_DEFAULT_HW_VERSION = 10;

	/**
	 * Stores disk configuration if VMDK disk image contains an embedded descriptor file.
	 */
	private final VmwareConfig vmdkConfig;

	/**
	 * Creates a new VMDK disk image from an existing VMDK image file.
	 * 
	 * @param diskImage file to a VMDK disk storing the image content.
	 * 
	 * @throws DiskImageException parsing of the VMDK's embedded descriptor file failed.
	 */
	DiskImageVmdk( RandomAccessFile diskImage ) throws DiskImageException
	{
		super( diskImage );

		this.vmdkConfig = this.parseVmdkConfig();
	}

	/**
	 * Probe specified file with unknown format to be a VMDK disk image file.
	 * 
	 * @param diskImage file with unknown format that should be probed.
	 * @return state whether file is a VMDK disk image or not.
	 * 
	 * @throws DiskImageException cannot probe specified file with unknown format.
	 */
	public static boolean probe( RandomAccessFile diskImage ) throws DiskImageException
	{
		final boolean isVmdkImageFormat;

		// goto the beginning of the disk image to read the magic bytes
		final int diskImageMagic = DiskImageUtils.readInt( diskImage, 0 );

		// check if disk image's magic bytes can be found
		if ( diskImageMagic == DiskImageVmdk.VMDK_MAGIC ) {
			isVmdkImageFormat = true;
		} else {
			isVmdkImageFormat = false;
		}

		return isVmdkImageFormat;
	}

	/**
	 * Returns the creation type from the VMDK's embedded descriptor file.
	 * 
	 * @return creation type from the VMDK's embedded descriptor file.
	 */
	private String getCreationType()
	{
		final VmwareConfig vmdkConfig = this.getVmdkConfig();
		final String vmdkCreationType;

		if ( vmdkConfig == null ) {
			// VMDK disk image does not contain any descriptor file
			// assume that the file is not stand alone
			vmdkCreationType = null;
		} else {
			// VMDK disk image contains a descriptor file
			// get creation type from the content of the descriptor file
			vmdkCreationType = this.vmdkConfig.get( "createType" );
		}

		return vmdkCreationType;
	}

	/**
	 * Parse the configuration of the VMDK's embedded descriptor file.
	 * 
	 * @return parsed configuration of the VMDK's embedded descriptor file.
	 * 
	 * @throws DiskImageException parsing of the VMDK's embedded descriptor file failed.
	 */
	protected VmwareConfig parseVmdkConfig() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final VmwareConfig vmdkConfig;

		// get offset and size of descriptor file embedded into the VMDK disk image
		final long vmdkDescriptorSectorOffset = Long.reverseBytes( DiskImageUtils.readLong( diskFile, 28 ) );
		final long vmdkDescriptorSectorSize = Long.reverseBytes( DiskImageUtils.readLong( diskFile, 36 ) );

		if ( vmdkDescriptorSectorOffset > 0 ) {
			// get content of descriptor file embedded into the VMDK disk image
			final long vmdkDescriptorOffset = vmdkDescriptorSectorOffset * DiskImageVmdk.VMDK_SECTOR_SIZE;
			final long vmdkDescriptorSizeMax = vmdkDescriptorSectorSize * DiskImageVmdk.VMDK_SECTOR_SIZE;
			final String descriptorStr = DiskImageUtils.readBytesAsString( diskFile, vmdkDescriptorOffset,
					Long.valueOf( vmdkDescriptorSizeMax ).intValue() );

			// get final length of the content within the sectors to be able to trim all 'zero' characters
			final int vmdkDescriptorSize = descriptorStr.indexOf( 0 );

			// if final length of the content is invalid, throw an exception 
			if ( vmdkDescriptorSize > vmdkDescriptorSizeMax || vmdkDescriptorSize < 0 ) {
				final String errorMsg = new String( "Embedded descriptor size in VMDK disk image is invalid!" );
				throw new DiskImageException( errorMsg );
			}

			// trim all 'zero' characters at the end of the descriptor content to avoid errors during parsing
			final String configStr = descriptorStr.substring( 0, vmdkDescriptorSize );

			// create configuration instance from content of the descriptor file
			try {
				vmdkConfig = new VmwareConfig( configStr.getBytes(), vmdkDescriptorSize );
			} catch ( UnsupportedVirtualizerFormatException e ) {
				throw new DiskImageException( e.getLocalizedMessage() );
			}
		} else {
			// there is no descriptor file embedded into the VMDK disk image
			vmdkConfig = null;
		}

		return vmdkConfig;
	}

	/**
	 * Returns parsed configuration of the VMDK's embedded descriptor file.
	 * 
	 * @return parsed configuration of the VMDK's embedded descriptor file.
	 */
	protected VmwareConfig getVmdkConfig()
	{
		return this.vmdkConfig;
	}

	/**
	 * Returns the hardware version from the VMDK's embedded descriptor file.
	 * 
	 * If the VMDK's embedded descriptor file does not contain any hardware version configuration
	 * entry, the default hardware version (see {@link #VMDK_DEFAULT_HW_VERSION}) is returned.
	 * 
	 * @return hardware version from the VMDK's embedded descriptor file.
	 * 
	 * @throws DiskImageException
	 */
	public int getHwVersion() throws DiskImageException
	{
		final VmwareConfig vmdkConfig = this.getVmdkConfig();
		final int hwVersion;

		if ( vmdkConfig != null ) {
			// VMDK image contains a hardware version, so return parsed hardware version
			// if hardware version cannot be parsed, return default hardware version 
			final String hwVersionStr = vmdkConfig.get( "ddb.virtualHWVersion" );

			final int hwVersionMajor = Util.parseInt( hwVersionStr, DiskImageVmdk.VMDK_DEFAULT_HW_VERSION );
			hwVersion = DiskImageUtils.versionFromMajor( Integer.valueOf( hwVersionMajor ).shortValue() );
		} else {
			// VMDK image does not contain any hardware version, so return default hardware version
			final int hwVersionMajor = DiskImageVmdk.VMDK_DEFAULT_HW_VERSION;
			hwVersion = DiskImageUtils.versionFromMajor( Integer.valueOf( hwVersionMajor ).shortValue() );
		}

		return hwVersion;
	}

	@Override
	public boolean isStandalone() throws DiskImageException
	{
		final String vmdkCreationType = this.getCreationType();
		final boolean vmdkStandalone;

		if ( vmdkCreationType != null ) {
			// creation type is defined, so check if VMDK disk image is a snapshot
			if ( this.isSnapshot() ) {
				// VMDK disk image is a snapshot and not stand alone
				vmdkStandalone = false;
			} else {
				// VMDK disk image is not a snapshot
				// determine stand alone disk image property
				vmdkStandalone = vmdkCreationType.equalsIgnoreCase( "streamOptimized" ) ||
						vmdkCreationType.equalsIgnoreCase( "monolithicSparse" );
			}
		} else {
			// creation type is not defined
			// assume that the file is not stand alone
			vmdkStandalone = false;
		}

		return vmdkStandalone;
	}

	@Override
	public boolean isCompressed() throws DiskImageException
	{
		final String vmdkCreationType = this.getCreationType();
		final boolean vmdkCompressed;

		if ( vmdkCreationType != null && vmdkCreationType.equalsIgnoreCase( "streamOptimized" ) ) {
			// creation type is defined, and VMDK disk image is compressed
			vmdkCompressed = true;
		} else {
			// creation type for compression is not defined
			// assume that the file is not compressed
			vmdkCompressed = false;
		}

		return vmdkCompressed;
	}

	@Override
	public boolean isSnapshot() throws DiskImageException
	{
		final VmwareConfig vmdkConfig = this.getVmdkConfig();
		final boolean vmdkSnapshot;

		if ( vmdkConfig == null ) {
			// VMDK disk image does not contain any descriptor file
			// assume that the file is not a snapshot
			vmdkSnapshot = false;
		} else {
			// get parent CID to determine snapshot disk image property
			final String parentCid = vmdkConfig.get( "parentCID" );

			if ( parentCid != null && !parentCid.equalsIgnoreCase( "ffffffff" ) ) {
				// link to parent content identifier is defined, so VMDK disk image is a snapshot
				vmdkSnapshot = true;
			} else {
				// link to parent content identifier is not defined, so VMDK disk image is not a snapshot
				vmdkSnapshot = false;
			}
		}

		return vmdkSnapshot;
	}

	@Override
	public int getVersion() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final int vmdkVersion = Integer.reverseBytes( DiskImageUtils.readInt( diskFile, 4 ) );

		return DiskImageUtils.versionFromMajor( Integer.valueOf( vmdkVersion ).shortValue() );
	}

	@Override
	public String getDescription() throws DiskImageException
	{
		return null;
	}

	@Override
	public ImageFormat getFormat()
	{
		return ImageFormat.VMDK;
	}
}
