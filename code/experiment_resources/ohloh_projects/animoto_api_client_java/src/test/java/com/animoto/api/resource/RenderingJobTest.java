package com.animoto.api.resource;

import junit.framework.TestCase;

import com.animoto.api.ApiError;
import com.animoto.api.RenderingManifest;
import com.animoto.api.enums.HttpCallbackFormat;
import com.animoto.api.dto.ApiResponse;
import com.animoto.api.util.GsonUtil;
import com.animoto.api.util.RenderingManifestFactory;

import org.json.simple.*;
import org.json.simple.parser.*;

public class RenderingJobTest extends TestCase {
  public void testToJson() throws ParseException {
    RenderingJob renderingJob = new RenderingJob();
    RenderingManifest renderingManifest = RenderingManifestFactory.newInstance(); 
    String json;

    renderingJob.setHttpCallback("http://partner.com/some/callback");    
    renderingJob.setHttpCallbackFormat(HttpCallbackFormat.XML); 

    renderingJob.setRenderingManifest(renderingManifest);
    json = renderingJob.toJson();
    
    /*
      Let's use JSON Simple to parse the JSON and validate it's correctness.
     */   
    JSONObject jsonObject, jsonRenderingJob, jsonRenderingManifest, jsonRenderingParameters;
    jsonObject = (JSONObject) new JSONParser().parse(json);
    jsonRenderingJob = (JSONObject) jsonObject.get("rendering_job");
    assertEquals("http://partner.com/some/callback", jsonRenderingJob.get("http_callback"));
    assertEquals("XML", jsonRenderingJob.get("http_callback_format"));

    jsonRenderingManifest = (JSONObject) jsonRenderingJob.get("rendering_manifest");
    assertEquals("http://animoto.com/storyboard/123", jsonRenderingManifest.get("storyboard_url"));

    jsonRenderingParameters = (JSONObject) jsonRenderingManifest.get("rendering_parameters");
    assertEquals(30.0, jsonRenderingParameters.get("framerate")); 
    assertEquals("h264", jsonRenderingParameters.get("format"));
    assertEquals("720p", jsonRenderingParameters.get("resolution"));
  } 

  public void testErrorParsing() {
    String json = "{\"response\":{\"status\":{\"code\":400,\"errors\":[{\"code\":\"PRESENCE\",\"message\":\"Vertical resolution can't be empty\"},{\"code\":\"ENUMERATION\",\"message\":\"Vertical resolution must be one of 180p, 240p, 360p, 480p, 720p, or 1080p\"},{\"code\":\"ENUMERATION\",\"message\":\"Framerate must be one of 12, 15, 24, or 30\"},{\"code\":\"PRESENCE\",\"message\":\"Format can't be empty\"},{\"code\":\"ENUMERATION\",\"message\":\"Format must be one of h264, h264-iphone, flv, or iso\"}]}}}";
    RenderingJob renderingJob = new RenderingJob();
    ApiResponse apiResponse = GsonUtil.create().fromJson(json, ApiResponse.class);
    ApiError[] apiErrors = null;

    assertNotNull(apiResponse);
    assertNotNull(apiResponse.getResponse());
    assertNull(apiResponse.getResponse().getPayload());
    assertNotNull(apiResponse.getResponse().getStatus());
    apiErrors = apiResponse.getResponse().getStatus().getApiErrors();
    assertNotNull(apiErrors);
    assertEquals(5, apiErrors.length);
  }
}
