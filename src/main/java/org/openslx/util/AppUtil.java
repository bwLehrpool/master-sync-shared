package org.openslx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Logger;

public class AppUtil
{
	private static final int PROPERTY_MAX_WIDTH = 30;

	private static final String MANIFEST_REVISION_VERSION = "Revision-Version";
	private static final String MANIFEST_REVISION_BRANCH = "Revision-Branch";
	private static final String MANIFEST_BUILD_TIMESTAMP = "Build-Timestamp";
	private static final String MANIFEST_BUILD_JDK = "Build-Jdk";

	private static final String PROPERTY_APP_NAME = "app.name";
	private static final String PROPERTY_APP_VERSION = "app.version";
	private static final String PROPERTY_APP_REVISION_VERSION = "app.revision.version";
	private static final String PROPERTY_APP_REVISION_BRANCH = "app.revision.branch";
	private static final String PROPERTY_APP_BUILD_DATE = "app.build.date";
	private static final String PROPERTY_APP_BUILD_JDK = "app.build.jdk";

	private static final String PROPERTY_JAVA_OS_NAME = "os.name";

	private static final String PROPERTY_JAVA_SPEC_VENDOR = "java.specification.vendor";
	private static final String PROPERTY_JAVA_SPEC_NAME = "java.specification.name";
	private static final String PROPERTY_JAVA_SPEC_VERSION = "java.specification.version";

	private static final String PROPERTY_JAVA_VERSION = "java.version";
	private static final String PROPERTY_JAVA_VERSION_VM = "java.vm.version";
	private static final String PROPERTY_JAVA_VERSION_RUNTIME = "java.runtime.version";

	private static String getManifestValue( final String entry )
	{
		File jarFile = null;
		InputStream jarFileStream = null;
		JarInputStream jarStream = null;
		String value = null;

		try {
			final String jarFilename = AppUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			jarFile = new File( jarFilename );
			jarFileStream = new FileInputStream( jarFile );
			jarStream = new JarInputStream( jarFileStream );

			final Manifest mf = jarStream.getManifest();
			value = mf.getMainAttributes().getValue( entry );
		} catch ( URISyntaxException | IOException e ) {
			return value;
		} finally {
			Util.safeClose( jarStream );
			Util.safeClose( jarFileStream );
		}

		return value;
	}

	public static String getRevisionVersion()
	{
		return AppUtil.getManifestValue( AppUtil.MANIFEST_REVISION_VERSION );
	}

	public static String getRevisionBranch()
	{
		return AppUtil.getManifestValue( AppUtil.MANIFEST_REVISION_BRANCH );
	}

	public static long getBuildTimestamp()
	{
		final String timestampRaw = AppUtil.getManifestValue( AppUtil.MANIFEST_BUILD_TIMESTAMP );
		long timestamp = 0;

		try {
			timestamp = Long.valueOf( timestampRaw );
			timestamp /= 1000L;
		} catch ( NumberFormatException e ) {
			timestamp = 0;
		}

		return timestamp;
	}

	public static String getBuildTime()
	{
		final long timestamp = AppUtil.getBuildTimestamp();
		final String buildTime;

		if ( timestamp > 0 ) {
			final Instant time = Instant.ofEpochSecond( timestamp );
			buildTime = DateTimeFormatter.RFC_1123_DATE_TIME.withZone( ZoneId.systemDefault() ).format( time );
		} else {
			buildTime = null;
		}

		return buildTime;
	}

	public static String getBuildJdk()
	{
		return AppUtil.getManifestValue( AppUtil.MANIFEST_BUILD_JDK );
	}

	private static String formatProperty( final String property )
	{
		return String.format( "%-" + AppUtil.PROPERTY_MAX_WIDTH + "s", property );
	}

	public static void logProperty( final Logger logger, final String propertyName, final String propertyValue )
	{
		logger.info( AppUtil.formatProperty( propertyName ) + propertyValue );
	}

	public static void logJavaProperty( Logger logger, final String javaProperty )
	{
		AppUtil.logProperty( logger, javaProperty, System.getProperty( javaProperty ) );
	}

	public static void logHeader( final Logger logger, final String appName, final String appVersion )
	{
		logger.info( "-------------------------------------------------------------------------------" );
		logger.info( appName );
		logger.info( "-------------------------------------------------------------------------------" );
		logProperty( logger, AppUtil.PROPERTY_APP_NAME, appName );
		logProperty( logger, AppUtil.PROPERTY_APP_VERSION, appVersion );
		logProperty( logger, AppUtil.PROPERTY_APP_REVISION_VERSION, AppUtil.getRevisionVersion() );
		logProperty( logger, AppUtil.PROPERTY_APP_REVISION_BRANCH, AppUtil.getRevisionBranch() );
		logProperty( logger, AppUtil.PROPERTY_APP_BUILD_DATE, AppUtil.getBuildTime() );
		logProperty( logger, AppUtil.PROPERTY_APP_BUILD_JDK, AppUtil.getBuildJdk() );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_OS_NAME );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_SPEC_VENDOR );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_SPEC_NAME );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_SPEC_VERSION );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_VERSION );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_VERSION_VM );
		logJavaProperty( logger, AppUtil.PROPERTY_JAVA_VERSION_RUNTIME );
		logger.info( "-------------------------------------------------------------------------------" );
	}
}
