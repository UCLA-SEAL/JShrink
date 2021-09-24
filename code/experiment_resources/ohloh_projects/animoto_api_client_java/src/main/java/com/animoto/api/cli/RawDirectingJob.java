package com.animoto.api.cli;

import com.animoto.api.resource.DirectingJob;

public class RawDirectingJob extends DirectingJob implements Raw {
  private String rawJson;

  public void setRawEntity(String rawEntity) {
    this.rawJson = rawEntity;
  }

  public String toJson() {
    return rawJson;
  }
}
