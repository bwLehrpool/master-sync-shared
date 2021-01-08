package org.openslx.util.vm;

import java.io.File;
import java.net.URL;

public class DiskImageTestResources
{
	private static final String DISK_PREFIX_PATH = File.separator + "disk";

	public static File getDiskFile( String diskFileName )
	{
		String diskPath = DiskImageTestResources.DISK_PREFIX_PATH + File.separator + diskFileName;
		URL disk = DiskImageTestResources.class.getResource( diskPath );
		return new File( disk.getFile() );
	}
}
