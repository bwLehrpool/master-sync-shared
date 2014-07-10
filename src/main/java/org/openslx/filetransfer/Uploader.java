package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

public class Uploader
{
	// Some member variables.
	private SSLSocketFactory sslSocketFactory;
	private SSLSocket satelliteSocket;
	private DataOutputStream dataToServer;
	private DataInputStream dataFromServer;
	private String TOKEN = null;
	private String RANGE = null;
	private String ERROR = null;

	private static Logger log = Logger.getLogger( Uploader.class );

	/***********************************************************************/
	/**
	 * Constructor for satellite uploader.
	 * Tries to connect to specific ip and port and sending type of action.
	 * 
	 * @param ip
	 * @param port
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws UnknownHostException
	 */
	public Uploader( String ip, int port, SSLContext context )
	{
		try {
			sslSocketFactory = context.getSocketFactory();

			satelliteSocket = (SSLSocket)sslSocketFactory.createSocket( ip, port );
			satelliteSocket.setSoTimeout( 2000 ); // set socket timeout.

			dataToServer = new DataOutputStream( satelliteSocket.getOutputStream() );
			dataToServer.writeByte( 'U' );
			dataFromServer = new DataInputStream( satelliteSocket.getInputStream() );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	/***********************************************************************/
	/**
	 * Constructor for master uploader.
	 * Sends back the socket for datatransfer.
	 * 
	 * @throws IOException
	 */
	public Uploader( SSLSocket socket )
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
	 * Method for sending token from satellite to master.
	 * Needfull for getting to know what should happens over connection.
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
		} catch ( SocketTimeoutException ste) {
			ste.printStackTrace();
			log.info( "Socket timeout occured ... close connection." );
			this.close();
			return false;
		} catch ( IOException e ) {
			e.printStackTrace();
			readMetaData();
			if (ERROR != null) {
				if (ERROR == "timeout") {
					log.info( "Socket timeout occured ... close connection." );
					this.close();
				}
			}
			log.info( "Sending TOKEN in Uploader failed..." );
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Getter for TOKEN.
	 */
	public String getToken()
	{
		return TOKEN;
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
			dataToServer.writeByte( 0 );
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
	 * Getter for RANGE.
	 * 
	 * @return
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
		if ( getStartOfRange() == -1 || getEndOfRange() == -1 ) {
			return -1;
		}
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
				byte[] incoming = new byte[ 255 ];

				// First get length.
				dataFromServer.read( incoming, 0, 1 );
				int length = incoming[0] & 0xFF;
				// System.out.println("length: " + length);

				if ( length == 0 ) // Stop if 0 was read.
					break;

				/**
				 * Read the next available bytes and split by '=' for
				 * getting TOKEN or RANGE.
				 */
				int hasRead = 0;
				while ( hasRead < length ) {
					int ret = dataFromServer.read( incoming, hasRead, length - hasRead );
					if ( ret == -1 ) {
						System.out.println( "Error in reading Metadata occured!" );
						return false;
					}
					hasRead += ret;
				}
				String data = new String( incoming, "UTF-8" );
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
					System.out.println( "RANGE: " + RANGE );
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
			log.info( "Socket timeout occured ... close connection" );
			this.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/***********************************************************************/
	/**
	 * Method for sending File with filename.
	 * 
	 * @param filename
	 */
	public Boolean sendFile( String filename )
	{
		RandomAccessFile file = null;
		try {
			 file = new RandomAccessFile( new File( filename ), "r" );

			if ( getStartOfRange() == -1 ) {
				file.close();
				return false;
			}
			file.seek( getStartOfRange() );

			byte[] data = new byte[ 255 ]; // TODO: problematische Größe.
			int hasRead = 0;
			int length = getDiffOfRange();
			System.out.println( "diff of Range: " + length );
			while ( hasRead < length ) {
				int ret = file.read( data, 0, Math.min( length - hasRead, data.length ) );
				if ( ret == -1 ) {
					System.out.println( "Error occured in Uploader.sendFile(),"
							+ " while reading from File to send." );
					return false;
				}
				hasRead += ret;
				dataToServer.write( data, 0, ret );
			}
			file.close();
		} catch ( SocketTimeoutException ste) {
			ste.printStackTrace();
			sendErrorCode( "timeout" );
			log.info( "Socket timeout occured ... close connection." );
			this.close();
			return false;
		} catch ( IOException ioe ) {
			ioe.printStackTrace();
			readMetaData();
			if (ERROR != null) {
				if (ERROR == "timeout") {
					log.info( "Socket timeout occured ... close connection." );
					this.close();
				}
			}
			log.info( "Sending RANGE " + getStartOfRange() + ":" + getEndOfRange() + " of File "
					+ filename + " failed..." );
			return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				file.close();
			} catch ( IOException e ) {
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
	 * Method for closing connection, if upload has finished.
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
