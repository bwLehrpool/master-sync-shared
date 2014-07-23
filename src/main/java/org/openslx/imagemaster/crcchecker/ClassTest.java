package org.openslx.imagemaster.crcchecker;

import java.io.IOException;

public class ClassTest
{
	public static void main( String[] args ) throws IOException
	{
		String filename = "/home/nils/win98-dp-demo-de.vmdk.r1";
		final int bs = 16 * 1024 * 1024;

		CRCFile f = new CRCFile( filename.concat( ".crc" ) );
		System.out.println( f.getMasterSum() );
		System.out.println( f.getCrcSums() );
		System.out.println( f.isValid() );

		System.out.println( CRCFile.sumsAreValid( f.getCrcSums() ) );

		ImageFile i = new ImageFile( filename, bs );

		CRCChecker c = new CRCChecker( i, f );
		System.out.println( c.checkBlock( 0 ) );
		System.out.println( c.checkBlock( 1 ) );
		System.out.println( c.checkBlock( 2 ) );
		System.out.println( c.checkBlock( 3 ) );
		System.out.println( c.checkBlock( 4 ) );
		System.out.println( c.checkBlock( 5 ) );
		System.out.println( c.checkBlock( 6 ) );
		System.out.println( c.checkBlock( 7 ) );
		System.out.println( c.checkBlock( 8 ) );
		System.out.println( c.checkBlock( 9 ) );
		System.out.println( c.checkBlock( 10 ) );
		System.out.println( c.checkBlock( 11 ) );
		System.out.println( c.checkBlock( 12 ) );
		System.out.println( c.checkBlock( 13 ) );
		System.out.println( c.checkBlock( 14 ) );
		System.out.println( c.checkBlock( 15 ) );
		c.done();
	}
}
