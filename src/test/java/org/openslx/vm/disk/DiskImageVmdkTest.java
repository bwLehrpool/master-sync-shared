package org.openslx.vm.disk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openslx.virtualization.Version;
import org.openslx.vm.disk.DiskImage.ImageFormat;

public class DiskImageVmdkTest
{
	@Test
	@DisplayName( "Test detection of default VMDK disk image" )
	public void testVmdkDiskImage() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image-default.vmdk" ) );
		final Version imageVersion = new Version( Short.valueOf( "1" ) );
		final Version imageHwVersion = new Version( Short.valueOf( "18" ) );

		assertEquals( ImageFormat.VMDK.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );

		// test special features of the VMDK disk image format
		final DiskImageVmdk vmdkImage = DiskImageVmdk.class.cast( image );
		assertEquals( imageHwVersion, vmdkImage.getHwVersion() );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 0: single growable virtual disk)" )
	public void testVmdkDiskImageType0() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t0.vmdk" ) );
		final Version imageVersion = new Version( Short.valueOf( "1" ) );
		final Version imageHwVersion = new Version( Short.valueOf( "18" ) );

		assertEquals( ImageFormat.VMDK.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( false, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );

		// test special features of the VMDK disk image format
		final DiskImageVmdk vmdkImage = DiskImageVmdk.class.cast( image );
		assertEquals( imageHwVersion, vmdkImage.getHwVersion() );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 1: growable virtual disk split into multiple files)" )
	public void testVmdkDiskImageType1() throws DiskImageException, IOException
	{
		Assertions.assertThrows( DiskImageException.class, () -> {
			DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t1.vmdk" ) );
		} );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 2: preallocated virtual disk)" )
	public void testVmdkDiskImageType2() throws DiskImageException, IOException
	{
		Assertions.assertThrows( DiskImageException.class, () -> {
			DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t2.vmdk" ) );
		} );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 3: preallocated virtual disk split into multiple files)" )
	public void testVmdkDiskImageType3() throws DiskImageException, IOException
	{
		Assertions.assertThrows( DiskImageException.class, () -> {
			DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t3.vmdk" ) );
		} );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 4: preallocated ESX-type virtual disk)" )
	public void testVmdkDiskImageType4() throws DiskImageException, IOException
	{
		Assertions.assertThrows( DiskImageException.class, () -> {
			DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t4.vmdk" ) );
		} );
	}

	@Test
	@DisplayName( "Test detection of VMDK disk image (type 5: compressed disk optimized for streaming)" )
	public void testVmdkDiskImageType5() throws DiskImageException, IOException
	{
		final DiskImage image = DiskImage.newInstance( DiskImageTestResources.getDiskFile( "image_t5.vmdk" ) );
		final Version imageVersion = new Version( Short.valueOf( "3" ) );
		final Version imageHwVersion = new Version( Short.valueOf( "18" ) );

		assertEquals( ImageFormat.VMDK.toString(), image.getFormat().toString() );
		assertEquals( true, image.isStandalone() );
		assertEquals( false, image.isSnapshot() );
		assertEquals( true, image.isCompressed() );
		assertEquals( imageVersion, image.getVersion() );
		assertNull( image.getDescription() );

		// test special features of the VMDK disk image format
		final DiskImageVmdk vmdkImage = DiskImageVmdk.class.cast( image );
		assertEquals( imageHwVersion, vmdkImage.getHwVersion() );
	}
}
