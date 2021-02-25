package org.openslx.vm.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.vm.disk.DiskImage.ImageFormat;

public class DiskImageVdiTest
{
	@Test
	@DisplayName( "Test detection of default VDI disk image" )
	public void testVdiDiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default.vdi" ) );
		final int imageVersion = DiskImageUtils.versionFromMajorMinor( Short.valueOf( "1" ), Short.valueOf( "1" ) );

		assertEquals( ImageFormat.VDI.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNotNull( image.getDescription() );
	}
	
	@Test
	@DisplayName( "Test detection of VDI disk image snapshot" )
	public void testVdiDiskImageSnapshot() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default_snapshot.vdi" ) );
		final int imageVersion = DiskImageUtils.versionFromMajorMinor( Short.valueOf( "1" ), Short.valueOf( "1" ) );

		assertEquals( ImageFormat.VDI.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( true, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNotNull( image.getDescription() );
	}
}
