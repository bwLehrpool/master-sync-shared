package org.openslx.imagemaster.crcchecker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Representing an image file.
 * Is able to return certain blocks of this file.
 * 
 * @author nils
 * 
 */
public class ImageFile
{
	private File f;
	private RandomAccessFile file = null;
	private int blockSize;

	public ImageFile( String filename, int blockSize )
	{
		this.f = new File( filename );
		this.blockSize = blockSize;
	}

	/**
	 * Get a certain block (uses RandomAccessFile)
	 * If the last block is not full an array with a smaller size is set
	 * and the actual number of bytes is returned.
	 * 
	 * @param block The number of the block you want to get
	 * @return The actual number of read bytes or -1 if nothing was read.
	 * @throws IOException When file was not found or could not be read
	 */
	public int getBlock( int block, byte[] array ) throws IOException
	{
		if ( block < 0 )
			return 0;
		if ( block > f.length() / blockSize )
			return 0;

		if ( file == null ) {
			file = new RandomAccessFile( f, "r" );
		}

		file.seek( (long)block * blockSize );
		
		return file.read( array );
	}

	public long length()
	{
		return f.length();
	}

	/**
	 * Closes the file.
	 * It's not a problem to use getBlock() again. The file will then be re-opened.
	 */
	public void close()
	{
		try {
			file.close();
			file = null;
		} catch ( IOException e ) {/* Can't do anything about it.*/}
	}
}
