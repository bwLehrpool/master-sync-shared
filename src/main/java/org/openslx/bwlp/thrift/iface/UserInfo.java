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

public class UserInfo implements org.apache.thrift.TBase<UserInfo, UserInfo._Fields>, java.io.Serializable, Cloneable, Comparable<UserInfo> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UserInfo");

  private static final org.apache.thrift.protocol.TField USER_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("userId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField FIRST_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("firstName", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField LAST_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("lastName", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField E_MAIL_FIELD_DESC = new org.apache.thrift.protocol.TField("eMail", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField ORGANIZATION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("organizationId", org.apache.thrift.protocol.TType.STRING, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new UserInfoStandardSchemeFactory());
    schemes.put(TupleScheme.class, new UserInfoTupleSchemeFactory());
  }

  public String userId; // required
  public String firstName; // required
  public String lastName; // required
  public String eMail; // required
  public String organizationId; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    USER_ID((short)1, "userId"),
    FIRST_NAME((short)2, "firstName"),
    LAST_NAME((short)3, "lastName"),
    E_MAIL((short)4, "eMail"),
    ORGANIZATION_ID((short)5, "organizationId");

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
        case 1: // USER_ID
          return USER_ID;
        case 2: // FIRST_NAME
          return FIRST_NAME;
        case 3: // LAST_NAME
          return LAST_NAME;
        case 4: // E_MAIL
          return E_MAIL;
        case 5: // ORGANIZATION_ID
          return ORGANIZATION_ID;
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
    tmpMap.put(_Fields.USER_ID, new org.apache.thrift.meta_data.FieldMetaData("userId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FIRST_NAME, new org.apache.thrift.meta_data.FieldMetaData("firstName", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.LAST_NAME, new org.apache.thrift.meta_data.FieldMetaData("lastName", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.E_MAIL, new org.apache.thrift.meta_data.FieldMetaData("eMail", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ORGANIZATION_ID, new org.apache.thrift.meta_data.FieldMetaData("organizationId", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UserInfo.class, metaDataMap);
  }

  public UserInfo() {
  }

  public UserInfo(
    String userId,
    String firstName,
    String lastName,
    String eMail,
    String organizationId)
  {
    this();
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.eMail = eMail;
    this.organizationId = organizationId;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public UserInfo(UserInfo other) {
    if (other.isSetUserId()) {
      this.userId = other.userId;
    }
    if (other.isSetFirstName()) {
      this.firstName = other.firstName;
    }
    if (other.isSetLastName()) {
      this.lastName = other.lastName;
    }
    if (other.isSetEMail()) {
      this.eMail = other.eMail;
    }
    if (other.isSetOrganizationId()) {
      this.organizationId = other.organizationId;
    }
  }

  public UserInfo deepCopy() {
    return new UserInfo(this);
  }

  @Override
  public void clear() {
    this.userId = null;
    this.firstName = null;
    this.lastName = null;
    this.eMail = null;
    this.organizationId = null;
  }

  public String getUserId() {
    return this.userId;
  }

  public UserInfo setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public void unsetUserId() {
    this.userId = null;
  }

  /** Returns true if field userId is set (has been assigned a value) and false otherwise */
  public boolean isSetUserId() {
    return this.userId != null;
  }

  public void setUserIdIsSet(boolean value) {
    if (!value) {
      this.userId = null;
    }
  }

  public String getFirstName() {
    return this.firstName;
  }

  public UserInfo setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public void unsetFirstName() {
    this.firstName = null;
  }

  /** Returns true if field firstName is set (has been assigned a value) and false otherwise */
  public boolean isSetFirstName() {
    return this.firstName != null;
  }

  public void setFirstNameIsSet(boolean value) {
    if (!value) {
      this.firstName = null;
    }
  }

  public String getLastName() {
    return this.lastName;
  }

  public UserInfo setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public void unsetLastName() {
    this.lastName = null;
  }

  /** Returns true if field lastName is set (has been assigned a value) and false otherwise */
  public boolean isSetLastName() {
    return this.lastName != null;
  }

  public void setLastNameIsSet(boolean value) {
    if (!value) {
      this.lastName = null;
    }
  }

  public String getEMail() {
    return this.eMail;
  }

  public UserInfo setEMail(String eMail) {
    this.eMail = eMail;
    return this;
  }

  public void unsetEMail() {
    this.eMail = null;
  }

  /** Returns true if field eMail is set (has been assigned a value) and false otherwise */
  public boolean isSetEMail() {
    return this.eMail != null;
  }

  public void setEMailIsSet(boolean value) {
    if (!value) {
      this.eMail = null;
    }
  }

  public String getOrganizationId() {
    return this.organizationId;
  }

  public UserInfo setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public void unsetOrganizationId() {
    this.organizationId = null;
  }

  /** Returns true if field organizationId is set (has been assigned a value) and false otherwise */
  public boolean isSetOrganizationId() {
    return this.organizationId != null;
  }

  public void setOrganizationIdIsSet(boolean value) {
    if (!value) {
      this.organizationId = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case USER_ID:
      if (value == null) {
        unsetUserId();
      } else {
        setUserId((String)value);
      }
      break;

    case FIRST_NAME:
      if (value == null) {
        unsetFirstName();
      } else {
        setFirstName((String)value);
      }
      break;

    case LAST_NAME:
      if (value == null) {
        unsetLastName();
      } else {
        setLastName((String)value);
      }
      break;

    case E_MAIL:
      if (value == null) {
        unsetEMail();
      } else {
        setEMail((String)value);
      }
      break;

    case ORGANIZATION_ID:
      if (value == null) {
        unsetOrganizationId();
      } else {
        setOrganizationId((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case USER_ID:
      return getUserId();

    case FIRST_NAME:
      return getFirstName();

    case LAST_NAME:
      return getLastName();

    case E_MAIL:
      return getEMail();

    case ORGANIZATION_ID:
      return getOrganizationId();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case USER_ID:
      return isSetUserId();
    case FIRST_NAME:
      return isSetFirstName();
    case LAST_NAME:
      return isSetLastName();
    case E_MAIL:
      return isSetEMail();
    case ORGANIZATION_ID:
      return isSetOrganizationId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof UserInfo)
      return this.equals((UserInfo)that);
    return false;
  }

  public boolean equals(UserInfo that) {
    if (that == null)
      return false;

    boolean this_present_userId = true && this.isSetUserId();
    boolean that_present_userId = true && that.isSetUserId();
    if (this_present_userId || that_present_userId) {
      if (!(this_present_userId && that_present_userId))
        return false;
      if (!this.userId.equals(that.userId))
        return false;
    }

    boolean this_present_firstName = true && this.isSetFirstName();
    boolean that_present_firstName = true && that.isSetFirstName();
    if (this_present_firstName || that_present_firstName) {
      if (!(this_present_firstName && that_present_firstName))
        return false;
      if (!this.firstName.equals(that.firstName))
        return false;
    }

    boolean this_present_lastName = true && this.isSetLastName();
    boolean that_present_lastName = true && that.isSetLastName();
    if (this_present_lastName || that_present_lastName) {
      if (!(this_present_lastName && that_present_lastName))
        return false;
      if (!this.lastName.equals(that.lastName))
        return false;
    }

    boolean this_present_eMail = true && this.isSetEMail();
    boolean that_present_eMail = true && that.isSetEMail();
    if (this_present_eMail || that_present_eMail) {
      if (!(this_present_eMail && that_present_eMail))
        return false;
      if (!this.eMail.equals(that.eMail))
        return false;
    }

    boolean this_present_organizationId = true && this.isSetOrganizationId();
    boolean that_present_organizationId = true && that.isSetOrganizationId();
    if (this_present_organizationId || that_present_organizationId) {
      if (!(this_present_organizationId && that_present_organizationId))
        return false;
      if (!this.organizationId.equals(that.organizationId))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int compareTo(UserInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetUserId()).compareTo(other.isSetUserId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUserId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.userId, other.userId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFirstName()).compareTo(other.isSetFirstName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFirstName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.firstName, other.firstName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLastName()).compareTo(other.isSetLastName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLastName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastName, other.lastName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEMail()).compareTo(other.isSetEMail());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEMail()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.eMail, other.eMail);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOrganizationId()).compareTo(other.isSetOrganizationId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOrganizationId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.organizationId, other.organizationId);
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
    StringBuilder sb = new StringBuilder("UserInfo(");
    boolean first = true;

    sb.append("userId:");
    if (this.userId == null) {
      sb.append("null");
    } else {
      sb.append(this.userId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("firstName:");
    if (this.firstName == null) {
      sb.append("null");
    } else {
      sb.append(this.firstName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("lastName:");
    if (this.lastName == null) {
      sb.append("null");
    } else {
      sb.append(this.lastName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("eMail:");
    if (this.eMail == null) {
      sb.append("null");
    } else {
      sb.append(this.eMail);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("organizationId:");
    if (this.organizationId == null) {
      sb.append("null");
    } else {
      sb.append(this.organizationId);
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

  private static class UserInfoStandardSchemeFactory implements SchemeFactory {
    public UserInfoStandardScheme getScheme() {
      return new UserInfoStandardScheme();
    }
  }

  private static class UserInfoStandardScheme extends StandardScheme<UserInfo> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, UserInfo struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // USER_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.userId = iprot.readString();
              struct.setUserIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // FIRST_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.firstName = iprot.readString();
              struct.setFirstNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // LAST_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.lastName = iprot.readString();
              struct.setLastNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // E_MAIL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.eMail = iprot.readString();
              struct.setEMailIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // ORGANIZATION_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.organizationId = iprot.readString();
              struct.setOrganizationIdIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, UserInfo struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.userId != null) {
        oprot.writeFieldBegin(USER_ID_FIELD_DESC);
        oprot.writeString(struct.userId);
        oprot.writeFieldEnd();
      }
      if (struct.firstName != null) {
        oprot.writeFieldBegin(FIRST_NAME_FIELD_DESC);
        oprot.writeString(struct.firstName);
        oprot.writeFieldEnd();
      }
      if (struct.lastName != null) {
        oprot.writeFieldBegin(LAST_NAME_FIELD_DESC);
        oprot.writeString(struct.lastName);
        oprot.writeFieldEnd();
      }
      if (struct.eMail != null) {
        oprot.writeFieldBegin(E_MAIL_FIELD_DESC);
        oprot.writeString(struct.eMail);
        oprot.writeFieldEnd();
      }
      if (struct.organizationId != null) {
        oprot.writeFieldBegin(ORGANIZATION_ID_FIELD_DESC);
        oprot.writeString(struct.organizationId);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class UserInfoTupleSchemeFactory implements SchemeFactory {
    public UserInfoTupleScheme getScheme() {
      return new UserInfoTupleScheme();
    }
  }

  private static class UserInfoTupleScheme extends TupleScheme<UserInfo> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, UserInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetUserId()) {
        optionals.set(0);
      }
      if (struct.isSetFirstName()) {
        optionals.set(1);
      }
      if (struct.isSetLastName()) {
        optionals.set(2);
      }
      if (struct.isSetEMail()) {
        optionals.set(3);
      }
      if (struct.isSetOrganizationId()) {
        optionals.set(4);
      }
      oprot.writeBitSet(optionals, 5);
      if (struct.isSetUserId()) {
        oprot.writeString(struct.userId);
      }
      if (struct.isSetFirstName()) {
        oprot.writeString(struct.firstName);
      }
      if (struct.isSetLastName()) {
        oprot.writeString(struct.lastName);
      }
      if (struct.isSetEMail()) {
        oprot.writeString(struct.eMail);
      }
      if (struct.isSetOrganizationId()) {
        oprot.writeString(struct.organizationId);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, UserInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(5);
      if (incoming.get(0)) {
        struct.userId = iprot.readString();
        struct.setUserIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.firstName = iprot.readString();
        struct.setFirstNameIsSet(true);
      }
      if (incoming.get(2)) {
        struct.lastName = iprot.readString();
        struct.setLastNameIsSet(true);
      }
      if (incoming.get(3)) {
        struct.eMail = iprot.readString();
        struct.setEMailIsSet(true);
      }
      if (incoming.get(4)) {
        struct.organizationId = iprot.readString();
        struct.setOrganizationIdIsSet(true);
      }
    }
  }

}
