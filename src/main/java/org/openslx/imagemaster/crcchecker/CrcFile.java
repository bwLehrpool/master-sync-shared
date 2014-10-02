package org.openslx.imagemaster.crcchecker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

/**
 * Represents a crc file
 */
public class CrcFile
{
	private final int masterCrc;
	private final int[] crcSums;
	private Boolean valid = null;
	private final File file;

	private static Logger log = Logger.getLogger( CrcFile.class );

	/**
	 * Loads a crcFile from file
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public CrcFile( String filename ) throws IOException
	{
		file = new File( filename );
		DataInputStream dis = null;
		try {
			dis = new DataInputStream( new FileInputStream( file ) );
			int numSums = (int) ( file.length() / 4 ) - 1;
			if ( numSums < 0 )
				throw new IOException( "Invalid crc file: " + filename );
			masterCrc = dis.readInt();
			int[] sums = new int[ numSums ];
			for ( int i = 0; i < numSums; i++ ) {
				sums[i] = dis.readInt();
			}
			crcSums = sums;
		} finally {
			if ( dis != null ) {
				try {
					dis.close();
				} catch ( IOException e ) {
				}
			}
		}
	}

	/**
	 * Creates a crc file which is not on the drive.
	 * 
	 * @param crcSums
	 */
	public CrcFile( int[] crcSumsWithLeadingMasterCrc )
	{
		this.file = null;
		this.masterCrc = crcSumsWithLeadingMasterCrc[0];
		this.crcSums = Arrays.copyOfRange( crcSumsWithLeadingMasterCrc, 1, crcSumsWithLeadingMasterCrc.length );
	}

	public CrcFile( List<Integer> crcSumsWithLeadingMasterCrc )
	{
		this.file = null;
		this.masterCrc = crcSumsWithLeadingMasterCrc.get( 0 );
		this.crcSums = new int[ crcSumsWithLeadingMasterCrc.size() - 1 ];
		for ( int i = 0; i < crcSums.length; i++ )
			crcSums[i] = crcSumsWithLeadingMasterCrc.get( i + 1 );
	}

	/**
	 * Creates a new crc file with the given sums.
	 * The first crc sum in the list needs to be the sum over the other sums.
	 * Deletes existing files with the same name.
	 * 
	 * @param listOfCrcSums The list of the crc sums that are going into the crc file
	 * @param filename Where to save the created crc file
	 * @throws IOException If it's not possible to write the file
	 */
	public void writeCrcFile( String filename )
	{
		File file = new File( filename );

		if ( file.exists() )
			file.delete();

		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream( file );
		} catch ( FileNotFoundException e ) {
			log.error( "File " + filename + " not found.", e );
			return;
		}
		try {
			dos = new DataOutputStream( fos );
			dos.writeInt( Integer.reverseBytes( masterCrc ) );
			for ( int sum : crcSums ) {
				dos.writeInt( Integer.reverseBytes( sum ) );
			}
		} catch ( IOException e ) {
			log.error( "IOException", e );
			return;
		} finally {
			if ( dos != null )
				try {
					dos.close();
				} catch ( IOException e ) {
				}
		}
	}

	/**
	 * Checks if this crc file is valid.
	 * (If the crc over the file is equal to the first crc sum.)
	 * 
	 * @return Whether the crc file is valid
	 */
	public boolean isValid()
	{
		if ( valid == null ) {
			if ( crcSums == null || crcSums.length < 1 )
				return false;

			int masterSum = crcSums[0];

			CRC32 crcCalc = new CRC32();
			byte[] buffer = new byte[ 4 ];

			for ( int i = 1; i < crcSums.length; i++ ) {
				crcCalc.update( intToByteArrayLittleEndian( crcSums[i], buffer ) ); // update the crc calculator with the next 4 bytes of the integer
			}

			valid = ( masterSum == Integer.reverseBytes( (int)crcCalc.getValue() ) );
		}
		return valid;
	}

	/**
	 * Delete the file that is backing this list of crc sums, if any.
	 */
	public void delete()
	{
		if ( file != null )
			file.delete();
	}

	/**
	 * Get a specified crcSum for a block number
	 * 
	 * @param blockNumber
	 * @return The crcSum or 0 if the block number is invalid
	 * @throws IOException If the crcSums could not be loaded from file
	 */
	public int getCRCSum( int blockNumber )
	{
		if ( crcSums == null || blockNumber < 0 || blockNumber >= crcSums.length )
			return 0;
		return crcSums[blockNumber];
	}

	public List<Integer> getCrcSums()
	{
		List<Integer> ret = new ArrayList<Integer>( crcSums.length );
		for ( int i = 0; i < crcSums.length; i++ )
			ret.add( crcSums[i] );
		return ret;
	}

	public int getMasterSum()
	{
		return masterCrc;
	}

	private static final byte[] intToByteArrayLittleEndian( int value, byte[] buffer )
	{
		buffer[3] = (byte) ( value >>> 24 );
		buffer[2] = (byte) ( value >>> 16 );
		buffer[1] = (byte) ( value >>> 8 );
		buffer[0] = (byte)value;
		return buffer;
	}

}
