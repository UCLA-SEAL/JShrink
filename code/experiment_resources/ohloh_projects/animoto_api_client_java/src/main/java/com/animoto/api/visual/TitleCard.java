package com.animoto.api.visual;

import com.animoto.api.enums.VisualType;

/**
 * A TitleCard represents text to show in your Animoto video.<p/>
 *
 * It is added to a DirectingManifest when directing.<p/>
 *
 * @see com.animoto.api.DirectingManifest
 */
public class TitleCard extends BaseVisual {
  private Boolean spotlit;
  private String h1;
  private String h2;

  public TitleCard() {
    visualType = VisualType.TITLE_CARD;
  }

  public void setSpotlit(Boolean spotlit) {
    this.spotlit = spotlit;
  }

  public Boolean getSpotlit() {
    return spotlit;
  }

  public void setH1(String h1) {
    this.h1 = h1;
  }

  public String getH1() {
    return h1;
  }

  public void setH2(String h2) {
    this.h2 = h2;
  }

  public String getH2() {
    return h2;
  }
}
