package org.openslx.filetransfer;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.Logger;
import org.openslx.util.Util;

import net.jpountz.lz4.LZ4Factory;

public abstract class Transfer
{
	protected final Socket transferSocket;
	protected final DataOutputStream outStream;
	protected final DataInputStream dataFromServer;
	private String remoteError;
	private boolean shouldGetToken;
	protected boolean useCompression = true;

	protected final Logger log;

	protected final static LZ4Factory lz4factory = LZ4Factory.fastestInstance();

	/**
	 * Actively initiated transfer.
	 * 
	 * @param host Remote Host
	 * @param port Remote Port
	 * @param context SSL Context for encryption, null if plain
	 * @param log Logger to use
	 * @throws IOException
	 */
	protected Transfer( String host, int port, int readTimeoutMs, SSLContext context, Logger log ) throws IOException
	{
		this.log = log;
		// create socket.
		transferSocket = Util.connectAllRecords(
				context == null ? SocketFactory.getDefault() : context.getSocketFactory(),
				host, port, 4000 );
		transferSocket.setSoTimeout( readTimeoutMs );

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
	protected Transfer( Socket socket, Logger log ) throws IOException
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
			sendKeyValuePair( "RANGE", startOffset + ":" + endOffset );
		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected void sendUseCompression()
	{
		try {
			sendKeyValuePair( "COMPRESS", "true" );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
			sendEndOfMeta();
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

	public void sendDoneAndClose()
	{
		sendDone();
		sendEndOfMeta();
		close( "Transfer finished" );
	}

	protected boolean sendDone()
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
			outStream.writeShort( 0 );
			outStream.flush();
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
				String data = dataFromServer.readUTF();

				if ( data == null || data.length() == 0 )
					break; // End of meta data

				String[] splitted = data.split( "=", 2 );
				if ( splitted.length != 2 ) {
					log.warn( "Invalid key value pair received (" + data + ")" );
					continue;
				}
				if ( splitted[0].equals( "ERROR" ) )
					remoteError = splitted[1];
				if ( entries.containsKey( splitted[0] ) ) {
					log.warn( "Received meta data key " + splitted[0] + " when already received, ignoring!" );
				} else {
					entries.put( splitted[0], splitted[1] );
				}
			}
		} catch ( SocketTimeoutException ste ) {
			sendErrorCode( "timeout" );
			this.close( "Socket Timeout occured in readMetaData." );
			return null;
		} catch ( Exception e ) {
			this.close( "Exception occured in readMetaData: " + e.toString() );
			return null;
		}
		return new MetaData( entries );
	}

	private void sendKeyValuePair( String key, String value ) throws IOException
	{
		if ( outStream == null )
			return;
		try {
			outStream.writeUTF( key + "=" + value );
		} catch ( Exception e ) {
			this.close( e.getClass().getSimpleName() + " when sending KVP with key " + key );
		}
	}

	/***********************************************************************/
	/**
	 * Method for closing connection, if download has finished.
	 * 
	 */
	protected void close( String error, UploadStatusCallback callback, boolean sendToPeer )
	{
		close( error, callback, sendToPeer, null );
	}

	protected void close( String error, UploadStatusCallback callback, boolean sendToPeer, Exception e )
	{
		if ( error != null ) {
			if ( sendToPeer )
				sendErrorCode( error );
			if ( callback != null )
				callback.uploadError( error );
			log.info( "Closing with error '" + error + "'", e );
		}
		synchronized ( transferSocket ) {
			safeClose( dataFromServer, outStream, transferSocket );
		}
	}

	protected void close( String error )
	{
		close( error, null );
	}

	protected void close( String error, Exception e )
	{
		close( error, null, false, e );
	}

	public void cancel()
	{
		synchronized ( transferSocket ) {
			try {
				transferSocket.shutdownOutput();
			} catch ( Exception e ) {
				// Silence
			}
			try {
				transferSocket.shutdownInput();
			} catch ( Exception e ) {
				// Silence
			}
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
		synchronized ( transferSocket ) {
			return transferSocket.isConnected() && !transferSocket.isClosed()
					&& !transferSocket.isInputShutdown() && !transferSocket.isOutputShutdown();
		}
	}

	/**
	 * Get error string received from remote side, if any.
	 * 
	 * @return Error string, if received, or null.
	 */
	public String getRemoteError()
	{
		return remoteError;
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
		if (meta.peerWantsCompression()) {
			useCompression = true;
		}
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
	 * Close given stream, socket, anything closeable.
	 * Never throws any exception, if it's not closeable there's
	 * not much else we can do.
	 * 
	 * @param list one or more closeables. Pass one, many, or an array
	 */
	static protected void safeClose( Closeable... list )
	{
		Util.safeClose( list );
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

		/**
		 * Peer indicated that it wants to use snappy compression.
		 */
		public boolean peerWantsCompression()
		{
			return meta.containsKey( "COMPRESS" );
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			for ( Entry<String, String> it : meta.entrySet() ) {
				if ( sb.length() != 0 ) {
					sb.append( ' ' );
				}
				sb.append( it.getKey() );
				sb.append( '=' );
				sb.append( it.getValue() );
			}
			return sb.toString();
		}

	}

}
