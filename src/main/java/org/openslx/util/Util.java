package org.openslx.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util
{
	private static Logger log = LogManager.getLogger( Util.class );

	/**
	 * Check if the given object is null, abort program if true. An optional
	 * message to be printed can be passed. A stack trace will be printed, too.
	 * Finally the application terminates with exit code 2.
	 * 
	 * This comes in handy if something must not be null, and you want user
	 * friendly output. A perfect example would be reading settings from a
	 * config file. You can use this on mandatory fields.
	 * 
	 * @param something the object to compare to null
	 * @param message the message to be printed if something is null
	 */
	public static void notNullFatal( Object something, String message )
	{
		if ( something == null ) {
			if ( message != null )
				log.fatal( "[NOTNULL] " + message );
			log.warn( "Fatal null pointer exception", new NullPointerException() );
			System.exit( 2 );
		}
	}

	private static Pattern nonprintableExp = Pattern.compile( "[\\p{C}\\p{Zl}\\p{Zp}]" );
	private static Pattern nonSpaceExp = Pattern.compile( "[^\\p{C}\\p{Z}]" );

	/**
	 * Whether the given string contains only printable characters.
	 */
	public static boolean isPrintable( String string )
	{
		return !nonprintableExp.matcher( string ).find();
	}

	/**
	 * Whether given string is null, empty, or only matches space-like
	 * characters.
	 */
	public static boolean isEmptyString( String string )
	{
		return string == null || !nonSpaceExp.matcher( string ).find();
	}

	/**
	 * Check if given string is null or empty and abort program if so.
	 * 
	 * @param something String to check
	 * @param message Error message to display on abort
	 */
	public static void notNullOrEmptyFatal( String something, String message )
	{
		if ( isEmptyString( something ) ) {
			if ( message != null )
				log.fatal( "[NOTNULL] " + message );
			log.warn( "Fatal null pointer or empty exception", new NullPointerException() );
			System.exit( 2 );
		}
	}

	/**
	 * Parse the given String as a base10 integer.
	 * If the string does not represent a valid integer, return the given
	 * default value.
	 * 
	 * @param value string representation to parse to an int
	 * @param defaultValue fallback value if given string can't be parsed
	 */
	public static int parseInt( String value, int defaultValue )
	{
		try {
			return Integer.parseInt( value );
		} catch ( Exception e ) {
			return defaultValue;
		}
	}

	/**
	 * Parse the given String as a base10 long.
	 * If the string does not represent a valid long, return the given
	 * default value.
	 * 
	 * @param value string representation to parse to a long
	 * @param defaultValue fallback value if given string can't be parsed
	 */
	public static long parseLong( String value, long defaultValue )
	{
		try {
			return Long.parseLong( value );
		} catch ( Exception e ) {
			return defaultValue;
		}
	}

	public static void safeClose( AutoCloseable... closeable )
	{
		for ( AutoCloseable c : closeable ) {
			if ( c == null )
				continue;
			try {
				c.close();
			} catch ( Throwable t ) {
			}
		}
	}

	public static boolean sleep( int millis )
	{
		try {
			Thread.sleep( millis );
			return true;
		} catch ( InterruptedException e ) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public static boolean joinThread( Thread t )
	{
		try {
			t.join();
			return true;
		} catch ( InterruptedException e ) {
			return false;
		}
	}

	/**
	 * Number of seconds elapsed since 1970-01-01 UTC.
	 */
	public static long unixTime()
	{
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * Monotonic tick count in milliseconds, not bound to RTC.
	 */
	public static long tickCount()
	{
		return System.nanoTime() / 1000_000;
	}

	private static final String[] UNITS = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "???" };

	public static String formatBytes( double val )
	{
		int unit = 0;
		while ( Math.abs(val) > 1024 ) {
			val /= 1024;
			unit++;
		}
		if (unit >= UNITS.length) {
			unit = UNITS.length - 1;
		}
		return String.format( "%.1f %s", val, UNITS[unit] );
	}

	/**
	 * Connect to given host(name), trying all addresses it resolves to.
	 */
	public static Socket connectAllRecords( SocketFactory fac, String host, int port, int timeout ) throws IOException
	{
		InetAddress[] addrList;
		addrList = InetAddress.getAllByName( host );
		if ( addrList.length == 0 ) {
			throw new UnknownHostException( "Unknown host: " + host );
		} else if ( addrList.length == 1 ) {
			// Simple case
			Socket s = fac.createSocket();
			s.connect( new InetSocketAddress( addrList[0], port ), timeout );
			return s;
		}
		// Cascaded connects
		log.debug( "Got " + addrList.length + " hosts for " + host );
		String name = host.length() > 12 ? host.substring( 0, 12 ) : host;
		ThreadPoolExecutor tpe = new CascadedThreadPoolExecutor( Math.min( addrList.length, 4 ), 4,
				2, TimeUnit.SECONDS,
				4, new ThreadPoolExecutor.AbortPolicy(), name );
		final AtomicReference<IOException> fe = new AtomicReference<>();
		final AtomicReference<Socket> retSock = new AtomicReference<>();
		final Semaphore sem = new Semaphore( 0 );
		try {
			int endIdx = addrList.length - 1;
			for ( int idx = 0; idx <= endIdx; idx++ ) {
				InetAddress addr = addrList[idx];
				// Create next connect task
				Runnable task = new Runnable() {
					@Override
					public void run()
					{
						try {
							Socket s = fac.createSocket();
							log.debug( "Trying " + addr.toString() );
							s.connect( new InetSocketAddress( addr, port ), timeout );
							if ( retSock.compareAndSet( null, s ) ) {
								log.debug( addr.toString() + ": Success" );
								sem.release();
							} else {
								// Lost race with another thread
								log.debug( addr.toString() + ": Success, but lost race" );
								s.close();
							}
						} catch ( IOException e ) {
							fe.set( e );
						}
					}
				};
				try {
					tpe.execute( task );
				} catch ( Exception e ) {
					log.debug( "Failed to queue connect for " + addr.toString() );
				}
				// Wait for semaphore, or 250ms timeout
				boolean gotit = false;
				log.debug( "Waiting for connect..." );
				try {
					// Wait longer on last iteration
					if ( idx == endIdx ) {
						gotit = sem.tryAcquire( timeout + 10, TimeUnit.MILLISECONDS );
					} else {
						gotit = sem.tryAcquire( 250, TimeUnit.MILLISECONDS );
					}
				} catch ( InterruptedException e ) {
				}
				if ( gotit ) {
					// Got semaphore, a task should have succeeded
					sem.release();
					return retSock.getAndSet( null );
				}
			}
			log.debug( "Out of addresses" );
			sem.release();
			throw fe.get();
		} finally {
			tpe.shutdownNow();
			// Make sure a connect that succeeded after we exit the loop 
			// but before we call sem.release() will be cleaned up
			Util.safeClose( retSock.get() );
		}
	}

}
