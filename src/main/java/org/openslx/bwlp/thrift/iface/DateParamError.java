/**
 * Autogenerated by Thrift Compiler (0.15.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.openslx.bwlp.thrift.iface;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.15.0)", date = "2021-12-17")
public enum DateParamError implements org.apache.thrift.TEnum {
  TOO_LOW(0),
  TOO_HIGH(1),
  NEGATIVE_RANGE(2);

  private final int value;

  private DateParamError(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  @org.apache.thrift.annotation.Nullable
  public static DateParamError findByValue(int value) { 
    switch (value) {
      case 0:
        return TOO_LOW;
      case 1:
        return TOO_HIGH;
      case 2:
        return NEGATIVE_RANGE;
      default:
        return null;
    }
  }
}
