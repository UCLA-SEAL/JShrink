package com.animoto.api;

import java.util.List;
import java.util.ArrayList;

import com.animoto.api.visual.Visual;
import com.animoto.api.enums.Pacing;
import com.animoto.api.enums.Style;
import com.animoto.api.postroll.*;

/**
 * A DirectingManifest is used to communicate the assets, resources, and metadata of your Animoto video.<p/>
 *
 * You will receive a DirectingJob once you have instructed the API to direct with the manifest.<p/>
 *
 * @see com.animoto.api.resource.DirectingJob
 */
public class DirectingManifest {
  private Visual[] visuals = new Visual[0];
  private String title;
  private Pacing pacing = Pacing.AUTO;
  private Style style = Style.ORIGINAL;
  private Song song;
  public  Postroll postroll = new BasicPostroll();

  /**
   * Add a visual to the manifest.
   */
  public void addVisual(Visual visual) {
    /*
      Unfortunately, JSON serialization isn't happy with Collections so we use a typed array :/
     */
    List list = new ArrayList();
    list.addAll(java.util.Arrays.asList(visuals));
    list.add(visual);
    visuals = (Visual[]) list.toArray(new Visual[list.size()]);
  }

  /**
   * Clear all visuals in the manifest.
   */
  public void clearVisuals() {
    visuals = new Visual[0];
  }

  public Visual[] getVisuals() {
    return visuals;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setPacing(Pacing pacing) {
    this.pacing = pacing;
  }

  public Pacing getPacing() {
    return pacing;
  }

  public void setStyle(Style style) {
    this.style = style;
  }

  public Style getStyle() {
    return style;
  }

  public void setSong(Song song) {
    this.song = song;
  }

  public Song getSong() {
    return song;
  }

  public void setPostroll(Postroll postroll) {
    this.postroll = postroll;
  }

  public Postroll getPostroll() {
    return postroll;
  }
}
