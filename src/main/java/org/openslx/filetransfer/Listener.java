package org.openslx.filetransfer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Listener extends Thread {
	private IncomingEvent incomingEvent;
	/*
	private static String pathToKeyStore =
			"/home/bjoern/javadev/DataTransfer/mySrvKeyStore.jks";
			*/
	private SSLContext context;
	private int port;

	
	/***********************************************************************//**
	 * Constructor for class Listener, which gets an instance of IncomingEvent.
	 * @param e
	 */
	public Listener(IncomingEvent e, SSLContext context, int port) {
		this.incomingEvent = e;
		this.context = context;
		this.port = port;
		/*
	    char[] passphrase = "test123".toCharArray();
	    KeyStore keystore = KeyStore.getInstance("JKS");
	    keystore.load(new FileInputStream(pathToKeyStore), passphrase);
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	    kmf.init(keystore, passphrase);
	    context = SSLContext.getInstance("SSLv3");
	    KeyManager[] keyManagers = kmf.getKeyManagers();

	    context.init(keyManagers, null, null);
	    */
	}
	
	/***********************************************************************//**
	 * Method listen, should run from Master Server. Listen for incoming
	 * connection, and start Downloader or Uploader.
	 * @throws Exception
	 */
	private void listen() throws Exception {
		SSLServerSocketFactory sslServerSocketFactory = context.getServerSocketFactory();
		SSLServerSocket welcomeSocket =
				(SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
		
		while (!isInterrupted()) {
			SSLSocket connectionSocket = (SSLSocket) welcomeSocket.accept();
			
			byte[] b = new byte[1];
			int length = connectionSocket.getInputStream().read(b);
			
			System.out.println(length);
			
			// Ascii - Code: 'U' = 85 ; 'D' = 68.
			if (b[0] == 85) {
				System.out.println("U erkannt --> Downloader starten");
				// --> start Downloader(socket).
				Downloader d = new Downloader(connectionSocket);
				incomingEvent.incomingDownloader(d);
			}
			else if (b[0] == 68) {
				System.out.println("D erkannt --> Uploader starten");
				// --> start Uploader(socket).
				Uploader u = new Uploader(connectionSocket);
				incomingEvent.incomingUploader(u);
				
			}
			else {
				System.out.println("Müll empfangen");
				connectionSocket.close();
			}
		}
	}

	@Override
	public void run() {
		try {
			this.listen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
