package com.animoto.api;

/**
 * A StoryboardUnbundlingManifest instructs Animoto to reload the assets associated
 * with a movie that was directed at some point in the past; unbundling the
 * storyboard allows the movie to be rendered.
 *
 * @see com.animoto.api.resource.StoryboardUnbundlingJob
 */
public class StoryboardUnbundlingManifest {
  private String bundleUrl;

  /**
   * Set the URL of the Bundle that is to be bundled by the API.
   */
  public void setBundleUrl(String bundleUrl) {
    this.bundleUrl = bundleUrl;
  }

  public String getBundleUrl() {
    return bundleUrl;
  }
}
