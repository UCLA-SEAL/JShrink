package com.animoto.api;

/**
 * A Song contains all information related to the audio file to be used when the API directs a video.
 *
 * @see DirectingManifest
 */
public class Song {
  private String sourceUrl;
  private Float startTime;
  private Float duration;

  /**
   * Set the HTTP URL to a valid MP3 file for your video.
   */
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setStartTime(Float startTime) {
    this.startTime = startTime;
  }

  public Float getStartTime() {
    return startTime;
  }

  public void setDuration(Float duration) {
    this.duration = duration;
  }

  public Float getDuration() {
    return duration;
  }
}
