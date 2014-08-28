package org.openslx.filetransfer;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class Listener extends Thread
{
	private IncomingEvent incomingEvent;
	private SSLContext context;
	private int port;
	final private int U = 85; // hex - code 'U' = 85.
	final private int D = 68; // hex - code 'D' = 68.

	private static Logger log = Logger.getLogger( Listener.class );

	/***********************************************************************/
	/**
	 * Constructor for class Listener, which gets an instance of IncomingEvent.
	 * 
	 * @param e
	 */
	public Listener( IncomingEvent e, SSLContext context, int port )
	{
		this.incomingEvent = e;
		this.context = context;
		this.port = port;
	}

	/***********************************************************************/
	/**
	 * Method listen, should run from Master Server. Listen for incoming
	 * connection, and start Downloader or Uploader.
	 * 
	 */
	private void listen()
	{
		SSLServerSocket welcomeSocket = null;
		try {
			SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
			welcomeSocket = (SSLServerSocket)sslServerSocketFactory.createServerSocket( this.port );

			while ( !isInterrupted() ) {
				SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
				connectionSocket.setSoTimeout( 2000 ); // 2 second timeout enough? Maybe even use a small thread pool for handling accepted connections

				byte[] b = new byte[ 1 ];
				int length = connectionSocket.getInputStream().read( b );

				log.debug( "Length (Listener): " + length );

				if ( b[0] == U ) {
					log.debug( "recognized U --> starting Downloader" );
					// --> start Downloader(socket).
					Downloader d = new Downloader( connectionSocket );
					incomingEvent.incomingDownloader( d );
				}
				else if ( b[0] == D ) {
					log.debug( "recognized D --> starting Uploader" );
					// --> start Uploader(socket).
					Uploader u = new Uploader( connectionSocket );
					incomingEvent.incomingUploader( u );
				}
				else {
					log.debug( "Got invalid option ... close connection" );
					connectionSocket.close();
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();	// same as writing to System.err.println(e.toString).
		} finally {
			try {
				welcomeSocket.close();
			} catch ( IOException e ) {
				// Nothing we can do
			}
		}
	}

	public int getPort()
	{
		return this.port;
	}

	@Override
	public void run()
	{
		try {
			this.listen();
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
