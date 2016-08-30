package org.openslx.util.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.util.Util;

public class DiskImage
{

	/**
	 * Big endian representation of the 4 bytes 'KDMV'
	 */
	private static final int VMDK_MAGIC = 0x4b444d56;

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
			return null;
		}
	}

	public final boolean isStandalone;
	public final boolean isCompressed;
	public final boolean isSnapshot;
	public final ImageFormat format;
	public final String subFormat;
	public final int hwVersion;

	public DiskImage( File disk ) throws FileNotFoundException, IOException,
			UnknownImageFormatException
	{
		// For now we only support VMDK...
		try ( RandomAccessFile file = new RandomAccessFile( disk, "r" ) ) {
			if ( file.readInt() != VMDK_MAGIC )
				throw new UnknownImageFormatException();
			file.seek( 512 );
			byte[] buffer = new byte[ 2048 ];
			file.readFully( buffer );
			VmwareConfig config = new VmwareConfig( buffer, findNull( buffer ) );
			subFormat = config.get( "createType" );
			String parent = config.get( "parentCID" );
			this.isStandalone = isStandaloneCreateType( subFormat, parent );
			this.isCompressed = subFormat != null
					&& subFormat.equalsIgnoreCase( "streamOptimized" );
			this.isSnapshot = parent != null
					&& !parent.equalsIgnoreCase( "ffffffff" );
			this.format = ImageFormat.VMDK;
			String hwv = config.get(  "ddb.virtualHWVersion" );
			if (hwv == null ) {
				this.hwVersion = 10;
			} else {
				this.hwVersion = Util.parseInt( hwv, 10 );
			}
		}
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
		return type.equalsIgnoreCase( "streamOptimized" )
				|| type.equalsIgnoreCase( "monolithicSparse" );
	}

	public static class UnknownImageFormatException extends Exception
	{
		private static final long serialVersionUID = -6647935235475007171L;
	}

}
