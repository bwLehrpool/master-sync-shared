package org.openslx.util.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TCompactProtocol;
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
	private static final String QEMU = "QFI";

	public enum ImageFormat
	{
		VMDK( "vmdk" ), QCOW2( "qcow2" ), VDI( "vdi" );

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
			if ( file.readInt() == VMDK_MAGIC ) {
				file.seek( 512 );
				byte[] buffer = new byte[ 2048 ];
				file.readFully( buffer );
				VmwareConfig config;
				try {
					config = new VmwareConfig( buffer, findNull( buffer ) );
				} catch ( UnsupportedVirtualizerFormatException e ) {
					config = null;
				}
				if ( config != null ) {
					subFormat = config.get( "createType" );
					String parent = config.get( "parentCID" );
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
				Arrays.fill( new byte[ 16 ], (byte)0 );
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

			// TODO: qcow
			file.seek( 0 );
			byte[] qcowBuffer = new byte[ QEMU.length() ];
			file.readFully( qcowBuffer );
			String qcowString = new String( qcowBuffer );
			if ( QEMU.equals( qcowString ) ) {
				// dummy values
				this.isStandalone = true;
				this.isCompressed = false;
				this.isSnapshot = false;
				this.format = ImageFormat.QCOW2;
				this.subFormat = null;
				this.diskDescription = null;
				this.hwVersion = 0;
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
