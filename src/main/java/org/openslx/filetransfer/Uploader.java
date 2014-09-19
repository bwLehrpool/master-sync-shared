package org.openslx.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
	public Uploader( String host, int port, SSLContext context, String token ) throws IOException
	{
		super( host, port, context, log );
		outStream.writeByte( 'U' );
		if ( !sendToken( token ) || !sendEndOfMeta() )
			throw new IOException( "Sending token failed" );
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
	 * Method for sending File with filename.
	 * 
	 * @param filename
	 */
	public boolean upload( String filename )
	{
		if ( shouldGetToken() ) {
			log.error( "You didn't call getToken yet!" );
			return false;
		}
		RandomAccessFile file = null;
		try {
			try {
				file = new RandomAccessFile( new File( filename ), "r" );
			} catch ( FileNotFoundException e ) {
				log.error( "Could not open " + filename + " for reading." );
				return false;
			}
			for ( ;; ) { // Loop as long as remote peer is requesting chunks from this file
				// Read meta data of remote peer - either new range, or it's telling us it's done
				MetaData meta = readMetaData();
				if ( meta == null ) {
					log.error( "Did not get meta data from remote peer." );
					return false;
				}
				if ( meta.isDone() ) // Download complete?
					break;
				// Not complete, so there must be another range request
				FileRange requestedRange = meta.getRange();
				if ( requestedRange == null ) {
					log.error( "Remote peer did not include RANGE in meta data." );
					sendErrorCode( "no (valid) range in request" );
					return false;
				}
				// Range inside file?
				try {
					if ( requestedRange.endOffset > file.length() ) {
						log.error( "Requested range is larger than file size, aborting." );
						sendErrorCode( "range out of file bounds" );
						return false;
					}
				} catch ( IOException e ) {
					log.error( "Could not get current length of file " + filename );
					return false;
				}
				// Seek to requested chunk
				try {
					file.seek( requestedRange.startOffset );
				} catch ( IOException e ) {
					log.error( "Could not seek to start of requested range in " + filename + " (" + requestedRange.startOffset + ")" );
					return false;
				}
				// Send confirmation of range we're about to send
				try {
					long ptr = file.getFilePointer();
					if ( !sendRange( ptr, ptr + requestedRange.getLength() ) || !sendEndOfMeta() ) {
						log.error( "Could not send range confirmation" );
						return false;
					}
				} catch ( IOException e ) {
					log.error( "Could not determine current position in file " + filename );
					return false;
				}
				// Finally send requested chunk
				byte[] data = new byte[ 500000 ]; // 500kb
				int hasRead = 0;
				int length = requestedRange.getLength();
				while ( hasRead < length ) {
					int ret;
					try {
						ret = file.read( data, 0, Math.min( length - hasRead, data.length ) );
					} catch ( IOException e ) {
						log.error( "Error reading from file " + filename );
						return false;
					}
					if ( ret == -1 ) {
						this.close( "Error occured in Uploader.sendFile(),"
								+ " while reading from File to send." );
						return false;
					}
					hasRead += ret;
					try {
						outStream.write( data, 0, ret );
					} catch ( IOException e ) {
						log.error( "Sending payload failed" );
						return false;
					}
				}
			}
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
