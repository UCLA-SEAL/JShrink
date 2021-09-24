package com.animoto.api;

import com.animoto.api.enums.Resolution;
import com.animoto.api.enums.Format;
import com.animoto.api.enums.Framerate;

/**
 * A RenderingParameters contains video metadata of how you want API to render a video.<p/>
 *
 * It is part of the RenderingManifest.<p/>
 *
 * @see RenderingManifest
 */
public class RenderingParameters {
  private Resolution resolution;
  private Framerate framerate;
  private Format format;

  public void setResolution(Resolution resolution) {
    this.resolution = resolution;
  }

  public Resolution getResolution() {
    return resolution;
  }

  public void setFramerate(Framerate framerate) {
    this.framerate = framerate;
  }

  public Framerate getFramerate() {
    return this.framerate;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public Format getFormat() {
    return format;
  }
}
