package com.animoto.api.resource;

import com.animoto.api.Jsonable;
import com.animoto.api.DirectingManifest;
import com.animoto.api.exception.HttpExpectationException;
import com.animoto.api.exception.ContractException;
import com.animoto.api.util.GsonUtil;

import java.io.IOException;

import org.apache.http.HttpResponse;

/**
 * A DirectingJob represents the status of your directing job on the API.<p/>
 *
 * You will need to call ApiClient.reload() in order to obtain the latest information from API.<p/>
 *
 * When the directing job is complete, a Storyboard should be available.<p/>
 *
 * @see com.animoto.api.ApiClient
 * @see Storyboard
 */
public class DirectingJob extends BaseResource implements Jsonable {
  private DirectingManifest directingManifest;

  public String getContentType() {
    return "application/vnd.animoto.directing_manifest-v1+json";
  }

  public String getAccept() {
    return "application/vnd.animoto.directing_job-v1+json";
  }

  public void setDirectingManifest(DirectingManifest directingManifest) {
    this.directingManifest = directingManifest;
  }

  public DirectingManifest getDirectingManifest() {
    return directingManifest;
  }

  public String toJson() {
    return newGson().toJson(new Container(this));
  }

  protected boolean containsStoryboard() {
    return true;
  }

  /**
   * Allows for a Gson to reflect the outer class context into JSON.
   */
  private class Container {
    private DirectingJob directingJob;

    public Container(DirectingJob directingJob) {
      this.directingJob = directingJob;  
    }
  }
}
