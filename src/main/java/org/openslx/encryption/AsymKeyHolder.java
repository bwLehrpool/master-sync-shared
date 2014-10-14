package org.openslx.encryption;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.log4j.Logger;

public class AsymKeyHolder
{
	private static final Logger LOG = Logger.getLogger( AsymKeyHolder.class );

	private static RSAPrivateKey privKey = null;
	private static RSAPublicKey pubKey = null;

	/**
	 * Create private and/or public key according to given numbers.
	 * 
	 * @param privExp private exponent
	 * @param pubExp public exponent
	 * @param mod modulus of keypair
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public AsymKeyHolder( BigInteger privExp, BigInteger pubExp, BigInteger mod )
			throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		if ( mod == null )
			throw new InvalidKeySpecException( "No modulus given!" );
		final KeyFactory keyFact;
		keyFact = KeyFactory.getInstance( "RSA" );
		if ( pubExp != null ) {
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec( mod, pubExp );
			pubKey = (RSAPublicKey)keyFact.generatePublic( keySpec );
		}
		if ( privExp != null ) {
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec( mod, privExp );
			privKey = (RSAPrivateKey)keyFact.generatePrivate( keySpec );
		}
	}

	/**
	 * Create new keypair.
	 */
	public AsymKeyHolder()
	{
		generateKey();
	}

	/**
	 * Get private key or null if not known.
	 * 
	 * @return sausages
	 */
	public PrivateKey getPrivateKey()
	{
		return privKey;
	}

	/**
	 * Get public key or null if not known.
	 * 
	 * @return public key
	 */
	public PublicKey getPublicKey()
	{
		return pubKey;
	}

	private boolean generateKey()
	{
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance( "RSA" );
		} catch ( NoSuchAlgorithmException e ) {
			LOG.error( "NoSuchAlgorithmException", e );
			return false;
		}

		kpg.initialize( 4096 );
		KeyPair kp = kpg.generateKeyPair();
		privKey = (RSAPrivateKey)kp.getPrivate();
		pubKey = (RSAPublicKey)kp.getPublic();

		BigInteger pubMod = pubKey.getModulus();
		BigInteger privMod = privKey.getModulus();
		assert ( pubMod.equals( privMod ) );
		return true;
	}

	public BigInteger getModulus()
	{
		if ( privKey != null )
			return privKey.getModulus();
		if ( pubKey != null )
			return pubKey.getModulus();
		return null; // Should never happen, unless only a modulus was given
	}

	public BigInteger getPrivateExponent()
	{
		if ( privKey == null )
			return null;
		return privKey.getPrivateExponent();
	}

	public BigInteger getPublicExponent()
	{
		if ( pubKey == null )
			return null;
		return pubKey.getPublicExponent();
	}

}
