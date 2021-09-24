package com.animoto.api.resource;

import com.animoto.api.Jsonable;
import com.animoto.api.StoryboardUnbundlingManifest;

/**
 * A StoryboardUnbundlingJob represents the status of your storyboardUnbundling job on the API.<p/>
 *
 * You will need to call ApiClient.reload() in order to obtain the latest information from API.<p/>
 *
 * When the unbundling job is complete, a StoryboardBundle should be available.
 *
 * @see com.animoto.api.ApiClient.unbundle
 */
public class StoryboardUnbundlingJob extends BaseResource implements Jsonable {
  private StoryboardUnbundlingManifest storyboardUnbundlingManifest;

  public String getContentType() {
    return "application/vnd.animoto.storyboard_unbundling_manifest-v1+json";
  }

  public String getAccept() {
    return "application/vnd.animoto.storyboard_unbundling_job-v1+json";
  }

  public void setStoryboardUnbundlingManifest(StoryboardUnbundlingManifest storyboardUnbundlingManifest) {
    this.storyboardUnbundlingManifest = storyboardUnbundlingManifest;
  }

  public StoryboardUnbundlingManifest getStoryboardUnundlingManifest() {
    return storyboardUnbundlingManifest;
  }

  public String toJson() {
    return newGson().toJson(new Container(this));
  }

  protected boolean containsStoryboard() {
    return true;
  }

  /**
   * Allows for a Gson to reflect the outer class context into JSON.
   */
  private class Container {
    @SuppressWarnings("unused")
    private StoryboardUnbundlingJob storyboardUnbundlingJob;

    public Container(StoryboardUnbundlingJob storyboardUnbundlingJob) {
      this.storyboardUnbundlingJob = storyboardUnbundlingJob;
    }
  }
}
