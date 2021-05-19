package org.openslx.virtualization.disk;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DiskImageTest
{
	@Test
	@DisplayName( "Test of invalid disk image" )
	public void testInvalidDiskImage() throws IOException
	{
		Assertions.assertThrows( DiskImageException.class, () -> {
			DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default.invalid" ) );
		} );
	}
}
