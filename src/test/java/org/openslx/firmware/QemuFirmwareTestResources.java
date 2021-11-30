package org.openslx.firmware;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

public class QemuFirmwareTestResources
{
	private static final String QEMU_PREFIX_PATH = File.separator + "qemu";
	private static final String QEMU_PREFIX_PATH_FW = QEMU_PREFIX_PATH + File.separator + "firmware";

	private static final String QEMU_TEMP_PREFIX = "qemu-";

	public static String getQemuFirmwareSpecPath()
	{
		String fwSpecDir = null;

		try {
			fwSpecDir = getResourceDirectory( QemuFirmwareTestResources.class, QEMU_PREFIX_PATH_FW );
		} catch ( IOException e ) {
			fwSpecDir = null;
		}

		return fwSpecDir;
	}

	private static String getResourceDirectory( Class<?> clazz, String resourceDir ) throws IOException
	{
		final String fwDirPath = resourceDir.substring( 1 ).concat( File.separator );
		final URL fwResource = clazz.getResource( resourceDir );
		final File fwDirectory;
		String fwDirectoryPath = null;

		if ( fwResource != null && "jar".equals( fwResource.getProtocol() ) ) {
			// create temporary directory to copy Jar files into it
			fwDirectory = Files.createTempDirectory( QEMU_TEMP_PREFIX ).toFile();

			// obtain file list from a directory within the Jar file
			// strip out only the JAR file path
			final String jarPath = fwResource.getPath().substring( 5, fwResource.getPath().indexOf( "!" ) );
			final JarFile jar = new JarFile( URLDecoder.decode( jarPath, "UTF-8" ) );
			// get all entries in the Jar file
			final Enumeration<JarEntry> jarEntries = jar.entries();
			final Set<String> fileNames = new HashSet<String>();
			while ( jarEntries.hasMoreElements() ) {
				final String jarEntryName = jarEntries.nextElement().getName();
				if ( jarEntryName.startsWith( fwDirPath ) ) {
					String jarEntry = jarEntryName.substring( fwDirPath.length() );
					if ( !jarEntry.isEmpty() ) {
						fileNames.add( jarEntry );
					}
				}
			}

			// copy each file from the Jar to the temporary directory
			fileNames.forEach( fileName -> {
				final String resourceFileName = resourceDir + File.separator + fileName;
				final File tempFile = new File( fwDirectory.getPath() + File.separator + fileName );
				final InputStream fileInput = QemuFirmwareTestResources.class.getResourceAsStream( resourceFileName );
				try {
					FileUtils.copyInputStreamToFile( fileInput, tempFile );
				} catch ( IOException e ) {
					e.printStackTrace();
				}
				tempFile.deleteOnExit();
			} );
			fwDirectory.deleteOnExit();
		} else if ( fwResource != null && "file".equals( fwResource.getProtocol() ) ) {
			fwDirectory = new File( fwResource.getFile() );
		} else {
			fwDirectory = null;
		}

		try {
			fwDirectoryPath = fwDirectory.toURI().toURL().getFile();
		} catch ( MalformedURLException | NullPointerException e ) {
			fwDirectoryPath = null;
		}

		return fwDirectoryPath;
	}

	public static File getQemuFirmwareSpecFile( String fileName )
	{
		final String fwSpecFilePath = QEMU_PREFIX_PATH_FW + File.separator + fileName;
		final URL fwSpecFileUrl = QemuFirmwareTestResources.class.getResource( fwSpecFilePath );
		return new File( fwSpecFileUrl.getFile() );
	}
}
