package com.animoto.api.resource;

import junit.framework.TestCase;

import com.animoto.api.ApiError;
import com.animoto.api.dto.*;

public class DirectingAndRenderingJobTest extends TestCase {
  public void testFromJson() {
    String json = "{\"response\":{\"payload\":{\"directing_and_rendering_job\":{\"links\":{\"self\":\"https://api2-sandbox.animoto.com/jobs/directing_and_rendering/4d24a45485b4f12d3b000580\"},\"state\":\"failed\"}}, \"status\":{\"code\":200,\"errors\": [{\"message\":\"#<JobError:0xb66b504c>\",\"code\":\"ERROR\"}, {\"message\":\"#<JobError:0xb66b4e44>\",\"code\":\"ERROR\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"},{\"code\":\"SYSTEM_ASSET_SERVICE_400\", \"message\":\"Asset Service interuption: During request for http://dog.com ()\"}]}}}";

    try {
      DirectingAndRenderingJob directingAndRenderingJob = new DirectingAndRenderingJob();
      ApiResponse apiResponse = directingAndRenderingJob.fromJson(json);
      ApiError[] apiErrors = apiResponse.getResponse().getStatus().getApiErrors();
      assertNotNull(apiErrors);
      assertEquals(8, apiErrors.length);
      for (int i = 0; i < apiErrors.length; i++) {
        assertNotNull(apiErrors[i].getMessage());
        assertNotNull(apiErrors[i].getCode());
      }
    }
    catch (Exception e) {
      fail();
    }
  }
}
