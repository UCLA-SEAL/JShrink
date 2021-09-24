package com.animoto.api.visual;

import com.animoto.api.enums.VisualType;
import com.animoto.api.enums.AudioMix;

/**
 * A Footage object represents video to be used in an Animoto video.
 *
 * @see com.animoto.api.DirectingManifest
 */
public class Footage extends BaseVisual {
  private String sourceUrl;
  private AudioMix audioMix = AudioMix.NONE;
  private Float startTime = new Float(0);
  private Float duration;
  private boolean cover = false;

  public Footage() {
    visualType = VisualType.FOOTAGE;
  }

  /**
   * Set a valid HTTP URL for where the video can be found by the API.
   */
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  /**
   * Set whether the video's audio track should be used or not in the video.
   */
  public void setAudioMix(AudioMix audioMix) {
    this.audioMix = audioMix;
  }

  public AudioMix getAudioMix() {
    return audioMix;
  }

  /**
   * Set the start offset used by the API when using the video.
   */
  public void setStartTime(Float startTime) {
    this.startTime = startTime;
  }

  public Float getStartTime() {
    return startTime;
  }

  /**
   * Set how many seconds of video to use.
   */
  public void setDuration(Float duration) {
    this.duration = duration;
  }

  public Float getDuration() {
    return duration;
  }

  /**
   * Set whether this Footage should be used for the cover. Only one Image or Footage can be cover.
   */
  public void setCover(boolean cover) {
    this.cover = cover;
  }

  public boolean isCover() {
    return this.cover;
  }
}
  
