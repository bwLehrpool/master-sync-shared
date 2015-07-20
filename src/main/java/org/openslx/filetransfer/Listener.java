package org.openslx.filetransfer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
	private final int readTimeoutMs;

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
	 * @param timeoutMs socket timeout for accepted connections
	 */
	public Listener( IncomingEvent e, SSLContext context, int port, int readTimeoutMs )
	{
		this.incomingEvent = e;
		this.context = context;
		this.port = port;
		this.readTimeoutMs = readTimeoutMs;
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
				listenSocket = new ServerSocket();
			} else {
				SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
				listenSocket = sslServerSocketFactory.createServerSocket();
			}
			listenSocket.setReuseAddress( true );
			listenSocket.bind( new InetSocketAddress( this.port ) );
		} catch ( Exception e ) {
			log.error( "Cannot listen on port " + this.port, e );
			return false;
		}
		return true;
	}

	private void run()
	{
		final Listener instance = this;
		acceptThread = new Thread( "BFTP-Listen-" + this.port ) {
			@Override
			public void run()
			{
				try {
					while ( !isInterrupted() ) {
						Socket connectionSocket = null;
						try {
							connectionSocket = listenSocket.accept();
						} catch ( SocketTimeoutException e ) {
							continue;
						} catch ( Exception e ) {
							log.warn( "Some exception when accepting! Trying to resume...", e );
							Transfer.safeClose( listenSocket );
							if ( !listen() ) {
								log.error( "Could not re-open listening socket" );
								break;
							}
							continue;
						}
						try {
							connectionSocket.setSoTimeout( 2000 ); // 2 second timeout enough? Maybe even use a small thread pool for handling accepted connections

							byte[] b = new byte[ 1 ];
							int length = connectionSocket.getInputStream().read( b );
							if ( length == -1 )
								continue;

							connectionSocket.setSoTimeout( readTimeoutMs );

							if ( b[0] == U ) {
								// --> start Downloader(socket).
								Downloader d = new Downloader( connectionSocket );
								incomingEvent.incomingUploadRequest( d );
							}
							else if ( b[0] == D ) {
								// --> start Uploader(socket).
								Uploader u = new Uploader( connectionSocket );
								incomingEvent.incomingDownloadRequest( u );
							}
							else {
								log.debug( "Got invalid init-byte ... close connection" );
								connectionSocket.close();
							}
						} catch ( Exception e ) {
							log.warn( "Error accepting client", e );
							Transfer.safeClose( connectionSocket );
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
		acceptThread.setDaemon( true );
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
