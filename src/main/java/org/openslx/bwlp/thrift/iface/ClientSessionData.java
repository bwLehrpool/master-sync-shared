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

public class ClientSessionData implements org.apache.thrift.TBase<ClientSessionData, ClientSessionData._Fields>, java.io.Serializable, Cloneable, Comparable<ClientSessionData> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ClientSessionData");

  private static final org.apache.thrift.protocol.TField SESSION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("sessionId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField AUTH_TOKEN_FIELD_DESC = new org.apache.thrift.protocol.TField("authToken", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField SATELLITES_FIELD_DESC = new org.apache.thrift.protocol.TField("satellites", org.apache.thrift.protocol.TType.LIST, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ClientSessionDataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ClientSessionDataTupleSchemeFactory());
  }

  public String sessionId; // required
  public String authToken; // required
  public List<Satellite> satellites; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SESSION_ID((short)1, "sessionId"),
    AUTH_TOKEN((short)2, "authToken"),
    SATELLITES((short)3, "satellites");

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
        case 1: // SESSION_ID
          return SESSION_ID;
        case 2: // AUTH_TOKEN
          return AUTH_TOKEN;
        case 3: // SATELLITES
          return SATELLITES;
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
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SESSION_ID, new org.apache.thrift.meta_data.FieldMetaData("sessionId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Token")));
    tmpMap.put(_Fields.AUTH_TOKEN, new org.apache.thrift.meta_data.FieldMetaData("authToken", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , "Token")));
    tmpMap.put(_Fields.SATELLITES, new org.apache.thrift.meta_data.FieldMetaData("satellites", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Satellite.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ClientSessionData.class, metaDataMap);
  }

  public ClientSessionData() {
  }

  public ClientSessionData(
    String sessionId,
    String authToken,
    List<Satellite> satellites)
  {
    this();
    this.sessionId = sessionId;
    this.authToken = authToken;
    this.satellites = satellites;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ClientSessionData(ClientSessionData other) {
    if (other.isSetSessionId()) {
      this.sessionId = other.sessionId;
    }
    if (other.isSetAuthToken()) {
      this.authToken = other.authToken;
    }
    if (other.isSetSatellites()) {
      List<Satellite> __this__satellites = new ArrayList<Satellite>(other.satellites.size());
      for (Satellite other_element : other.satellites) {
        __this__satellites.add(new Satellite(other_element));
      }
      this.satellites = __this__satellites;
    }
  }

  public ClientSessionData deepCopy() {
    return new ClientSessionData(this);
  }

  @Override
  public void clear() {
    this.sessionId = null;
    this.authToken = null;
    this.satellites = null;
  }

  public String getSessionId() {
    return this.sessionId;
  }

  public ClientSessionData setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public void unsetSessionId() {
    this.sessionId = null;
  }

  /** Returns true if field sessionId is set (has been assigned a value) and false otherwise */
  public boolean isSetSessionId() {
    return this.sessionId != null;
  }

  public void setSessionIdIsSet(boolean value) {
    if (!value) {
      this.sessionId = null;
    }
  }

  public String getAuthToken() {
    return this.authToken;
  }

  public ClientSessionData setAuthToken(String authToken) {
    this.authToken = authToken;
    return this;
  }

  public void unsetAuthToken() {
    this.authToken = null;
  }

  /** Returns true if field authToken is set (has been assigned a value) and false otherwise */
  public boolean isSetAuthToken() {
    return this.authToken != null;
  }

  public void setAuthTokenIsSet(boolean value) {
    if (!value) {
      this.authToken = null;
    }
  }

  public int getSatellitesSize() {
    return (this.satellites == null) ? 0 : this.satellites.size();
  }

  public java.util.Iterator<Satellite> getSatellitesIterator() {
    return (this.satellites == null) ? null : this.satellites.iterator();
  }

  public void addToSatellites(Satellite elem) {
    if (this.satellites == null) {
      this.satellites = new ArrayList<Satellite>();
    }
    this.satellites.add(elem);
  }

  public List<Satellite> getSatellites() {
    return this.satellites;
  }

  public ClientSessionData setSatellites(List<Satellite> satellites) {
    this.satellites = satellites;
    return this;
  }

  public void unsetSatellites() {
    this.satellites = null;
  }

  /** Returns true if field satellites is set (has been assigned a value) and false otherwise */
  public boolean isSetSatellites() {
    return this.satellites != null;
  }

  public void setSatellitesIsSet(boolean value) {
    if (!value) {
      this.satellites = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case SESSION_ID:
      if (value == null) {
        unsetSessionId();
      } else {
        setSessionId((String)value);
      }
      break;

    case AUTH_TOKEN:
      if (value == null) {
        unsetAuthToken();
      } else {
        setAuthToken((String)value);
      }
      break;

    case SATELLITES:
      if (value == null) {
        unsetSatellites();
      } else {
        setSatellites((List<Satellite>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case SESSION_ID:
      return getSessionId();

    case AUTH_TOKEN:
      return getAuthToken();

    case SATELLITES:
      return getSatellites();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case SESSION_ID:
      return isSetSessionId();
    case AUTH_TOKEN:
      return isSetAuthToken();
    case SATELLITES:
      return isSetSatellites();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ClientSessionData)
      return this.equals((ClientSessionData)that);
    return false;
  }

  public boolean equals(ClientSessionData that) {
    if (that == null)
      return false;

    boolean this_present_sessionId = true && this.isSetSessionId();
    boolean that_present_sessionId = true && that.isSetSessionId();
    if (this_present_sessionId || that_present_sessionId) {
      if (!(this_present_sessionId && that_present_sessionId))
        return false;
      if (!this.sessionId.equals(that.sessionId))
        return false;
    }

    boolean this_present_authToken = true && this.isSetAuthToken();
    boolean that_present_authToken = true && that.isSetAuthToken();
    if (this_present_authToken || that_present_authToken) {
      if (!(this_present_authToken && that_present_authToken))
        return false;
      if (!this.authToken.equals(that.authToken))
        return false;
    }

    boolean this_present_satellites = true && this.isSetSatellites();
    boolean that_present_satellites = true && that.isSetSatellites();
    if (this_present_satellites || that_present_satellites) {
      if (!(this_present_satellites && that_present_satellites))
        return false;
      if (!this.satellites.equals(that.satellites))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(ClientSessionData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetSessionId()).compareTo(other.isSetSessionId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSessionId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sessionId, other.sessionId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAuthToken()).compareTo(other.isSetAuthToken());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAuthToken()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authToken, other.authToken);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSatellites()).compareTo(other.isSetSatellites());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSatellites()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.satellites, other.satellites);
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
    StringBuilder sb = new StringBuilder("ClientSessionData(");
    boolean first = true;

    sb.append("sessionId:");
    if (this.sessionId == null) {
      sb.append("null");
    } else {
      sb.append(this.sessionId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("authToken:");
    if (this.authToken == null) {
      sb.append("null");
    } else {
      sb.append(this.authToken);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("satellites:");
    if (this.satellites == null) {
      sb.append("null");
    } else {
      sb.append(this.satellites);
    }
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ClientSessionDataStandardSchemeFactory implements SchemeFactory {
    public ClientSessionDataStandardScheme getScheme() {
      return new ClientSessionDataStandardScheme();
    }
  }

  private static class ClientSessionDataStandardScheme extends StandardScheme<ClientSessionData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ClientSessionData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SESSION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.sessionId = iprot.readString();
              struct.setSessionIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // AUTH_TOKEN
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.authToken = iprot.readString();
              struct.setAuthTokenIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SATELLITES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list16 = iprot.readListBegin();
                struct.satellites = new ArrayList<Satellite>(_list16.size);
                for (int _i17 = 0; _i17 < _list16.size; ++_i17)
                {
                  Satellite _elem18;
                  _elem18 = new Satellite();
                  _elem18.read(iprot);
                  struct.satellites.add(_elem18);
                }
                iprot.readListEnd();
              }
              struct.setSatellitesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ClientSessionData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.sessionId != null) {
        oprot.writeFieldBegin(SESSION_ID_FIELD_DESC);
        oprot.writeString(struct.sessionId);
        oprot.writeFieldEnd();
      }
      if (struct.authToken != null) {
        oprot.writeFieldBegin(AUTH_TOKEN_FIELD_DESC);
        oprot.writeString(struct.authToken);
        oprot.writeFieldEnd();
      }
      if (struct.satellites != null) {
        oprot.writeFieldBegin(SATELLITES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.satellites.size()));
          for (Satellite _iter19 : struct.satellites)
          {
            _iter19.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ClientSessionDataTupleSchemeFactory implements SchemeFactory {
    public ClientSessionDataTupleScheme getScheme() {
      return new ClientSessionDataTupleScheme();
    }
  }

  private static class ClientSessionDataTupleScheme extends TupleScheme<ClientSessionData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ClientSessionData struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetSessionId()) {
        optionals.set(0);
      }
      if (struct.isSetAuthToken()) {
        optionals.set(1);
      }
      if (struct.isSetSatellites()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetSessionId()) {
        oprot.writeString(struct.sessionId);
      }
      if (struct.isSetAuthToken()) {
        oprot.writeString(struct.authToken);
      }
      if (struct.isSetSatellites()) {
        {
          oprot.writeI32(struct.satellites.size());
          for (Satellite _iter20 : struct.satellites)
          {
            _iter20.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ClientSessionData struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.sessionId = iprot.readString();
        struct.setSessionIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.authToken = iprot.readString();
        struct.setAuthTokenIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.satellites = new ArrayList<Satellite>(_list21.size);
          for (int _i22 = 0; _i22 < _list21.size; ++_i22)
          {
            Satellite _elem23;
            _elem23 = new Satellite();
            _elem23.read(iprot);
            struct.satellites.add(_elem23);
          }
        }
        struct.setSatellitesIsSet(true);
      }
    }
  }

}
