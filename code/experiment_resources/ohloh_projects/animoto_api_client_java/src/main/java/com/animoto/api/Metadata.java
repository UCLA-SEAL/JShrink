package com.animoto.api;

/**
 * Encapsulates all metadata returned from an API resource.
 *
 * This model was introduced in 1.2 to better represent the response from API. Previously it was represented as a Map<String, String>.
 *
 * @since 1.2
 */

import java.util.List;

public class Metadata {
  private float duration;
  private List<String> songs;
  private List<String> visuals; 
  private RenderingParameters renderingParameters;

  public void setDuration(float duration) {
    this.duration = duration;
  }

  public float getDuration() {
    return duration;
  }

  public void setSongs(List<String> songs) {
    this.songs = songs;
  }

  public List<String> getSongs() {
    return this.songs;
  }

  public void setVisuals(List<String> visuals) {
    this.visuals = visuals;
  }

  public List<String> getVisuals() {
    return this.visuals;
  }

  public void setRenderingParameters(RenderingParameters renderingParameters) {
    this.renderingParameters = renderingParameters;
  }

  public RenderingParameters getRenderingParameters() {
    return renderingParameters;
  }
}
