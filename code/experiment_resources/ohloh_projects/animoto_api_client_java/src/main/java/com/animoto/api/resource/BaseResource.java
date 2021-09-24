package com.animoto.api.resource;

import com.animoto.api.ApiClient;
import com.animoto.api.Metadata;
import com.animoto.api.enums.HttpCallbackFormat;
import com.animoto.api.util.GsonUtil;
import com.animoto.api.util.StringUtil;
import com.animoto.api.dto.ApiResponse;
import com.animoto.api.exception.ApiException;
import com.animoto.api.exception.HttpExpectationException;
import com.animoto.api.exception.ContractException;
import com.animoto.api.dto.Response;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.google.gson.Gson;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Abstract resource that contains common fields and methods for all child resources.
 */
public abstract class BaseResource implements Resource {
  protected String httpCallback;
  protected HttpCallbackFormat httpCallbackFormat = HttpCallbackFormat.XML;
  protected String state;
  protected String requestId;
  protected Map<String, String> links = new HashMap<String, String>();
  protected Metadata metadata;
  protected Storyboard storyboard;
  protected Video video;
  protected Response response;

  /**
   * Indicate if the resource should contain a Storyboard if complete. <p/>
   *
   * Override to indicate if it does.
   *
   * @see DirectingJob
   * @see RenderingJob
   * @see DirectingAndRenderingJob
   */
  protected boolean containsStoryboard() {
    return false;
  }

  /**
   * Indicate if the resource should contain a Video if complete. <p/>
   *
   * Override to indicate if it does.
   *
   * @see RenderingJob
   * @see DirectingAndRenderingJob
   */
  protected boolean containsVideo() {
    return false;
  }

  public void setHttpCallback(String httpCallback) {
    this.httpCallback = httpCallback;
  }

  public String getHttpCallback() {
    return httpCallback;
  }

  public void setHttpCallbackFormat(HttpCallbackFormat httpCallbackFormat) {
    this.httpCallbackFormat = httpCallbackFormat;
  }

  public HttpCallbackFormat getHttpCallbackFormat() {
    return httpCallbackFormat;
  }

  public void setUrl(String url) {
    setLocation(url);
  }

  /**
   * Alias method for getLocation.
   *
   * @see #getLocation
   */
  public String getUrl() {
    return getLocation();
  }

  public void setLocation(String location) {
    getLinks().put("self", location);
  }

  /**
   * Returns the URL of this resource as it is RESTfully represented on API.
   */
  public String getLocation() {
    return links.get("self");
  }

  public void setState(String state) {
    this.state = state;
  }

