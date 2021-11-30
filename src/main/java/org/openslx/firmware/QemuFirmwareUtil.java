package org.openslx.firmware;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.openslx.util.LevenshteinDistance;
import org.openslx.virtualization.configuration.transformation.TransformationException;

/**
 * Utilities to process QEMU firmware specification files.
 * 
 * @author Manuel Bentele
 * @version 1.0
 */
public class QemuFirmwareUtil
{
	/**
	 * Lookup a virtual machine's target OS loader based on QEMU firmware specification files.
	 * 
	 * @param fwSpecDir QEMU firmware specification directory of the target host.
	 * @param sourceOsLoader OS loader of the virtual machine.
	 * @param sourceOsArch OS architecture of the virtual machine.
	 * @param sourceOsMachine OS machine type of the virtual machine.
	 * @return Path to the new target OS loader file for the virtual machine.
	 * 
	 * @throws TransformationException Failed to lookup target OS loader for virtual machine.
	 */
	public static String lookupTargetOsLoader( String fwSpecDir, String sourceOsLoader, String sourceOsArch,
			String sourceOsMachine )
			throws FirmwareException
	{
		String lookupOsLoader = null;

		// parse and check firmware specification directory
		final File fwSpecDirFile = new File( fwSpecDir );
		if ( !fwSpecDirFile.exists() || !fwSpecDirFile.isDirectory() ) {
			throw new FirmwareException( "Path to QEMU firmware specifications directory is invalid!" );
		}

		// get all firmware specification files
		final FileFilter fwSpecFilesFilter = file -> !file.isDirectory() && file.getName().endsWith( ".json" );
		final File[] fwSpecFiles = fwSpecDirFile.listFiles( fwSpecFilesFilter );

		// get paths to firmware files from firmware specification files
		if ( fwSpecFiles != null ) {
			final ArrayList<QemuFirmware> uefiFirmwares = new ArrayList<QemuFirmware>();

			for ( final File fwSpecFile : fwSpecFiles ) {
				// parse the firmware file
				final QemuFirmware firmware = QemuFirmware.fromFwSpec( fwSpecFile );
				if ( firmware == null ) {
					throw new FirmwareException( "Firmware '" + fwSpecFile.toString() + "' can not be parsed correctly!" );
				} else {
					final Predicate<String> byInterfaceType = s -> s.toLowerCase().equals( "uefi" );
					if ( firmware.getInterfaceTypes().stream().filter( byInterfaceType ).findAny().isPresent() ) {
						// found valid UEFI firmware
						// check if architecture and machine type of the VM is supported by the firmware
						final Predicate<FirmwareTarget> byArchitecture = t -> sourceOsArch.equals( t.getArchitecture() );
						final Predicate<String> byMachineType = s -> sourceOsMachine.startsWith( s.replace( "*", "" ) );
						final Predicate<FirmwareTarget> byMachines = t -> t.getMachines().stream().filter( byMachineType )
								.findAny().isPresent();

						if ( firmware.getTargets().stream().filter( byArchitecture ).filter( byMachines ).findAny()
								.isPresent() ) {
							// found UEFI firmware supporting suitable architecture and machine type from VM
							uefiFirmwares.add( firmware );
						}
					}
				}
			}

			if ( uefiFirmwares.isEmpty() ) {
				throw new FirmwareException( "There aren't any suitable UEFI firmwares locally available!" );
			} else {
				final LevenshteinDistance distance = new LevenshteinDistance( 1, 1, 1 );
				int minFileNameDistance = Integer.MAX_VALUE;
				Path suitablestUefiFirmwarePath = null;

				for ( final QemuFirmware uefiFirmware : uefiFirmwares ) {
					final Path uefiFirmwarePath = Paths.get( uefiFirmware.getMapping().getExecutable().getFileName() );
					final Path sourceOsLoaderPath = Paths.get( sourceOsLoader );
					final String uefiFirmwareFileName = uefiFirmwarePath.getFileName().toString().toLowerCase();
					final String sourceOsLoaderFileName = sourceOsLoaderPath.getFileName().toString().toLowerCase();

					final int fileNameDistance = distance.calculateDistance( uefiFirmwareFileName, sourceOsLoaderFileName );
					if ( fileNameDistance < minFileNameDistance ) {
						minFileNameDistance = fileNameDistance;
						suitablestUefiFirmwarePath = uefiFirmwarePath;
					}
				}

				lookupOsLoader = suitablestUefiFirmwarePath.toString();
			}
		}

		return lookupOsLoader;
	}

	/**
	 * Lookup a virtual machine's target OS loader based on QEMU firmware specification files under
	 * the default path.
	 * 
	 * @param sourceOsLoader OS loader of the virtual machine.
	 * @param sourceOsArch OS architecture of the virtual machine.
	 * @param sourceOsMachine OS machine type of the virtual machine.
	 * @return Path to the new target OS loader file for the virtual machine.
	 * 
	 * @throws TransformationException Failed to lookup target OS loader for virtual machine.
	 */
	public static String lookupTargetOsLoaderDefaultFwSpecDir( String sourceOsLoader, String sourceOsArch,
			String sourceOsMachine )
			throws FirmwareException
	{
		return QemuFirmwareUtil.lookupTargetOsLoader( QemuFirmware.DEFAULT_SPEC_DIR, sourceOsLoader, sourceOsArch,
				sourceOsMachine );
	}
}
