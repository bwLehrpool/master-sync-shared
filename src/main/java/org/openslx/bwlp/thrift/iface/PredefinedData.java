/**
 * Autogenerated by Thrift Compiler (0.9.3)
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
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2019-02-25")
public class PredefinedData implements org.apache.thrift.TBase<PredefinedData, PredefinedData._Fields>, java.io.Serializable, Cloneable, Comparable<PredefinedData> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("PredefinedData");

  private static final org.apache.thrift.protocol.TField NET_SHARES_FIELD_DESC = new org.apache.thrift.protocol.TField("netShares", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField LDAP_FILTER_FIELD_DESC = new org.apache.thrift.protocol.TField("ldapFilter", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField RUN_SCRIPTS_FIELD_DESC = new org.apache.thrift.protocol.TField("runScripts", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField NETWORK_EXCEPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("networkExceptions", org.apache.thrift.protocol.TType.LIST, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new PredefinedDataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new PredefinedDataTupleSchemeFactory());
  }

  public List<NetShare> netShares; // required
  public List<LdapFilter> ldapFilter; // required
  public List<PresetRunScript> runScripts; // required
  public List<PresetNetRule> networkExceptions; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    NET_SHARES((short)1, "netShares"),
    LDAP_FILTER((short)2, "ldapFilter"),
    RUN_SCRIPTS((short)3, "runScripts"),
    NETWORK_EXCEPTIONS((short)4, "networkExceptions");

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
        case 1: // NET_SHARES
          return NET_SHARES;
        case 2: // LDAP_FILTER
          return LDAP_FILTER;
        case 3: // RUN_SCRIPTS
          return RUN_SCRIPTS;
        case 4: // NETWORK_EXCEPTIONS
          return NETWORK_EXCEPTIONS;
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
    tmpMap.put(_Fields.NET_SHARES, new org.apache.thrift.meta_data.FieldMetaData("netShares", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, NetShare.class))));
    tmpMap.put(_Fields.LDAP_FILTER, new org.apache.thrift.meta_data.FieldMetaData("ldapFilter", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, LdapFilter.class))));
    tmpMap.put(_Fields.RUN_SCRIPTS, new org.apache.thrift.meta_data.FieldMetaData("runScripts", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PresetRunScript.class))));
    tmpMap.put(_Fields.NETWORK_EXCEPTIONS, new org.apache.thrift.meta_data.FieldMetaData("networkExceptions", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PresetNetRule.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(PredefinedData.class, metaDataMap);
  }

  public PredefinedData() {
  }

  public PredefinedData(
    List<NetShare> netShares,
    List<LdapFilter> ldapFilter,
    List<PresetRunScript> runScripts,
    List<PresetNetRule> networkExceptions)
  {
    this();
    this.netShares = netShares;
    this.ldapFilter = ldapFilter;
    this.runScripts = runScripts;
    this.networkExceptions = networkExceptions;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public PredefinedData(PredefinedData other) {
    if (other.isSetNetShares()) {
      List<NetShare> __this__netShares = new ArrayList<NetShare>(other.netShares.size());
      for (NetShare other_element : other.netShares) {
        __this__netShares.add(new NetShare(other_element));
      }
      this.netShares = __this__netShares;
    }
    if (other.isSetLdapFilter()) {
      List<LdapFilter> __this__ldapFilter = new ArrayList<LdapFilter>(other.ldapFilter.size());
      for (LdapFilter other_element : other.ldapFilter) {
        __this__ldapFilter.add(new LdapFilter(other_element));
      }
      this.ldapFilter = __this__ldapFilter;
    }
    if (other.isSetRunScripts()) {
      List<PresetRunScript> __this__runScripts = new ArrayList<PresetRunScript>(other.runScripts.size());
      for (PresetRunScript other_element : other.runScripts) {
        __this__runScripts.add(new PresetRunScript(other_element));
      }
      this.runScripts = __this__runScripts;
    }
    if (other.isSetNetworkExceptions()) {
      List<PresetNetRule> __this__networkExceptions = new ArrayList<PresetNetRule>(other.networkExceptions.size());
      for (PresetNetRule other_element : other.networkExceptions) {
        __this__networkExceptions.add(new PresetNetRule(other_element));
      }
      this.networkExceptions = __this__networkExceptions;
    }
  }

  public PredefinedData deepCopy() {
    return new PredefinedData(this);
  }

  @Override
  public void clear() {
    this.netShares = null;
    this.ldapFilter = null;
    this.runScripts = null;
    this.networkExceptions = null;
  }

  public int getNetSharesSize() {
    return (this.netShares == null) ? 0 : this.netShares.size();
  }

  public java.util.Iterator<NetShare> getNetSharesIterator() {
    return (this.netShares == null) ? null : this.netShares.iterator();
  }

  public void addToNetShares(NetShare elem) {
    if (this.netShares == null) {
      this.netShares = new ArrayList<NetShare>();
    }
    this.netShares.add(elem);
  }

  public List<NetShare> getNetShares() {
    return this.netShares;
  }

  public PredefinedData setNetShares(List<NetShare> netShares) {
    this.netShares = netShares;
    return this;
  }

  public void unsetNetShares() {
    this.netShares = null;
  }

  /** Returns true if field netShares is set (has been assigned a value) and false otherwise */
  public boolean isSetNetShares() {
    return this.netShares != null;
  }

  public void setNetSharesIsSet(boolean value) {
    if (!value) {
      this.netShares = null;
    }
  }

  public int getLdapFilterSize() {
    return (this.ldapFilter == null) ? 0 : this.ldapFilter.size();
  }

  public java.util.Iterator<LdapFilter> getLdapFilterIterator() {
    return (this.ldapFilter == null) ? null : this.ldapFilter.iterator();
  }

  public void addToLdapFilter(LdapFilter elem) {
    if (this.ldapFilter == null) {
      this.ldapFilter = new ArrayList<LdapFilter>();
    }
    this.ldapFilter.add(elem);
  }

  public List<LdapFilter> getLdapFilter() {
    return this.ldapFilter;
  }

  public PredefinedData setLdapFilter(List<LdapFilter> ldapFilter) {
    this.ldapFilter = ldapFilter;
    return this;
  }

  public void unsetLdapFilter() {
    this.ldapFilter = null;
  }

  /** Returns true if field ldapFilter is set (has been assigned a value) and false otherwise */
  public boolean isSetLdapFilter() {
    return this.ldapFilter != null;
  }

  public void setLdapFilterIsSet(boolean value) {
    if (!value) {
      this.ldapFilter = null;
    }
  }

  public int getRunScriptsSize() {
    return (this.runScripts == null) ? 0 : this.runScripts.size();
  }

  public java.util.Iterator<PresetRunScript> getRunScriptsIterator() {
    return (this.runScripts == null) ? null : this.runScripts.iterator();
  }

  public void addToRunScripts(PresetRunScript elem) {
    if (this.runScripts == null) {
      this.runScripts = new ArrayList<PresetRunScript>();
    }
    this.runScripts.add(elem);
  }

  public List<PresetRunScript> getRunScripts() {
    return this.runScripts;
  }

  public PredefinedData setRunScripts(List<PresetRunScript> runScripts) {
    this.runScripts = runScripts;
    return this;
  }

  public void unsetRunScripts() {
    this.runScripts = null;
  }

  /** Returns true if field runScripts is set (has been assigned a value) and false otherwise */
  public boolean isSetRunScripts() {
    return this.runScripts != null;
  }

  public void setRunScriptsIsSet(boolean value) {
    if (!value) {
      this.runScripts = null;
    }
  }

  public int getNetworkExceptionsSize() {
    return (this.networkExceptions == null) ? 0 : this.networkExceptions.size();
  }

  public java.util.Iterator<PresetNetRule> getNetworkExceptionsIterator() {
    return (this.networkExceptions == null) ? null : this.networkExceptions.iterator();
  }

  public void addToNetworkExceptions(PresetNetRule elem) {
    if (this.networkExceptions == null) {
      this.networkExceptions = new ArrayList<PresetNetRule>();
    }
    this.networkExceptions.add(elem);
  }

  public List<PresetNetRule> getNetworkExceptions() {
    return this.networkExceptions;
  }

  public PredefinedData setNetworkExceptions(List<PresetNetRule> networkExceptions) {
    this.networkExceptions = networkExceptions;
    return this;
  }

  public void unsetNetworkExceptions() {
    this.networkExceptions = null;
  }

  /** Returns true if field networkExceptions is set (has been assigned a value) and false otherwise */
  public boolean isSetNetworkExceptions() {
    return this.networkExceptions != null;
  }

  public void setNetworkExceptionsIsSet(boolean value) {
    if (!value) {
      this.networkExceptions = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case NET_SHARES:
      if (value == null) {
        unsetNetShares();
      } else {
        setNetShares((List<NetShare>)value);
      }
      break;

    case LDAP_FILTER:
      if (value == null) {
        unsetLdapFilter();
      } else {
        setLdapFilter((List<LdapFilter>)value);
      }
      break;

    case RUN_SCRIPTS:
      if (value == null) {
        unsetRunScripts();
      } else {
        setRunScripts((List<PresetRunScript>)value);
      }
      break;

    case NETWORK_EXCEPTIONS:
      if (value == null) {
        unsetNetworkExceptions();
      } else {
        setNetworkExceptions((List<PresetNetRule>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case NET_SHARES:
      return getNetShares();

    case LDAP_FILTER:
      return getLdapFilter();

    case RUN_SCRIPTS:
      return getRunScripts();

    case NETWORK_EXCEPTIONS:
      return getNetworkExceptions();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case NET_SHARES:
      return isSetNetShares();
    case LDAP_FILTER:
      return isSetLdapFilter();
    case RUN_SCRIPTS:
      return isSetRunScripts();
    case NETWORK_EXCEPTIONS:
      return isSetNetworkExceptions();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof PredefinedData)
      return this.equals((PredefinedData)that);
    return false;
  }

  public boolean equals(PredefinedData that) {
    if (that == null)
      return false;

    boolean this_present_netShares = true && this.isSetNetShares();
    boolean that_present_netShares = true && that.isSetNetShares();
    if (this_present_netShares || that_present_netShares) {
      if (!(this_present_netShares && that_present_netShares))
        return false;
      if (!this.netShares.equals(that.netShares))
        return false;
    }

    boolean this_present_ldapFilter = true && this.isSetLdapFilter();
    boolean that_present_ldapFilter = true && that.isSetLdapFilter();
    if (this_present_ldapFilter || that_present_ldapFilter) {
      if (!(this_present_ldapFilter && that_present_ldapFilter))
        return false;
      if (!this.ldapFilter.equals(that.ldapFilter))
        return false;
    }

    boolean this_present_runScripts = true && this.isSetRunScripts();
    boolean that_present_runScripts = true && that.isSetRunScripts();
    if (this_present_runScripts || that_present_runScripts) {
      if (!(this_present_runScripts && that_present_runScripts))
        return false;
      if (!this.runScripts.equals(that.runScripts))
        return false;
    }

    boolean this_present_networkExceptions = true && this.isSetNetworkExceptions();
    boolean that_present_networkExceptions = true && that.isSetNetworkExceptions();
    if (this_present_networkExceptions || that_present_networkExceptions) {
      if (!(this_present_networkExceptions && that_present_networkExceptions))
        return false;
      if (!this.networkExceptions.equals(that.networkExceptions))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_netShares = true && (isSetNetShares());
    list.add(present_netShares);
    if (present_netShares)
      list.add(netShares);

    boolean present_ldapFilter = true && (isSetLdapFilter());
    list.add(present_ldapFilter);
    if (present_ldapFilter)
      list.add(ldapFilter);

    boolean present_runScripts = true && (isSetRunScripts());
    list.add(present_runScripts);
    if (present_runScripts)
      list.add(runScripts);

    boolean present_networkExceptions = true && (isSetNetworkExceptions());
    list.add(present_networkExceptions);
    if (present_networkExceptions)
      list.add(networkExceptions);

    return list.hashCode();
  }

  @Override
  public int compareTo(PredefinedData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetNetShares()).compareTo(other.isSetNetShares());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNetShares()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.netShares, other.netShares);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLdapFilter()).compareTo(other.isSetLdapFilter());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLdapFilter()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ldapFilter, other.ldapFilter);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRunScripts()).compareTo(other.isSetRunScripts());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRunScripts()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.runScripts, other.runScripts);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetNetworkExceptions()).compareTo(other.isSetNetworkExceptions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetNetworkExceptions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.networkExceptions, other.networkExceptions);
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
    StringBuilder sb = new StringBuilder("PredefinedData(");
    boolean first = true;

    sb.append("netShares:");
    if (this.netShares == null) {
      sb.append("null");
    } else {
      sb.append(this.netShares);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("ldapFilter:");
    if (this.ldapFilter == null) {
      sb.append("null");
    } else {
      sb.append(this.ldapFilter);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("runScripts:");
    if (this.runScripts == null) {
      sb.append("null");
    } else {
      sb.append(this.runScripts);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("networkExceptions:");
    if (this.networkExceptions == null) {
      sb.append("null");
    } else {
      sb.append(this.networkExceptions);
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

  private static class PredefinedDataStandardSchemeFactory implements SchemeFactory {
    public PredefinedDataStandardScheme getScheme() {
      return new PredefinedDataStandardScheme();
    }
  }

  private static class PredefinedDataStandardScheme extends StandardScheme<PredefinedData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, PredefinedData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // NET_SHARES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list106 = iprot.readListBegin();
                struct.netShares = new ArrayList<NetShare>(_list106.size);
                NetShare _elem107;
                for (int _i108 = 0; _i108 < _list106.size; ++_i108)
                {
                  _elem107 = new NetShare();
                  _elem107.read(iprot);
                  struct.netShares.add(_elem107);
                }
                iprot.readListEnd();
              }
              struct.setNetSharesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // LDAP_FILTER
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list109 = iprot.readListBegin();
                struct.ldapFilter = new ArrayList<LdapFilter>(_list109.size);
                LdapFilter _elem110;
                for (int _i111 = 0; _i111 < _list109.size; ++_i111)
                {
                  _elem110 = new LdapFilter();
                  _elem110.read(iprot);
                  struct.ldapFilter.add(_elem110);
                }
                iprot.readListEnd();
              }
              struct.setLdapFilterIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // RUN_SCRIPTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list112 = iprot.readListBegin();
                struct.runScripts = new ArrayList<PresetRunScript>(_list112.size);
                PresetRunScript _elem113;
                for (int _i114 = 0; _i114 < _list112.size; ++_i114)
                {
                  _elem113 = new PresetRunScript();
                  _elem113.read(iprot);
                  struct.runScripts.add(_elem113);
                }
                iprot.readListEnd();
              }
              struct.setRunScriptsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // NETWORK_EXCEPTIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list115 = iprot.readListBegin();
                struct.networkExceptions = new ArrayList<PresetNetRule>(_list115.size);
                PresetNetRule _elem116;
                for (int _i117 = 0; _i117 < _list115.size; ++_i117)
                {
                  _elem116 = new PresetNetRule();
                  _elem116.read(iprot);
                  struct.networkExceptions.add(_elem116);
                }
                iprot.readListEnd();
              }
              struct.setNetworkExceptionsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, PredefinedData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.netShares != null) {
        oprot.writeFieldBegin(NET_SHARES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.netShares.size()));
          for (NetShare _iter118 : struct.netShares)
          {
            _iter118.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.ldapFilter != null) {
        oprot.writeFieldBegin(LDAP_FILTER_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.ldapFilter.size()));
          for (LdapFilter _iter119 : struct.ldapFilter)
          {
            _iter119.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.runScripts != null) {
        oprot.writeFieldBegin(RUN_SCRIPTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.runScripts.size()));
          for (PresetRunScript _iter120 : struct.runScripts)
          {
            _iter120.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.networkExceptions != null) {
        oprot.writeFieldBegin(NETWORK_EXCEPTIONS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.networkExceptions.size()));
          for (PresetNetRule _iter121 : struct.networkExceptions)
          {
            _iter121.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class PredefinedDataTupleSchemeFactory implements SchemeFactory {
    public PredefinedDataTupleScheme getScheme() {
      return new PredefinedDataTupleScheme();
    }
  }

  private static class PredefinedDataTupleScheme extends TupleScheme<PredefinedData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, PredefinedData struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetNetShares()) {
        optionals.set(0);
      }
      if (struct.isSetLdapFilter()) {
        optionals.set(1);
      }
      if (struct.isSetRunScripts()) {
        optionals.set(2);
      }
      if (struct.isSetNetworkExceptions()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetNetShares()) {
        {
          oprot.writeI32(struct.netShares.size());
          for (NetShare _iter122 : struct.netShares)
          {
            _iter122.write(oprot);
          }
        }
      }
      if (struct.isSetLdapFilter()) {
        {
          oprot.writeI32(struct.ldapFilter.size());
          for (LdapFilter _iter123 : struct.ldapFilter)
          {
            _iter123.write(oprot);
          }
        }
      }
      if (struct.isSetRunScripts()) {
        {
          oprot.writeI32(struct.runScripts.size());
          for (PresetRunScript _iter124 : struct.runScripts)
          {
            _iter124.write(oprot);
          }
        }
      }
      if (struct.isSetNetworkExceptions()) {
        {
          oprot.writeI32(struct.networkExceptions.size());
          for (PresetNetRule _iter125 : struct.networkExceptions)
          {
            _iter125.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, PredefinedData struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list126 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.netShares = new ArrayList<NetShare>(_list126.size);
          NetShare _elem127;
          for (int _i128 = 0; _i128 < _list126.size; ++_i128)
          {
            _elem127 = new NetShare();
            _elem127.read(iprot);
            struct.netShares.add(_elem127);
          }
        }
        struct.setNetSharesIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list129 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.ldapFilter = new ArrayList<LdapFilter>(_list129.size);
          LdapFilter _elem130;
          for (int _i131 = 0; _i131 < _list129.size; ++_i131)
          {
            _elem130 = new LdapFilter();
            _elem130.read(iprot);
            struct.ldapFilter.add(_elem130);
          }
        }
        struct.setLdapFilterIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list132 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.runScripts = new ArrayList<PresetRunScript>(_list132.size);
          PresetRunScript _elem133;
          for (int _i134 = 0; _i134 < _list132.size; ++_i134)
          {
            _elem133 = new PresetRunScript();
            _elem133.read(iprot);
            struct.runScripts.add(_elem133);
          }
        }
        struct.setRunScriptsIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TList _list135 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.networkExceptions = new ArrayList<PresetNetRule>(_list135.size);
          PresetNetRule _elem136;
          for (int _i137 = 0; _i137 < _list135.size; ++_i137)
          {
            _elem136 = new PresetNetRule();
            _elem136.read(iprot);
            struct.networkExceptions.add(_elem136);
          }
        }
        struct.setNetworkExceptionsIsSet(true);
      }
    }
  }

}
