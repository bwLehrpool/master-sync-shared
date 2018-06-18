package org.openslx.util;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Util
{
	private static Logger log = Logger.getLogger( Util.class );

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
			log.warn( Thread.currentThread().getStackTrace().toString() );
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
			log.warn( Thread.currentThread().getStackTrace().toString() );
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
	 * @return
	 */
	public static int parseInt( String value, int defaultValue )
	{
		try {
			return Integer.parseInt( value );
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

	public static long unixTime()
	{
		return System.currentTimeMillis() / 1000;
	}

}
