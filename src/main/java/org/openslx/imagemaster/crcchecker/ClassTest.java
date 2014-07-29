package org.openslx.imagemaster.crcchecker;

import java.io.IOException;

public class ClassTest
{
	public static void main( String[] args ) throws IOException
	{
		if ( args.length != 2 ) {
			System.out.println( "Usage: filename crcfilename" );
			return;
		}
		String filename = args[0];
		String filenameCrc = args[1];
		final int blockSize = 16 * 1024 * 1024;

		CrcFile f = new CrcFile( filenameCrc );
		System.out.println( "Master sum: '" + f.getMasterSum() + "'" );
		System.out.println( f.getCrcSums() );
		System.out.println( "CRC file is '" + ( ( f.isValid() ) ? "valid" : "invalid" ) + "'" );

		ImageFile imageFile = new ImageFile( filename, blockSize );
		CrcChecker crcFile = new CrcChecker( imageFile, f );

		int blocks = getNumberOfBlocks( imageFile.length(), blockSize );
		for ( int i = 0; i < blocks; i++ ) {
			System.out.println( "Block\t" + i + "\tis  '" + ( ( crcFile.checkBlock( i ) ) ? "valid" : "invalid" ) + "'" );
		}

		crcFile.done();
	}

	public static int getNumberOfBlocks( long fileSize, int blockSize )
	{
		int blocks = (int) ( fileSize / blockSize );
		if ( fileSize % blockSize != 0 )
			blocks++;
		return blocks;
	}

}
