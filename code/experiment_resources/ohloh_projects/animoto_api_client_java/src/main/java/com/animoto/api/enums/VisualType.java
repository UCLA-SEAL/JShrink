package com.animoto.api.enums;

public enum VisualType {
  FOOTAGE("footage"), IMAGE("image"), TITLE_CARD("title_card");

  private String value;

  VisualType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
