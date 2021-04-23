package org.openslx.vm.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.virtualization.Version;
import org.openslx.vm.disk.DiskImage.ImageFormat;

public class DiskImageQcow2Test
{
	@Test
	@DisplayName( "Test detection of default QCOW2 disk image" )
	public void testQcow2DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 16384 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed16384DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-on_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 16384 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed16384DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-on_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 16384 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed16384DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-off_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 16384 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed16384DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-off_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 65536 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed65536DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-on_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 65536 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed65536DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-on_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 65536 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed65536DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-off_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 65536 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed65536DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-off_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 2097152 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed2097152DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-on_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of compressed, 2097152 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed2097152DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-on_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 2097152 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed2097152DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-off_l2-on.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 2097152 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed2097152DiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage
				.newInstance( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-off_l2-off.qcow2" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );
	}
}
