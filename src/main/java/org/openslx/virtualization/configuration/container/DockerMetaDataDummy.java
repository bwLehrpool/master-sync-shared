package org.openslx.virtualization.configuration.container;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;
import org.openslx.virtualization.configuration.UnsupportedVirtualizerFormatException;
import org.openslx.virtualization.configuration.VmMetaData;
import org.openslx.vm.disk.DiskImage;
import org.openslx.vm.disk.DiskImage.ImageFormat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DockerSoundCardMeta
{
}

class DockerDDAccelMeta
{
}

class DockerHWVersionMeta
{
}

class DockerEthernetDevTypeMeta
{
}

class DockerUsbSpeedMeta
{
}

public class DockerMetaDataDummy extends VmMetaData<DockerSoundCardMeta, DockerDDAccelMeta, DockerHWVersionMeta, DockerEthernetDevTypeMeta, DockerUsbSpeedMeta> {

	/**
	 * List of supported image formats by the Docker hypervisor.
	 */
	private static final List<DiskImage.ImageFormat> SUPPORTED_IMAGE_FORMATS = Collections.unmodifiableList(
			Arrays.asList( ImageFormat.NONE ) );
	
	private static final Logger LOGGER = Logger.getLogger( DockerMetaDataDummy.class);

	private final Virtualizer virtualizer = new Virtualizer(TConst.VIRT_DOCKER, "Docker");

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

	public DockerMetaDataDummy(List<OperatingSystem> osList, File file) throws UnsupportedVirtualizerFormatException {
		super(osList);

		BufferedInputStream bis = null;
		
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			containerDefinition = new byte[(int) file.length()];
			bis.read(containerDefinition);

			checkIsTarGz();
		} catch (IOException | UnsupportedVirtualizerFormatException e) {
			LOGGER.error("Couldn't read dockerfile", e);
		} finally {
			try {
				bis.close();
			} catch ( IOException e ) {
				LOGGER.warn( "Could not close the input stream!" );
			}
		}
	}

	public DockerMetaDataDummy(List<OperatingSystem> osList, byte[] vmContent, int length)
			throws UnsupportedVirtualizerFormatException {
		super(osList);

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
	 * @throws UnsupportedVirtualizerFormatException
	 */
	private void checkIsTarGz() throws UnsupportedVirtualizerFormatException {
		if (!((31 == containerDefinition[0]) && (-117 == containerDefinition[1]))) {
			LOGGER.warn("Not Supported Content.");
			throw new UnsupportedVirtualizerFormatException(
					"DockerMetaDataDummy: Not tar.gz encoded content!");
		}
	}

	@Override public byte[] getFilteredDefinitionArray() {
		return containerDefinition;
	}
	
	@Override
	public List<DiskImage.ImageFormat> getSupportedImageFormats()
	{
		return DockerMetaDataDummy.SUPPORTED_IMAGE_FORMATS;
	}

	@Override public void applySettingsForLocalEdit() {

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

	@Override public void setHWVersion(HWVersion type) {

	}

	@Override public HWVersion getHWVersion() {
		return HWVersion.DEFAULT;
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

	@Override public byte[] getDefinitionArray() {
		return new byte[0];
	}

	@Override public boolean addEthernet(EtherType type) {
		return false;
	}

	@Override public Virtualizer getVirtualizer() {
		return virtualizer;
	}

	@Override public boolean tweakForNonPersistent() {
		return false;
	}

	@Override public void registerVirtualHW() {

	}
}
