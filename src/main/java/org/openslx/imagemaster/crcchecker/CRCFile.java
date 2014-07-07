package org.openslx.imagemaster.crcchecker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Represents a crcfile
 * 
 * @author nils
 * 
 */
public class CRCFile
{
	private File file;
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
	 * Creates a new crc file with the given sums.
	 * The first crc sum in the list needs to be the sum over the other sums.
	 * 
	 * @param listOfCrcSums The list of the crc sums that are going into the crc file
	 * @param filename Where to save the created crc file
	 * @throws IOException If it's not possible to write the file
	 */
	public CRCFile( List<Integer> listOfCrcSums, String filename ) throws IOException
	{
		this.file = new File( filename );
		FileOutputStream fos = new FileOutputStream( file );
		DataOutputStream dos = new DataOutputStream( fos );

		for ( Integer sum : listOfCrcSums ) {
			dos.writeInt( Integer.reverseBytes( sum.intValue() ) );		// save byte-reversed integers to match right order in crc file
		}

		dos.close();
		this.crcSums = listOfCrcSums;
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
		FileInputStream fis = new FileInputStream( file );
		DataInputStream dis = new DataInputStream( fis );

		int crcSum = dis.readInt();

		CRC32 crcCalc = new CRC32();

		byte[] bytes = new byte[ (int)file.length() - Integer.SIZE / 8 ];	// byte array with length of the file minus the first crc sum (=4byte)
		fis.read( bytes );
		crcCalc.update( bytes );

		dis.close();

		if ( crcSum == Integer.reverseBytes( (int)crcCalc.getValue() ) )
			return true;
		else
			return false;
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
		if (crcSums == null) loadSums();

		if ( blockNumber < 0 )
			return 0;
		if ( blockNumber > crcSums.size() - 1 )
			return 0;

		return crcSums.get( blockNumber );
	}
	
	/**
	 * Returns the loaded crcSums.
	 * @return The loaded crcSums
	 * @throws IOException If the crcSums could not be loaded from file
	 */
	public List<Integer> getCrcSums() throws IOException {
		if (crcSums == null) loadSums();
		return this.crcSums;
	}
	
	private void loadSums() throws IOException
	{
		// the crcSums were not read yet
		DataInputStream dis = new DataInputStream( new FileInputStream( file ) );
		crcSums = new ArrayList<>();
		for ( int i = 0; i < file.length() / 4; i++ ) {
			int s = dis.readInt();
			if ( i > 0 )
				crcSums.add( s );	// skip the first crcSum because it's the sum over the crcFile
		}
		dis.close();
	}
}
