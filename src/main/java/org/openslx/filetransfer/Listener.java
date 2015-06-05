package org.openslx.filetransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.log4j.Logger;

public class Listener
{
	private final IncomingEvent incomingEvent;
	private final SSLContext context;
	private final int port;
	private ServerSocket listenSocket = null;
	private Thread acceptThread = null;

	private static final byte U = 85; // hex - code 'U' = 85.
	private static final byte D = 68; // hex - code 'D' = 68.
	private static Logger log = Logger.getLogger( Listener.class );

	/***********************************************************************/
	/**
	 * File transfer listener. This is the active side, opening a port and
	 * waiting for incoming connections.
	 * 
	 * @param e the event handler for incoming connections
	 * @param context the SSL context used for encryption; if null, unencrypted connections will be
	 *           used
	 * @param port port to listen on
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
	private boolean listen()
	{
		try {
			if ( this.context == null ) {
				listenSocket = new ServerSocket( this.port );
			} else {
				SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
				listenSocket = sslServerSocketFactory.createServerSocket( this.port );
			}
		} catch ( Exception e ) {
			log.error( "Cannot listen on port " + this.port );
			return false;
		}
		return true;
	}

	private void run()
	{
		final Listener instance = this;
		acceptThread = new Thread() {
			@Override
			public void run()
			{
				try {
					while ( !isInterrupted() ) {
						Socket connectionSocket = null;
						try {
							connectionSocket = listenSocket.accept();
							connectionSocket.setSoTimeout( 2000 ); // 2 second timeout enough? Maybe even use a small thread pool for handling accepted connections

							byte[] b = new byte[ 1 ];
							int length = connectionSocket.getInputStream().read( b );

							connectionSocket.setSoTimeout( 10000 );

							log.debug( "Length (Listener): " + length );

							if ( b[0] == U ) {
								log.debug( "recognized U --> starting Downloader" );
								// --> start Downloader(socket).
								Downloader d = new Downloader( connectionSocket );
								incomingEvent.incomingUploadRequest( d );
							}
							else if ( b[0] == D ) {
								log.debug( "recognized D --> starting Uploader" );
								// --> start Uploader(socket).
								Uploader u = new Uploader( connectionSocket );
								incomingEvent.incomingDownloadRequest( u );
							}
							else {
								log.debug( "Got invalid option ... close connection" );
								connectionSocket.close();
							}
						} catch ( IOException e ) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} finally {
					synchronized ( instance ) {
						Transfer.safeClose( listenSocket );
						listenSocket = null;
					}
				}
			}
		};
		acceptThread.start();
		log.info( "Starting to accept " + ( this.context == null ? "UNENCRYPTED" : "encrypted" ) + " connections on port " + this.port );
	}

	public int getPort()
	{
		return this.port;
	}

	/**
	 * Check whether this listener is running.
	 * 
	 * @return true if this instance is currently listening for connections and runs the accept loop.
	 */
	public synchronized boolean isRunning()
	{
		return acceptThread != null && acceptThread.isAlive() && listenSocket != null && !listenSocket.isClosed();
	}

	/**
	 * Check whether this listener was started.
	 * 
	 * @return true if this instance was started before, but might have been stopped already.
	 */
	public synchronized boolean wasStarted()
	{
		return acceptThread != null;
	}

	/**
	 * Start this listener.
	 * 
	 * @return true if the port could be openened and the accepting thread was started
	 */
	public synchronized boolean start()
	{
		if ( !this.listen() )
			return false;
		this.run();
		return true;
	}
}
