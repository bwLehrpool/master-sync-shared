package org.openslx.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class Downloader extends Transfer
{
	// Some instance variables.
	private String outputFilename = null;

	private static final Logger log = Logger.getLogger( Downloader.class );

	/***********************************************************************/
	/**
	 * Actively initiate a connection to a remote peer for downloading.
	 * 
	 * @param host Host name or address to connect to
	 * @param port Port to connect to
	 * @throws IOException
	 */
	public Downloader( String host, int port, SSLContext context ) throws IOException
	{
		super( host, port, context, log );
		dataToServer.writeByte( 'D' );
	}

	/***********************************************************************/
	/**
	 * Constructor used by Listener to create an incoming download connection.
	 * 
	 * @param socket established connection to peer which requested an upload.
	 * @throws IOException 
	 */
	protected Downloader( SSLSocket socket ) throws IOException
	{
		super( socket, log );
	}

	/***********************************************************************/
	/**
	 * Method for setting outputFilename.
	 * 
	 * @param filename
	 */
	public void setOutputFilename( String filename )
	{
		outputFilename = filename;
	}

	/***********************************************************************/
	/**
	 * Method for getting outputFilename.
	 * 
	 * @return outputFilename
	 */
	public String getOutputFilename()
	{
		return outputFilename;
	}

	/***********************************************************************/
	/**
	 * Method to request a byte range within the file to download. This
	 * method is called by the party that initiated the connection.
	 * 
	 * @param startOffset offset in file where to start the transfer (inclusive)
	 * @param endOffset end offset where to end the transfer (exclusive)
	 * @return success or failure
	 */
	public boolean requestRange( int startOffset, int endOffset )
	{
		return super.sendRange( startOffset, endOffset );
	}

	/***********************************************************************/
	/**
	 * Method for reading Binary. Reading the current Range of incoming binary.
	 * 
	 */
	public boolean receiveBinary()
	{
		RandomAccessFile file = null;
		try {
			int chunkLength = getDiffOfRange();
			byte[] incoming = new byte[ 64000 ];
			int hasRead = 0;
			file = new RandomAccessFile( new File( outputFilename ), "rw" );
			file.seek( getStartOfRange() );
			while ( hasRead < chunkLength ) {
				int ret = dataFromServer.read( incoming, 0, Math.min( chunkLength - hasRead, incoming.length ) );
				// log.info("hasRead: " + hasRead + " length: " + length + " ret: " + ret); 
				if ( ret == -1 ) {
					log.info( "Error occured while receiving payload." );
					return false;
				}
				hasRead += ret;
				file.write( incoming, 0, ret );

			}
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			sendErrorCode( "timeout" );
			log.info( "Socket timeout occured ... close connection." );
			this.close();
			return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			log.info( "Reading RANGE " + getStartOfRange() + ":" + getEndOfRange()
					+ " of file failed..." );
			this.close();
			return false;
		} finally {
			if ( file != null ) {
				try {
					file.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
			RANGE = null; // Reset range for next iteration
		}
		return true;
	}
}
