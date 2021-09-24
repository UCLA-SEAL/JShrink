package com.animoto.api.resource;

import junit.framework.TestCase;

import com.animoto.api.resource.Storyboard;

import org.json.simple.*;
import org.json.simple.parser.*;

public class StoryboardTest extends TestCase {
  public void testFromJson() {
    Storyboard storyboard = new Storyboard();
    String json = "{\"response\":{\"payload\":{\"storyboard\":{\"metadata\":{\"duration\":10.26,\"songs\":[\"https://asset-service-sandbox.animoto.com/assets/fd76d52d85e8aec5040f50f9\"],\"visuals\":[\"https://asset-service-sandbox.animoto.com/assets/b2b1cb676fc71f8c755e1fea\",\"https://asset-service-sandbox.animoto.com/assets/10d18ee29947a4026460b6a9\"]},\"links\":{\"self\":\"https://api2-sandbox.animoto.com/storyboards/4ced24c7f0c88655d0000002\"}}},\"status\":{\"code\":200}}}";
    try {
      storyboard.fromJson(json); 
    }
    catch (Exception e) {
      fail(e.toString());
    }

    assertNotNull(storyboard.getMetadata());
    assertEquals(1, storyboard.getMetadata().getSongs().size());
    assertEquals(2, storyboard.getMetadata().getVisuals().size());
  }
}
