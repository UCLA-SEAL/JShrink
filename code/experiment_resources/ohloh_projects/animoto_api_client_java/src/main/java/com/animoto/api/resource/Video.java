package com.animoto.api.resource;

import com.animoto.api.RenderingParameters;

/**
 * A Video represents the video metadata of the video Animoto generated for you.<p/>
 *
 * You will need to call ApiClient.reload() in order to obtain the latest information from API.<p/>
 *
 * @see com.animoto.api.ApiClient
 */
public class Video extends BaseHttpGetOnlyResource {
  @Override
  protected boolean containsStoryboard() {
    return true;
  }

  /**
   * A video is *NOT* required to have a cover image; if it doesn't, this
   * method will return null.
   */
  public String getCoverImageUrl() {
    return getLinks().get("cover_image");
  }

  public String getDownloadUrl() {
    return getLinks().get("file");
  }

  public RenderingParameters getRenderingParameters() {
    return getMetadata().getRenderingParameters();
  }

  public String getAccept() {
    return "application/vnd.animoto.video-v1+json";
  }
}
