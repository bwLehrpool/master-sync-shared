package org.openslx.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import javax.net.ssl.SSLContext;

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
	protected Downloader( Socket socket ) throws IOException
	{
		super( socket, log );
	}

	/**
	 * Initiate the download. This method does not return until the file transfer finished.
	 * 
	 * @param destinationFile destination file name to download to
	 * @param rangeCallback this object's .get() method is called whenever the downloader needs to
	 *           know which part of the file to request next. This method should return null if no
	 *           more parts are needed, which in turn let's this method return true
	 * @return true on success, false otherwise
	 */
	public boolean download( final String destinationFile, final WantRangeCallback callback )
	{
		RandomAccessFile file = null;
		try {
			try {
				file = new RandomAccessFile( new File( destinationFile ), "rw" );
			} catch ( FileNotFoundException e2 ) {
				log.error( "Cannot open " + destinationFile + " for writing." );
				return false;
			}
			final RandomAccessFile f = file;
			DataReceivedCallback cb = new DataReceivedCallback() {
				public boolean dataReceived( final long fileOffset, final int dataLength, final byte[] data )
				{
					try {
						f.seek( fileOffset );
						f.write( data, 0, dataLength );
					} catch ( Exception e ) {
						log.error( "Could not write to file " + destinationFile + " at offset " + fileOffset, e );
						return false;
					}
					return true;
				}
			};
			return download( cb, callback );
		} finally {
			Transfer.safeClose( file );
		}
	}

	/**
	 * Initiate the download. This method does not return until the file transfer finished.
	 * 
	 * @param dataCallback this object's .dataReceived() method is called whenever a chunk of data is
	 *           received
	 * @param rangeCallback this object's .get() method is called whenever the downloader needs to
	 *           know which part of the file to request next. This method should return null if no
	 *           more parts are needed, which in turn let's this method return true
	 * @return true on success, false otherwise
	 */
	public boolean download( DataReceivedCallback dataCallback, WantRangeCallback rangeCallback )
	{
		if ( shouldGetToken() ) {
			log.error( "You didn't call getToken yet!" );
			return false;
		}
		FileRange requestedRange;
		try {
			while ( ( requestedRange = rangeCallback.get() ) != null ) {
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
					if ( !dataCallback.dataReceived( requestedRange.startOffset + hasRead, ret, incoming ) ) {
						this.close( "Aborting due to I/O error..." );
						return false;
					}
					hasRead += ret;
				}
			}
			sendDone();
			sendEndOfMeta();
		} finally {
			this.close( null );
		}
		return true;
	}

}
