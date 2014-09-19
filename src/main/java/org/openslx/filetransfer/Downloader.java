package org.openslx.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class Downloader extends Transfer
{

	private static final Logger log = Logger.getLogger( Downloader.class );

	/***********************************************************************/
	/**
	 * Actively initiate a connection to a remote peer for downloading.
	 * 
	 * @param host Host name or address to connect to
	 * @param port Port to connect to
	 * @throws IOException
	 */
	public Downloader( String host, int port, SSLContext context, String token ) throws IOException
	{
		super( host, port, context, log );
		outStream.writeByte( 'D' );
		if ( !sendToken( token ) || !sendEndOfMeta() )
			throw new IOException( "Sending token failed" );
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

	public boolean download( String destinationFile, WantRangeCallback callback )
	{
		if ( shouldGetToken() ) {
			log.error( "You didn't call getToken yet!" );
			return false;
		}
		FileRange requestedRange;
		RandomAccessFile file = null;
		try {
			try {
				file = new RandomAccessFile( new File( destinationFile ), "rw" );
			} catch ( FileNotFoundException e2 ) {
				log.error( "Cannot open " + destinationFile + " for writing." );
				return false;
			}
			while ( ( requestedRange = callback.get() ) != null ) {
				if ( requestedRange.startOffset < 0 || requestedRange.startOffset >= requestedRange.endOffset ) {
					log.error( "Callback supplied bad range (" + requestedRange.startOffset + " to " + requestedRange.endOffset + ")" );
					return false;
				}
				// Send range request
				if ( !sendRange( requestedRange.startOffset, requestedRange.endOffset ) || !sendEndOfMeta() ) {
					log.error( "Could not send next range request, download failed." );
					return false;
				}
				// See if remote peer acknowledges range request
				MetaData meta = readMetaData();
				if ( meta == null ) {
					log.error( "Did not receive meta data from uploading remote peer after requesting range, aborting." );
					return false;
				}
				FileRange remoteRange = meta.getRange();
				if ( remoteRange == null || !remoteRange.equals( requestedRange ) ) {
					log.error( "Confirmed range by remote peer does not match requested range, aborting download." );
					return false;
				}
				// Receive requested range
				int chunkLength = requestedRange.getLength();
				byte[] incoming = new byte[ 500000 ]; // 500kb
				int hasRead = 0;
				try {
					file.seek( requestedRange.startOffset );
				} catch ( IOException e1 ) {
					log.error( "Could not seek to " + requestedRange.startOffset + " in " + destinationFile + ". Disk full?" );
					return false;
				}
				while ( hasRead < chunkLength ) {
					int ret;
					try {
						ret = dataFromServer.read( incoming, 0, Math.min( chunkLength - hasRead, incoming.length ) );
					} catch ( IOException e ) {
						log.error( "Could not read payload from socket" );
						sendErrorCode( "payload read error" );
						return false;
					}
					if ( ret == -1 ) {
						log.info( "Remote peer unexpectedly closed the connection." );
						return false;
					}
					hasRead += ret;
					try {
						file.write( incoming, 0, ret );
					} catch ( IOException e ) {
						log.error( "Could not write to " + destinationFile + ". Disk full?" );
						return false;
					}
				}
			}
			sendDone();
			sendEndOfMeta();
		} finally {
			if ( file != null )
				try {
					file.close();
				} catch ( IOException e ) {
				}
			this.close( null );
		}
		return true;
	}

}
