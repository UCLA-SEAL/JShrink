package com.animoto.api.visual;

import com.animoto.api.enums.Rotation;
import com.animoto.api.enums.VisualType;

/**
 * An Image contains all information needed to place an image in an Animoto video.<p/>
 *
 * It is added to a DirectingManifest when directing.<p/>
 *
 * @see com.animoto.api.DirectingManifest
 */
public class Image extends BaseVisual {
  private Boolean spotlit;
  private Rotation rotation;
  private String sourceUrl;
  private boolean cover;

  public Image() {
    visualType = VisualType.IMAGE;
  }

  /**
   * Set whether the image should be spotlit in the video by the API.
   */
  public void setSpotlit(Boolean spotlit) {
    this.spotlit = spotlit;
  }

  public Boolean isSpotlit() {
    return spotlit;
  }

  /**
   * Set the rotation for the image.
   */
  public void setRotation(Rotation rotation) {
    this.rotation = rotation;
  }

  public Rotation getRotation() {
    return rotation;
  }

  /**
   * Set a valid HTTP URL for where the image can be located by API.
   */
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getSourceUrl() {
    return sourceUrl;
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
