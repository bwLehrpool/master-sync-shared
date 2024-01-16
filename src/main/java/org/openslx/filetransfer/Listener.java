package org.openslx.filetransfer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.util.PrioThreadFactory;

public class Listener
{
	private final IncomingEvent incomingEvent;
	private final SSLContext context;
	private final int port;
	private ServerSocket listenSocket = null;
	private Thread acceptThread = null;
	private final int readTimeoutMs;
	private final ExecutorService processingPool = new ThreadPoolExecutor( 0, 8, 5, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),
			new PrioThreadFactory( "BFTP-BS" ) );

	private static final byte CONNECTING_PEER_WANTS_TO_UPLOAD = 85; // ASCII 'U' = 85.
	private static final byte CONNECTING_PEER_WANTS_TO_DOWNLOAD = 68; // ASCII 'D' = 68.
	private static Logger log = LogManager.getLogger( Listener.class );

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
	private synchronized boolean listen()
	{
		if ( listenSocket != null )
			return true;
		try {
			if ( this.context == null ) {
				listenSocket = new ServerSocket();
			} else {
				SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
				listenSocket = sslServerSocketFactory.createServerSocket();
			}
			listenSocket.setSoTimeout( 5000 );
			listenSocket.setReuseAddress( true );
			listenSocket.bind( new InetSocketAddress( this.port ) );
			listenSocket.setSoTimeout( 0 );
		} catch ( Exception e ) {
			log.error( "Cannot listen on port " + this.port, e );
			listenSocket = null;
			return false;
		}
		return true;
	}

	private synchronized void run()
	{
		if ( acceptThread != null )
			return;
		final Listener instance = this;
		acceptThread = new Thread( "BFTP:" + this.port ) {
			@Override
			public void run()
			{
				try {
					// Run accept loop in own thread
					while ( !isInterrupted() ) {
						final Socket connection;
						try {
							connection = listenSocket.accept();
						} catch ( SocketTimeoutException e ) {
							continue;
						} catch ( Exception e ) {
							log.warn( "Some exception when accepting! Trying to resume...", e );
							Transfer.safeClose( listenSocket );
							listenSocket = null;
							if ( !listen() ) {
								log.error( "Could not re-open listening socket" );
								break;
							}
							continue;
						}
						// Handle each accepted connection in a thread pool
						Runnable handler = new Runnable() {
							@Override
							public void run()
							{
								try {
									// Give initial byte signaling mode of operation 5 secs to arrive
									connection.setSoTimeout( 5000 );

									byte[] b = new byte[ 1 ];
									int length = connection.getInputStream().read( b );
									if ( length == -1 ) {
										Transfer.safeClose( connection );
										return;
									}
									// Byte arrived, now set desired timeout
									connection.setSoTimeout( readTimeoutMs );

									if ( b[0] == CONNECTING_PEER_WANTS_TO_UPLOAD ) {
										// --> start Downloader(socket).
										Downloader d = new Downloader( connection );
										// Will take care of connection cleanup
										incomingEvent.incomingUploadRequest( d );
									} else if ( b[0] == CONNECTING_PEER_WANTS_TO_DOWNLOAD ) {
										// --> start Uploader(socket).
										Uploader u = new Uploader( connection );
										// Will take care of connection cleanup
										incomingEvent.incomingDownloadRequest( u );
									} else {
										log.debug( "Got invalid init-byte ... closing connection" );
										Transfer.safeClose( connection );
									}
								} catch ( SSLException e ) {
									Transfer.safeClose( connection );
									log.warn( "SSL error when acceping client " + connection.getInetAddress().getHostAddress() );
								} catch ( SocketException e ) {
									// No reason to log, probably - connection where client did nothing after connecting.
								} catch ( Exception e ) {
									Transfer.safeClose( connection );
									log.warn( "Error handling client", e );
								}
							}
						};
						try {
							processingPool.execute( handler );
						} catch ( RejectedExecutionException e ) {
							Transfer.safeClose( connection );
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
