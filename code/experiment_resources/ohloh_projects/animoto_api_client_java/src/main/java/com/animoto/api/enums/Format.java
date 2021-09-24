package com.animoto.api.enums;

public enum Format {
  H264("h264");

  private String value;

  Format(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
