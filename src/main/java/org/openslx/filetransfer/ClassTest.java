/**
 * File transfer between master server and satellite server.
 * The connection should always be from satellite to master, because of
 * open port knowledge on master server.
 * 
 * For uploading file to master, satellite should send a request with
 * token "U" for want upload. --> start Uploader(IP, PORT).
 * 
 * For downloading a file from master, satellite should send a request
 * with token "D" for want download. --> start Downloader(IP, PORT).
 * 
 * Means the master server has to start the opposite part:
 * If master receives token "U" --> start Downloader(socket)
 * If master receives token "D" --> start Uploader(socket)
 */

package org.openslx.filetransfer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;

public class ClassTest
{
	
	private static final int CHUNK_SIZE = 11111111;

	private static String inFile;
	private static String outFile;

	static {
		// This is a temporary workaround for this annoying log4j error msg.
		// Initializing the logger before anything else is done.
		BasicConfigurator.configure();
		LoggerFactory.getLogger( "ROOT" );
	}

	public static void main( String[] args ) throws Exception
	{
		if ( args.length != 4 ) {
			System.out.println( "Need 4 argument: <keystore> <passphrase> <infile> <outfile>" );
			System.exit( 1 );
		}
		String pathToKeyStore = args[0];
		final char[] passphrase = args[1].toCharArray();
		inFile = args[2];
		outFile = args[3];
		KeyStore keystore = KeyStore.getInstance( "JKS" );
		keystore.load( new FileInputStream( pathToKeyStore ), passphrase );
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
		kmf.init( keystore, passphrase );
		SSLContext context = SSLContext.getInstance( "SSLv3" );
		KeyManager[] keyManagers = kmf.getKeyManagers();

		context.init( keyManagers, null, null );

		Listener listener = new Listener( new Test(), context, 6789 );
		listener.start();

		Thread.sleep( 2000 );

		TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
		tmf.init( keystore );

		context = SSLContext.getInstance( "SSLv3" );
		TrustManager[] trustManagers = tmf.getTrustManagers();

		context.init( null, trustManagers, null );

		Downloader d = new Downloader( "localhost", 6789, context, "xyz" );
		boolean res = d.download( outFile, new WantRangeCallback() {
			long pos = 0;
			long size = -1;

			@Override
			public FileRange get()
			{
				if ( size == -1 ) {
					try {
						size = Files.size( Paths.get( inFile ) );
					} catch ( IOException e ) {
						return null;
					}
				}
				if ( pos >= size )
					return null;
				long end = Math.min( pos + CHUNK_SIZE, size );
				FileRange range = new FileRange( pos, end );
				pos += CHUNK_SIZE;
				return range;
			}
		} );
		if ( res )
			System.out.println( "Active Download OK" );
		else
			System.out.println( "Active Download FAILED" );

		/*
		String pathToKeyStore =
				"/home/bjoern/javadev/DataTransfer/mySrvKeyStore.jks";
		 char[] passphrase = "test123".toCharArray();
		 KeyStore keystore = KeyStore.getInstance("JKS");
		 keystore.load(new FileInputStream(pathToKeyStore), passphrase);
		 KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		 kmf.init(keystore, passphrase);
		 SSLContext context = SSLContext.getInstance("SSLv3");
		 KeyManager[] keyManagers = kmf.getKeyManagers();

		 context.init(keyManagers, null, null);

		Uploader u = new Uploader("localhost", 6789, context);
		u.sendToken("xyz");
		
		RandomAccessFile file = new RandomAccessFile(new File("test.txt"), "rw");
		long length = file.length();
		file.close();
		
		int diff = 0;
		for (int i = 0; (i + 5) < length; i += 5) {
			u.sendRange(i, i + 5);
			u.sendFile("test.txt");
			diff = (int) (length - i);
		}
		
		u.sendRange((int)(length - diff), (int)length);
		u.sendFile("test.txt");
		*/
	}

	// Implementing IncomingEvent for testing case.
	static class Test implements IncomingEvent
	{
		public void incomingUploader( Uploader uploader ) throws IOException
		{
			if ( uploader.getToken() == null ) {
				System.out.println( "Incoming uploader: could not get token!" );
				return;
			}
			if ( !uploader.upload( inFile ) )
				System.out.println( "Incoming uploader failed!" );
			else
				System.out.println( "Incomgin uploader OK" );
		}

		public void incomingDownloader( Downloader downloader ) throws IOException
		{
			if ( downloader.getToken() == null ) {
				System.out.println( "Incoming downloader: could not get token!" );
				return;
			}
			// TODO: if (!downloader.download( destinationFile, callback ))
		}
	}

}
