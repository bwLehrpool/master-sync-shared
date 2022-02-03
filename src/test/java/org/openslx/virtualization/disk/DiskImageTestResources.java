package org.openslx.virtualization.disk;

import java.io.File;
import java.net.URL;

import org.openslx.util.Resources;

public class DiskImageTestResources
{
	private static final String DISK_PREFIX_PATH = Resources.PATH_SEPARATOR + "disk";

	public static File getDiskFile( String diskFileName )
	{
		String diskPath = DiskImageTestResources.DISK_PREFIX_PATH + Resources.PATH_SEPARATOR + diskFileName;
		URL disk = DiskImageTestResources.class.getResource( diskPath );
		return new File( disk.getFile() );
	}
}
