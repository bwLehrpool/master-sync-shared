package org.openslx.firmware;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Data representation of QEMU firmware specification files (*.json).
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class QemuFirmware
{
	/**
	 * Default QEMU firmware specification directory under Linux-based systems.
	 */
	public final static String DEFAULT_SPEC_DIR = "/usr/share/qemu/firmware";

	@SerializedName( "description" )
	private String description;
	@SerializedName( "interface-types" )
	private ArrayList<String> interfaceTypes;
	@SerializedName( "mapping" )
	private FirmwareMapping mapping;
	@SerializedName( "targets" )
	private ArrayList<FirmwareTarget> targets;
	@SerializedName( "features" )
	private ArrayList<String> features;
	@SerializedName( "tags" )
	private ArrayList<String> tags;

	public String getDescription()
	{
		return description;
	}

	public ArrayList<String> getInterfaceTypes()
	{
		return interfaceTypes;
	}

	public FirmwareMapping getMapping()
	{
		return mapping;
	}

	public ArrayList<FirmwareTarget> getTargets()
	{
		return targets;
	}

	public ArrayList<String> getFeatures()
	{
		return features;
	}

	public ArrayList<String> getTags()
	{
		return tags;
	}

	/**
	 * Parse QEMU firmware specification from firmware specification Json file.
	 * 
	 * @param fwSpecFile firmware specification Json file.
	 * @return QEMU firmware specification.
	 */
	public static QemuFirmware fromFwSpec( File fwSpecFile )
	{
		final Gson gson = new Gson();
		QemuFirmware firmware = null;

		try {
			final Reader jsonContent = new FileReader( fwSpecFile );
			firmware = gson.fromJson( jsonContent, QemuFirmware.class );
		} catch ( FileNotFoundException | NullPointerException | JsonSyntaxException | JsonIOException e ) {
			firmware = null;
		}

		return firmware;
	}
}

class FirmwareMapping
{
	@SerializedName( "device" )
	private String device;
	@SerializedName( "executable" )
	private FirmwareMappingExecutable executable;
	@SerializedName( "nvram-template" )
	private FirmwareMappingNvramTemplate nvramTemplate;

	public String getDevice()
	{
		return device;
	}

	public FirmwareMappingExecutable getExecutable()
	{
		return executable;
	}

	public FirmwareMappingNvramTemplate getNvramTemplate()
	{
		return nvramTemplate;
	}
}

class FirmwareMappingExecutable
{
	@SerializedName( "filename" )
	private String fileName;
	@SerializedName( "format" )
	private String format;

	public String getFileName()
	{
		return fileName;
	}

	public String getFormat()
	{
		return format;
	}
}

class FirmwareMappingNvramTemplate
{
	@SerializedName( "filename" )
	private String fileName;
	@SerializedName( "format" )
	private String format;

	public String getFileName()
	{
		return fileName;
	}

	public String getFormat()
	{
		return format;
	}
}

class FirmwareTarget
{
	@SerializedName( "architecture" )
	private String architecture;
	@SerializedName( "machines" )
	private ArrayList<String> machines;

	public String getArchitecture()
	{
		return architecture;
	}

	public ArrayList<String> getMachines()
	{
		return machines;
	}
}
