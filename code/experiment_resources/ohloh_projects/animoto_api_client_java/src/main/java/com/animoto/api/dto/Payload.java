package com.animoto.api.dto;

import com.animoto.api.resource.*;

import java.lang.reflect.*;

public class Payload {
  private DirectingJob directingJob;
  private RenderingJob renderingJob;
  private DirectingAndRenderingJob directingAndRenderingJob;
  private StoryboardBundlingJob storyboardBundlingJob;
  private StoryboardUnbundlingJob storyboardUnbundlingJob;
  private Storyboard storyboard;
  private Video video;

  public DirectingJob getDirectingJob() {
    return directingJob;
  }

  public DirectingJob getRawDirectingJob() {
    return getDirectingJob();
  }

  public RenderingJob getRenderingJob() {
    return renderingJob;
  }

  public RenderingJob getRawRenderingJob() {
    return getRenderingJob();
  }

  public Storyboard getStoryboard() {
    return storyboard;
  }

  public Video getVideo() {
    return video;
  }

  public DirectingAndRenderingJob getDirectingAndRenderingJob() {
    return directingAndRenderingJob;
  }

  public DirectingAndRenderingJob getRawDirectingAndRenderingJob() {
    return getDirectingAndRenderingJob();
  }

  public StoryboardBundlingJob getStoryboardBundlingJob() {
    return storyboardBundlingJob;
  }

  public StoryboardUnbundlingJob getStoryboardUnbundlingJob() {
    return storyboardUnbundlingJob;
  }

  /**
   * Get the associated BaseResource based on the Class you pass in.
   */
  public BaseResource getBaseResource(Class<?> clazz) {
    try {
      String name = "get" + clazz.getName().substring(clazz.getName().lastIndexOf ('.') + 1);
      Method method = this.getClass().getMethod(name);
      Object value = method.invoke(this);
      return (BaseResource) value;
    }
    catch (Exception e) {
      throw new Error(e);
    }
  }
}
