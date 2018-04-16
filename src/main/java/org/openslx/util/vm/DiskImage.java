package org.openslx.util.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.util.Util;

public class DiskImage
{
	/**
	 * Big endian representation of the 4 bytes 'KDMV'
	 */
	private static final int VMDK_MAGIC = 0x4b444d56;
	private static final String VDI_PREFIX = "<<< ";
	private static final String VDI_SUFFIX = "Disk Image >>>";
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
			if ( virtId.equals( "vmware" ) )
				return VMDK;
			if ( virtId.equals( "virtualbox" ) )
				return VDI;
			if ( virtId.equals( "qemukvm" ) )
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

	public ImageFormat getImageFormat()
	{
		return format;
	}

	public DiskImage( File disk ) throws FileNotFoundException, IOException, UnknownImageFormatException
	{
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
					return;
				}
			}
			// vdi
			file.seek( 0 );
			byte[] prefixBuffer = new byte[ VDI_PREFIX.length() ];
			file.readFully( prefixBuffer );
			String prefixString = new String( prefixBuffer );
			if ( VDI_PREFIX.equals( prefixString ) ) {

				byte[] localBuffer = new byte[ 1 ];
				byte[] suffixBuffer = new byte[ VDI_SUFFIX.length() - 1 ];
				// 30 in this case would be the remaining length of the vdi header
				// the longest string to date would be "<<< QEMU VM Virtual Disk Image >>>"
				// if the loop doesn't find the first letter of the VID_SUFFIX then we have another format on our hands and should throw exception
				for ( int i = 0; i < 30; i++ ) {
					file.readFully( localBuffer );
					String localString = new String( localBuffer );

					if ( !localString.equals( VDI_SUFFIX.substring( 0, 1 ) ) ) {
						continue;
					}
					file.readFully( suffixBuffer );
					String suffixString = new String( suffixBuffer );
					if ( suffixString.equals( VDI_SUFFIX.substring( 1 ) ) ) {
						// TODO still don't know where they are found in a .vdi file
						this.isStandalone = true;
						this.isCompressed = false;
						this.isSnapshot = false;
						this.format = ImageFormat.VDI;
						this.subFormat = "";
						this.hwVersion = 0;
						return;
					} else {
						// this will ensure the search doesn't stop at the first D we find
						file.seek( i + VDI_PREFIX.length() + 1 );
					}
				}
			}
			//qcow
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
				this.subFormat = "";
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
