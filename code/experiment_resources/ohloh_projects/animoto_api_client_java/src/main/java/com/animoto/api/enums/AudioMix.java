package com.animoto.api.enums;

public enum AudioMix {
  NONE("none"), MIX("mix");

  private String value;

  AudioMix(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
