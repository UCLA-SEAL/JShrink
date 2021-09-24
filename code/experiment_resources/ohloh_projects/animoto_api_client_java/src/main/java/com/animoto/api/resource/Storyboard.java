package com.animoto.api.resource;


/**
 * A Storyboard represents the directing metadata of all the assets you provided to Animoto for your video.<p/>
 *
 * You will need to call ApiClient.reload() in order to obtain the latest information from API.<p/>
 *
 * @see com.animoto.api.ApiClient
 */
public class Storyboard extends BaseHttpGetOnlyResource {
  public String getAccept() {
    return "application/vnd.animoto.storyboard-v1+json";
  }
}
