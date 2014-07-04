package org.openslx.imagemaster.crcchecker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

public class CRCChecker
{

	private static Logger log = Logger.getLogger( CRCChecker.class );
	private static final int blockSize = 16 * 1024 * 1024;

	/**
	 * Checks the CRC sum of given blocks of a given imageFile against a given crcFile.
	 * The caller needs to make sure that block that are going to be checked are complete!
	 * 
	 * @param imageFile The imageFile to check
	 * @param crcFile The crcFile to check against
	 * @param blocks The blocks to check
	 * @return List of blocks where the crc matches, or null if the crc file is corrupted
	 * @throws IOException When crc file could not be read
	 */
	public static List<Integer> checkCRC( String imageFile, String crcFile, List<Integer> blocks ) throws IOException
	{
		List<Integer> result = new LinkedList<>();

		ImageFile image = new ImageFile( imageFile, blockSize );
		CRCFile crc = new CRCFile( crcFile );

		log.debug( "Checking image file: '" + imageFile + "' with crc file: '" + crcFile + "'" );
		try {
			if ( !crc.isValid() )
				return null;
			// TODO: also return null if the crc file contains the wrong number of checksums (only makes sense if the upload is complete)
		} catch ( IOException e ) {
			throw new IOException( "Could not read CRC file", e );
		}

		// check all blocks
		byte[] block = new byte[blockSize];
		for ( Integer blockN : blocks ) {
			try {
				image.getBlock( blockN, block );
			} catch ( IOException e ) {
				throw new IOException( "Could not read image file", e );
			}

			if ( block == null )
				continue; // some error occured (for example: someone tried to check a block that is not in the file)

			// check this block with CRC32
			// add this block to result, if check was ok with CRC file

			CRC32 crcCalc = new CRC32();
			crcCalc.update( block );
			int crcSum = Integer.reverseBytes( (int)crcCalc.getValue() );
			int crcSumFromFile;
			try {
				crcSumFromFile = crc.getCRCSum( blockN );
			} catch ( IOException e ) {
				throw new IOException( "Could not read CRC file", e );
			}

			if ( crcSum == crcSumFromFile )
				result.add( blockN );
			else
				log.debug( blockN + " was invalid" );
		}

		return result;
	}
}
