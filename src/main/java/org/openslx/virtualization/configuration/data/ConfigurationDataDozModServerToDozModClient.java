package org.openslx.virtualization.configuration.data;

import java.io.File;

import org.openslx.bwlp.thrift.iface.OperatingSystem;

public class ConfigurationDataDozModServerToDozModClient
{
	private final String displayName;
	private final File diskImage;
	private final OperatingSystem guestOs;
	private final String virtualizerId;
	private final int totalMemory;

	public ConfigurationDataDozModServerToDozModClient( String displayName, File diskImage, OperatingSystem guestOs,
			String virtualizerId, int totalMemory )
	{
		this.displayName = displayName;
		this.diskImage = diskImage;
		this.guestOs = guestOs;
		this.virtualizerId = virtualizerId;
		this.totalMemory = totalMemory;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public File getDiskImage()
	{
		return this.diskImage;
	}

	public OperatingSystem getGuestOs()
	{
		return this.guestOs;
	}

	public String getVirtualizerId()
	{
		return this.virtualizerId;
	}

	public int getTotalMemory()
	{
		return this.totalMemory;
	}
}
