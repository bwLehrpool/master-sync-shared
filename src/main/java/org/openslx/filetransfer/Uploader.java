package org.openslx.filetransfer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import javax.net.ssl.SSLContext;

import net.jpountz.lz4.LZ4Compressor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Uploader extends Transfer
{

	private static final Logger log = LogManager.getLogger( Uploader.class );
	
	private final LZ4Compressor compressor = lz4factory.fastCompressor();
	
	private final Lz4OutStream compressedOut;
	
	/***********************************************************************/
	/**
	 * Actively establish upload connection to given peer.
	 * 
	 * @param host Host name or address to connect to
	 * @param port Port to connect to
	 * @param context ssl context for establishing a secure connection
	 * @throws IOException
	 */
	public Uploader( String host, int port, int readTimeoutMs, SSLContext context, String token ) throws IOException
	{
		super( host, port, readTimeoutMs, context, log );
		compressedOut = new Lz4OutStream( outStream );
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
	public Uploader( Socket socket ) throws IOException
	{
		super( socket, log );
		compressedOut = new Lz4OutStream( outStream );
	}

	/***********************************************************************/
	/**
	 * Method for sending File with filename.
	 * 
	 * @param filename
	 */
	public boolean upload( String filename )
	{
		return upload( filename, null );
	}
	
	private class Lz4OutStream extends OutputStream
	{
		
		private final DataOutputStream parentStream;
		
		private byte[] buffer;
		
		private long compressed, uncompressed;
		
		private int chunksCompressed, chunksUncompressed;
		
		public Lz4OutStream( DataOutputStream out )
		{
			parentStream = out;
			log.info( "Compressor: " + compressor.getClass().getSimpleName() );
		}

		@Override
		public void write( int b ) throws IOException
		{
			throw new UnsupportedOperationException( "Cannot do this" );
		}

		@Override
		public void write( byte[] data, int off, int decompressedLength ) throws IOException
		{
			int maxCompressedLength = compressor.maxCompressedLength( decompressedLength );
			if ( buffer == null || buffer.length < maxCompressedLength ) {
				buffer = new byte[ maxCompressedLength ];
			}
			uncompressed += decompressedLength;
			int compressedLength = compressor.compress( data, off, decompressedLength, buffer, 0, maxCompressedLength );
			parentStream.writeInt( decompressedLength );
			if ( ( compressedLength * 9 / 8 ) < decompressedLength ) {
				compressed += compressedLength;
				chunksCompressed++;
				parentStream.writeInt( compressedLength );
				parentStream.write( buffer, 0, compressedLength );
			} else {
				compressed += decompressedLength;
				chunksUncompressed++;
				parentStream.writeInt( decompressedLength );
				parentStream.write( data, off, decompressedLength );
			}
		}
		
		public void printStats()
		{
			if ( compressed == 0 )
				return;
			log.info( "Sent bytes: " + compressed + ", decompressed bytes: " + uncompressed );
			log.info( "Sent compressed: " + chunksCompressed + ", uncompressed: " + chunksUncompressed );
		}

	}

	public boolean upload( String filename, UploadStatusCallback callback )
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
				this.close( "Could not open given file for reading.", callback, true );
				return false;
			}
			while ( !Thread.currentThread().isInterrupted() ) { // Loop as long as remote peer is requesting chunks from this file
				// Read meta data of remote peer - either new range, or it's telling us it's done
				MetaData meta = readMetaData();
				if ( meta == null ) {
					this.close( "Did not get meta data from remote peer.", callback, true );
					return false;
				}
				if ( meta.isDone() ) // Download complete?
					break;
				// Not complete, so there must be another range request
				FileRange requestedRange = meta.getRange();
				if ( requestedRange == null ) {
					this.close( "Peer did not include RANGE in meta data.", callback, true );
					return false;
				}
				// Range inside file?
				try {
					if ( requestedRange.endOffset > file.length() ) {
						this.close( "Requested range is larger than file size, aborting.", callback, true );
						return false;
					}
				} catch ( IOException e ) {
					this.close( "Could not get current length of file " + filename, callback, false );
					return false;
				}
				// Seek to requested chunk
				try {
					file.seek( requestedRange.startOffset );
				} catch ( IOException e ) {
					this.close( "Could not seek to start of requested range in given file (" + requestedRange.startOffset + ")", callback, true );
					return false;
				}
				// Send confirmation of range and compression mode we're about to send
				OutputStream outStr = outStream;
				try {
					if ( meta.peerWantsCompression() && useCompression ) {
						sendUseCompression();
						outStr = compressedOut;
					}
					long ptr = file.getFilePointer();
					if ( !sendRange( ptr, ptr + requestedRange.getLength() ) || !sendEndOfMeta() ) {
						this.close( "Could not send range confirmation" );
						return false;
					}
				} catch ( IOException e ) {
					this.close( "Could not determine current position in file " + filename );
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
						this.close( "Error reading from file ", callback, true );
						return false;
					}
					if ( ret == -1 ) {
						this.close( "Error occured in Uploader.sendFile() while reading from File to send.", callback, true );
						return false;
					}
					hasRead += ret;
					try {
						outStr.write( data, 0, ret );
					} catch ( IOException e ) {
						this.close( "Sending payload failed" );
						return false;
					}
					if ( callback != null )
						callback.uploadProgress( ret );
				}
			}
		} finally {
			Transfer.safeClose( file, transferSocket );
			compressedOut.printStats();
		}
		return true;
	}

}
