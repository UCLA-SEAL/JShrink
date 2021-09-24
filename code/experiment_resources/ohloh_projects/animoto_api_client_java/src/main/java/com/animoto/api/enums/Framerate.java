package com.animoto.api.enums;

public enum Framerate {
  F_12(12), F_15(15), F_24(24), F_30(30);

  private float value;

  Framerate(float value) {
    this.value = value;
  }

  public float getValue() {
    return value;
  }
}
