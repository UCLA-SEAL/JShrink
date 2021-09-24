package com.animoto.api.cli;

import com.animoto.api.resource.RenderingJob;

public class RawRenderingJob extends RenderingJob implements Raw {
  private String rawJson;

  public void setRawEntity(String rawEntity) {
    this.rawJson = rawEntity;
  }

  public String toJson() {
    return rawJson;
  }
}
