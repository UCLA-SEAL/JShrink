package com.animoto.api;

import com.animoto.api.resource.Storyboard;

/**
 * A RenderingManifest is used to communicate video rendering instructions to the API and return a RenderingJob.<p/>
 *
 * @see com.animoto.api.resource.RenderingJob
 */
public class RenderingManifest {
  private String storyboardUrl;
  private RenderingParameters renderingParameters;

  /**
   * Set the Storyboard that is to be rendered by API.
   */
  public void setStoryboard(Storyboard storyboard) {
    this.storyboardUrl = storyboard.getLocation();
  }

  /**
   * Set the URL of the Storyboard that is to be rendered by the API.
   */
  public void setStoryboardUrl(String storyboardUrl) {
    this.storyboardUrl = storyboardUrl;
  }

  public String getStoryboardUrl() {
    return storyboardUrl;
  }

  /**
   * Set the RenderingParameters for the render request to API.
   */
  public void setRenderingParameters(RenderingParameters renderingParameters) {
    this.renderingParameters = renderingParameters;
  }

  public RenderingParameters getRenderingParameters() {
    return renderingParameters;
  }
}
