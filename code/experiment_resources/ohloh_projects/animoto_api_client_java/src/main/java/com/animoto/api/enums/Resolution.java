package com.animoto.api.enums;

public enum Resolution {
  R_180P("180p"), R_270P("270p"), R_360P("360p"), R_480P("480p"), R_720P("720p"), R_1080P("1080p");

  private String value;

  Resolution(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
