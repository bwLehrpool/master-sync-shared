package org.openslx.imagemaster.crcchecker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Represents a crc file
 */
public class CRCFile
{
	private File file = null;
	private List<Integer> crcSums = null;

	/**
	 * Loads a crcFile from file
	 * 
	 * @param filename
	 */
	public CRCFile( String filename )
	{
		this.file = new File( filename );
	}

	/**
	 * Creates a crc file which is not on the drive.
	 * 
	 * @param crcSums
	 */
	public CRCFile( List<Integer> crcSums )
	{
		this.crcSums = crcSums;
	}

	/**
	 * Creates a new crc file with the given sums.
	 * The first crc sum in the list needs to be the sum over the other sums.
	 * 
	 * @param listOfCrcSums The list of the crc sums that are going into the crc file
	 * @param filename Where to save the created crc file
	 * @throws IOException If it's not possible to write the file
	 */
	public static CRCFile writeCrcFile( List<Integer> listOfCrcSums, String filename ) throws IOException
	{
		File file = new File( filename );
		FileOutputStream fos = new FileOutputStream( file );
		DataOutputStream dos = new DataOutputStream( fos );

		for ( Integer sum : listOfCrcSums ) {
			dos.writeInt( sum.intValue() );
		}

		dos.close();
		return new CRCFile( filename );
	}

	/**
	 * Checks if given sums are valid.
	 * 
	 * @param listOfCrcSums
	 * @return
	 */
	public static boolean sumsAreValid( List<Integer> listOfCrcSums )
	{
		if ( listOfCrcSums == null || listOfCrcSums.isEmpty() )
			return false;
		int masterSum = listOfCrcSums.remove( 0 );
		byte[] bytesOfInt;
		int size = listOfCrcSums.size();
		byte[] bytes = new byte[ size * Integer.SIZE / 8 ];
		for ( int i = 0; i < size; i++ ) {
			bytesOfInt = ByteBuffer.allocate( 4 ).putInt( listOfCrcSums.remove( 0 ) ).array(); // get the bytes of this integer
			bytes[4 * i] = bytesOfInt[0];
			bytes[4 * i + 1] = bytesOfInt[1];
			bytes[4 * i + 2] = bytesOfInt[2];
			bytes[4 * i + 3] = bytesOfInt[3];
		}
		CRC32 crcCalc = new CRC32();
		crcCalc.update( bytes );
		return ( masterSum == Integer.reverseBytes( (int)crcCalc.getValue() ) );
	}

	/**
	 * Checks if this crc file is valid.
	 * (If the crc over the file is equal to the first crc sum.)
	 * 
	 * @return Whether the crc file is valid
	 * @throws IOException If the file could not be read or could not be found
	 */
	public boolean isValid() throws IOException
	{
		if ( file == null ) {
			return sumsAreValid( this.crcSums );
		} else {
			if ( crcSums == null )
				loadSums();
			return sumsAreValid( this.crcSums );
		}
	}

	/**
	 * Get a specified crcSum for a block number
	 * 
	 * @param blockNumber
	 * @return The crcSum or 0 if the block number is invalid
	 * @throws IOException If the crcSums could not be loaded from file
	 */
	public int getCRCSum( int blockNumber ) throws IOException
	{
		if ( crcSums == null )
			loadSums();

		if ( blockNumber < 0 )
			return 0;
		if ( blockNumber > crcSums.size() - 2 )
			return 0;

		return crcSums.get( blockNumber + 1 );
	}

	/**
	 * Returns the loaded crcSums.
	 * 
	 * @return The loaded crcSums
	 * @throws IOException If the crcSums could not be loaded from file
	 */
	public List<Integer> getCrcSums() throws IOException
	{
		if ( crcSums == null )
			loadSums();
		return this.crcSums;
	}

	private void loadSums() throws IOException
	{
		if ( crcSums != null )
			return;
		// the crcSums were not read yet
		DataInputStream dis = new DataInputStream( new FileInputStream( file ) );
		crcSums = new ArrayList<>();
		for ( int i = 0; i < file.length() / 4; i++ ) {
			crcSums.add( dis.readInt() );
		}
		dis.close();
	}
	
	public int getMasterSum() throws IOException
	{
		if ( crcSums == null )
			loadSums();
		return this.crcSums.get( 0 );
	}
}
