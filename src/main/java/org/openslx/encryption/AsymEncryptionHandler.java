package org.openslx.encryption;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AsymEncryptionHandler
{
	private static final Logger LOG = LogManager.getLogger( AsymEncryptionHandler.class );

	private final Key key;

	/**
	 * Create a handler.
	 */
	public AsymEncryptionHandler( Key key )
	{
		this.key = key;
	}

	/**
	 * Encrypt given plain text message with the key this class was
	 * instantiated with.
	 * 
	 * @param cleartext a clear text message
	 * @return The encrypted message
	 */
	public byte[] encryptMessage( byte[] cleartext )
	{
		try {
			Cipher cipher = Cipher.getInstance( "RSA" );
			cipher.init( Cipher.ENCRYPT_MODE, key );
			return cipher.doFinal( cleartext );
		} catch ( NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e ) {
			LOG.warn( "Cannot encrypt message", e );
		}
		return null;
	}

	/**
	 * Verify an encrypted message, where we know the plain text.
	 * 
	 * @param encryptedMessage
	 * @param expectedCleartext
	 * @return true if the message matches the expected plain text after decrypting
	 */
	public boolean verifyMessage( byte[] encryptedMessage, byte[] expectedCleartext )
	{
		try {
			Cipher cipher = Cipher.getInstance( "RSA" );
			cipher.init( Cipher.DECRYPT_MODE, key );
			byte[] result = cipher.doFinal( encryptedMessage );
			return Arrays.equals( expectedCleartext, result );
		} catch ( NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e ) {
			LOG.warn( "Cannot verify message", e );
		}
		return false;
	}

	/**
	 * Generate a fresh RSA key pair.
	 * 
	 * @param bits length of key
	 * @return key pair, or null on error
	 */
	public static KeyPair generateKeyPair( int bits )
	{
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance( "RSA" );
			kpg.initialize( bits );
			return kpg.genKeyPair();
		} catch ( NoSuchAlgorithmException | InvalidParameterException e ) {
			LOG.warn( "Cannot generate RSA Keypair", e );
			return null;
		}
	}

}
