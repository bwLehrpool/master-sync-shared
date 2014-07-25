package org.openslx.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class Uploader extends Transfer
{

	private static final Logger log = Logger.getLogger( Uploader.class );

	/***********************************************************************/
	/**
	 * Actively establish upload connection to given peer.
	 * 
	 * @param host Host name or address to connect to
	 * @param port Port to connect to
	 * @param context ssl context for establishing a secure connection
	 * @throws IOException
	 */
	public Uploader( String host, int port, SSLContext context ) throws IOException
	{
		super( host, port, context, log );
		dataToServer.writeByte( 'U' );
	}

	/***********************************************************************/
	/**
	 * Constructor for master uploader.
	 * Sends back the socket for datatransfer.
	 * 
	 * @throws IOException
	 */
	public Uploader( SSLSocket socket ) throws IOException
	{
		super( socket, log );
	}

	/***********************************************************************/
	/**
	 * Used by the peer that initiated the connection to tell the remote
	 * peer which part of the file is being uploaded
	 * 
	 * @param startOffset start offset in bytes in the file (inclusive)
	 * @param endOffset end offset in file (exclusive)
	 * @return
	 */
	public boolean prepareSendRange( int startOffset, int endOffset )
	{
		return super.sendRange( startOffset, endOffset );
	}

	/***********************************************************************/
	/**
	 * Method for sending File with filename.
	 * 
	 * @param filename
	 */
	public boolean sendFile( String filename )
	{
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile( new File( filename ), "r" );

			if ( getStartOfRange() == -1 ) {
				this.close();
				return false;
			}
			file.seek( getStartOfRange() );

			byte[] data = new byte[ 64000 ];
			int hasRead = 0;
			int length = getDiffOfRange();
			//			System.out.println( "diff of Range: " + length );
			while ( hasRead < length ) {
				int ret = file.read( data, 0, Math.min( length - hasRead, data.length ) );
				if ( ret == -1 ) {
					log.warn( "Error occured in Uploader.sendFile(),"
							+ " while reading from File to send." );
					this.close();
					return false;
				}
				hasRead += ret;
				dataToServer.write( data, 0, ret );
			}
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			sendErrorCode( "timeout" );
			log.warn( "Socket timeout occured ... close connection." );
			this.close();
			return false;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
			readMetaData();
			if ( ERROR != null ) {
				if ( ERROR == "timeout" ) {
					log.warn( "Socket timeout occured ... close connection." );
					this.close();
				}
			}
			log.warn( "Sending RANGE " + getStartOfRange() + ":" + getEndOfRange() + " of File "
					+ filename + " failed..." );
			this.close();
			return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			this.close();
			return false;
		} finally {
			if ( file != null ) {
				try {
					file.close();
				} catch ( IOException e ) {
				}
			}
		}
		return true;
	}

}
