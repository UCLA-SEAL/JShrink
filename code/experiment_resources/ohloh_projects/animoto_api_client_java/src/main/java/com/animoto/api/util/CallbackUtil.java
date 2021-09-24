package com.animoto.api.util;

import com.animoto.api.resource.Resource;
import com.animoto.api.resource.BaseResource;
import com.animoto.api.resource.RenderingJob;
import com.animoto.api.resource.DirectingJob;
import com.animoto.api.resource.DirectingAndRenderingJob;
import com.animoto.api.exception.ContractException;

/**
 * This utility allows you to quickly populate a Resource object given the JSON when handling API callbacks.<p/>
 * 
 * In order for this utility to work, you must specify HttpCallbackFormat.JSON in your callback. XML will not work.<p/>
 *
 * @see com.animoto.api.ApiClient
 * @see com.animoto.api.enums.HttpCallbackFormat 
 */
public class CallbackUtil {
  /**
   * Utility method to generate a Resource from the callback JSON received from API.<p/>
   *
   * Currently only expects DirectingJob or RenderingJob.<p/>
   *
   * @param       json
   * @exception   ContractException
   * @exception   IllegalArgumentException
   * @see         DirectingJob
   * @see         RenderingJob
   */
  public static Resource generateFromJson(String json) throws ContractException, IllegalArgumentException {
    BaseResource resource = null;
    if (json.indexOf("rendering_job") > -1 && json.indexOf("directing_and_rendering_job") == -1) {
      resource = new RenderingJob();
    }
    else if (json.indexOf("directing_job") > -1 && json.indexOf("directing_and_rendering_job") == -1) {
      resource = new DirectingJob();
    }
    else if (json.indexOf("directing_and_rendering_job") > -1) {
      resource = new DirectingAndRenderingJob();
    }
    else {
      throw new IllegalArgumentException("Expecting either a rendering_job, directing_job, or directing_and_rendering_job.");
    }
    resource.fromJson(json);
    return resource;
  }
}
