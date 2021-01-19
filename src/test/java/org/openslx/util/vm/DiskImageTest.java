package org.openslx.util.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.util.vm.DiskImage.ImageFormat;
import org.openslx.util.vm.DiskImage.UnknownImageFormatException;

public class DiskImageTest
{
	@BeforeAll
	public static void setUp()
	{
		// disable logging with log4j
		LogManager.getRootLogger().setLevel( Level.OFF );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image" )
	public void testVmdkDiskImage() throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		File file = DiskImageTestResources.getDiskFile( "image-default.vmdk" );
		DiskImage image = new DiskImage( file );

		assertEquals( ImageFormat.VMDK.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 18, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of VDI disk image" )
	public void testVdiDiskImage() throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image-default.vdi" ) );

		assertEquals( ImageFormat.VDI.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 0, image.hwVersion );
		assertNotNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of default QCOW2 disk image" )
	public void testQcow2DiskImage() throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image-default.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 16384 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed16384DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-on_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 16384 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed16384DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-on_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 16384 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed16384DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-off_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 16384 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed16384DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-16384_cp-off_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 65536 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed65536DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-on_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 65536 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed65536DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-on_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 65536 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed65536DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-off_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 65536 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed65536DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-65536_cp-off_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 2097152 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2Compressed2097152DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-on_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of compressed, 2097152 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2Compressed2097152DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-on_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( true, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 2097152 byte cluster QCOW2 disk image with extended L2 tables" )
	public void testQcow2DetectionL2NonCompressed2097152DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-off_l2-on.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test detection of non-compressed, 2097152 byte cluster QCOW2 disk image without extended L2 tables" )
	public void testQcow2DetectionNonL2NonCompressed2097152DiskImage()
			throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		DiskImage image = new DiskImage( DiskImageTestResources.getDiskFile( "image_cs-2097152_cp-off_l2-off.qcow2" ) );

		assertEquals( ImageFormat.QCOW2.toString(), image.getImageFormat().toString() );
		assertEquals( true, image.isStandalone );
		assertEquals( false, image.isSnapshot );
		assertEquals( false, image.isCompressed );
		assertEquals( 3, image.hwVersion );
		assertNull( image.diskDescription );
	}

	@Test
	@DisplayName( "Test of invalid disk image" )
	public void testInvalidDiskImage() throws FileNotFoundException, IOException, UnknownImageFormatException
	{
		Assertions.assertThrows( UnknownImageFormatException.class, () -> {
			new DiskImage( DiskImageTestResources.getDiskFile( "image-default.invalid" ) );
		} );
	}
}
