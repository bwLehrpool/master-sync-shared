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

public class TransferInformation implements org.apache.thrift.TBase<TransferInformation, TransferInformation._Fields>, java.io.Serializable, Cloneable, Comparable<TransferInformation> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TransferInformation");

  private static final org.apache.thrift.protocol.TField TOKEN_FIELD_DESC = new org.apache.thrift.protocol.TField("token", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField PLAIN_PORT_FIELD_DESC = new org.apache.thrift.protocol.TField("plainPort", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField SSL_PORT_FIELD_DESC = new org.apache.thrift.protocol.TField("sslPort", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TransferInformationStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TransferInformationTupleSchemeFactory());
  }

  public String token; // required
  public int plainPort; // required
  public int sslPort; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TOKEN((short)1, "token"),
    PLAIN_PORT((short)2, "plainPort"),
    SSL_PORT((short)3, "sslPort");

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
        case 1: // TOKEN
          return TOKEN;
        case 2: // PLAIN_PORT
          return PLAIN_PORT;
        case 3: // SSL_PORT
          return SSL_PORT;
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
  private static final int __PLAINPORT_ISSET_ID = 0;
  private static final int __SSLPORT_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TOKEN, new org.apache.thrift.meta_data.FieldMetaData("token", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PLAIN_PORT, new org.apache.thrift.meta_data.FieldMetaData("plainPort", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SSL_PORT, new org.apache.thrift.meta_data.FieldMetaData("sslPort", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TransferInformation.class, metaDataMap);
  }

  public TransferInformation() {
  }

  public TransferInformation(
    String token,
    int plainPort,
    int sslPort)
  {
    this();
    this.token = token;
    this.plainPort = plainPort;
    setPlainPortIsSet(true);
    this.sslPort = sslPort;
    setSslPortIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TransferInformation(TransferInformation other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetToken()) {
      this.token = other.token;
    }
    this.plainPort = other.plainPort;
    this.sslPort = other.sslPort;
  }

  public TransferInformation deepCopy() {
    return new TransferInformation(this);
  }

  @Override
  public void clear() {
    this.token = null;
    setPlainPortIsSet(false);
    this.plainPort = 0;
    setSslPortIsSet(false);
    this.sslPort = 0;
  }

  public String getToken() {
    return this.token;
  }

  public TransferInformation setToken(String token) {
    this.token = token;
    return this;
  }

  public void unsetToken() {
    this.token = null;
  }

  /** Returns true if field token is set (has been assigned a value) and false otherwise */
  public boolean isSetToken() {
    return this.token != null;
  }

  public void setTokenIsSet(boolean value) {
    if (!value) {
      this.token = null;
    }
  }

  public int getPlainPort() {
    return this.plainPort;
  }

  public TransferInformation setPlainPort(int plainPort) {
    this.plainPort = plainPort;
    setPlainPortIsSet(true);
    return this;
  }

  public void unsetPlainPort() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PLAINPORT_ISSET_ID);
  }

  /** Returns true if field plainPort is set (has been assigned a value) and false otherwise */
  public boolean isSetPlainPort() {
    return EncodingUtils.testBit(__isset_bitfield, __PLAINPORT_ISSET_ID);
  }

  public void setPlainPortIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PLAINPORT_ISSET_ID, value);
  }

  public int getSslPort() {
    return this.sslPort;
  }

  public TransferInformation setSslPort(int sslPort) {
    this.sslPort = sslPort;
    setSslPortIsSet(true);
    return this;
  }

  public void unsetSslPort() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SSLPORT_ISSET_ID);
  }

  /** Returns true if field sslPort is set (has been assigned a value) and false otherwise */
  public boolean isSetSslPort() {
    return EncodingUtils.testBit(__isset_bitfield, __SSLPORT_ISSET_ID);
  }

  public void setSslPortIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SSLPORT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TOKEN:
      if (value == null) {
        unsetToken();
      } else {
        setToken((String)value);
      }
      break;

    case PLAIN_PORT:
      if (value == null) {
        unsetPlainPort();
      } else {
        setPlainPort((Integer)value);
      }
      break;

    case SSL_PORT:
      if (value == null) {
        unsetSslPort();
      } else {
        setSslPort((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TOKEN:
      return getToken();

    case PLAIN_PORT:
      return Integer.valueOf(getPlainPort());

    case SSL_PORT:
      return Integer.valueOf(getSslPort());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TOKEN:
      return isSetToken();
    case PLAIN_PORT:
      return isSetPlainPort();
    case SSL_PORT:
      return isSetSslPort();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TransferInformation)
      return this.equals((TransferInformation)that);
    return false;
  }

  public boolean equals(TransferInformation that) {
    if (that == null)
      return false;

    boolean this_present_token = true && this.isSetToken();
    boolean that_present_token = true && that.isSetToken();
    if (this_present_token || that_present_token) {
      if (!(this_present_token && that_present_token))
        return false;
      if (!this.token.equals(that.token))
        return false;
    }

    boolean this_present_plainPort = true;
    boolean that_present_plainPort = true;
    if (this_present_plainPort || that_present_plainPort) {
      if (!(this_present_plainPort && that_present_plainPort))
        return false;
      if (this.plainPort != that.plainPort)
        return false;
    }

    boolean this_present_sslPort = true;
    boolean that_present_sslPort = true;
    if (this_present_sslPort || that_present_sslPort) {
      if (!(this_present_sslPort && that_present_sslPort))
        return false;
      if (this.sslPort != that.sslPort)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(TransferInformation other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetToken()).compareTo(other.isSetToken());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetToken()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.token, other.token);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPlainPort()).compareTo(other.isSetPlainPort());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPlainPort()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.plainPort, other.plainPort);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSslPort()).compareTo(other.isSetSslPort());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSslPort()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sslPort, other.sslPort);
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
    StringBuilder sb = new StringBuilder("TransferInformation(");
    boolean first = true;

    sb.append("token:");
    if (this.token == null) {
      sb.append("null");
    } else {
      sb.append(this.token);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("plainPort:");
    sb.append(this.plainPort);
    first = false;
    if (!first) sb.append(", ");
    sb.append("sslPort:");
    sb.append(this.sslPort);
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

  private static class TransferInformationStandardSchemeFactory implements SchemeFactory {
    public TransferInformationStandardScheme getScheme() {
      return new TransferInformationStandardScheme();
    }
  }

  private static class TransferInformationStandardScheme extends StandardScheme<TransferInformation> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TransferInformation struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TOKEN
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.token = iprot.readString();
              struct.setTokenIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // PLAIN_PORT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.plainPort = iprot.readI32();
              struct.setPlainPortIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SSL_PORT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.sslPort = iprot.readI32();
              struct.setSslPortIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TransferInformation struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.token != null) {
        oprot.writeFieldBegin(TOKEN_FIELD_DESC);
        oprot.writeString(struct.token);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(PLAIN_PORT_FIELD_DESC);
      oprot.writeI32(struct.plainPort);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(SSL_PORT_FIELD_DESC);
      oprot.writeI32(struct.sslPort);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TransferInformationTupleSchemeFactory implements SchemeFactory {
    public TransferInformationTupleScheme getScheme() {
      return new TransferInformationTupleScheme();
    }
  }

  private static class TransferInformationTupleScheme extends TupleScheme<TransferInformation> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TransferInformation struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetToken()) {
        optionals.set(0);
      }
      if (struct.isSetPlainPort()) {
        optionals.set(1);
      }
      if (struct.isSetSslPort()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetToken()) {
        oprot.writeString(struct.token);
      }
      if (struct.isSetPlainPort()) {
        oprot.writeI32(struct.plainPort);
      }
      if (struct.isSetSslPort()) {
        oprot.writeI32(struct.sslPort);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TransferInformation struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.token = iprot.readString();
        struct.setTokenIsSet(true);
      }
      if (incoming.get(1)) {
        struct.plainPort = iprot.readI32();
        struct.setPlainPortIsSet(true);
      }
      if (incoming.get(2)) {
        struct.sslPort = iprot.readI32();
        struct.setSslPortIsSet(true);
      }
    }
  }

}
