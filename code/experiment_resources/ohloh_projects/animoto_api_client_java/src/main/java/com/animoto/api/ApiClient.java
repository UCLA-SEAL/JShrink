package com.animoto.api;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

import com.animoto.api.resource.Resource;
import com.animoto.api.resource.BaseResource;
import com.animoto.api.resource.DirectingJob;
import com.animoto.api.resource.RenderingJob;
import com.animoto.api.resource.DirectingAndRenderingJob;
import com.animoto.api.resource.StoryboardBundlingJob;
import com.animoto.api.resource.StoryboardUnbundlingJob;
import com.animoto.api.util.StringUtil;
import com.animoto.api.DirectingManifest;
import com.animoto.api.RenderingManifest;

import com.animoto.api.exception.HttpExpectationException;
import com.animoto.api.exception.HttpException;
import com.animoto.api.exception.ContractException;

import com.animoto.api.enums.HttpCallbackFormat;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.entity.StringEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * ApiClient is the main class for interacting with the Animoto API. <p/>
 *
 * To create an ApiClient using a resource bundle, animoto_api_client.properties, use the ApiClientFactory.<p/>
 *
 * For further information visit: <p/>
 *
 * <ul>
 *  <li><a href="http://github.com/animoto/api_client_java">API Java Client Githib</a></li>
 *  <li><a href="http://animoto.com/developer/api">The Animoto API</a></li>
 * </ul>
 *
 * @author  SunDawg
 * @since   1.0
 * @version 1.1
 *
 * @see ApiClientFactory
 */
public class ApiClient {
  private String key;
  private String secret;
  private String host = "https://platform.animoto.com";
  private static final Log logger = LogFactory.getLog(ApiClient.class);

  public static Log getLogger() {
    return logger;
  }

  /**
   * Default constructor. You will need to set a key and secret.
   */
  public ApiClient() {
    this(null, null);
  }

  /**
   * Constructor
   *
   * @param key     Your Animoto API key
   * @param secret  Your Animoto API secret
   */
  public ApiClient(String key, String secret) {
    this(key, secret, null);
  }

  /**
   * Constructor
   *
   * @param key
   * @param secret
   * @param host
   */
  public ApiClient(String key, String secret, String host) {
    this.key = key;
    this.secret = secret;
    this.host = host;
  }

  public String getVersion() {
    return "1.3.0";
  }

