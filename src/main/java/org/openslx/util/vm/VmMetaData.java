package org.openslx.util.vm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;

/**
 * Describes a configured virtual machine. This class is parsed from a machine description, like a
 * *.vmx for VMware machines.
 */
public abstract class VmMetaData
{

	/*
	 * Helper types
	 */

	public static enum DriveBusType
	{
		SCSI,
		IDE,
		SATA;
	}

	public static class HardDisk
	{
		public final String chipsetDriver;
		public final DriveBusType bus;
		public final String diskImage;

		public HardDisk( String chipsetDriver, DriveBusType bus, String diskImage )
		{
			this.chipsetDriver = chipsetDriver;
			this.bus = bus;
			this.diskImage = diskImage;
		}
	}

	/*
	 * Members
	 */

	protected final List<HardDisk> hdds = new ArrayList<>();

	private final List<OperatingSystem> osList;

	private OperatingSystem os = null;

	protected String displayName = null;

	/*
	 * Guettas
	 */

	/**
	 * Get operating system of this VM.
	 */
	public OperatingSystem getOs()
	{
		return os;
	}

	/**
	 * Get all hard disks of this VM.
	 */
	public List<HardDisk> getHdds()
	{
		return Collections.unmodifiableList( hdds );
	}

	/**
	 * Get display name of VM.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * This method should return a minimal representation of the input meta data. The representation
	 * is platform dependent, and should be stripped of all non-essential configuration, such as
	 * CD/DVD/FLoppy drives, serial or parallel ports, shared folders, or anything else that could be
	 * considered sensible information (absolute paths containing the local user's name).
	 */
	public abstract byte[] getFilteredDefinitionArray();

	public final ByteBuffer getFilteredDefinition()
	{
		return ByteBuffer.wrap( getFilteredDefinitionArray() );
	}

	/*
	 * Methods
	 */

	public VmMetaData( List<OperatingSystem> osList )
	{
		this.osList = osList;
	}

	/**
	 * Called from subclass to set the OS. If the OS cannot be determined from the given parameters,
	 * it will not be set.
	 * 
	 * @param virtId virtualizer, eg "vmware" for VMware
	 * @param virtOsId the os identifier used by the virtualizer, eg. windows7-64 for 64bit Windows 7
	 *           on VMware
	 */
	protected final void setOs( String virtId, String virtOsId )
	{
		OperatingSystem lazyMatch = null;
		for ( OperatingSystem os : osList ) {
			if ( os.getVirtualizerOsId() == null )
				continue;
			for ( Entry<String, String> entry : os.getVirtualizerOsId().entrySet() ) {
				if ( !entry.getValue().equals( virtOsId ) )
					continue;
				if ( entry.getKey().equals( virtId ) ) {
					this.os = os;
					return;
				} else {
					lazyMatch = os;
				}
			}
		}
		this.os = lazyMatch;
	}

	public abstract Virtualizer getVirtualizer();
	
	public abstract void enableUsb(boolean enabled);

}
