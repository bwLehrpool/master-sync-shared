package org.openslx.util.vm;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;

public class QemuMetaData extends VmMetaData<VBoxSoundCardMeta, VBoxDDAccelMeta, VBoxHWVersionMeta, VBoxEthernetDevTypeMeta>
{

	private Map<String, String> arguments = new HashMap<String, String>();
	// the above map's elements will take the place of <args> in the config string
	private static String config = "qemu-system-i386 <args> <image> -enable-kvm \n\r qemu-system-x86_64 <args> <image> -enable-kvm";
	private static final Logger LOGGER = Logger.getLogger( QemuMetaData.class );

	private static final Virtualizer virtualizer = new Virtualizer( "qemukvm", "QEMU-KVM" );

	public QemuMetaData( List<OperatingSystem> osList, File file )
	{
		super( osList );
		displayName = file.getName().substring( 0, file.getName().indexOf( "." ) );
		setOs( "qemukvm", "anyOs" );
		hdds.add( new HardDisk( "anychipset", DriveBusType.IDE, file.getAbsolutePath() ) );
		makeStartSequence();
	}

	public QemuMetaData( List<OperatingSystem> osList, byte[] vmContent )
	{
		super( osList );
		config = new String( vmContent );
		displayName = "QemuVM";
		setOs( "qemukvm", "anyOs" );
	}

	// initiates the arguments map with a default working sequence that will later be used in the definition array
	public void makeStartSequence()
	{
		arguments.put( "cpu", "host" );
		arguments.put( "smp", "2" );
		arguments.put( "m", "1024" );
		arguments.put( "vga", "std" );
	}

	private String configWithArgs()
	{
		String tempString = "";
		for ( String key : arguments.keySet() ) {
			tempString += "-" + key + " " + arguments.get( key ) + " ";
		}
		return config.replaceAll( "<args>", tempString );
	}

	@Override
	public byte[] getFilteredDefinitionArray()
	{
		return configWithArgs().getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public void applySettingsForLocalEdit()
	{
	}

	@Override
	public boolean addHddTemplate( File diskImage, String hddMode, String redoDir )
	{
		String tempS = config.replaceAll( "<image>", diskImage.getAbsolutePath() );
		config = tempS;
		hdds.add( new HardDisk( "anychipset", DriveBusType.IDE, diskImage.getAbsolutePath() ) );
		return true;
	}

	@Override
	public boolean addHddTemplate( String diskImagePath, String hddMode, String redoDir )
	{
		String tempS = config.replaceAll( "<image>", diskImagePath );
		config = tempS;
		hdds.add( new HardDisk( "anychipset", DriveBusType.IDE, diskImagePath ) );
		return true;
	}

	@Override
	public boolean addDefaultNat()
	{
		return true;
	}

	@Override
	public void setOs( String vendorOsId )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addDisplayName( String name )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addRam( int mem )
	{
		this.arguments.put( "m", Integer.toString( mem ) );
		return true;
	}

	@Override
	public void addFloppy( int index, String image, boolean readOnly )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addCdrom( String image )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addCpuCoreCount( int nrOfCores )
	{
		this.arguments.put( "smp", Integer.toString( nrOfCores ) );
		return true;
	}

	@Override
	public void setSoundCard( VmMetaData.SoundCardType type )
	{
	}

	@Override
	public VmMetaData.SoundCardType getSoundCard()
	{
		return null;
	}

	@Override
	public void setDDAcceleration( VmMetaData.DDAcceleration type )
	{
	}

	@Override
	public VmMetaData.DDAcceleration getDDAcceleration()
	{
		return null;
	}

	@Override
	public void setHWVersion( VmMetaData.HWVersion type )
	{
	}

	@Override
	public VmMetaData.HWVersion getHWVersion()
	{
		return null;
	}

	@Override
	public void setEthernetDevType( int cardIndex, VmMetaData.EthernetDevType type )
	{
	}

	@Override
	public VmMetaData.EthernetDevType getEthernetDevType( int cardIndex )
	{
		return null;
	}

	@Override
	public byte[] getDefinitionArray()
	{
		return configWithArgs().getBytes( StandardCharsets.UTF_8 );
	}

	@Override
	public boolean addEthernet( VmMetaData.EtherType type )
	{
		return false;
	}

	@Override
	public Virtualizer getVirtualizer()
	{
		return virtualizer;
	}

	@Override
	public void enableUsb( boolean enabled )
	{
		// TODO test this properly
		if ( enabled ) {
			arguments.put( "usb", "" );
		} else {
			arguments.remove( "usb" );
		}
	}

	@Override
	public boolean disableSuspend()
	{
		return false;
	}

	@Override
	public void registerVirtualHW()
	{
	}

}