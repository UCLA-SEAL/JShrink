package com.animoto.api.resource;

import com.animoto.api.Jsonable;
import com.animoto.api.StoryboardBundlingManifest;
import com.animoto.api.exception.ContractException;

/**
 * A StoryboardBundlingJob represents the status of your storyboardBundling job on the API.<p/>
 *
 * You will need to call ApiClient.reload() in order to obtain the latest information from API.<p/>
 *
 * When the bundling job is complete, a StoryboardBundle should be available.
 *
 * @see com.animoto.api.ApiClient.bundle
 */
public class StoryboardBundlingJob extends BaseResource implements Jsonable {
  private StoryboardBundlingManifest storyboardBundlingManifest;
  private String bundleUrl;

  public String getContentType() {
    return "application/vnd.animoto.storyboard_bundling_manifest-v1+json";
  }

  public String getAccept() {
    return "application/vnd.animoto.storyboard_bundling_job-v1+json";
  }

  public void setStoryboardBundlingManifest(StoryboardBundlingManifest storyboardBundlingManifest) {
    this.storyboardBundlingManifest = storyboardBundlingManifest;
  }

  public StoryboardBundlingManifest getStoryboardBundlingManifest() {
    return storyboardBundlingManifest;
  }

  public String getBundleUrl() {
    return bundleUrl;
  }

  public String toJson() {
    return newGson().toJson(new Container(this));
  }

  @Override
  protected void onComplete() throws ContractException {
    bundleUrl = getLinks().get("bundle");
    if (bundleUrl == null) {
      throw new ContractException("Expected Bundle URL to be present.");
    }
  }

  /**
   * Allows for a Gson to reflect the outer class context into JSON.
   */
  private class Container {
    @SuppressWarnings("unused")
    private StoryboardBundlingJob storyboardBundlingJob;

    public Container(StoryboardBundlingJob storyboardBundlingJob) {
      this.storyboardBundlingJob = storyboardBundlingJob;
    }
  }
}
