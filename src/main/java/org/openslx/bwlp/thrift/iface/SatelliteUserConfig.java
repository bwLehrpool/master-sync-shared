/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.openslx.bwlp.thrift.iface;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SatelliteUserConfig implements org.apache.thrift.TBase<SatelliteUserConfig, SatelliteUserConfig._Fields>, java.io.Serializable, Cloneable, Comparable<SatelliteUserConfig> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SatelliteUserConfig");

  private static final org.apache.thrift.protocol.TField EMAIL_NOTIFICATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("emailNotifications", org.apache.thrift.protocol.TType.BOOL, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new SatelliteUserConfigStandardSchemeFactory());
    schemes.put(TupleScheme.class, new SatelliteUserConfigTupleSchemeFactory());
  }

  public boolean emailNotifications; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    EMAIL_NOTIFICATIONS((short)1, "emailNotifications");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // EMAIL_NOTIFICATIONS
          return EMAIL_NOTIFICATIONS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __EMAILNOTIFICATIONS_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.EMAIL_NOTIFICATIONS, new org.apache.thrift.meta_data.FieldMetaData("emailNotifications", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SatelliteUserConfig.class, metaDataMap);
  }

  public SatelliteUserConfig() {
  }

  public SatelliteUserConfig(
    boolean emailNotifications)
  {
    this();
    this.emailNotifications = emailNotifications;
    setEmailNotificationsIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public SatelliteUserConfig(SatelliteUserConfig other) {
    __isset_bitfield = other.__isset_bitfield;
    this.emailNotifications = other.emailNotifications;
  }

  public SatelliteUserConfig deepCopy() {
    return new SatelliteUserConfig(this);
  }

  @Override
  public void clear() {
    setEmailNotificationsIsSet(false);
    this.emailNotifications = false;
  }

  public boolean isEmailNotifications() {
    return this.emailNotifications;
  }

  public SatelliteUserConfig setEmailNotifications(boolean emailNotifications) {
    this.emailNotifications = emailNotifications;
    setEmailNotificationsIsSet(true);
    return this;
  }

  public void unsetEmailNotifications() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __EMAILNOTIFICATIONS_ISSET_ID);
  }

  /** Returns true if field emailNotifications is set (has been assigned a value) and false otherwise */
  public boolean isSetEmailNotifications() {
    return EncodingUtils.testBit(__isset_bitfield, __EMAILNOTIFICATIONS_ISSET_ID);
  }

  public void setEmailNotificationsIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __EMAILNOTIFICATIONS_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case EMAIL_NOTIFICATIONS:
      if (value == null) {
        unsetEmailNotifications();
      } else {
        setEmailNotifications((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case EMAIL_NOTIFICATIONS:
      return Boolean.valueOf(isEmailNotifications());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case EMAIL_NOTIFICATIONS:
      return isSetEmailNotifications();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof SatelliteUserConfig)
      return this.equals((SatelliteUserConfig)that);
    return false;
  }

  public boolean equals(SatelliteUserConfig that) {
    if (that == null)
      return false;

    boolean this_present_emailNotifications = true;
    boolean that_present_emailNotifications = true;
    if (this_present_emailNotifications || that_present_emailNotifications) {
      if (!(this_present_emailNotifications && that_present_emailNotifications))
        return false;
      if (this.emailNotifications != that.emailNotifications)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(SatelliteUserConfig other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetEmailNotifications()).compareTo(other.isSetEmailNotifications());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEmailNotifications()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.emailNotifications, other.emailNotifications);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("SatelliteUserConfig(");
    boolean first = true;

    sb.append("emailNotifications:");
    sb.append(this.emailNotifications);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class SatelliteUserConfigStandardSchemeFactory implements SchemeFactory {
    public SatelliteUserConfigStandardScheme getScheme() {
      return new SatelliteUserConfigStandardScheme();
    }
  }

  private static class SatelliteUserConfigStandardScheme extends StandardScheme<SatelliteUserConfig> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, SatelliteUserConfig struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // EMAIL_NOTIFICATIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.emailNotifications = iprot.readBool();
              struct.setEmailNotificationsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, SatelliteUserConfig struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(EMAIL_NOTIFICATIONS_FIELD_DESC);
      oprot.writeBool(struct.emailNotifications);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class SatelliteUserConfigTupleSchemeFactory implements SchemeFactory {
    public SatelliteUserConfigTupleScheme getScheme() {
      return new SatelliteUserConfigTupleScheme();
    }
  }

  private static class SatelliteUserConfigTupleScheme extends TupleScheme<SatelliteUserConfig> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, SatelliteUserConfig struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetEmailNotifications()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetEmailNotifications()) {
        oprot.writeBool(struct.emailNotifications);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, SatelliteUserConfig struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.emailNotifications = iprot.readBool();
        struct.setEmailNotificationsIsSet(true);
      }
    }
  }

}
