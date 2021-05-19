package org.openslx.virtualization.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.disk.DiskImage.ImageFormat;

public class DiskImageVdiTest
{
	@Test
	@DisplayName( "Test detection of default VDI disk image" )
	public void testVdiDiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default.vdi" ) );
		final Version imageVersion = new Version( Short.valueOf( "1" ), Short.valueOf( "1" ) );

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
		final Version imageVersion = new Version( Short.valueOf( "1" ), Short.valueOf( "1" ) );

		assertEquals( ImageFormat.VDI.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( true, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNotNull( image.getDescription() );
	}
}
