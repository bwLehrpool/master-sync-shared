package org.openslx.virtualization.virtualizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.Version;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

/**
 * Representation of the VMware virtualizer for virtual machines.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class VirtualizerVmware extends Virtualizer
{
	/**
	 * Name of the VMware virtualizer.
	 */
	private static final String VIRTUALIZER_NAME = "VMware";

	/**
	 * List of supported image formats by the VMware virtualizer.
	 */
	private static final List<DiskImage.ImageFormat> VIRTUALIZER_SUPPORTED_IMAGE_FORMATS = Collections
			.unmodifiableList( Arrays.asList( ImageFormat.VMDK ) );

	/**
	 * List of supported versions of the VMware virtualizer.
	 */
	private static final List<Version> VIRTUALIZER_SUPPORTED_VERSIONS = Collections.unmodifiableList(
			Arrays.asList(
					new Version( Short.valueOf( "03" ), "Workstation 4/5, Player 1" ),
					new Version( Short.valueOf( "04" ), "Workstation 4/5, Player 1/2, Fusion 1" ),
					new Version( Short.valueOf( "06" ), "Workstation 6" ),
					new Version( Short.valueOf( "07" ), "Workstation 6.5/7, Player 3, Fusion 2/3" ),
					new Version( Short.valueOf( "08" ), "Workstation 8, Player/Fusion 4" ),
					new Version( Short.valueOf( "09" ), "Workstation 9, Player/Fusion 5" ),
					new Version( Short.valueOf( "10" ), "Workstation 10, Player/Fusion 6" ),
					new Version( Short.valueOf( "11" ), "Workstation 11, Player/Fusion 7" ),
					new Version( Short.valueOf( "12" ), "Workstation/Player 12, Fusion 8" ),
					new Version( Short.valueOf( "14" ), "Workstation/Player 14, Fusion 10" ),
					new Version( Short.valueOf( "15" ), "Workstation/Player 15, Fusion 11" ),
					new Version( Short.valueOf( "16" ), "Workstation/Player 15.1, Fusion 11.1" ),
					new Version( Short.valueOf( "17" ), "Workstation/Player 16, Fusion 12" ),
					new Version( Short.valueOf( "18" ), "Workstation/Player 16.1, Fusion 12.1" ) ) );

	/**
	 * Creates a new VMware virtualizer.
	 */
	public VirtualizerVmware()
	{
		super( new org.openslx.bwlp.thrift.iface.Virtualizer( TConst.VIRT_VMWARE, VirtualizerVmware.VIRTUALIZER_NAME ) );
	}

	@Override
	public List<ImageFormat> getSupportedImageFormats()
	{
		return VirtualizerVmware.VIRTUALIZER_SUPPORTED_IMAGE_FORMATS;
	}

	@Override
	public List<Version> getSupportedVersions()
	{
		return VirtualizerVmware.VIRTUALIZER_SUPPORTED_VERSIONS;
	}

}
