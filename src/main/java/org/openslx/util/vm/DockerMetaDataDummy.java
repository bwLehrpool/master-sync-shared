package org.openslx.util.vm;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.vm.DiskImage.ImageFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DockerMetaDataDummy extends VmMetaData {
	// TODO Define DOCKER CONSTANT

	/**
	 * List of supported image formats by the Docker hypervisor.
	 */
	private static final List<DiskImage.ImageFormat> SUPPORTED_IMAGE_FORMATS = Collections.unmodifiableList(
			Arrays.asList( ImageFormat.DOCKER ) );
	
	private static final Logger LOGGER = Logger.getLogger( DockerMetaDataDummy.class);

	private final Virtualizer virtualizer = new Virtualizer( TConst.VIRT_DOCKER, "Docker" );

	/* this field is in vm context the machine description
	e.g. vmware = vmx.
	This field will be stored in table  imageversion.virtualizerconfig
	*/
	private byte[] dockerfile;

	public DockerMetaDataDummy(List osList, File file) {
		super(osList);

		try {
			BufferedInputStream  bis = new BufferedInputStream(new FileInputStream(file));
			dockerfile = new byte[(int) file.length()];
			bis.read(dockerfile);
		} catch (IOException e) {
			LOGGER.error("Couldn't read dockerfile",e);
		}
	}

	public DockerMetaDataDummy(List osList, byte[] vmContent, int length) {
		super(osList);

		dockerfile = vmContent;
	}

	@Override public byte[] getFilteredDefinitionArray() {
		return dockerfile;
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
