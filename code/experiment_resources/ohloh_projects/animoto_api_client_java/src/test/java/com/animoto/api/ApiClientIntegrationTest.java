package com.animoto.api;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpStatus;

import com.animoto.api.util.DirectingManifestFactory;
import com.animoto.api.util.RenderingManifestFactory;

import com.animoto.api.resource.BaseResource;
import com.animoto.api.resource.DirectingJob;
import com.animoto.api.resource.RenderingJob;
import com.animoto.api.resource.DirectingAndRenderingJob;
import com.animoto.api.resource.Storyboard;
import com.animoto.api.resource.StoryboardBundlingJob;
import com.animoto.api.resource.StoryboardUnbundlingJob;
import com.animoto.api.resource.Video;

import com.animoto.api.DirectingManifest;
import com.animoto.api.RenderingManifest;
import com.animoto.api.RenderingParameters;

import com.animoto.api.visual.Image;

import com.animoto.api.exception.ContractException;
import com.animoto.api.exception.HttpExpectationException;
import com.animoto.api.exception.HttpException;

import com.animoto.api.enums.Framerate;
import com.animoto.api.enums.Pacing;

import com.animoto.api.postroll.*;

public class ApiClientIntegrationTest extends TestCase {
  protected ApiClient apiClient = null;

  public void setUp() {
    apiClient = ApiClientFactory.newInstance();
  }

  public void testDirecting() {
    createDirectingJob();
  }

  public void testPacing() {
      boolean cover = true;

      RenderingJob renderingJob = createRenderingJob(Pacing.VERY_SLOW);

      try {
        assertVideo(renderingJob.getVideo(), cover);
        System.out.println("VERY_SLOW: " + renderingJob.getVideo().getLocation());
      }
      catch (Exception e) {
        fail(e.toString());
      }

      renderingJob = createRenderingJob(Pacing.VERY_FAST);

      try {
        assertVideo(renderingJob.getVideo(), cover);
        System.out.println("VERY_FAST: " + renderingJob.getVideo().getLocation());
      }
      catch (Exception e) {
        fail(e.toString());
      }
  }

  public void testDirectingWithInternationalCharacters() {
    boolean cover = false;
    createDirectingJob("Radical title \u21A4 \u00D3", cover);
  }

  public void testDelete() throws HttpException, HttpExpectationException, ContractException {
    DirectingJob directingJob = createDirectingJob();
    Storyboard storyboard = directingJob.getStoryboard();

    apiClient.reload(storyboard);
    apiClient.delete(storyboard);

    try {
      apiClient.delete(storyboard); // Should fail (already deleted)
      fail("No exception when deleting storyboard twice!");
    } catch(HttpExpectationException e) {
      System.out.println("Expected exception when deleting storyboard twice: " + e);
      assertEquals(e.getReceivedCode(), HttpStatus.SC_GONE);
    }

    try {
      apiClient.reload(storyboard); // Should fail, since we deleted the storyboad
      fail("No exception when trying to reload after delete!");
    } catch(HttpExpectationException e) {
      System.out.println("Expected exception when trying reload after delete: " + e);
      assertEquals(e.getReceivedCode(), HttpStatus.SC_GONE);
    }
  }

