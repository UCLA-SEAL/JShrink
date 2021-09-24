package com.animoto.api.enums;

public enum Style {
  ORIGINAL("original"),
  MOTHERS_DAY_2011("mothers_day_2011"),
  ELEGANCE("elegance_12"),
  VINTAGE("vintage"),
  COSMIC_TIDINGS("cosmic_tidings"),
  WONDERLAND_OF_SNOW("wonderland_of_snow");

  private String value;

  Style(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
