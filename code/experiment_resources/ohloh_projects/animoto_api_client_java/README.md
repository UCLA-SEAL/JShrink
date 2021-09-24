Animoto API Java Client
=======================

The Animoto API is a RESTful web service that transforms images, videos,
music, and text into amazing video presentations.

The Animoto API Java Client provides a convenient Java interface for working
with the Animoto RESTful HTTP API.

### Topics

* [Who should read this document](#who_should_read_this_document)
* [What is covered in this document](#what_is_covered_in_this_document)
* [Getting Started using the Java Client](#getting_started_using_the_java_client)
* [How to contribute to this client](#how_to_contribute)

For for detailed Java client documentation, see the [java client javadocs][javadoc].

<a name="who_should_read_this_document"></a>
## Who should read this document?

This document is primarily aimed at developers looking to integrate with
Animoto services from a Java environment or using the Java language.

<a name="what_is_covered_in_this_document"></a>
## What is covered in this document

This document covers the technical details of the Animoto API Java client and 
provides a general overview of its use.

This document does not cover the details of the Animoto API itself. For such
information please see the [Animoto API documentation][api_docs].

<a name="getting_started_using_the_java_client"></a>
## Getting Started using the Java Client

### Requirements

The Animoto API Client requires Java 1.5. It is highly recommended that you
have Maven 2.2 or greater installed as well since all of the builds and test
scripts work off of Maven.

To download or learn more about Maven:

http://maven.apache.org

You must also have a valid Animoto Platform credential set to use the library.

### Dependencies

The Animoto API Client uses the following libraries:

* Apache Commons Bean Utils (http://commons.apache.org/beanutils)
* Apache Http Client 4.x (http://hc.apache.org/httpcomponents-client)
* Google Gson (http://code.google.com/p/google-gson)
* Apache Commons CLI (http://commons.apache.org/cli)

For testing, the following is used to verify JSON results:

* JSON Simple (http://code.google.com/p/json-simple)

All versions and dependencies are declared in the Maven project object model.

### Build Commands

The project uses the basic Maven tasks: 

* `mvn clean` - Clean your project directory.
* `mvn compile` - Compile the library.
* `mvn test` - Run all unit tests for the library.
* `mvn package` - Create a JAR file of the library.
* `mvn javadoc:javadoc` - Generate Javadoc documentation locally.
* `mvn assembly:assembly` - Generate one uber-JAR with all dependencies in the JAR. This is useful for running the CLI interface or SubmitJob.

### Sample Code
Some snippets have been provided below that demostrate the API's basic functionality.  It also will be useful to look at:
    src/main/java/com/animoto/api/submitjob/SubmitJob.java
    src/test/java/com/animoto/api/ApiClientIntegrationTest.java

### Creating a video using the Java client

This example shows you how to create a video in one shot using a 
`DirectingAndRenderingJob`.

    // Set up the api client
    ApiClient apiClient = ApiClientFactory.newInstance();

    // Build a directing manifest with all the visuals and audio for the
    // video.
    DirectingManifest directingManifest = new DirectingManifest();
    Image image = new Image();
    TitleCard titleCard = new TitleCard();
    Footage footage = new Footage();
    Song song = new Song();

    // Provide a song.
    song.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/song.mp3");
    directingManifest.setSong(song);

    // Provide an image.
    image.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/image.jpg");
    directingManifest.addVisual(image);

    // Provide a title card.
    titleCard.setH1("hello");
    titleCard.setH2("world");
    directingManifest.addVisual(titleCard);

    // Provide a video.
    footage.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/footage.mp4");
    directingManifest.addVisual(footage);

    // Set the video title
    directingManifest.setTitle("My Animoto Video");

    // Create a rendering manifest to control things like video resolution and
    // framerate
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();

    // Setup our rendering profile.
    renderingParameters.setFramerate(Framerate.F_30);
    renderingParameters.setFormat(Format.H264);
    renderingParameters.setResolution(Resolution.R_720P);

    // Set the storyboard from the Directing Job into the Rendering Manifest.
    renderingManifest.setRenderingParameters(renderingParameters);
    renderingManifest.setStoryboard(directingJob.getStoryboard());

    // Send the job to the API.  Status updates will be communicated via a 
    // HTTP POST to "http://mysite.com/animoto_callback"
    String httpCallbackUrl = "http://mysite.com/animoto_callback";
    DirectingAndRenderingJob directingAndRenderingJob = null;
    directingAndRenderingJob = apiClient.directAndRender(directingManifest, renderingManifest, httpCallbackUrl, HttpCallbackFormat.JSON);

### Rendering an existing Storyboard

If you already have a storyboard (from a previous directing job), you can then
tell the API to render a video.

    RenderingJob renderingJob = null;
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();

    // Setup our rendering profile.
    renderingParameters.setFramerate(Framerate.F_30);
    renderingParameters.setFormat(Format.H264);
    renderingParameters.setResolution(Resolution.R_720P);

    // Set the storyboard from the Directing Job into the Rendering Manifest.
    renderingManifest.setRenderingParameters(renderingParameters);
    renderingManifest.setStoryboard(directingJob.getStoryboard());
    renderingJob = apiClient.render(renderingManifest);

    // Wait until it is completed or failed. (You could also use http 
    // callbacks...)
    while(renderingJob.isPending()) {

      // You should probably sleep here before calling reload.
      apiClient.reload(renderingJob);
    }

    if (renderingJob.isCompleted()) {
      // Now we have a video!
      // Remember to reload the Video if you want the metadata and link information.
      renderingJob.getVideo();
    }

### Working with Storyboards and Videos

When you have a Storyboard object, you must query the API to get all the information related to the resource.

    // We have Storyboard location/URL but no information :(
    storyboard.getLocation();

    apiClient.reload(storyboard);

    // Now we have critical Storyboard links and metadata! :)
    storyboard.getLinks();
    storyboard.getMetadata();

Similarly, when you have a Video object, you must query the API to get all the information related to the resource.

    // We have a Video location/URL but no information :(
    video.getLocation();

    apiClient.reload(video);

    // Now we have critical Storyboard links and metadata! :)
    video.getLinks();
    video.getMetadata();

### Bundling and Unbundling

Bundling is a process used for exporting an Animoto storyboard. The bundling process packages
up all the information used by Animoto to render videos into a single file, the storyboard
bundle. This file is essentially just a zip file with a special structure. After a storyboard
bundle has been created and retrieved by your system, the original storyboard can be deleted.
Later, the storyboard bundle can be reconstituted into an Animoto storyboard so that videos may
be rendered.  The following snippet demonstrates this functionality:

    /*
     * We'll test the full cycle:
     * * Create a directing job
     * * Bundle
     * * Delete the job
     * * Unbundle
     * * Render
     */
    ApiClient apiClient = ApiClientFactory.newInstance();

    // Build a directing manifest with all the visuals and audio for the 
    // video.
    DirectingManifest directingManifest = new DirectingManifest();

    // Provide an image.
    Image image = new Image();
    image.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/image.jpg");
    directingManifest.addVisual(image);

    // Provide a song.
    Song song = new Song();
    song.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/song.mp3");
    directingManifest.setSong(song);

    // Provide a title card.
    TitleCard titleCard = new TitleCard();
    titleCard.setH1("hello");
    titleCard.setH2("world");
    directingManifest.addVisual(titleCard);

    // Provide a video.
    Footage footage = new Footage();
    footage.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/footage.mp4");
    directingManifest.addVisual(footage);

    // Set the video title
    directingManifest.setTitle("My Animoto Video");

    DirectingJob directingJob = apiClient.direct(directingManifest);

    while(!directingJob.isCompleted()) {
      try {
          Thread.sleep(1000);
      } catch(InterruptedException e) {}
      apiClient.reload(directingJob);
    }

    Storyboard storyboard = directingJob.getStoryboard();

    StoryboardBundlingManifest bundlingManifest = new StoryboardBundlingManifest();
    bundlingManifest.setStoryboard(storyboard);
    StoryboardBundlingJob bundlingJob = apiClient.bundle(bundlingManifest);

    while(!bundlingJob.isCompleted()) {
      try {
          Thread.sleep(1000);
      } catch(InterruptedException e) {}
      apiClient.reload(bundlingJob);
    }

    String bundleUrl = bundlingJob.getBundleUrl();
    System.out.println("Created storyboard bundle: " + bundleUrl);

    apiClient.delete(storyboard);

    StoryboardUnbundlingManifest unbundlingManifest = new StoryboardUnbundlingManifest();
    unbundlingManifest.setBundleUrl(bundleUrl);
    StoryboardUnbundlingJob unbundlingJob = apiClient.unbundle(unbundlingManifest);

    while(!unbundlingJob.isCompleted()) {
      try {
          Thread.sleep(1000);
      } catch(InterruptedException e) {}
      apiClient.reload(unbundlingJob);
    }

    System.out.println("Unbundled " + bundleUrl + " to " + unbundlingJob.getStoryboard().getLocation());

    // Create a rendering manifest to control things like video resolution and
    // framerate
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();

    // Setup our rendering profile.
    renderingParameters.setFramerate(Framerate.F_30);
    renderingParameters.setFormat(Format.H264);
    renderingParameters.setResolution(Resolution.R_720P);

    // Set the storyboard from the Directing Job into the Rendering Manifest.
    renderingManifest.setRenderingParameters(renderingParameters);
    renderingManifest.setStoryboard(unbundlingJob.getStoryboard());

    RenderingJob renderingJob = apiClient.render(renderingManifest);

    while(!renderingJob.isCompleted()) {
      try {
          Thread.sleep(1000);
      } catch(InterruptedException e) {}
      apiClient.reload(renderingJob);
    }

    System.out.println("Rendered unbundling job to " + renderingJob.getVideo().getLocation());

### Command Line Interface (CLI)

A small CLI application is provided to help you run the API on the command
line. Assuming you have generated a JAR with all dependencies (and you just
don't like using curl on the command line), you can run the CLI with a utility
shell script that sets the classpath and executes the CLI class:

./cli.sh --help

Note that you *must* run mvn assembly:assembly in order to generate the required JAR with all dependencies.

### SubmitJob

A small CLI application is provided to submit a job on the command line.
Assuming you have generated a JAR with all dependencies (mvn assembly:assembly),
you can run SubmitJob with a utility shell script that sets the classpath
and executes the SubmitJob class:

./submit_job.sh --help

### Integration Tests

By default when you run tests, only the unit tests are run. If you want to run
the integration test against the actual API services, then you can run the
primary integration test as follows:

  mvn -Dtest=ApiClientIntegrationTest test

You will need to have network connectivity for the integration test to work.
The integration test uses the ApiClientFactory which reads credentials from
the animoto_api_client.properties. Please edit this file under
src/test/resources, to add your own credentials.

### Wire Logging

You can use the built in features of Apache Http Client and Commons Logging to
view the network information between the Java API Client and the API. This
will allow you to use your current logging framework, whatever it is, to view
network information.

http://hc.apache.org/httpcomponents-client-4.0.1/logging.html

As a helper, you can enable wire logging by opening up the project object
model (POM) and enable the argList node for maven testing. The argList node
contains the recommended JVM arguments for wire logging. For the Animoto API
CLI, the cli wrapper script already contains wire logging arguments to the
JVM.

<a name="how_to_contribute"></a>
## How to contribute to this client

1. [Fork](http://help.github.com/forking/) `animoto/animoto_api_client_java`
2. Create a topic branch - `git checkout -b my_branch`
3. Push to your branch - `git push origin my_branch`
4. Create an [Issue](http://github.com/animoto/animoto_api_client_java/issues) with a link to your branch
5. That's it!

You might want to checkout our [the wiki page](http://wiki.github.com/animoto/animoto_api_client_java) for information on coding standards, new features, etc.

## Copyright Information

Copyright © 2010 Animoto Inc. All rights reserved. 

Notice – this software is owned by Animoto Inc. (“Animoto”) and is being licensed to you in source code form on the condition that you comply these terms. By continuing to use the software, you are agreeing to be bound by these terms. If you disagree with these terms, you must immediately stop using the software and permanently destroy it, or return it to Animoto. 

Animoto hereby grants to you a limited, personal, worldwide, nontransferable license to copy, execute, compile, reproduce, modify, prepare and have prepared derivative works of the software solely for the Purpose. The “Purpose” shall mean use of the software for accessing the Animoto Application Programming Interface (“API”) in accordance with Animoto’s Terms of Service, and any other terms and conditions applicable to your Animoto user account. 

For purposes of clarity, you agree that you have no right to use the software for any other purpose other than the Purpose as defined above. This license does not include the right to distribute, transfer, sell, or otherwise commercialize the software or any portion thereof. 

To the extent you make any derivative works from software, you hereby grant back to Animoto a worldwide, irrevocable, perpetual, sublicenseable, assignable, royalty-free license to such derivative works, in source and object code form. 

You agree to include these terms in all copies made of the software, and to not remove or alter the above copyright notice or these terms in such copies. 

THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE, OR THE WARRANTY OF NON-INFRINGEMENT. 

IN NO EVENT SHALL ANIMOTO BE LIABLE TO YOU OR ANY THIRD PARTY FOR ANY SPECIAL, PUNITIVE, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND, OR ANY DAMAGES WHATSOEVER, INCLUDING, WITHOUT LIMITATION, THOSE RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER OR NOT ANIMOTO HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES, AND ON ANY THEORY OF LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OF THIS SOFTWARE. 

You agree to indemnify defend and hold Animoto, its parents, subsidiaries, affiliates, officers and employees, harmless from any liabilities, claims, expenses or demands, including reasonable attorneys’ fees and costs, made by any third party due to or arising out of your use of this software, derivative works created by you, the violation of laws, rules, regulations or these terms, and the infringement of any intellectual property right by your derivative works or by you.

[javadoc]: http://api.client.java.animoto.s3.amazonaws.com/apidocs/index.html
[api_docs]: http://api-documentation.animoto.com/index.html
