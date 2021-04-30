package org.openslx.virtualization.configuration;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.virtualization.Version;
import org.openslx.virtualization.virtualizer.VirtualizerDocker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

class DockerSoundCardMeta
{
}

class DockerDDAccelMeta
{
}

class DockerEthernetDevTypeMeta
{
}

class DockerUsbSpeedMeta
{
}

public class VirtualizationConfigurationDocker extends VirtualizationConfiguration<DockerSoundCardMeta, DockerDDAccelMeta, DockerEthernetDevTypeMeta, DockerUsbSpeedMeta> {

	/**
	 * File name extension for Docker virtualization configuration files.
	 */
	private static final String FILE_NAME_EXTENSION = null;

	private static final Logger LOGGER = Logger.getLogger( VirtualizationConfigurationDocker.class);

	/**
	 * containerDefinition is a serialized tar.gz archive and represents a
	 * ContainerDefinition. This archive contains a serialized Container Recipe (e.g. Dockerfile)
	 * and a ContainerMeta witch is serialized as a json file.
	 * <p>
	 * See ContainerDefintion in tutor-module (bwsuite).
	 * <p>
	 * This field is in vm context the machine description e.g. vmware = vmx.
	 * This field will be stored in table  imageversion.virtualizerconfig
	 */
	private byte[] containerDefinition;

	public VirtualizationConfigurationDocker(List<OperatingSystem> osList, File file) throws VirtualizationConfigurationException {
		super(new VirtualizerDocker(), osList);

		BufferedInputStream bis = null;
		
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			containerDefinition = new byte[(int) file.length()];
			bis.read(containerDefinition);

			checkIsTarGz();
		} catch (IOException | VirtualizationConfigurationException e) {
			LOGGER.error("Couldn't read dockerfile", e);
		} finally {
			try {
				bis.close();
			} catch ( IOException e ) {
				LOGGER.warn( "Could not close the input stream!" );
			}
		}
	}

	public VirtualizationConfigurationDocker(List<OperatingSystem> osList, byte[] vmContent, int length)
			throws VirtualizationConfigurationException {
		super(new VirtualizerDocker(), osList);

		containerDefinition = vmContent;

		checkIsTarGz();
	}

	/*
	TODO This is just a simple check to prevent the workflow from considering any content as acceptable.
	 */
	/**
	 * Checks if the first two bytes of the content identifies a tar.gz archive.
	 * The first byte is 31 == 0x1f, the second byte has to be -117 == 0x8b.
	 *
	 * @throws VirtualizationConfigurationException
	 */
	private void checkIsTarGz() throws VirtualizationConfigurationException {
		if (!((31 == containerDefinition[0]) && (-117 == containerDefinition[1]))) {
			LOGGER.warn("Not Supported Content.");
			throw new VirtualizationConfigurationException(
					"DockerMetaDataDummy: Not tar.gz encoded content!");
		}
	}

	@Override public void transformEditable() throws VirtualizationConfigurationException {

	}
	
	@Override
	public void transformPrivacy() throws VirtualizationConfigurationException {
	}

	@Override public boolean addHddTemplate(File diskImage, String hddMode, String redoDir) {
		return false;
	}

	@Override public boolean addHddTemplate(String diskImagePath, String hddMode, String redoDir) {
		return false;
	}

	@Override public boolean addDefaultNat() {
		return false;
	}

	@Override public void setOs(String vendorOsId) {

	}

	@Override public boolean addDisplayName(String name) {
		return false;
	}

	@Override public boolean addRam(int mem) {
		return false;
	}

	@Override public void addFloppy(int index, String image, boolean readOnly) {

	}

	@Override public boolean addCdrom(String image) {
		return false;
	}

	@Override public boolean addCpuCoreCount(int nrOfCores) {
		return false;
	}

	@Override public void setSoundCard(SoundCardType type) {

	}

	@Override public SoundCardType getSoundCard() {
		return SoundCardType.NONE;
	}

	@Override public void setDDAcceleration(DDAcceleration type) {

	}

	@Override public DDAcceleration getDDAcceleration() {
		return DDAcceleration.OFF;
	}

	@Override public void setVirtualizerVersion(Version type) {

	}

	@Override public Version getVirtualizerVersion() {
		return null;
	}

	@Override public void setEthernetDevType(int cardIndex, EthernetDevType type) {

	}

	@Override public EthernetDevType getEthernetDevType(int cardIndex) {
		return EthernetDevType.NONE;
	}

	@Override public void setMaxUsbSpeed(UsbSpeed speed) {

	}

	@Override public UsbSpeed getMaxUsbSpeed() {
		return UsbSpeed.NONE;
	}

	@Override public byte[] getConfigurationAsByteArray() {
		return this.containerDefinition;
	}

	@Override public boolean addEthernet(EtherType type) {
		return false;
	}

	@Override public void transformNonPersistent() throws VirtualizationConfigurationException {
		
	}

	@Override public void registerVirtualHW() {

	}

	@Override
	public String getFileNameExtension() {
		return VirtualizationConfigurationDocker.FILE_NAME_EXTENSION;
	}
	
	@Override
	public void validate() throws VirtualizationConfigurationException
	{
	}
}
