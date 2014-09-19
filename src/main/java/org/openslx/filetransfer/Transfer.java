package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

public abstract class Transfer
{
	protected final SSLSocket transferSocket;
	protected final DataOutputStream outStream;
	protected final DataInputStream dataFromServer;
	protected String ERROR = null;
	private boolean shouldGetToken;

	protected final Logger log;

	/**
	 * Actively initiated transfer.
	 * 
	 * @param host Remote Host
	 * @param port Remote Port
	 * @param context SSL Context for encryption
	 * @param log Logger to use
	 * @throws IOException
	 */
	protected Transfer( String host, int port, SSLContext context, Logger log ) throws IOException
	{
		this.log = log;
		// create socket.
		SSLSocketFactory sslSocketFactory = context.getSocketFactory();

		transferSocket = (SSLSocket)sslSocketFactory.createSocket();
		transferSocket.setSoTimeout( 5000 ); // set socket timeout.
		transferSocket.connect( new InetSocketAddress( host, port ) );

		outStream = new DataOutputStream( transferSocket.getOutputStream() );
		dataFromServer = new DataInputStream( transferSocket.getInputStream() );
		shouldGetToken = false;
	}

	/**
	 * Passive transfer through incoming connection.
	 * 
	 * @param socket already connected socket to remote peer
	 * @param log Logger to use
	 * @throws IOException
	 */
	protected Transfer( SSLSocket socket, Logger log ) throws IOException
	{
		this.log = log;
		transferSocket = socket;
		outStream = new DataOutputStream( transferSocket.getOutputStream() );
		dataFromServer = new DataInputStream( transferSocket.getInputStream() );
		shouldGetToken = true;
	}