  public String getUserAgent() {
    return "Animoto Java API Client - " + getVersion();
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getSecret() {
    return secret;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  /**
   * @see bundle(StoryboardBundlingManifest, ApiCommand)
   */
  public StoryboardBundlingJob bundle(StoryboardBundlingManifest manifest) throws HttpExpectationException, HttpException, ContractException {
    return bundle(manifest, new ApiCommand());
  }

  /**
   * Instruct Animoto to bundle all resources associated with the Storyboard
   * referenced by the manifest into a file, which can be downloaded and
   * uploaded back to Animoto in order to recommence work on a movie at some point
   * in the future.
   *
   * @param manifest Contains the storyboard to bundle
   * @return the bundling job, which can be polled for completion (and then queried for the bundle URL)
   */
  public StoryboardBundlingJob bundle(StoryboardBundlingManifest manifest, ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    StoryboardBundlingJob job = new StoryboardBundlingJob();
    job.setStoryboardBundlingManifest(manifest);
    apiCommand.setBaseResource(job);
    executeApiCommandAndExpectHttp201(apiCommand);
    return job;
  }

  /**
   * @see unbundle(StoryboardUnbundlingManifest, ApiCommand)
   */
  public StoryboardUnbundlingJob unbundle(StoryboardUnbundlingManifest manifest) throws HttpExpectationException, HttpException, ContractException {
    return unbundle(manifest, new ApiCommand());
  }

  /**
   * Instruct Animoto to unbundle all resources associated with the bundle
   * referenced by the manifest, so that work on the video can be recommenced.
   *
   * @param manifest Contains the bundle URL
   * @return the unbundling job, which can be polled for completion (and then queried for the storyboard)
   */
  public StoryboardUnbundlingJob unbundle(StoryboardUnbundlingManifest manifest, ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    StoryboardUnbundlingJob job = new StoryboardUnbundlingJob();
    job.setStoryboardUnbundlingManifest(manifest);
    apiCommand.setBaseResource(job);
    executeApiCommandAndExpectHttp201(apiCommand);
    return job;
  }

  /**
   * Delete a resource.  May not be supported for all types of resources.
   * Will throw if the resource is not found or could not be
   * deleted for some reason.
   *
   * @param resource the resource to delete
   */
  public void delete(BaseResource resource) throws HttpException, HttpExpectationException {
    Map<String, String> headers = new HashMap<String, String>();

    try {
      HttpResponse httpResponse = doHttpDelete(resource.getLocation(), headers, null, null);
      getLogger().info("delete resource [" + (StringUtil.isBlank(resource.getLocation()) ? toString() : resource.getLocation()) + "] received [" + httpResponse.getStatusLine().getStatusCode() + "] and expected [" + HttpStatus.SC_NO_CONTENT + "]");

      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
        throw new HttpExpectationException(statusCode, HttpStatus.SC_NO_CONTENT, null, null);
      }
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
  }

  /**
   * Creates a directing job with no http callbacks.
   *
   * @param       directingManifest           The manifest payload to direct.
   * @return      DirectingJob
   * @exception   HttpExpectationException
   * @exception   HttpException
   * @exception   ContractException
   */
  public DirectingJob direct(DirectingManifest directingManifest) throws HttpExpectationException, HttpException, ContractException {
    return direct(directingManifest, new ApiCommand());
  }

  /**
   * Creates a directing job with http callbacks.
   *
   * @param       directingManifest           The manifest payload to direct.
   * @param       httpCallback                The callback URL the API will communicate back to.
   * @param       httpCallbackFormat          The payload type when the callback is made.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public DirectingJob direct(DirectingManifest directingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    return direct(directingManifest, apiCommand);
  }

  /**
   * Creates a directing job with http callbacks, retry handler, and request interceptors.
   *
   * @param       directingManifest           The manifest payload to direct.
   * @param       httpCallback                The callback URL the API will communicate back to.
   * @param       httpCallbackFormat          The payload type when the callback is made.
   * @param       httpRequestRetryHandler     The retry handler that you want to use.
   * @param       httpRequestInterceptors     The interceptors you want registered for the request.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public DirectingJob direct(DirectingManifest directingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    apiCommand.setHttpRequestRetryHandler(httpRequestRetryHandler);
    apiCommand.setHttpRequestInterceptors(httpRequestInterceptors);
    return direct(directingManifest, apiCommand);
  }

  /**
   * Creates a directing job with command object.
   *
   * @param       directingManifest
   * @param       apiCommand
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public DirectingJob direct(DirectingManifest directingManifest, ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    DirectingJob directingJob = new DirectingJob();
    directingJob.setDirectingManifest(directingManifest);
    apiCommand.setBaseResource(directingJob);
    executeApiCommandAndExpectHttp201(apiCommand);
    return directingJob;
  }

  protected void executeApiCommandAndExpectHttp201(ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    HttpResponse httpResponse = doApiHttpPost(apiCommand);
    try {
      apiCommand.getBaseResource().handleHttpResponse(httpResponse, 201);
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
  }

  /**
   * Creates a rendering job with no http callbacks.
   *
   * @param       renderingManifest           The manifest payload to render.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public RenderingJob render(RenderingManifest renderingManifest) throws HttpExpectationException, HttpException, ContractException {
    return render(renderingManifest, new ApiCommand());
  }

  /**
   * Creates a rendering job with http callbacks.
   *
   * @param       renderingManifest           The manifest payload to render.
   * @param       httpCallback                The callback URL the API will communicate back to.
   * @param       httpCallbackFormat          The payload type when the callback is made.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public RenderingJob render(RenderingManifest renderingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    return render(renderingManifest, apiCommand);
  }

  /**
   * Creates a rendering job with http callbacks, retry handler, and request interceptors.
   *
   * @param       renderingManifest           The manifest payload to render.
   * @param       httpCallback                The callback URL the API will communicate back to.
   * @param       httpCallbackFormat          The payload type when the callback is made.
   * @param       httpRequestRetryHandler     The retry handler that you want to use.
   * @param       httpRequestInterceptors     The interceptors you want registered for the request.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public RenderingJob render(RenderingManifest renderingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    apiCommand.setHttpRequestRetryHandler(httpRequestRetryHandler);
    apiCommand.setHttpRequestInterceptors(httpRequestInterceptors);
    return render(renderingManifest, apiCommand);
  }

  /**
   * Creates a rendering job with command object.
   *
   * @param       renderingManifest
   * @param       apiCommand
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public RenderingJob render(RenderingManifest renderingManifest, ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    RenderingJob renderingJob = new RenderingJob();
    renderingJob.setRenderingManifest(renderingManifest);
    apiCommand.setBaseResource(renderingJob);
    executeApiCommandAndExpectHttp201(apiCommand);
    return renderingJob;
  }

  /**
   *
   */
  public DirectingAndRenderingJob directAndRender(DirectingManifest directingManifest, RenderingManifest renderingManifest) throws HttpExpectationException, HttpException, ContractException {
    return directAndRender(directingManifest, renderingManifest, new ApiCommand());
  }

  /**
   * Creates a DirectingAndRendering job from the API.
   *
   * @param       directingManifest             The manifest payload to direct.
   * @param       renderingManifest             The manifest payload to render.
   * @param       httpCallback                  The callback URL the API will communicate back to.
   * @param       httpCallbackFormat            The payload type when the callback is made.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public DirectingAndRenderingJob directAndRender(DirectingManifest directingManifest, RenderingManifest renderingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    return directAndRender(directingManifest, renderingManifest, apiCommand);
  }

  /**
   * Creates a DirectingAndRendering job from the API.
   *
   * @param       directingManifest             The manifest payload to direct.
   * @param       renderingManifest             The manifest payload to render.
   * @param       httpCallback                  The callback URL the API will communicate back to.
   * @param       httpCallbackFormat            The payload type when the callback is made.
   * @param       httpRequestRetryHandler     The retry handler that you want to use.
   * @param       httpRequestInterceptors     The interceptors you want registered for the request.
   * @exception   HttpExpectationException
   * @exception   HttpExpectation
   * @exception   ContractException
   */
  public DirectingAndRenderingJob directAndRender(DirectingManifest directingManifest, RenderingManifest renderingManifest, String httpCallback, HttpCallbackFormat httpCallbackFormat, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws HttpExpectationException, HttpException, ContractException {
    ApiCommand apiCommand = new ApiCommand();
    apiCommand.setHttpCallback(httpCallback);
    apiCommand.setHttpCallbackFormat(httpCallbackFormat);
    apiCommand.setHttpRequestRetryHandler(httpRequestRetryHandler);
    apiCommand.setHttpRequestInterceptors(httpRequestInterceptors);
    return directAndRender(directingManifest, renderingManifest, apiCommand);
  }

  /**
   *
   */
  public DirectingAndRenderingJob directAndRender(DirectingManifest directingManifest, RenderingManifest renderingManifest, ApiCommand apiCommand) throws HttpExpectationException, HttpException, ContractException {
    DirectingAndRenderingJob directingAndRenderingJob = new DirectingAndRenderingJob();
    directingAndRenderingJob.setDirectingManifest(directingManifest);
    directingAndRenderingJob.setRenderingManifest(renderingManifest);
    renderingManifest.setStoryboardUrl(null);
    apiCommand.setBaseResource(directingAndRenderingJob);
    executeApiCommandAndExpectHttp201(apiCommand);
    return directingAndRenderingJob;
  }

  /**
   * Communicates with the API to grab the latest information for a resource.
   *
   * @param       resource                    The resource to refresh with the latest information from API.
   * @exception   HttpException
   * @exception   HttpExpectationException
   * @exception   ContractException
   */
  public void reload(Resource resource) throws HttpException, HttpExpectationException, ContractException {
    reload(resource, null, null);
  }

  /**
   * Communicates with the API to grab the latest information for a resource.
   *
   * @param       resource                    The resource to refresh with the latest information from API.
   * @param       httpRequestRetryHandler     The retry handler that you want to use.
   * @param       httpRequestInterceptors     The interceptors you want registered for the request.
   * @exception   HttpException
   * @exception   HttpExpectationException
   * @exception   ContractException
   */
  public void reload(Resource resource, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws HttpException, HttpExpectationException, ContractException {
    Map<String, String> headers = new HashMap<String, String>();
    HttpResponse httpResponse;

    try {
      headers.put("Accept", resource.getAccept());
      httpResponse = doHttpGet(resource.getLocation(), headers, httpRequestRetryHandler, httpRequestInterceptors);
      ((BaseResource) resource).handleHttpResponse(httpResponse, 200);
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
  }

  private HttpResponse doHttpDelete(String url, Map<String, String> headers, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws IOException, UnsupportedEncodingException {
    HttpDelete httpDelete = new HttpDelete(url);
    return doHttpRequest(httpDelete, headers, httpRequestRetryHandler, httpRequestInterceptors);
  }

  private HttpResponse doHttpGet(String url, Map<String, String> headers, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws IOException, UnsupportedEncodingException {
    HttpGet httpGet = new HttpGet(url);
    return doHttpRequest(httpGet, headers, httpRequestRetryHandler, httpRequestInterceptors);
  }

  private HttpResponse doHttpPost(String url, String postBody, Map<String, String> headers, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws IOException, UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(new StringEntity(postBody, "utf-8"));
    return doHttpRequest(httpPost, headers, httpRequestRetryHandler, httpRequestInterceptors);
  }

  private HttpResponse doApiHttpPost(ApiCommand apiCommand) throws HttpException {
    HttpResponse httpResponse = null;
    Map<String, String> headers = new HashMap<String, String>();

    if (apiCommand.getHttpCallback() != null) {
      apiCommand.getBaseResource().setHttpCallback(apiCommand.getHttpCallback());
    }

    if (apiCommand.getHttpCallbackFormat() != null) {
      apiCommand.getBaseResource().setHttpCallbackFormat(apiCommand.getHttpCallbackFormat());
    }

    try {
      //FIXME: do after platform v1.2 is released: headers.put("Content-Type", apiCommand.getBaseResource().getContentType() + "; charset=utf-8");
      headers.put("Content-Type", apiCommand.getBaseResource().getContentType());
      headers.put("Accept", apiCommand.getBaseResource().getAccept());
      headers.put("Accept-Charset", "utf-8");
      httpResponse = doHttpPost(host + "/jobs/" + apiCommand.getEndpoint(),
        ((Jsonable) apiCommand.getBaseResource()).toJson(),
        headers,
        apiCommand.getHttpRequestRetryHandler(),
        apiCommand.getHttpRequestInterceptors());
    }
    catch (IOException e) {
      throw new HttpException(e);
    }
    return httpResponse;
  }

  /*
   * Who knew?  It turns out that the best way to force basic authentication in
   * Apache Http Client v4 is by using a request interceptor.  See:
   * http://javaevangelist.blogspot.com/2010/12/apache-httpclient-4x-preemptive.html
   */
  class PreemptiveAuth implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) {
      AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

      authState.setAuthScheme(new BasicScheme());
      authState.setCredentials(new UsernamePasswordCredentials(key, secret));
    }
  }

  private HttpResponse doHttpRequest(HttpRequestBase httpRequestBase, Map<String, String> headers, HttpRequestRetryHandler httpRequestRetryHandler, Collection<HttpRequestInterceptor> httpRequestInterceptors) throws IOException, UnsupportedEncodingException {
    DefaultHttpClient httpClient = new DefaultHttpClient();

    for (Iterator it = headers.keySet().iterator(); it.hasNext();) {
      String headerKey = (String) it.next();
      httpRequestBase.addHeader(headerKey, headers.get(headerKey));
    }
    httpRequestBase.addHeader("User-Agent", getUserAgent());

    // Register the retry handler
    if (httpRequestRetryHandler != null) {
      httpClient.setHttpRequestRetryHandler(httpRequestRetryHandler);
    }

    // Register the interceptors
    if (httpRequestInterceptors != null) {
      for (Iterator<HttpRequestInterceptor> it = httpRequestInterceptors.iterator(); it.hasNext();) {
        httpClient.addRequestInterceptor(it.next());
      }
    }

    /*
     * The preemptive authentication interceptor should run first, so that the
     * built-in HTTP client authentication interceptors will back off (they'll detect that
     * the authentication scheme already has been set).
     */
    httpClient.addRequestInterceptor(new PreemptiveAuth(), 0);

    return httpClient.execute(httpRequestBase);
  }
}