  public void testHttpExceptionThrownOnNetworkIssues() {
    try {
      apiClient.setHost("http://nowhere.com");
      apiClient.direct(DirectingManifestFactory.newInstance());
      fail("Expected exception to be thrown!");
    }
    catch (HttpException e) {
      assertTrue(e.getException() instanceof java.net.UnknownHostException);
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testBundlingUnbundling() throws HttpExpectationException, HttpException, ContractException {
    /*
     * We'll test the full cycle:
     * * Create a directing job
     * * Bundle
     * * Delete the job
     * * Unbundle
     * * Render
     */
    boolean cover = true;
    DirectingJob directingJob = createDirectingJob("BOOG", cover);
    Storyboard storyboard = directingJob.getStoryboard();

    StoryboardBundlingManifest bundlingManifest = new StoryboardBundlingManifest();
    bundlingManifest.setStoryboard(storyboard);

    StoryboardBundlingJob bundlingJob = apiClient.bundle(bundlingManifest);

    assertNotNull(bundlingJob);
    assertNotNull(bundlingJob.getLocation());
    assertNotNull(bundlingJob.getRequestId());
    assertEquals("bundling", bundlingJob.getState());

    waitForJobCompletion(bundlingJob);

    assertTrue(bundlingJob.isCompleted());

    String bundleUrl = bundlingJob.getBundleUrl();

    System.out.println("Created storyboard bundle: " + bundleUrl);

    apiClient.delete(storyboard);

    try {
      apiClient.reload(storyboard); // Should fail, since we deleted the storyboad
      fail("No exception when trying to reload after delete!");
    } catch(HttpExpectationException e) {
      assertEquals(e.getReceivedCode(), HttpStatus.SC_GONE);
    }

    StoryboardUnbundlingManifest unbundlingManifest = new StoryboardUnbundlingManifest();
    unbundlingManifest.setBundleUrl(bundleUrl);
    StoryboardUnbundlingJob unbundlingJob = apiClient.unbundle(unbundlingManifest);

    waitForJobCompletion(unbundlingJob);

    assertTrue(unbundlingJob.isCompleted());

    System.out.println("Unbundled " + bundleUrl + " to " + unbundlingJob.getStoryboard().getLocation());

    apiClient.reload(unbundlingJob.getStoryboard());

    RenderingManifest renderingManifest = RenderingManifestFactory.newInstance();
    renderingManifest.setStoryboard(unbundlingJob.getStoryboard());
    RenderingJob renderingJob = apiClient.render(renderingManifest);

    waitForJobCompletion(renderingJob);

    assertTrue(renderingJob.isCompleted());
    assertNotNull(renderingJob.getVideo());

    assertVideo(renderingJob.getVideo(), cover);

    System.out.println("Rendered unbundling job to " + renderingJob.getVideo().getLocation());
  }

  public void testStoryboard() {
    DirectingJob directingJob = createDirectingJob();
    Storyboard storyboard = directingJob.getStoryboard();

    try {
      apiClient.reload(storyboard);
      assertNotNull(storyboard.getLinks());
      assertTrue(storyboard.getLinks().size() > 0);
      assertNotNull(storyboard.getMetadata());
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testPostroll() {
    try {
      DirectingJob directingJob = createDirectingJob();
      DirectingManifest directingManifest = directingJob.getDirectingManifest();
      BasicPostroll postroll = (BasicPostroll) directingManifest.getPostroll();

      assertNotNull(postroll);
      assertNotNull(postroll.getTemplate());
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testCustomFootagePostroll() {
    try {
      String sourceUrl = "https://postrolls.com/postroll.mp4";
      DirectingJob directingJob = createDirectingJobWithCustomFootagePostroll(sourceUrl);
      DirectingManifest directingManifest = directingJob.getDirectingManifest();
      CustomFootagePostroll postroll = (CustomFootagePostroll) directingJob.getDirectingManifest().getPostroll();

      assertEquals("custom_footage", postroll.getTemplate());
      assertNotNull(postroll.getSourceUrl());
      assertEquals(sourceUrl, postroll.getSourceUrl());
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testDirectingInterceptor() throws Exception {
    DirectingManifest directingManifest = DirectingManifestFactory.newInstance();
    List<HttpRequestInterceptor> list = new ArrayList<HttpRequestInterceptor>();
    DummyHttpRequestInterceptor interceptor = new DummyHttpRequestInterceptor();

    list.add(interceptor);
    apiClient.direct(directingManifest, null, null, null, list);
    assertTrue(interceptor.isVisited());
  }

  public void testDirectingFail() throws Exception {
    DirectingJob directingJob = null;
    DirectingManifest directingManifest = DirectingManifestFactory.newInstance();
    Image image = new Image();
    ApiError[] apiErrors = null;

    try {
      image.setSourceUrl("http://bad.com/link.gif");
      directingManifest.clearVisuals();
      directingManifest.addVisual(image);
      directingJob = apiClient.direct(directingManifest);

      waitForJobCompletion(directingJob);

      assertTrue(directingJob.isFailed());
      assertNotNull(directingJob.getResponse());
      apiErrors = directingJob.getResponse().getStatus().getApiErrors();
      assertTrue(apiErrors.length > 0);
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testRenderingRaisedException() throws Exception {
    DirectingJob directingJob = createDirectingJob();
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();

    renderingParameters.setFramerate(Framerate.F_30);
    renderingManifest.setStoryboard(directingJob.getStoryboard());
    renderingManifest.setRenderingParameters(renderingParameters);
    try {
      apiClient.render(renderingManifest);
      fail("Expected error from API!");
    }
    catch (HttpExpectationException e) {
      assertEquals(201, e.getExpectedCode());
      assertEquals(400, e.getReceivedCode());
      assertNotNull(e.getApiErrors());
      assertNotNull(e.getBody());
      assertEquals(4, e.getApiErrors().length);
    }
    catch (Exception e) {
      throw e;
    }
  }

  public void testRenderingJob() {
    createRenderingJob();
  }

  public void testVideo() {
    boolean cover = true;
    RenderingJob renderingJob = createRenderingJob();

    try {
      assertVideo(renderingJob.getVideo(), cover);
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  public void testDirectingAndRendering() {
    DirectingAndRenderingJob directingAndRenderingJob;
    boolean cover = false;
    DirectingManifest directingManifest = DirectingManifestFactory.newInstance(cover);
    RenderingManifest renderingManifest = RenderingManifestFactory.newInstance();

    try {
      directingAndRenderingJob = apiClient.directAndRender(directingManifest, renderingManifest);

      waitForJobCompletion(directingAndRenderingJob);

      assertTrue(directingAndRenderingJob.isCompleted());
      assertNotNull(directingAndRenderingJob.getStoryboard());
      assertNotNull(directingAndRenderingJob.getVideo());

      assertVideo(directingAndRenderingJob.getVideo(), cover);
    }
    catch (Exception e) {
      fail(e.toString());
    }
  }

  protected static final String DEFAULT_JOB_TITLE = "Java API Client Integration Test Video";

  protected DirectingJob createDirectingJob() {
    boolean cover = true;
    return createDirectingJob(DEFAULT_JOB_TITLE, cover);
  }

  protected DirectingJob createDirectingJob(String title, boolean cover) {
    return createDirectingJob(title, cover, null);
  }

  protected DirectingJob createDirectingJob(String title, boolean cover, Pacing pacing) {
    DirectingManifest manifest = DirectingManifestFactory.newInstance();
    return createDirectingJobFromManifest(title, cover, pacing, manifest);
  }

  protected DirectingJob createDirectingJobWithCustomFootagePostroll(String sourceUrl) {
    DirectingManifest manifest = DirectingManifestFactory.newInstanceWithCustomFootagePostroll(sourceUrl);
    return createDirectingJobFromManifest(DEFAULT_JOB_TITLE, false, null, manifest);
  }

  protected DirectingJob createDirectingJobFromManifest(String title, boolean cover, Pacing pacing, DirectingManifest directingManifest) {
    directingManifest.setTitle(title);

    if(pacing != null) {
        directingManifest.setPacing(pacing);
    }

    DirectingJob directingJob = null;

    try {
      // Post a directing job to the API.
      directingJob = apiClient.direct(directingManifest);
      assertNotNull(directingJob);
      assertNotNull(directingJob.getLocation());
      assertNotNull(directingJob.getRequestId());
      assertEquals("retrieving_assets", directingJob.getState());

      waitForJobCompletion(directingJob);

      // Job is complete!
      assertTrue(directingJob.isCompleted());
      assertNotNull(directingJob.getStoryboard());
      assertNotNull(directingJob.getResponse());
      assertNotNull(directingJob.getStoryboard().getLocation());
    }
    catch (Exception e) {
      fail(e.toString());
    }
    return directingJob;
  }

  protected RenderingJob createRenderingJob() {
    return createRenderingJob(null);
  }

  protected RenderingJob createRenderingJob(Pacing pacing) {
    DirectingJob directingJob = createDirectingJob("Test", true, pacing);
    RenderingJob renderingJob = null;
    RenderingManifest renderingManifest = RenderingManifestFactory.newInstance();

    try {
      renderingManifest.setStoryboard(directingJob.getStoryboard());
      renderingJob = apiClient.render(renderingManifest);
      assertNotNull(renderingJob.getLocation());
      assertNotNull(renderingJob.getRequestId());

      waitForJobCompletion(renderingJob);

      assertTrue(renderingJob.isCompleted());
      assertNotNull(renderingJob.getVideo());
      assertNotNull(renderingJob.getStoryboard());
    }
    catch (Exception e) {
      fail(e.toString());
    }
    return renderingJob;
  }

  private void assertVideo(Video video, boolean cover) throws HttpException, HttpExpectationException, ContractException {
    apiClient.reload(video);

    assertNotNull(video.getLinks());
    assertTrue(video.getLinks().size() > 0);

    assertNotNull(video.getDownloadUrl());
    assertNotNull(video.getRenderingParameters());

    if(cover) {
      assertNotNull(video.getCoverImageUrl());
    } else {
      assertNull(video.getCoverImageUrl());
    }
  }

  /*
   * TODO: Consider making this part of ApiClient; how long to sleep
   * between polling attempts certainly is a best practices issue.
   * Thought: should we poll for a very short amount of time initially
   * (say 50 ms) and slowly increase the polling interval (adaptive
   * polling)?
   */
  private void waitForJobCompletion(BaseResource job) throws HttpException, HttpExpectationException, ContractException {
    assertTrue(job.isPending());

    while(job.isPending()) {
      assertFalse(job.isCompleted());
      assertFalse(job.isFailed());
      try {
        Thread.sleep(1000);
      }
      catch (Exception ignored) {}
      apiClient.reload(job);
    }

    assertTrue(job.isCompleted() || job.isFailed());
    assertEquals(job.isCompleted(), !job.isFailed());
  }
}