	protected boolean sendRange( long startOffset, long endOffset )
	{
		try {
			log.debug( "Sending range: " + startOffset + " to " + endOffset );
			sendKeyValuePair( "RANGE", startOffset + ":" + endOffset );
		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for sending error Code to server. For example in case of wrong
	 * token, send code for wrong token.
	 * 
	 */
	public boolean sendErrorCode( String errString )
	{
		try {
			sendKeyValuePair( "ERROR", errString );
		} catch ( IOException e ) {
			e.printStackTrace();
			this.close( e.toString() );
			return false;
		}
		return true;
	}

	protected boolean sendToken( String token )
	{
		try {
			sendKeyValuePair( "TOKEN", token );
		} catch ( IOException e ) {
			e.printStackTrace();
			this.close( e.toString() );
			return false;
		}
		return true;
	}

	public boolean sendDone()
	{
		try {
			sendKeyValuePair( "DONE", "" );
		} catch ( IOException e ) {
			e.printStackTrace();
			this.close( e.toString() );
			return false;
		}
		return true;
	}

	protected boolean sendEndOfMeta()
	{
		try {
			outStream.writeByte( 0 );
		} catch ( SocketTimeoutException e ) {
			log.error( "Error sending end of meta - socket timeout" );
			return false;
		} catch ( IOException e ) {
			log.error( "Error sending end of meta - " + e.toString() );
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for reading MetaData, like TOKEN and FileRange.
	 * Split incoming bytes after first '=' and store value to specific
	 * variable.
	 * 
	 * @return map of meta data received, null on error
	 */
	protected MetaData readMetaData()
	{
		Map<String, String> entries = new HashMap<>();
		try {
			while ( true ) {
				byte[] incoming = new byte[ 255 ];

				// First get length.
				int retLengthByte;
				retLengthByte = dataFromServer.read( incoming, 0, 1 );
				// If .read() didn't return 1, it was not able to read first byte.
				if ( retLengthByte != 1 ) {
					log.debug( " retLenthByte was not 1! retLengthByte = " + retLengthByte );
					this.close( "Error occured while reading Metadata." );
					return null;
				}

				int length = incoming[0] & 0xFF;
				log.debug( "length (downloader): " + length );

				if ( length == 0 )
					break;

				/*
				 * Read the next available bytes and split by '=' for
				 * getting TOKEN or RANGE.
				 */
				int hasRead = 0;
				while ( hasRead < length ) {
					int ret = dataFromServer.read( incoming, hasRead, length - hasRead );
					if ( ret == -1 ) {
						this.close( "Error occured while reading Metadata." );
						return null;
					}
					hasRead += ret;
				}

				String data = new String( incoming, 0, length, StandardCharsets.UTF_8 );

				String[] splitted = data.split( "=", 2 );
				if ( splitted.length != 2 ) {
					log.warn( "Invalid key value pair received (" + data + ")" );
					continue;
				}
				if ( splitted[0].equals( "ERROR" ) )
					ERROR = splitted[1];
				if ( entries.containsKey( splitted[0] ) ) {
					log.warn( "Received meta data key " + splitted[0] + " when already received, ignoring!" );
				} else {
					entries.put( splitted[0], splitted[1] );
				}
			}
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			sendErrorCode( "timeout" );
			this.close( "Socket Timeout occured in readMetaData." );
			return null;
		} catch ( Exception e ) {
			e.printStackTrace();
			this.close( e.toString() );
			return null;
		}
		return new MetaData( entries );
	}

	private void sendKeyValuePair( String key, String value ) throws IOException
	{
		byte[] data = ( key + "=" + value ).getBytes( StandardCharsets.UTF_8 );
		try {
			outStream.writeByte( data.length );
			outStream.write( data );
		} catch ( SocketTimeoutException e ) {
			log.warn( "Socket timeout when sending KVP with key " + key );
		}
	}

	/***********************************************************************/
	/**
	 * Method for closing connection, if download has finished.
	 * 
	 */
	public void close( String error )
	{
		if ( error != null )
			log.info( error );
		try {
			if ( transferSocket != null )
				this.transferSocket.close();
			if ( dataFromServer != null )
				dataFromServer.close();
			if ( outStream != null )
				outStream.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns whether this transfer/connection is considered valid or usable,
	 * which means the socket is still properly connected to the remote peer.
	 * 
	 * @return true or false
	 */
	public boolean isValid()
	{
		return transferSocket.isConnected() && !transferSocket.isClosed()
				&& !transferSocket.isInputShutdown() && !transferSocket.isOutputShutdown();
	}

	/**
	 * Get error string received from remote side, if any.
	 * 
	 * @return Error string, if received, or null.
	 */
	public String getRemoteError()
	{
		return ERROR;
	}

	/**
	 * Get transfer token, sent by remote peer that initiated connection.
	 * Call this ONLY if all of the following conditions are met:
	 * - this is an incoming transfer connection
	 * - you didn't call it before
	 * - you didn't call download or upload yet
	 * 
	 * @return The transfer token
	 */
	public String getToken()
	{
		if ( !shouldGetToken ) {
			log.error( "Invalid call of getToken. You either initiated the connection yourself, or you already called getToken before." );
			this.close( null );
			return null;
		}
		shouldGetToken = false;
		MetaData meta = readMetaData();
		if ( meta == null )
			return null;
		return meta.getToken();
	}

	/**
	 * Should we call getToken()? Used internally for detecting wrong usage of
	 * the transfer classes.
	 * 
	 * @return yes or no
	 */
	protected boolean shouldGetToken()
	{
		return shouldGetToken;
	}

	/**
	 * High level access to key-value-pairs.
	 */
	class MetaData
	{

		private Map<String, String> meta;

		private MetaData( Map<String, String> meta )
		{
			this.meta = meta;
		}

		/**
		 * Get transfer token, sent by remote peer that initiated connection.
		 * 
		 * @return The transfer token
		 */
		public String getToken()
		{
			return meta.get( "TOKEN" );
		}

		/**
		 * Check if remote peer set the DONE key, telling us the transfer is complete.
		 * 
		 * @return yes or no
		 */
		public boolean isDone()
		{
			return meta.containsKey( "DONE" );
		}

		/**
		 * Return range from this meta data class, or null
		 * if it doesn't contain a (valid) range key-value-pair.
		 * 
		 * @return The range instance
		 */
		public FileRange getRange()
		{
			if ( meta.containsKey( "RANGE" ) )
				return parseRange( meta.get( "RANGE" ) );
			return null;
		}

		/**
		 * Parse range in format START:END to {@link FileRange} instance.
		 * 
		 * @param range String representation of range
		 * @return {@link FileRange} instance of range, or null on error
		 */
		private FileRange parseRange( String range )
		{
			if ( range == null )
				return null;
			String parts[] = range.split( ":" );
			if ( parts.length != 2 )
				return null;
			long start, end;
			try {
				start = Long.parseLong( parts[0] );
				end = Long.parseLong( parts[1] );
			} catch ( Throwable t ) {
				log.warn( "Not parsable range: '" + range + "'" );
				return null;
			}
			if ( start >= end ) {
				log.warn( "Invalid range. Start >= end" );
				return null;
			}
			return new FileRange( start, end );
		}

	}

}
