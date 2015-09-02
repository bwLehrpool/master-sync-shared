package org.openslx.util.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.openslx.bwlp.thrift.iface.Virtualizer;

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
	public final ImageFormat format;

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
			String ct = config.get( "createType" );
			this.isStandalone = isStandaloneCreateType( ct );
			this.isCompressed = ct != null
					&& ct.equalsIgnoreCase( "streamOptimized" );
			this.format = ImageFormat.VMDK;
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

	private boolean isStandaloneCreateType( String type )
	{
		if ( type == null )
			return false;
		return type.equalsIgnoreCase( "streamOptimized" )
				|| type.equalsIgnoreCase( "monolithicSparse" );
	}

	public static class UnknownImageFormatException extends Exception
	{
		private static final long serialVersionUID = -6647935235475007171L;
	}

}
