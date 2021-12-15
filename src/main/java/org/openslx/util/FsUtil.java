package org.openslx.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FsUtil
{
	
	private static final Logger LOGGER = LogManager.getLogger( FsUtil.class );

	public static String getRelativePath( File absolutePath, File parentDir )
	{
		String file;
		String dir;
		try {
			file = absolutePath.getCanonicalPath();
			dir = parentDir.getCanonicalPath() + File.separator;
		} catch ( Exception e ) {
			LOGGER.error( "Could not get relative path for " + absolutePath.toString(), e );
			return null;
		}
		if ( !file.startsWith( dir ) )
			return null;
		return file.substring( dir.length() );
	}

	public static String sanitizeFileName( String fileName )
	{
		fileName = fileName.replaceAll( "[^a-zA-Z0-9_\\-]+", "_" );
		if ( fileName.length() > 40 )
			fileName = fileName.substring( 0, 40 );
		return fileName;
	}

}
