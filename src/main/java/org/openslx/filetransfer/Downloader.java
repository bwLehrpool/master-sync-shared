package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import javax.net.ssl.SSLContext;

import net.jpountz.lz4.LZ4FastDecompressor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Downloader extends Transfer
{

	private static final Logger log = LogManager.getLogger( Downloader.class );
	
	private final LZ4FastDecompressor decompressor = lz4factory.fastDecompressor();
	
	private final Lz4InStream compressedIn;
	
	/***********************************************************************/
	/**
	 * Actively initiate a connection to a remote peer for downloading.
	 * 
	 * @param host Host name or address to connect to
	 * @param port Port to connect to
	 * @throws IOException
	 */
	public Downloader( String host, int port, int readTimeoutMs, SSLContext context, String token ) throws IOException
	{
		super( host, port, readTimeoutMs, context, log );
		compressedIn = new Lz4InStream( dataFromServer );
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
		compressedIn = new Lz4InStream( dataFromServer );
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
	
	private class Lz4InStream extends InputStream
	{
		private final DataInputStream parentStream;
		
		private long compressed, uncompressed;
		
		private byte[] buffer;
		
		public Lz4InStream( DataInputStream in )
		{
			parentStream = in;
			log.info( "DeCompressor: " + decompressor.getClass().getSimpleName() );
		}

		@Override
		public int read( byte b[], int off, int len ) throws IOException
		{
			try {
				int decompressedLength = parentStream.readInt();
				int compressedLength = parentStream.readInt();
				compressed += compressedLength;
				uncompressed += decompressedLength;
				if ( decompressedLength > len ) {
					// TODO: Partial reads with buffering, if remote payload is larger than our buffer
					throw new RuntimeException( "This should never happen! ;)" );
				}
				if ( decompressedLength == compressedLength ) {
					parentStream.readFully( b, off, decompressedLength );
				} else {
					// Compressed
					if ( buffer == null || buffer.length < compressedLength ) {
						buffer = new byte[ compressedLength ];
					}
					parentStream.readFully( buffer, 0, compressedLength );
					decompressor.decompress( buffer, 0, b, off, decompressedLength );
				}
				return decompressedLength;
			} catch ( Throwable e ) {
				throw new IOException( e );
			}
		}

		@Override
		public int read() throws IOException
		{
			throw new UnsupportedOperationException( "Cant do this!" );
		}
		
		public void printStats()
		{
			if ( compressed == 0 )
				return;
			log.info( "Received bytes: " + compressed + ", decompressed bytes: " + uncompressed );
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
	@SuppressWarnings( "resource" )
	public boolean download( DataReceivedCallback dataCallback, WantRangeCallback rangeCallback )
	{
		if ( shouldGetToken() ) {
			log.error( "You didn't call getToken yet!" );
			return false;
		}
		FileRange requestedRange;
		try {
			byte[] incoming = new byte[ 500000 ];
			/* TODO once the Lz4InputStream can handle small buffer sizes / partial reads
			for ( int bufsiz = 600; bufsiz >= 100 && incoming == null; bufsiz -= 100 ) {
				try {
					incoming = new byte[ bufsiz * 1024 ];
				} catch ( OutOfMemoryError e ) {
				}
			}
			if ( incoming == null ) {
				log.error( "Could not allocate buffer for receiving." );
				return false;
			}
			*/
			while ( ( requestedRange = rangeCallback.get() ) != null ) {
				if ( requestedRange.startOffset < 0 || requestedRange.startOffset >= requestedRange.endOffset ) {
					log.error( "Callback supplied bad range (" + requestedRange.startOffset + " to " + requestedRange.endOffset + ")" );
					return false;
				}
				if ( useCompression ) {
					// Request compressed transfer
					sendUseCompression();
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
				if ( getRemoteError() != null ) {
					log.error( "Remote peer sent error: " + getRemoteError() );
					return false;
				}
				FileRange remoteRange = meta.getRange();
				if ( remoteRange == null ) {
					log.error( "Remote metadata does not contain range confirmation. " + meta );
				}
				if ( !remoteRange.equals( requestedRange ) ) {
					log.error( "Confirmed range by remote peer (" + remoteRange
							+ ") does not match requested range (" + requestedRange + "), aborting download." );
					return false;
				}
				// Receive requested range
				int chunkLength = requestedRange.getLength();
				// If the uploader sets the COMPRESS field, assume compressed chunk
				InputStream inStream = meta.peerWantsCompression() ? compressedIn : dataFromServer;
				int hasRead = 0;
				while ( hasRead < chunkLength ) {
					int ret;
					try {
						ret = inStream.read( incoming, 0, Math.min( chunkLength - hasRead, incoming.length ) );
						if ( Thread.currentThread().isInterrupted() ) {
							log.debug( "Thread interrupted in download loop" );
							return false;
						}
					} catch ( IOException e ) {
						log.error( "Could not read payload from socket", e );
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
			compressedIn.printStats();
			try {
				transferSocket.shutdownOutput();
			} catch ( Exception e ) {
			}
		} finally {
			this.close( null );
		}
		return true;
	}

}
