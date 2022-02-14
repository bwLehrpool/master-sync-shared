package org.openslx.virtualization.disk;

import java.io.RandomAccessFile;

import org.openslx.virtualization.Version;

/**
 * QCOW2 disk image for virtual machines.
 * 
 * A QCOW2 disk image consists of a header, one L1 table and several L2 tables used for lookup data
 * clusters in the file via a two-level lookup. The QCOW2 header contains the following fields:
 * 
 * <pre>
 *   QCOW2 (version 2 and 3) header format:
 *   
 *   magic                   (4 byte)
 *   version                 (4 byte)
 *   backing_file_offset     (8 byte)
 *   backing_file_size       (4 byte)
 *   cluster_bits            (4 byte)
 *   size                    (8 byte)
 *   crypt_method            (4 byte)
 *   l1_size                 (4 byte)
 *   l1_table_offset         (8 byte)
 *   refcount_table_offset   (8 byte)
 *   refcount_table_clusters (4 byte)
 *   nb_snapshots            (4 byte)
 *   snapshots_offset        (8 byte)
 *   incompatible_features   (8 byte)  [*]
 *   compatible_features     (8 byte)  [*]
 *   autoclear_features      (8 byte)  [*]
 *   refcount_order          (8 byte)  [*]
 *   header_length           (4 byte)  [*]
 *
 *   [*] these fields are only available in the QCOW2 version 3 header format
 * </pre>
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class DiskImageQcow2 extends DiskImage
{
	/**
	 * Big endian representation of the big endian QCOW2 magic bytes <code>QFI\xFB</code>.
	 */
	private static final int QCOW2_MAGIC = 0x514649fb;

	/**
	 * Creates a new QCOW2 disk image from an existing QCOW2 image file.
	 * 
	 * @param diskImage file to a QCOW2 disk storing the image content.
	 */
	DiskImageQcow2( RandomAccessFile diskImage )
	{
		super( diskImage );
	}

	/**
	 * Probe specified file with unknown format to be a QCOW2 disk image file.
	 * 
	 * @param diskImage file with unknown format that should be probed.
	 * @return state whether file is a QCOW2 disk image or not.
	 * 
	 * @throws DiskImageException cannot probe specified file with unknown format.
	 */
	public static boolean probe( RandomAccessFile diskImage ) throws DiskImageException
	{
		final boolean isQcow2ImageFormat;

		// goto the beginning of the disk image to read the disk image
		final int diskImageMagic = DiskImageUtils.readInt( diskImage, 0 );

		// check if disk image's magic bytes can be found
		if ( diskImageMagic == DiskImageQcow2.QCOW2_MAGIC ) {
			isQcow2ImageFormat = true;
		} else {
			isQcow2ImageFormat = false;
		}

		return isQcow2ImageFormat;
	}

	@Override
	public boolean isStandalone() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final long qcowBackingFileOffset = DiskImageUtils.readLong( diskFile, 8 );
		final boolean qcowStandalone;

		// check if QCOW2 image does not refer to any backing file
		if ( qcowBackingFileOffset == 0 ) {
			qcowStandalone = true;
		} else {
			qcowStandalone = false;
		}

		return qcowStandalone;
	}

	@Override
	public boolean isCompressed() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final boolean qcowUseExtendedL2;
		boolean qcowCompressed = false;

		// check if QCOW2 image uses extended L2 tables
		// extended L2 tables are only possible in QCOW2 version 3 header format
		if ( this.getVersion().getMajor() >= Short.valueOf( "3" ) ) {
			// read incompatible feature bits
			final long qcowIncompatibleFeatures = DiskImageUtils.readLong( diskFile, 72 );

			// support for extended L2 tables is enabled if bit 4 is set
			qcowUseExtendedL2 = ( ( ( qcowIncompatibleFeatures & 0x000000000010 ) >>> 4 ) == 1 );
		} else {
			qcowUseExtendedL2 = false;
		}

		// get number of entries in L1 table
		final int qcowL1TableSize = DiskImageUtils.readInt( diskFile, 36 );

		// check if a valid L1 table is present
		if ( qcowL1TableSize > 0 ) {
			// QCOW2 image contains active L1 table with more than 0 entries: l1_size > 0
			// search for first L2 table and its first entry to get compression bit

			// get cluster bits to calculate the cluster size
			final int qcowClusterBits = DiskImageUtils.readInt( diskFile, 20 );
			final int qcowClusterSize = ( 1 << qcowClusterBits );

			// entries of a L1 table have always the size of 8 byte (64 bit)
			final int qcowL1TableEntrySize = 8;

			// entries of a L2 table have either the size of 8 or 16 byte (64 or 128 bit)
			final int qcowL2TableEntrySize = ( qcowUseExtendedL2 ) ? 16 : 8;

			// calculate number of L2 table entries
			final int qcowL2TableSize = qcowClusterSize / qcowL2TableEntrySize;

			// get offset of L1 table
			final long qcowL1TableOffset = DiskImageUtils.readLong( diskFile, 40 );

			// check for each L2 table referenced from an L1 table its entries
			// until a compressed cluster descriptor is found
			for ( long i = 0; i < qcowL1TableSize; i++ ) {
				// get offset of current L2 table from the current L1 table entry
				final long qcowL1TableEntryOffset = qcowL1TableOffset + ( i * qcowL1TableEntrySize );
				final long qcowL1TableEntry = DiskImageUtils.readLong( diskFile, qcowL1TableEntryOffset );

				// extract offset (bits 9 - 55) from L1 table entry
				final long qcowL2TableOffset = ( qcowL1TableEntry & 0x00fffffffffffe00L );

				if ( qcowL2TableOffset == 0 ) {
					// L2 table and all clusters described by this L2 table are unallocated
					continue;
				}

				// get each L2 table entry and check if it is a compressed cluster descriptor
				for ( long j = 0; j < qcowL2TableSize; j++ ) {
					// get current L2 table entry
					final long qcowL2TableEntryOffset = qcowL2TableOffset + ( j * qcowL2TableEntrySize );
					final long qcowL2TableEntry = DiskImageUtils.readLong( diskFile, qcowL2TableEntryOffset );

					// extract cluster type (standard or compressed) (bit 62)
					boolean qcowClusterCompressed = ( ( ( qcowL2TableEntry & 0x4000000000000000L ) >>> 62 ) == 1 );

					// check if QCOW2 disk image contains at least one compressed cluster descriptor
					if ( qcowClusterCompressed ) {
						qcowCompressed = true;
						break;
					}
				}

				// terminate if one compressed cluster descriptor is already found
				if ( qcowCompressed ) {
					break;
				}
			}
		} else {
			// QCOW2 image does not contain an active L1 table with any entry: l1_size = 0
			qcowCompressed = false;
		}

		return qcowCompressed;
	}

	@Override
	public boolean isSnapshot() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final int qcowNumSnapshots = DiskImageUtils.readInt( diskFile, 56 );
		final boolean qcowSnapshot;

		// check if QCOW2 image contains at least one snapshot
		if ( qcowNumSnapshots == 0 ) {
			qcowSnapshot = true;
		} else {
			qcowSnapshot = false;
		}

		return qcowSnapshot;
	}

	@Override
	public Version getVersion() throws DiskImageException
	{
		final RandomAccessFile diskFile = this.getDiskImage();
		final int qcowVersion = DiskImageUtils.readInt( diskFile, 4 );

		// check QCOW2 file format version
		if ( qcowVersion < 2 || qcowVersion > 3 ) {
			// QCOW2 disk image does not contain a valid QCOW2 version
			final String errorMsg = "Invalid QCOW2 version in header found!";
			throw new DiskImageException( errorMsg );
		}

		return new Version( Integer.valueOf( qcowVersion ).shortValue() );
	}

	@Override
	public String getDescription() throws DiskImageException
	{
		// QCOW2 disk image format does not support any disk description
		return null;
	}

	@Override
	public ImageFormat getFormat()
	{
		return ImageFormat.QCOW2;
	}
}
