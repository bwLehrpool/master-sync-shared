package org.openslx.imagemaster.crcchecker;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Represents a crcfile
 * @author nils
 *
 */
public class CRCFile
{
	private File file;
	private List<Integer> crcSums = null;
	
	CRCFile(String filename) {
		this.file = new File( filename );
	}
	
	/**
	 * Checks if this crc file is valid.
	 * (If the crc over the file is equal to the first crc sum.)
	 * @return Whether the crc file is valid
	 * @throws IOException If the file could not be read or could not be found
	 */
	public boolean isValid() throws IOException {
		FileInputStream fis = new FileInputStream( file );
		DataInputStream dis = new DataInputStream( fis );
		
		int crcSum = dis.readInt();
		
		CRC32 crcCalc = new CRC32();
		
		byte[] bytes = new byte[(int) file.length() - Integer.SIZE/8];	// byte array with length of the file minus the first crc sum (=4byte)
		fis.read( bytes );
		crcCalc.update( bytes );
		
		dis.close();
		
		if (crcSum == Integer.reverseBytes( (int) crcCalc.getValue() ) ) return true;
		else return false;
	}
	
	/**
	 * Get a specified crcSum for a block number
	 * @param blockNumber
	 * @return The crcSum or 0 if the block number is invalid
	 * @throws IOException 
	 */
	public int getCRCSum(int blockNumber) throws IOException {
		if (crcSums == null) {
			// the crcSums were not read yet
			DataInputStream dis = new DataInputStream( new FileInputStream( file ) );
			crcSums = new ArrayList<>();
			for (int i = 0; i < file.length()/4; i++) {
				int s = dis.readInt();
				if (i > 0) crcSums.add( s );	// skip the first crcSum because it's the sum over the crcFile
			}
			dis.close();
		}
		
		if (blockNumber < 0) return 0;
		if (blockNumber > crcSums.size() - 1) return 0; 
		
		return crcSums.get( blockNumber );
	}		
}
