package com.animoto.api.enums;

/**
 * This enumeration represents the possible orientation values for your Image visual.<p/>
 *
 * @see com.animoto.api.visual.Image
 */
public enum Rotation {
  ZERO(0), ONE(1), TWO(2), THREE(3);

  private int value;

  Rotation(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
