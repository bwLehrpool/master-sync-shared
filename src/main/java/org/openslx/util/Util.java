package org.openslx.util;

import java.io.Closeable;

import org.apache.log4j.Logger;
import org.openslx.util.Util;

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
	 * @param something
	 *           the object to compare to null
	 * @param message
	 *           the message to be printed if something is null
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

	public static void notNullOrEmptyFatal( String something, String message )
	{
		if ( something == null || something.isEmpty() ) {
			if ( message != null )
				log.fatal( "[NOTNULL] " + message );
			log.warn( Thread.currentThread().getStackTrace().toString() );
			System.exit( 2 );
		}
	}

	
	/**
	 * Tries to parse an int. Returns 0 on error.
	 * 
	 * @param s
	 *           The string to parse
	 * @return The parsed int or 0 on error
	 */
	public static int tryToParseInt( String s )
	{
		try {
			return Integer.parseInt( s );
		} catch ( NumberFormatException e ) {
			return 0;
		}
	}
	
	public static void streamClose( Closeable... closeable )
	{
		for ( Closeable c : closeable ) {
			if ( c == null )
				continue;
			try {
				c.close();
			} catch ( Throwable t ) {
			}
		}
	}
}
