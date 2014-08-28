package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

public abstract class Transfer
{
	protected final SSLSocketFactory sslSocketFactory;
	protected final SSLSocket satelliteSocket;
	protected final DataOutputStream dataToServer;
	protected final DataInputStream dataFromServer;
	protected String TOKEN = null;
	protected long[] RANGE = null;
	protected String ERROR = null;

	protected final Logger log;

	protected Transfer( String ip, int port, SSLContext context, Logger log ) throws IOException
	{
		this.log = log;
		// create socket.
		sslSocketFactory = context.getSocketFactory();

		satelliteSocket = (SSLSocket)sslSocketFactory.createSocket( ip, port );
		satelliteSocket.setSoTimeout( 2000 ); // set socket timeout.

		dataToServer = new DataOutputStream( satelliteSocket.getOutputStream() );
		dataFromServer = new DataInputStream( satelliteSocket.getInputStream() );
	}

	protected Transfer( SSLSocket socket, Logger log ) throws IOException
	{
		this.log = log;
		satelliteSocket = socket;
		dataToServer = new DataOutputStream( satelliteSocket.getOutputStream() );
		dataFromServer = new DataInputStream( satelliteSocket.getInputStream() );
		sslSocketFactory = null;
	}

	protected boolean sendRange( long startOffset, long endOffset )
	{
		if ( RANGE != null ) {
			log.warn( "Range already set!" );
			return false;
		}
		try {
			sendKeyValuePair( "RANGE", startOffset + ":" + endOffset );
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			log.info( "Socket timeout occured ... close connection." );
			this.close();
		} catch ( IOException e ) {
			e.printStackTrace();
			readMetaData();
			if ( ERROR != null ) {
				if ( ERROR == "timeout" ) {
					log.info( "Socket timeout occured ... close connection." );
					this.close();
				}
			}
			log.info( "Sending RANGE in Uploader failed..." );
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for sending token for identification from satellite to master.
	 * 
	 * @param token The token to send
	 */
	public boolean sendToken( String token )
	{
		if ( TOKEN != null ) {
			log.warn( "Trying to send token while a token is already set! Ignoring..." );
			return false;
		}
		TOKEN = token;
		try {
			sendKeyValuePair( "TOKEN", TOKEN );
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			log.info( "Socket timeout occured ... close connection." );
			this.close();
		} catch ( IOException e ) {
			e.printStackTrace();
			readMetaData();
			if ( ERROR != null ) {
				if ( ERROR == "timeout" ) {
					log.info( "Socket timeout occured ... close connection." );
					this.close();
				}
			}
			log.info( "Sending TOKEN in Downloader failed..." );
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for reading incoming token for identification.
	 * 
	 */
	public String getToken()
	{
		return TOKEN;
	}

	private boolean parseRange( String range )
	{
		if ( range == null )
			return true;
		if ( RANGE != null ) {
			log.warn( "Warning: RANGE already set when trying to parse from " + range );
			return false;
		}
		String parts[] = range.split( ":", 2 );
		long ret[] = new long[ 2 ];
		try {
			ret[0] = Long.parseLong( parts[0] );
			ret[1] = Long.parseLong( parts[1] );
		} catch ( Throwable t ) {
			log.warn( "Not parsable range: '" + range + "'" );
			return false;
		}
		if ( ret[1] <= ret[0] ) {
			log.warn( "Invalid range. Start >= end" );
			return false;
		}
		RANGE = ret;
		return true;
	}

	/***********************************************************************/
	/**
	 * Getter for beginning of RANGE.
	 * 
	 * @return
	 */
	public long getStartOfRange()
	{
		if ( RANGE != null ) {
			return RANGE[0];
		}
		return -1;
	}

	/***********************************************************************/
	/**
	 * Getter for end of RANGE.
	 * 
	 * @return
	 */
	public long getEndOfRange()
	{
		if ( RANGE != null ) {
			return RANGE[1];
		}
		return -1;
	}

	/***********************************************************************/
	/**
	 * Method for returning difference of current Range.
	 * 
	 * @return
	 */
	public int getDiffOfRange()
	{
		return (int) Math.abs( getEndOfRange() - getStartOfRange() );
	}

	/***********************************************************************/
	/**
	 * Method for reading MetaData, like TOKEN and FileRange.
	 * Split incoming bytes after first '=' and store value to specific
	 * variable.
	 * 
	 * @return true on success, false if reading failed
	 */
	public boolean readMetaData()
	{
		try {
			while ( true ) {
				byte[] incoming = new byte[ 255 ];

				// First get length.
				int retLengthByte;
				log.debug("dataFromServer.available() : " + dataFromServer.available());
				retLengthByte = dataFromServer.read( incoming, 0, 1 );
				// If .read() didn't return 1, it was not able to read first byte.
				if ( retLengthByte != 1 ) {
					log.warn( "Error occured while reading Metadata." );
					log.debug( " retLenthByte was not 1! retLengthByte = " + retLengthByte);
					this.close();
					return false;
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
						log.warn( "Error occured while reading Metadata." );
						this.close();
						return false;
					}
					hasRead += ret;
				}

				String data = new String( incoming, 0, length, StandardCharsets.UTF_8 );

				String[] splitted = data.split( "=", 2 );
				if ( splitted.length != 2 ) {
					log.warn( "Invalid key value pair received (" + data + ")" );
					continue;
				}
				if ( splitted[0].equals( "TOKEN" ) ) {
					if ( TOKEN != null ) {
						log.warn( "Received a token when a token is already set!" );
						this.close();
						return false;
					}
					TOKEN = splitted[1];
					log.debug( "TOKEN: " + TOKEN );
				}
				else if ( splitted[0].equals( "RANGE" ) ) {
					if ( !parseRange( splitted[1] ) ) {
						this.close();
						return false;
					}
					log.debug( "RANGE: '" + splitted[1] + "'" );
				}
				else if ( splitted[0].equals( "ERROR" ) ) {
					ERROR = splitted[1];
					log.debug( "ERROR: " + ERROR );
				}
			}
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			sendErrorCode( "timeout" );
			log.info( "Socket Timeout occured in Downloader." );
			this.close();
			return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			this.close();
			return false;
		}
		return true;
	}

	private void sendKeyValuePair( String key, String value ) throws IOException
	{
		byte[] data = ( key + "=" + value ).getBytes( StandardCharsets.UTF_8 );
		dataToServer.writeByte( data.length );
		dataToServer.write( data );
	}

	/***********************************************************************/
	/**
	 * Method for sending error Code to server. For example in case of wrong
	 * token, send code for wrong token.
	 * 
	 */
	public Boolean sendErrorCode( String errString )
	{
		try {
			sendKeyValuePair( "ERROR", errString );
		} catch ( IOException e ) {
			e.printStackTrace();
			this.close();
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for closing connection, if download has finished.
	 * 
	 */
	public void close()
	{
		try {
			if ( satelliteSocket != null ) {
				this.satelliteSocket.close();
			}
			if ( dataFromServer != null )
				dataFromServer.close();
			if ( dataToServer != null )
				dataToServer.close();
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
		return satelliteSocket.isConnected() && !satelliteSocket.isClosed()
				&& !satelliteSocket.isInputShutdown() && !satelliteSocket.isOutputShutdown();
	}

}
