package org.openslx.filetransfer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

// TODO: (all files) apply formatting using strg+shift+f *after* importing scheme from ./extras/

public class Listener extends Thread
{
	private IncomingEvent incomingEvent;
	/*
	private static String pathToKeyStore =
			"/home/bjoern/javadev/DataTransfer/mySrvKeyStore.jks";
			*/
	private SSLContext context;
	private int port;

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
	 * @throws Exception
	 */
	private void listen()
	{ 
		try {
   		SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
   		SSLServerSocket welcomeSocket =
   				(SSLServerSocket)sslServerSocketFactory.createServerSocket( this.port );
   
   		while ( !isInterrupted() ) {
   			SSLSocket connectionSocket = (SSLSocket)welcomeSocket.accept();
   			connectionSocket.setSoTimeout( 2000 ); // 2 second timeout enough? Maybe even use a small thread pool for handling accepted connections
   			// TODO: Handle SocketTimeoutException for all reads and writes in Downloader and Uploader
   
   			byte[] b = new byte[ 1 ];
   			int length = connectionSocket.getInputStream().read( b );
   
   			System.out.println( length );
   
   			// Ascii - Code: 'U' = 85 ; 'D' = 68. TODO: byte constant as class member
   			if ( b[0] == 85 ) {
   				System.out.println( "U erkannt --> Downloader starten" ); // TODO: Use Logger (see masterserver code for example)
   				// --> start Downloader(socket).
   				Downloader d = new Downloader( connectionSocket );
   				incomingEvent.incomingDownloader( d );
   			}
   			else if ( b[0] == 68 ) {
   				System.out.println( "D erkannt --> Uploader starten" );
   				// --> start Uploader(socket).
   				Uploader u = new Uploader( connectionSocket );
   				incomingEvent.incomingUploader( u );
   			}
   			else {
   				System.out.println( "Müll empfangen" );
   				connectionSocket.close();
   			}
   		}
		} catch (Exception e) {
			e.printStackTrace();	// same as writing to System.err.println(e.toString).
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
