package com.animoto.api;

import com.animoto.api.resource.Storyboard;

/**
 * A StoryboardBundlingManifest instructs Animoto that all of the assets
 * for a single video should be bundled and made available for retrieval and
 * archival on the part of the caller (so that, at some arbitrary point
 * in the future, the caller can upload the bundle back to Animoto in
 * order to recommence work on the video).
 *
 * @see com.animoto.api.resource.StoryboardBundlingJob
 */
public class StoryboardBundlingManifest {
  private String storyboardUrl;

  /**
   * Set the Storyboard that is to be bundled by API.
   */
  public void setStoryboard(Storyboard storyboard) {
    this.storyboardUrl = storyboard.getLocation();
  }

  /**
   * Set the Storyboard Url that is to be bundled by API.
   */
  public void setStoryboardUrl(String storyboardUrl) {
    this.storyboardUrl = storyboardUrl;
  }

  public String getStoryboardUrl() {
    return storyboardUrl;
  }
}
