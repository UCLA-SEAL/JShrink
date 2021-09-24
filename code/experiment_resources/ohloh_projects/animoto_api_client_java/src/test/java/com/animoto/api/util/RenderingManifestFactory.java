package com.animoto.api.util;

import com.animoto.api.RenderingManifest;
import com.animoto.api.RenderingParameters;
import com.animoto.api.enums.*;

public class RenderingManifestFactory {
  public static RenderingManifest newInstance() {
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();

    renderingParameters.setFramerate(Framerate.F_30);
    renderingParameters.setFormat(Format.H264);
    renderingParameters.setResolution(Resolution.R_720P);

    renderingManifest.setRenderingParameters(renderingParameters);
    renderingManifest.setStoryboardUrl("http://animoto.com/storyboard/123");
    return renderingManifest;
  }
}
