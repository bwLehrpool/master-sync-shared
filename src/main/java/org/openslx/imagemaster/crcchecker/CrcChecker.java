package org.openslx.imagemaster.crcchecker;

import java.io.IOException;
import java.util.zip.CRC32;

public class CrcChecker
{
	private static final int blockSize = 16 * 1024 * 1024;

	private ImageFile imageFile;
	private CrcFile crcFile;

	private byte[] block = new byte[ blockSize ];	// array that is used to read the blocks

	/**
	 * Initialize a crc checker with an image file and a crc file.
	 * 
	 * @param imageFile The image file to check
	 * @param crcFile The crc file to check against
	 */
	public CrcChecker( ImageFile imageFile, CrcFile crcFile )
	{
		this.imageFile = imageFile;
		this.crcFile = crcFile;
	}

	public void done()
	{
		imageFile.close();
	}

	public boolean hasValidCrcFile()
	{
		return crcFile.isValid();
	}

	/**
	 * Checks a chosen block against the crc file.
	 * 
	 * @param block The block to check
	 * @return Whether the block was valid or not
	 * @throws IOException When image or crc file could not be read.
	 */
	public boolean checkBlock( int blockNumber ) throws IOException
	{
		if ( !this.hasValidCrcFile() )
			return false;

		int length;
		try {
			length = imageFile.getBlock( blockNumber, block );
		} catch ( IOException e ) {
			throw new IOException( "image", e );
		}

		if ( length <= 0 )
			return false;

		CRC32 crcCalc = new CRC32();
		if ( length == blockSize ) {
			crcCalc.update( block );
		} else {
			crcCalc.update( block, 0, length );
		}

		int crcSum = Integer.reverseBytes( (int)crcCalc.getValue() );
		int crcSumFromFile;
		crcSumFromFile = crcFile.getCRCSum( blockNumber );

		return ( crcSum == crcSumFromFile );
	}
}
