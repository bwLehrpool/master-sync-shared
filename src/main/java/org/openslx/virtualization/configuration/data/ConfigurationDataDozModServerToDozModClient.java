package org.openslx.virtualization.configuration.data;

import java.io.File;

import org.openslx.bwlp.thrift.iface.OperatingSystem;

/**
 * Data container to collect and store input arguments for a
 * {@link ConfigurationLogicDozModServerToDozModClient} transformation.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class ConfigurationDataDozModServerToDozModClient
{
	/**
	 * Display name for a transformation of a virtualization configuration.
	 */
	private final String displayName;

	/**
	 * Disk image file for a transformation of a virtualization configuration.
	 */
	private final File diskImage;

	/**
	 * Guest operating system for a transformation of a virtualization configuration.
	 */
	private final OperatingSystem guestOs;

	/**
	 * Virtualizer identifier for a transformation of a virtualization configuration.
	 */
	private final String virtualizerId;

	/**
	 * Total amount of available memory for a transformation of a virtualization configuration.
	 */
	private final int totalMemory;

	/**
	 * Creates a new data container to collect and store input arguments for a
	 * {@link ConfigurationLogicDozModServerToDozModClient} transformation.
	 * 
	 * @param displayName display name for a transformation of a virtualization configuration.
	 * @param diskImage disk image file for a transformation of a virtualization configuration.
	 * @param guestOs guest operating system for a transformation of a virtualization configuration.
	 * @param virtualizerId virtualizer identifier for a transformation of a virtualization
	 *           configuration.
	 * @param totalMemory total amount of available memory for a transformation of a virtualization
	 *           configuration.
	 */
	public ConfigurationDataDozModServerToDozModClient( String displayName, File diskImage, OperatingSystem guestOs,
			String virtualizerId, int totalMemory )
	{
		this.displayName = displayName;
		this.diskImage = diskImage;
		this.guestOs = guestOs;
		this.virtualizerId = virtualizerId;
		this.totalMemory = totalMemory;
	}

	/**
	 * Returns the display name for a transformation of a virtualization configuration.
	 * 
	 * @return display name for a transformation of a virtualization configuration.
	 */
	public String getDisplayName()
	{
		return this.displayName;
	}

	/**
	 * Returns the disk image file for a transformation of a virtualization configuration.
	 * 
	 * @return disk image file for a transformation of a virtualization configuration.
	 */
	public File getDiskImage()
	{
		return this.diskImage;
	}

	/**
	 * Returns the guest operating system for a transformation of a virtualization configuration.
	 * 
	 * @return guest operating system for a transformation of a virtualization configuration.
	 */
	public OperatingSystem getGuestOs()
	{
		return this.guestOs;
	}

	/**
	 * Returns the virtualizer identifier for a transformation of a virtualization configuration.
	 * 
	 * @return virtualizer identifier for a transformation of a virtualization configuration.
	 */
	public String getVirtualizerId()
	{
		return this.virtualizerId;
	}

	/**
	 * Returns the total amount of available memory for a transformation of a virtualization
	 * configuration.
	 * 
	 * @return total amount of available memory for a transformation of a virtualization
	 *         configuration.
	 */
	public int getTotalMemory()
	{
		return this.totalMemory;
	}
}
