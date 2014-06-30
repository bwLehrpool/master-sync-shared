package org.openslx.filetransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Downloader {
	// Some instance variables.
	private SSLSocketFactory sslSocketFactory;
	private SSLSocket satelliteSocket;
	private DataOutputStream dataToServer;
	private DataInputStream dataFromServer;
	private String TOKEN = null;
	private String RANGE = null;
	private String outputFilename;
	
	/***********************************************************************//**
	 * Constructor for satellite downloader.
	 * Tries to connect to specific ip and port and sending type of action.
	 * @param ip
	 * @param port
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public Downloader(String ip, int port, String filename, SSLContext context) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
		outputFilename = filename;
		
		/*
	    char[] passphrase = "test123".toCharArray();
	    KeyStore keystore = KeyStore.getInstance("JKS");
	    keystore.load(new FileInputStream(pathToTrustStore), passphrase);

	    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(keystore);

	    SSLContext context = SSLContext.getInstance("SSLv3");
	    TrustManager[] trustManagers = tmf.getTrustManagers();

	    context.init(null, trustManagers, null);
		*/
		
	    // create socket.
		sslSocketFactory = context.getSocketFactory();
		
		satelliteSocket = (SSLSocket) sslSocketFactory.createSocket(ip, port);
		
		dataToServer = new DataOutputStream(satelliteSocket.getOutputStream());
		dataToServer.writeByte('D');
		dataFromServer = new DataInputStream(satelliteSocket.getInputStream());
	}

	/***********************************************************************//**
	 * Constructor for master downloader.
	 * Given parameter is the socket over which the transfer is going.
	 * @param socket
	 * @throws IOException 
	 */
	public Downloader(SSLSocket socket, String filename) throws IOException {
		outputFilename = filename;
		satelliteSocket = socket;
		dataToServer = new DataOutputStream(satelliteSocket.getOutputStream());
		dataFromServer = new DataInputStream(satelliteSocket.getInputStream());
	}

	/***********************************************************************//**
	 * Method for sending token for identification from satellite to master.
	 * @param t
	 * @throws IOException 
	 */
	public void sendToken(String token) throws IOException {
		TOKEN = token;
		String sendToken = "TOKEN=" + TOKEN;
		byte[] data = sendToken.getBytes(StandardCharsets.UTF_8);
		dataToServer.writeByte(data.length);
		dataToServer.write(data);
	}
	
	/***********************************************************************//**
	 * Method to send range of the file, which should be uploaded.
	 * Helpful for knowing how much was already uploaded if
	 * connection aborts.
	 * @param a
	 * @param b
	 * @throws IOException 
	 */
	public void sendRange(int a, int b) throws IOException {
		RANGE = a + ":" + b;
		String sendRange = "RANGE=" + RANGE;
		byte[] data = sendRange.getBytes(StandardCharsets.UTF_8);
		dataToServer.writeByte(data.length);
		dataToServer.write(data);
	}
	
	/***********************************************************************//**
	 * Method for reading incoming token for identification.
	 * @throws IOException
	 */
	public String getToken() throws IOException {
		if (TOKEN != null)
			return TOKEN;
		return null;
	}
	
	/***********************************************************************//**
	 * Method for reading range of file, which is downloaded.
	 * Helpful for knowing how much is already downloaded if connection aborts.
	 */
	public String getRange() {
		if (RANGE != null)
			return RANGE;
		return null;
	}
	
	/***********************************************************************//**
	 * Getter for beginning of RANGE.
	 * @return
	 */
	public int getStartOfRange() {
		if (RANGE != null) {
			String[] splitted = RANGE.split(":");
			return Integer.parseInt(splitted[0]);
		}
		return -1;
	}
	
	/***********************************************************************//**
	 * Getter for end of RANGE.
	 * @return
	 */
	public int getEndOfRange() {
		if (RANGE != null) {
			String[] splitted = RANGE.split(":");
			return Integer.parseInt(splitted[1]);
		}
		return -1;
	}
	
	/***********************************************************************//**
	 * Method for returning difference of current Range.
	 * @return
	 */
	public int getDiffOfRange() {
		int diff = Math.abs(getEndOfRange() - getStartOfRange()); 
		return diff;
	}
	
	/***********************************************************************//**
	 * Method for reading MetaData, like TOKEN and FileRange.
	 * Split incoming bytes after first '=' and store value to specific
	 * variable.
	 * @throws IOException 
	 */
	public Boolean readMetaData() throws IOException {
		try {
			while (true) {
				byte[] incoming = new byte[255];
				
				// First get length.
				dataFromServer.read(incoming, 0, 1);
				int length = incoming[0];
				System.out.println("length: " + length);
				
				if (length == 0)
					break;
				
				/**
				 *  Read the next available bytes and split by '=' for
				 *  getting TOKEN or RANGE.
				 */
				int hasRead = 0;
				while (hasRead < length) {
					int ret = dataFromServer.read(incoming, hasRead, length - hasRead);
					if (ret == -1) {
						System.out.println("Error occured while reading Metadata.");
						return false;
					}
					hasRead += ret;
				}
				
				String data = new String(incoming, 0, length, "UTF-8");
				// System.out.println(data);
				
				String[] splitted = data.split("=");
				// System.out.println("splitted[0]: " + splitted[0]);
				// System.out.println("splitted[1]: " + splitted[1]);
				if (splitted[0] != null && splitted[0].equals("TOKEN")) {
					if (splitted[1] != null)
						TOKEN = splitted[1];
					System.out.println("TOKEN: " + TOKEN);
				}
				else if (splitted[0].equals("RANGE")) {
					if (splitted[1] != null)
						RANGE = splitted[1];
					System.out.println("RANGE: '" + RANGE + "'");
				}
			}
		} catch (IOException e) {
			throw e;
			// return false;
		}
		return true;
	}
	
	/***********************************************************************//**
	 * Method for reading Binary. Reading the current Range of incoming binary.
	 * @throws IOException 
	 */
	public Boolean readBinary() throws IOException {
		int length = getDiffOfRange();
		byte[] incoming = new byte[4000];
		
		int hasRead = 0;
		while (hasRead < length) {
			int ret = dataFromServer.read(incoming, hasRead, length - hasRead);
			if (ret == -1) {
				System.out.println("Error occured in Downloader.readBinary(),"
						+ " while reading binary.");
				return false;
			}
			hasRead += ret;
		}
		
		RandomAccessFile file = new RandomAccessFile(new File(outputFilename), "rw");
		file.seek(getStartOfRange());
		file.write(incoming, 0, length);
		file.close();
		return true;
	}
	
	/***********************************************************************//**
	 * Method for closing connection, if download has finished.
	 */
	public void close() {
		
	}
}
