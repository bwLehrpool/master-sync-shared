package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

public class Downloader
{
	// Some instance variables.
	private SSLSocketFactory sslSocketFactory;
	private SSLSocket satelliteSocket;
	private DataOutputStream dataToServer;
	private DataInputStream dataFromServer;
	private String TOKEN = null;
	private String RANGE = null;
	private String outputFilename = null;
	private String ERROR = null;
	
	private static Logger log = Logger.getLogger( Downloader.class );

	/***********************************************************************/
	/**
	 * Constructor for satellite downloader.
	 * Tries to connect to specific ip and port and sending type of action.
	 * 
	 * @param ip
	 * @param port
	 */
	public Downloader( String ip, int port, SSLContext context )
	{
		try {
			// TODO: Remove old code, that's why we have git.. ;)
			// create socket.
			sslSocketFactory = context.getSocketFactory();

			satelliteSocket = (SSLSocket)sslSocketFactory.createSocket( ip, port );
			satelliteSocket.setSoTimeout( 2000 ); // set socket timeout.

			dataToServer = new DataOutputStream( satelliteSocket.getOutputStream() );
			dataToServer.writeByte( 'D' );
			dataFromServer = new DataInputStream( satelliteSocket.getInputStream() );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/***********************************************************************/
	/**
	 * Constructor for master downloader.
	 * Given parameter is the socket over which the transfer is going.
	 * 
	 * @param socket
	 */
	public Downloader( SSLSocket socket )
	{
		try {
			satelliteSocket = socket;
			dataToServer = new DataOutputStream( satelliteSocket.getOutputStream() );
			dataFromServer = new DataInputStream( satelliteSocket.getInputStream() );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
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
	 * Method for sending token for identification from satellite to master.
	 * 
	 * @param t
	 */
	public Boolean sendToken( String token )
	{
		try {
			TOKEN = token;
			String sendToken = "TOKEN=" + TOKEN;
			byte[] data = sendToken.getBytes( StandardCharsets.UTF_8 );
			dataToServer.writeByte( data.length );
			dataToServer.write( data );
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			log.info( "Socket timeout occured ... close connection." );
			this.close();
		} catch ( IOException e ) {
			e.printStackTrace();
			readMetaData();
			if (ERROR != null) {
				if (ERROR == "timeout") {
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
	 * Method to send range of the file, which should be uploaded.
	 * Helpful for knowing how much was already uploaded if
	 * connection aborts.
	 * 
	 * @param a
	 * @param b
	 */
	public Boolean sendRange( int a, int b )
	{
		try {
			RANGE = a + ":" + b;
			String sendRange = "RANGE=" + RANGE;
			byte[] data = sendRange.getBytes( StandardCharsets.UTF_8 );
			dataToServer.writeByte( data.length );
			dataToServer.write( data );
		} catch ( SocketTimeoutException ste ) {
			ste.printStackTrace();
			log.info( "Socket timeout occured ... close connection." );
			this.close();
		} catch ( IOException e ) {
			e.printStackTrace();
			readMetaData();
			if (ERROR != null) {
				if (ERROR == "timeout") {
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
	 * Method for reading incoming token for identification.
	 * 
	 */
	public String getToken()
	{
		return TOKEN;
	}

	/***********************************************************************/
	/**
	 * Method for reading range of file, which is downloaded.
	 * Helpful for knowing how much is already downloaded if connection aborts.
	 */
	public String getRange()
	{
		return RANGE;
	}

	/***********************************************************************/
	/**
	 * Getter for beginning of RANGE.
	 * 
	 * @return
	 */
	public int getStartOfRange()
	{
		if ( RANGE != null ) {
			String[] splitted = RANGE.split( ":" );
			return Integer.parseInt( splitted[0] );
		}
		return -1;
	}

	/***********************************************************************/
	/**
	 * Getter for end of RANGE.
	 * 
	 * @return
	 */
	public int getEndOfRange()
	{
		if ( RANGE != null ) {
			String[] splitted = RANGE.split( ":" );
			return Integer.parseInt( splitted[1] );
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
		int diff = Math.abs( getEndOfRange() - getStartOfRange() );
		return diff;
	}

	/***********************************************************************/
	/**
	 * Method for reading MetaData, like TOKEN and FileRange.
	 * Split incoming bytes after first '=' and store value to specific
	 * variable.
	 * 
	 */
	public Boolean readMetaData()
	{
		try {
			while ( true ) {
				byte[] incoming = new byte[ 255 ]; // TODO: problematische Größe.

				// First get length.
				dataFromServer.read( incoming, 0, 1 );
				int length = incoming[0];
				System.out.println( "length (downloader): " + length );

				if ( length == 0 )
					break;

				/**
				 * Read the next available bytes and split by '=' for
				 * getting TOKEN or RANGE.
				 */
				int hasRead = 0;
				while ( hasRead < length ) {
					int ret = dataFromServer.read( incoming, hasRead, length - hasRead );
					if ( ret == -1 ) {
						System.out.println( "Error occured while reading Metadata." );
						return false;
					}
					hasRead += ret;
				}

				String data = new String( incoming, 0, length, "UTF-8" );
				// System.out.println(data);

				String[] splitted = data.split( "=" );
				// System.out.println("splitted[0]: " + splitted[0]);
				// System.out.println("splitted[1]: " + splitted[1]);
				if ( splitted[0] != null && splitted[0].equals( "TOKEN" ) ) {
					if ( splitted[1] != null )
						TOKEN = splitted[1];
					System.out.println( "TOKEN: " + TOKEN );
				}
				else if ( splitted[0].equals( "RANGE" ) ) {
					if ( splitted[1] != null )
						RANGE = splitted[1];
					System.out.println( "RANGE: '" + RANGE + "'" );
				}
				else if ( splitted[0].equals( "ERROR" ) ) {
					if ( splitted[1] != null )
						ERROR = splitted[1];
					System.err.println( "ERROR: " + ERROR );
					return false;
				}
			}
		} catch (SocketTimeoutException ste) {
			ste.printStackTrace();
			sendErrorCode("timeout");
			log.info( "Socket Timeout occured in Downloader." );
			this.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for reading Binary. Reading the current Range of incoming binary.
	 * 
	 */
	public Boolean readBinary()
	{
		RandomAccessFile file = null;
		try {
			int length = getDiffOfRange();
			byte[] incoming = new byte[ 4000 ]; // TODO: größe Problematisch, abchecken.

			int hasRead = 0;
			file = new RandomAccessFile( new File( outputFilename ), "rw" );
			file.seek( getStartOfRange() );
			while ( hasRead < length ) {
				int ret = dataFromServer.read( incoming, 0, Math.min( length - hasRead, incoming.length ) );
				if ( ret == -1 ) {
					log.info( "Error occured in Downloader.readBinary(),"
							+ " while reading binary." );
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
		} catch ( Exception e ) {
			e.printStackTrace();
			log.info( "Reading RANGE " + getStartOfRange() + ":" + getEndOfRange()
					+ " of file failed..." );
			return false;
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
		return true;
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
			String sendError = "ERROR=" + errString;
			byte[] data = sendError.getBytes( StandardCharsets.UTF_8 );
			dataToServer.writeByte( data.length );
			dataToServer.write( data );
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
			this.satelliteSocket.close();
			if (dataFromServer != null) dataFromServer.close();
			if (dataToServer != null) dataToServer.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