  /**
   * Get the state of your resource on the API.
   *
   * @see #isPending
   * @see #isCompleted
   * @see #isFailed
   */
  public String getState() {
    return state;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   * Get the Animoto API Request ID that was used for your request. Useful for debugging and tracing with Animoto.
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * True if the state is "failed" from API.
   */
  public boolean isFailed() {
    return "failed".equals(state);
  }

  /**
   * Determines if a resource is pending on the API, that is, it is neither failed or completed.
   */
  public boolean isPending() {
    return !isFailed() && !isCompleted();
  }

  /**
   * True if the state is "completed" from API.
   * TODO: Separate out the job semantics from the resource semantics.
   */
  public boolean isCompleted() {
    return (state == null) || "completed".equals(state);
  }

  public void setLinks(Map<String, String> links) {
    this.links = links;
  }

  /**
   * Get the related links of the resource from API.
   */
  public Map<String, String> getLinks() {
    if (links == null) {
      links = new HashMap<String, String>();
    }
    return links;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  /**
   * Get the metadata of the resource from API.
   */
  public Metadata getMetadata() {
    return metadata;
  }

  public void setStoryboard(Storyboard storyboard) {
    this.storyboard = storyboard;
  }

  /**
   * Get the Storyboard associated with the resource, if available.<p/>
   *
   * You will need to call ApiClient.reload() to get all associated information.</p>
   *
   * @see com.animoto.api.ApiClient
   */
  public Storyboard getStoryboard() {
    return storyboard;
  }

  public void setVideo(Video video) {
    this.video = video;
  }

  /**
   * Get the Video associated with the resource, if available.<p/>
   *
   * You will need to call ApiClient.reload() to get all associated information.</p>
   *
   * @see com.animoto.api.ApiClient
   */
  public Video getVideo() {
    return video;
  }

  /**
   * Get the underlying data transfer object response when deserializing JSON into pojos.
   */
  public Response getResponse() {
    return response;
  }

  /**
   * Utility method to create a new Gson parser for all child classes.
   */
  protected Gson newGson() {
    return GsonUtil.create();
  }

  /**
   * Common handler to parse the HTTP response and deserialize all information into the the resource.
   *
   * @param httpResponse              The HttpResponse from HttpClient
   * @param expectedStatusCode        The expected HTTP code we want from the API.
   * @exception HttpExpectationException
   * @exception ContractException
   * @exception IOException
   */
  public void handleHttpResponse(HttpResponse httpResponse, int expectedStatusCode) throws HttpExpectationException, ContractException, IOException {
    int statusCode;
    String body;

    statusCode = httpResponse.getStatusLine().getStatusCode();
    body = StringUtil.convertStreamToString(httpResponse.getEntity().getContent());
    ApiClient.getLogger().info("resource [" + (StringUtil.isBlank(getLocation()) ? toString() : getLocation()) + "] received [" + statusCode + "] and expected [" + expectedStatusCode + "]");

    ApiResponse apiResponse = null;

    /*
     * Only parse JSON if the content type indicates that the response body is JSON; some
     * errors will be returned from the server without a body (401) or with
     * an HTML body (502).
     */
    Header contentTypeHeader = httpResponse.getFirstHeader("Content-Type");
    if((contentTypeHeader != null) && (contentTypeHeader.getValue() != null) && contentTypeHeader.getValue().contains("json")) {
      apiResponse = fromJson(body);
    }

    if (statusCode != expectedStatusCode) {
      throw new HttpExpectationException(statusCode, expectedStatusCode, body, apiResponse);
    }

    if(apiResponse == null) {
      throw new ContractException("Expected a JSON body instead of " + contentTypeHeader.getValue());
    }

    setRequestId(httpResponse.getFirstHeader("x-animoto-request-id").getValue());
    if (getLocation() == null ||  StringUtil.isBlank(getLocation())) {
      throw new ContractException("Expected location URL to be present.");
    }
  }

  /**
   * Allows you to populate this bean given a JSON from API.<p/>
   *
   * Will call storyboard and video populate methods if expected by resource contract.<p/>
   *
   * @param       json
   * @exception   ContractException
   */
  public ApiResponse fromJson(String json) throws ContractException {
    ApiResponse apiResponse = null;
    BaseResource dtoBaseResource;

    apiResponse = newGson().fromJson(json, ApiResponse.class);
    this.response = apiResponse.getResponse();
    if (this.response == null || this.response.getPayload() == null) {
      return apiResponse;
    }

    dtoBaseResource = getResponse().getPayload().getBaseResource(this.getClass());
    doErrorableBeanCopy(dtoBaseResource);

    if(isCompleted()) {
      onComplete();
    }

    /*
     * TODO: populateStoryboard, populateVideo probably should be refactored into onComplete
     */
    if (containsStoryboard()) {
      populateStoryboard();
    }

    if (containsVideo()) {
      populateVideo();
    }
    return apiResponse;
  }

  protected void onComplete() throws ContractException {

  }

  /**
   * Utility method to populate the job with a Storyboard resource if it is related to the resource.
   *
   * @see DirectingJob
   * @see RenderingJob
   * @see DirectingAndRenderingJob
   */
  protected void populateStoryboard() throws ContractException {
    if (isCompleted()) {
      Storyboard storyboard = new Storyboard();
      storyboard.setLocation(getLinks().get("storyboard"));
      setStoryboard(storyboard);
      if (storyboard.getLocation() == null) {
        throw new ContractException("Expected Storyboard URL to be present.");
      }
    }
  }

  /**
   * Utility method to populate the job with a Videoresource if it is related to the resource.
   *
   * @see RenderingJob
   * @see DirectingAndRenderingJob
   */
  protected void populateVideo() throws ContractException {
    if (isCompleted()) {
      Video video = new Video();
      video.getLinks().put("self", getLinks().get("video"));
      setVideo(video);
      if (video.getLocation() == null) {
        throw new ContractException("Expected Video URL to be present.");
      }
    }
  }

  /**
   * Saftey wrapper around Apache Bean Utils.
   */
  private void doErrorableBeanCopy(Object bean) {
    try {
      BeanUtils.copyProperties(this, bean);
    }
    catch (IllegalAccessException e) {
      throw new Error(e.toString());
    }
    catch (IllegalArgumentException e) {
      throw new Error(e.toString());
    }
    catch (InvocationTargetException e) {
      throw new Error(e.toString());
    }
  }

  private static final String BREAK = "------------------------------------------------------------\n";
  /**
   * Utility method to print this Resource to STDOUT.
   */
  public void prettyPrintToSystem() {
    StringBuffer buf = new StringBuffer();
    String key;
    Iterator it = null;

    buf.append(this.getClass().getName() + " - " + new java.util.Date().toString() + "\n");
    buf.append(BREAK);
    buf.append("request id: " + getRequestId() + "\n");
    buf.append("state: " + getState() + "\n");
    buf.append("location: " + getLocation() + "\n");
    buf.append("current links\n");
    buf.append(BREAK);
    it = getLinks().keySet().iterator();
    while (it.hasNext()) {
      key = (String) it.next();
      buf.append(key + ": " + getLinks().get(key) + "\n");
    }
    System.out.println(buf.toString());
  }
}
