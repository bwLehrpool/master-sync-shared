package org.openslx.virtualization.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.hardware.VirtOptionValue;
import org.openslx.virtualization.hardware.ConfigurationGroups;
import org.openslx.virtualization.virtualizer.Virtualizer;

/**
 * Describes a configured virtual machine. This class is parsed from a machine
 * description, like a *.vmx for VMware machines.
 */
public abstract class VirtualizationConfiguration
{
	private static final Logger LOGGER = LogManager.getLogger( VirtualizationConfiguration.class );

	private final Virtualizer virtualizer;


	public static enum DriveBusType
	{
		SCSI, IDE, SATA, NVME;
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

	public static enum EtherType
	{
		NAT, BRIDGED, HOST_ONLY;
	}
	
	public static class ConfigurableOptionGroup
	{
		public final ConfigurationGroups groupIdentifier;

		public final List<VirtOptionValue> availableOptions;

		public ConfigurableOptionGroup( ConfigurationGroups groupIdentifier, List<VirtOptionValue> availableOptions )
		{
			this.groupIdentifier = groupIdentifier;
			this.availableOptions = Collections.unmodifiableList( availableOptions );
		}
		
		public VirtOptionValue getSelected()
		{
			for (VirtOptionValue hw : availableOptions) {
				if ( hw.isActive() )
					return hw;
			}
			return null;
		}

	}
	
	/*
	 * Members
	 */

	protected final List<HardDisk> hdds = new ArrayList<HardDisk>();

	protected final List<OperatingSystem> osList;

	private OperatingSystem os = null;

	protected String displayName = null;

	protected boolean isMachineSnapshot;
	
	protected final List<ConfigurableOptionGroup> configurableOptions = new ArrayList<>();

	/**
	 * Get operating system of this VM.
	 * 
	 * @return operating system of the VM.
	 */
	public OperatingSystem getOs()
	{
		return os;
	}
	
	/**
	 * Sets the operating system for the virtualization configuration.
	 * 
	 * @param os operating system for the virtualization configuration.
	 */
	public void setOs( OperatingSystem os )
	{
		this.os = os;
	}

	/**
	 * Get all hard disks of this VM.
	 * 
	 * @return list of hard disks of the VM.
	 */
	public List<HardDisk> getHdds()
	{
		return Collections.unmodifiableList( hdds );
	}

	/**
	 * Get display name of VM.
	 * 
	 * @return display name of the VM.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/*
	 * Getter for isMachineSnapshot
	 */
	public boolean isMachineSnapshot()
	{
		return isMachineSnapshot;
	}
	
	private class VersionOption extends VirtOptionValue {
		
		private final Version version;
		
		public VersionOption( Version version )
		{
			super( Integer.toString( version.getVersion() ), version.getName() );
			this.version = version;
		}
		
		@Override
		public boolean isActive()
		{
			try {
				return getVirtualizerVersion().equals( version );
			} catch (NullPointerException e) {
				return false;
			}
		}
		
		@Override
		public void apply()
		{
			setVirtualizerVersion( version );
		}
	}

	/*
	 * Methods
	 */

	public VirtualizationConfiguration( Virtualizer virtualizer, List<OperatingSystem> osList )
	{
		this.virtualizer = virtualizer;
		
		if ( osList == null ) {
			// create empty operating system list if none is specified
			this.osList = new ArrayList<OperatingSystem>();
		} else {
			this.osList = osList;
		}

		// register virtual hardware models for graphical editing of virtual devices (GPU, sound, USB, ...)
		final List<Version> availables = this.getVirtualizer().getSupportedVersions();
		//Collections.sort( availables ); // XXX WTF? How did this not break before? It's an unmodifiable collection
		if ( availables != null ) {
			// XXX List is null for qemu?
			List<VirtOptionValue> list = new ArrayList<>();
			for ( Version ver : availables ) {
				list.add( new VersionOption( ver ) );
			}
			configurableOptions.add( new ConfigurableOptionGroup( ConfigurationGroups.HW_VERSION, list ) );
		}
		this.registerVirtualHW();
	}

	/**
	 * Returns a VmMetaData instance of the given machine description given as file
	 *
	 * @param osList List of supported operating systems
	 * @param file VM's machine description file to get the metadata instance from
	 * @return VmMetaData object representing the relevant parts of the given machine description
	 * @throws IOException failed to read machine description from specified file.
	 */
	public static VirtualizationConfiguration getInstance( List<OperatingSystem> osList, File file )
			throws IOException
	{
		try {
			return new VirtualizationConfigurationVmware( osList, file );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a VMware file", e );
		}
		try {
			return new VirtualizationConfigurationVirtualBox( osList, file );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a VirtualBox file", e );
		}
		try {
			return new VirtualizationConfigurationQemu( osList, file );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a Libvirt file", e );
		}
		try {
			return new VirtualizationConfigurationDocker( osList, file );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a tar.gz file, for docker container", e );
		}

		LOGGER.error( "Could not detect any known virtualizer format" );
		return null;
	}

