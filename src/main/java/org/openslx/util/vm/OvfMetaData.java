package org.openslx.util.vm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.openslx.bwlp.thrift.iface.OperatingSystem;
import org.openslx.bwlp.thrift.iface.Virtualizer;
import org.openslx.thrifthelper.TConst;
import org.openslx.util.vm.VboxConfig.PlaceHolder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class handling the metadata of ovf images. Only needed
 * until the ovf has been converted into a vmx.
 */
public class OvfMetaData extends VmMetaData {

    private static final Logger LOGGER = Logger.getLogger(OvfMetaData.class);

    private final OvfConfig config;

    public OvfMetaData(List<OperatingSystem> osList, File file)
            throws IOException, UnsupportedVirtualizerFormatException {
        super(osList);
        LOGGER.info("Constructor worked");
        this.config = new OvfConfig(file);
        LOGGER.info("Creating config worked");
        init();
        LOGGER.info("Init worked");
    }

    private void init() {
        registerVirtualHW();
        displayName = config.getDisplayName();
        // setOs( config.getOsName() );
        // this.isMachineSnapshot = config.isMachineSnapshot();
        // for ( HardDisk hardDisk : config.getHdds() ) {
        // 	hdds.add( hardDisk );
        // }
    }

    @Override
    public byte[] getFilteredDefinitionArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void applySettingsForLocalEdit() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addHddTemplate(File diskImage, String hddMode, String redoDir) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addHddTemplate(String diskImagePath, String hddMode, String redoDir) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addDefaultNat() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setOs(String vendorOsId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addDisplayName(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addRam(int mem) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addFloppy(int index, String image, boolean readOnly) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addCdrom(String image) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addCpuCoreCount(int nrOfCores) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setSoundCard(SoundCardType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public SoundCardType getSoundCard() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDDAcceleration(DDAcceleration type) {
        // TODO Auto-generated method stub

    }

    @Override
    public DDAcceleration getDDAcceleration() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHWVersion(HWVersion type) {
        // TODO Auto-generated method stub

    }

    @Override
    public HWVersion getHWVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEthernetDevType(int cardIndex, EthernetDevType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public EthernetDevType getEthernetDevType(int cardIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMaxUsbSpeed(UsbSpeed speed) {
        // TODO Auto-generated method stub

    }

    @Override
    public UsbSpeed getMaxUsbSpeed() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getDefinitionArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addEthernet(EtherType type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Virtualizer getVirtualizer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean tweakForNonPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void registerVirtualHW() {
        // TODO Auto-generated method stub

    }

}