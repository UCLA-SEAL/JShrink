package com.animoto.api.cli;

import com.animoto.api.resource.DirectingAndRenderingJob;

public class RawDirectingAndRenderingJob extends DirectingAndRenderingJob implements Raw {
  private String rawJson;

  public void setRawEntity(String rawEntity) {
    this.rawJson = rawEntity;
  }

  public String toJson() {
    return rawJson;
  }
}