	/**
	 * Returns a VmMetaData instance of the given machine description given as a byte array
	 *
	 * @param osList List of supported operating systems
	 * @param vmContent VM's machine description as byte array (e.g. stored in DB)
	 * @param length length of the byte array given as vmContent
	 * @return VmMetaData object representing the relevant parts of the given machine description
	 * @throws IOException failed to read machine description from specified byte stream.
	 * @throws VirtualizationConfigurationException 
	 */
	public static VirtualizationConfiguration getInstance( List<OperatingSystem> osList, byte[] vmContent,
			int length )
			throws IOException, VirtualizationConfigurationException
	{
		try {
			return new VirtualizationConfigurationVmware( osList, vmContent, length );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a VMware file", e );
		}
		try {
			return new VirtualizationConfigurationDocker( osList, vmContent, length );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a tar.gz file, for docker container", e );
		}
		try {
			return new VirtualizationConfigurationVirtualBox( osList, vmContent, length );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a VirtualBox file", e );
		}
		try {
			return new VirtualizationConfigurationQemu( osList, vmContent, length );
		} catch ( VirtualizationConfigurationException e ) {
			LOGGER.debug( "Not a Libvirt file", e );
		}
		throw new VirtualizationConfigurationException( "Unknown virtualizer config format" );
	}

	/**
	 * Returns the file name extension for the virtualization configuration file.
	 * 
	 * @return file name extension for the virtualization configuration file.
	 */
	public abstract String getFileNameExtension();

	public abstract boolean addEmptyHddTemplate();

	public abstract boolean addHddTemplate( File diskImage, String hddMode, String redoDir );

	public abstract boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir );

	public abstract boolean addDefaultNat();

	public abstract void setOs( String vendorOsId );

	public abstract boolean addDisplayName( String name );

	public abstract boolean addRam( int mem );

	public abstract void addFloppy( int index, String image, boolean readOnly );

	public abstract boolean addCdrom( String image );

	public abstract boolean addCpuCoreCount( int nrOfCores );
	
	public abstract void setVirtualizerVersion( Version type );

	public abstract Version getVirtualizerVersion();

	public abstract byte[] getConfigurationAsByteArray();

	public String getConfigurationAsString()
	{
		return new String( this.getConfigurationAsByteArray(), StandardCharsets.UTF_8 );
	}

	@Override
	public String toString()
	{
		return this.getConfigurationAsString();
	}

	public abstract boolean addEthernet( EtherType type );

	public Virtualizer getVirtualizer()
	{
		return this.virtualizer;
	}

	/**
	 * Validates the virtualization configuration and reports errors if its content is not a valid
	 * virtualization configuration.
	 * 
	 * @throws VirtualizationConfigurationException validation of the virtualization configuration
	 *            failed.
	 */
	public abstract void validate() throws VirtualizationConfigurationException;

	/**
	 * Transforms the virtualization configuration in terms of a privacy filter to filter out
	 * sensitive information like name of users in absolute paths.
	 * 
	 * @throws VirtualizationConfigurationException transformation of the virtualization
	 *            configuration failed.
	 */
	public abstract void transformPrivacy() throws VirtualizationConfigurationException;

	/**
	 * Transforms the virtualization configuration applying options that are desired when locally
	 * editing a virtualized system (e.g. disables automatic DPI scaling).
	 * 
	 * @throws VirtualizationConfigurationException transformation of the virtualization
	 *            configuration failed.
	 */
	public abstract void transformEditable() throws VirtualizationConfigurationException;

	/**
	 * Transforms the virtualization configuration applying options that are desired when running a
	 * virtualized system in a stateless manner.
	 * 
	 * @throws VirtualizationConfigurationException transformation of the virtualization
	 *            configuration failed.
	 */
	public abstract void transformNonPersistent() throws VirtualizationConfigurationException;

	/**
	 * Function used to register virtual devices.
	 */
	public abstract void registerVirtualHW();

	/**
	 * Get all config options this virtualizer supports, with all available options.
	 */
	public List<ConfigurableOptionGroup> getConfigurableOptions()
	{
		return Collections.unmodifiableList( configurableOptions );
	}
}
