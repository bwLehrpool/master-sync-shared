package org.openslx.util.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.Util;

public class DiskImage
{
	private static final Logger LOGGER = Logger.getLogger( DiskImage.class );
	/**
	 * Big endian representation of the 4 bytes 'KDMV'
	 */
	private static final int VMDK_MAGIC = 0x4b444d56;
	private static final int VDI_MAGIC = 0x7f10dabe;
	/**
	 * Big endian representation of the 4 bytes 'QFI\xFB'
	 */
	private static final int QEMU_MAGIC = 0x514649fb;

	public enum ImageFormat
	{
		VMDK( "vmdk" ), QCOW2( "qcow2" ), VDI( "vdi" ), DOCKER( "dockerfile" );

		public final String extension;

		private ImageFormat( String extension )
		{
			this.extension = extension;
		}

		public static ImageFormat defaultForVirtualizer( Virtualizer virt )
		{
			if ( virt == null )
				return null;
			return defaultForVirtualizer( virt.virtId );
		}

		public static ImageFormat defaultForVirtualizer( String virtId )
		{
			if ( virtId == null )
				return null;
			if ( virtId.equals( TConst.VIRT_VMWARE ) )
				return VMDK;
			if ( virtId.equals( TConst.VIRT_VIRTUALBOX ) )
				return VDI;
			if ( virtId.equals( TConst.VIRT_QEMU ) )
				return QCOW2;
			if ( virtId.equals( TConst.VIRT_DOCKER ) )
				return DOCKER;
			return null;
		}
	}

	public final boolean isStandalone;
	public final boolean isCompressed;
	public final boolean isSnapshot;
	public final ImageFormat format;
	public final String subFormat;
	public final int hwVersion;
	public final String diskDescription;

	public ImageFormat getImageFormat()
	{
		return format;
	}

