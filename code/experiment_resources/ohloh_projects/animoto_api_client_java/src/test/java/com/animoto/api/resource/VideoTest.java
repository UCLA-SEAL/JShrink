package com.animoto.api.resource;

import junit.framework.TestCase;

import com.animoto.api.RenderingParameters;
import com.animoto.api.resource.Video;
import com.animoto.api.enums.Framerate;
import com.animoto.api.enums.Resolution;
import com.animoto.api.enums.Format;

public class VideoTest extends TestCase {
  public void testFromJson() {
    String videoUrl = "https://api2-sandbox.animoto.com/videos/4ced2532f0c886406200062d";
    String storyboardUrl = "https://api2-sandbox.animoto.com/storyboards/4ced252cf0c88655f6000002";
    String downloadUrl = "http://testconsumer1-4c64-sa-0.s3.amazonaws.com/Video/4ced2532f0c886406200062d/720p_3c2a.mp4?Signature=ExDB6T%2F15ldF9mcwBfCz1XIXIl0%3D&Expires=1290631624&AWSAccessKeyId=AKIAI7FBSVU753FBARAQ";
    String coverImageUrl = "http://animototest-4c17-st-0.s3.amazonaws.com/Video/4d8ccadab3783209e3000005/cover_image.jpg?Signature=4V5xgcgVPur7E0qbpx7lQPv7w4A%3D&Expires=1301094239&AWSAccessKeyId=AKIAI7FBSVU753FBARAQ;";

    Video video = new Video();
    String json = "{\"response\":{\"payload\":{\"video\":{\"metadata\":{\"rendering_parameters\":{\"resolution\":\"720p\",\"format\":\"h264\",\"framerate\":30}},\"links\":{\"self\":\"" + videoUrl + "\",\"storyboard\":\"" + storyboardUrl + "\",\"file\":\"" + downloadUrl + "\",\"cover_image\":\"" + coverImageUrl + "\"}}},\"status\":{\"code\":200}}}";

    try {
      video.fromJson(json);
    }
    catch (Exception e) {
      fail(e.toString());
    }
    assertNotNull(video.getMetadata());
    assertNotNull(video.getMetadata().getRenderingParameters());

    RenderingParameters renderingParameters = video.getMetadata().getRenderingParameters();
    assertEquals(Framerate.F_30, renderingParameters.getFramerate());
    assertEquals(Resolution.R_720P, renderingParameters.getResolution());
    assertEquals(Format.H264, renderingParameters.getFormat());

    /*
     * Yes, I know that this is comparing the addresses, not the values.
     * I think that this is an acceptable shortcut, at least for the moment.
     * Right now, the addresses *should* be the same, and I think that
     * overriding equals() in RenderingParameters just will be confusing.
     */
    assertEquals(renderingParameters, video.getRenderingParameters());

    assertEquals(video.getDownloadUrl(), downloadUrl);
    assertEquals(video.getLinks().get("file"), downloadUrl); // We never should have exposed getLinks().get("file") to clients but the cat's already out of the bag
    assertEquals(video.getCoverImageUrl(), coverImageUrl);
    assertEquals(video.getStoryboard().getLocation(), storyboardUrl);
  }
}
