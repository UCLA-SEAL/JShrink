package com.animoto.api.resource;

import junit.framework.TestCase;

import com.animoto.api.DirectingManifest;
import com.animoto.api.enums.HttpCallbackFormat;
import com.animoto.api.util.DirectingManifestFactory;

import org.json.simple.*;
import org.json.simple.parser.*;

public class DirectingJobTest extends TestCase {
  DirectingJob directingJob = null;

  public void setUp() {
    directingJob = new DirectingJob();
  }

  public void testGetUrl() {
    directingJob.getLinks().put("self", "http://foo.com/bar");
    assertEquals("http://foo.com/bar", directingJob.getUrl());
  }

  public void testGetLocation() {
    directingJob.getLinks().put("self", "http://foo.com/bar");  
    assertEquals("http://foo.com/bar", directingJob.getLocation());
  }

  public void testIsComplete() {
    directingJob.setState("completed");
    assertTrue(directingJob.isCompleted());
    assertFalse(directingJob.isPending());
    assertFalse(directingJob.isFailed());
  }

  public void testIsFailed() {
    directingJob.setState("failed");
    assertTrue(directingJob.isFailed());
    assertFalse(directingJob.isCompleted());
    assertFalse(directingJob.isPending());
  }

  public void testIsPending() {
    directingJob.setState("other");
    assertTrue(directingJob.isPending());
    assertFalse(directingJob.isCompleted());
    assertFalse(directingJob.isFailed()); 
  }

  public void testToJson() throws ParseException {
    String customFootageUrl = "http://partner.com/postroll.mp4";
    DirectingManifest directingManifest = DirectingManifestFactory.newInstanceWithCustomFootagePostroll(customFootageUrl);
    String json = null;

    directingJob.setHttpCallback("http://partner.com/callback");
    directingJob.setHttpCallbackFormat(HttpCallbackFormat.JSON);
    directingJob.setDirectingManifest(directingManifest);

    json = directingJob.toJson();
    
    /*
      Let's use JSON Simple to parse the JSON and validate its correctness.
     */   
    JSONObject jsonObject, jsonDirectingJob, jsonDirectingManifest, jsonVisual, jsonSong, jsonPostroll;
    JSONArray jsonVisuals;

    jsonObject = (JSONObject) new JSONParser().parse(json);
    jsonDirectingJob = (JSONObject) jsonObject.get("directing_job");
    assertEquals("http://partner.com/callback", jsonDirectingJob.get("http_callback"));
    assertEquals("JSON", jsonDirectingJob.get("http_callback_format"));

    jsonDirectingManifest = (JSONObject) jsonDirectingJob.get("directing_manifest");
    assertEquals("original", jsonDirectingManifest.get("style"));
    assertEquals("My Animoto Video", jsonDirectingManifest.get("title"));
    assertEquals("HALF", jsonDirectingManifest.get("pacing"));

    jsonSong = (JSONObject) jsonDirectingManifest.get("song");
    assertEquals("http://api.client.java.animoto.s3.amazonaws.com/test_assets/song.mp3", jsonSong.get("source_url"));
    assertEquals(120.0, jsonSong.get("duration"));
    assertEquals(5.0, jsonSong.get("start_time"));
    
    jsonVisuals = (JSONArray) jsonDirectingManifest.get("visuals");
    assertEquals(3, jsonVisuals.size());    
    for (int i = 0; i < jsonVisuals.size(); i++) {
      jsonVisual = (JSONObject) jsonVisuals.get(i);
      if (jsonVisual.get("type").equals("image")) {
        assertEquals(2, ((Number) jsonVisual.get("rotation")).intValue());
        assertEquals("http://api.client.java.animoto.s3.amazonaws.com/test_assets/image.jpg", jsonVisual.get("source_url"));
        assertEquals(true, jsonVisual.get("cover"));
      }
      else if (jsonVisual.get("type").equals("footage")) {
        assertEquals("MIX", jsonVisual.get("audio_mix"));  
        assertEquals("http://api.client.java.animoto.s3.amazonaws.com/test_assets/footage.mp4", jsonVisual.get("source_url"));
        assertEquals(false, jsonVisual.get("cover"));
      }
      else if (jsonVisual.get("type").equals("title_card")) {
        assertEquals("hello", jsonVisual.get("h1"));
        assertEquals("world", jsonVisual.get("h2"));
      }
      else {
        fail("Unknown Visual Type");
      }
    }

    jsonPostroll = (JSONObject) jsonDirectingManifest.get("postroll");
    assertEquals("custom_footage", jsonPostroll.get("template"));
    assertEquals(customFootageUrl, jsonPostroll.get("source_url"));
  } 
}