	public DiskImage( File disk ) throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		LOGGER.debug( "Validating disk file: " + disk.getAbsolutePath() );
		try ( RandomAccessFile file = new RandomAccessFile( disk, "r" ) ) {
			// vmdk
			boolean isVmdkMagic = ( file.readInt() == VMDK_MAGIC );
			if ( isVmdkMagic || file.length() < 4096 ) {
				if ( isVmdkMagic ) {
					file.seek( 512 );
				} else {
					file.seek( 0 );
				}
				byte[] buffer = new byte[ (int)Math.min( 2048, file.length() ) ];
				file.readFully( buffer );
				VmwareConfig config;
				try {
					config = new VmwareConfig( buffer, findNull( buffer ) );
				} catch ( UnsupportedVirtualizerFormatException e ) {
					config = null;
				}
				if ( config != null ) {
					String sf = config.get( "createType" );
					String parent = config.get( "parentCID" );
					if ( sf != null || parent != null ) {
						subFormat = sf;
						this.isStandalone = isStandaloneCreateType( subFormat, parent );
						this.isCompressed = subFormat != null && subFormat.equalsIgnoreCase( "streamOptimized" );
						this.isSnapshot = parent != null && !parent.equalsIgnoreCase( "ffffffff" );
						this.format = ImageFormat.VMDK;
						String hwv = config.get( "ddb.virtualHWVersion" );
						if ( hwv == null ) {
							this.hwVersion = 10;
						} else {
							this.hwVersion = Util.parseInt( hwv, 10 );
						}
						this.diskDescription = null;
						return;
					}
				}
			}
			// Format spec from: https://forums.virtualbox.org/viewtopic.php?t=8046
			// First 64 bytes are the opening tag: <<< .... >>>
			// which we don't care about, then comes the VDI signature
			file.seek( 64 );
			if ( file.readInt() == VDI_MAGIC ) {
				// skip the next 4 ints as they don't interest us:
				// - VDI version
				// - size of header, strangely irrelevant?
				// - image type, 1 for dynamic allocated storage, 2 for fixed size
				// - image flags, seem to be always 00 00 00 00
				file.skipBytes( 4 * 4 );

				// next 256 bytes are image description
				byte[] imageDesc = new byte[ 256 ];
				file.readFully( imageDesc );
				// next sections are irrelevant (int if not specified):
				// - offset blocks
				// - offset data
				// - cylinders
				// - heads
				// - sectors
				// - sector size
				// - <unused>
				// - disk size (long = 8 bytes)
				// - block size
				// - block extra data
				// - blocks in hdd
				// - blocks allocated
				file.skipBytes( 4 * 13 );

				// now it gets interesting, UUID of VDI
				byte[] diskUuid = new byte[ 16 ];
				file.readFully( diskUuid );
				// last snapshot uuid, mostly uninteresting since we don't support snapshots -> skip 16 bytes
				// TODO: meaning of "uuid link"? for now, skip 16
				file.skipBytes( 32 );

				// parent uuid, indicator if this VDI is a snapshot or not
				byte[] parentUuid = new byte[ 16 ];
				file.readFully( parentUuid );
				byte[] zeroUuid = new byte[ 16 ];
				Arrays.fill( zeroUuid, (byte)0 );
				this.isSnapshot = !Arrays.equals( parentUuid, zeroUuid );
				// VDI does not seem to support split VDI files so always standalone
				this.isStandalone = true;
				// compression is done by sparsifying the disk files, there is no flag for it
				this.isCompressed = false;
				this.format = ImageFormat.VDI;
				this.subFormat = null;
				this.diskDescription = new String( imageDesc );
				this.hwVersion = 0;
				return;
			}

			// qcow2 disk image
			file.seek( 0 );
			if ( file.readInt() == QEMU_MAGIC ) {
				// 
				// qcow2 (version 2 and 3) header format:
				//
				// magic                   (4 byte)
				// version                 (4 byte)
				// backing_file_offset     (8 byte)
				// backing_file_size       (4 byte)
				// cluster_bits            (4 byte)
				// size                    (8 byte)
				// crypt_method            (4 byte)
				// l1_size                 (4 byte)
				// l1_table_offset         (8 byte)
				// refcount_table_offset   (8 byte)
				// refcount_table_clusters (4 byte)
				// nb_snapshots            (4 byte)
				// snapshots_offset        (8 byte)
				// incompatible_features   (8 byte)  [*]
				// compatible_features     (8 byte)  [*]
				// autoclear_features      (8 byte)  [*]
				// refcount_order          (8 byte)  [*]
				// header_length           (4 byte)  [*]
				//
				// [*] these fields are only available in the qcow2 version 3 header format
				// 

				//
				// check qcow2 file format version
				//
				file.seek( 4 );
				final int qcowVersion = file.readInt();
				if ( qcowVersion < 2 || qcowVersion > 3 ) {
					// disk image format is not a qcow2 disk format
					throw new UnknownImageFormatException();
				} else {
					// disk image format is a valid qcow2 disk format
					this.hwVersion = qcowVersion;
					this.subFormat = null;
				}

				//
				// check if qcow2 image does not refer to any backing file
				//
				file.seek( 8 );
				this.isStandalone = ( file.readLong() == 0 ) ? true : false;

				//
				// check if qcow2 image does not contain any snapshot
				//
				file.seek( 56 );
				this.isSnapshot = ( file.readInt() == 0 ) ? true : false;

				//
				// check if qcow2 image uses extended L2 tables
				//
				boolean qcowUseExtendedL2 = false;

				// extended L2 tables are only possible in qcow2 version 3 header format
				if ( qcowVersion == 3 ) {
					// read incompatible feature bits
					file.seek( 72 );
					final long qcowIncompatibleFeatures = file.readLong();

					// support for extended L2 tables is enabled if bit 4 is set
					qcowUseExtendedL2 = ( ( ( qcowIncompatibleFeatures & 0x000000000010 ) >>> 4 ) == 1 );
				}

				//
				// check if qcow2 image contains compressed clusters
				//
				boolean qcowCompressed = false;

				// get number of entries in L1 table
				file.seek( 36 );
				final int qcowL1TableSize = file.readInt();

				// check if a valid L1 table is present
				if ( qcowL1TableSize > 0 ) {
					// qcow2 image contains active L1 table with more than 0 entries: l1_size > 0
					// search for first L2 table and its first entry to get compression bit

					// get cluster bits to calculate the cluster size
					file.seek( 20 );
					final int qcowClusterBits = file.readInt();
					final int qcowClusterSize = ( 1 << qcowClusterBits );

					// entries of a L1 table have always the size of 8 byte (64 bit)
					final int qcowL1TableEntrySize = 8;

					// entries of a L2 table have either the size of 8 or 16 byte (64 or 128 bit)
					final int qcowL2TableEntrySize = ( qcowUseExtendedL2 ) ? 16 : 8;

					// calculate number of L2 table entries
					final int qcowL2TableSize = qcowClusterSize / qcowL2TableEntrySize;

					// get offset of L1 table
					file.seek( 40 );
					long qcowL1TableOffset = file.readLong();

					// check for each L2 table referenced from an L1 table its entries
					// until a compressed cluster descriptor is found
					for ( long i = 0; i < qcowL1TableSize; i++ ) {
						// get offset of current L2 table from the current L1 table entry
						long qcowL1TableEntryOffset = qcowL1TableOffset + ( i * qcowL1TableEntrySize );
						file.seek( qcowL1TableEntryOffset );
						long qcowL1TableEntry = file.readLong();

						// extract offset (bits 9 - 55) from L1 table entry
						long qcowL2TableOffset = ( qcowL1TableEntry & 0x00fffffffffffe00L );

						if ( qcowL2TableOffset == 0 ) {
							// L2 table and all clusters described by this L2 table are unallocated
							continue;
						}

						// get each L2 table entry and check if it is a compressed cluster descriptor
						for ( long j = 0; j < qcowL2TableSize; j++ ) {
							// get current L2 table entry
							long qcowL2TableEntryOffset = qcowL2TableOffset + ( j * qcowL2TableEntrySize );
							file.seek( qcowL2TableEntryOffset );
							long qcowL2TableEntry = file.readLong();

							// extract cluster type (standard or compressed) (bit 62)
							boolean qcowClusterCompressed = ( ( ( qcowL2TableEntry & 0x4000000000000000L ) >>> 62 ) == 1 );

							// check if qcow2 disk image contains at least one compressed cluster descriptor
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
					// qcow2 image does not contain an active L1 table with any entry: l1_size = 0
					qcowCompressed = false;
				}

				this.isCompressed = qcowCompressed;
				this.format = ImageFormat.QCOW2;
				this.diskDescription = null;

				return;
			}
		}
		throw new UnknownImageFormatException();
	}

	private int findNull( byte[] buffer )
	{
		for ( int i = 0; i < buffer.length; ++i ) {
			if ( buffer[i] == 0 )
				return i;
		}
		return buffer.length;
	}

	private boolean isStandaloneCreateType( String type, String parent )
	{
		if ( type == null )
			return false;
		if ( parent != null && !parent.equalsIgnoreCase( "ffffffff" ) )
			return false;
		return type.equalsIgnoreCase( "streamOptimized" ) || type.equalsIgnoreCase( "monolithicSparse" );
	}

	public static class UnknownImageFormatException extends Exception
	{
		private static final long serialVersionUID = -6647935235475007171L;
	}
}
