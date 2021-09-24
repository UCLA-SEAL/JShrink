package com.animoto.api.submitjob;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;

import com.animoto.api.ApiClient;
import com.animoto.api.DirectingManifest;
import com.animoto.api.RenderingManifest;
import com.animoto.api.RenderingParameters;
import com.animoto.api.Song;
import com.animoto.api.enums.Format;
import com.animoto.api.enums.Framerate;
import com.animoto.api.enums.Resolution;
import com.animoto.api.exception.ContractException;
import com.animoto.api.exception.HttpException;
import com.animoto.api.exception.HttpExpectationException;
import com.animoto.api.resource.DirectingAndRenderingJob;
import com.animoto.api.resource.Video;
import com.animoto.api.visual.Image;
import com.animoto.api.visual.TitleCard;

/*
 * This program allows someone to dispatch a job to Animoto on the command-line, specifying
 * images, songs, ...
 *
 * This is meant to aide in reproducing and debugging issues.  Currently, only direct and render is supported.
 */
public class SubmitJob {
  private enum JobType {
    DIRECT_AND_RENDER
  }

  public static void usage() {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("SubmitJob", getOptions());
  }

  public SubmitJob(String[] args) throws ParseException {
    CommandLineParser parser = new PosixParser();
    CommandLine commandLine = parser.parse(getOptions(), args);

    if(commandLine.hasOption("loop")) {
      numIterations_ = Integer.parseInt(commandLine.getOptionValue("loop"));
    } else {
      numIterations_ = 1;
    }

    String key = commandLine.getOptionValue("key");
    String secret = commandLine.getOptionValue("secret");
    String host = commandLine.getOptionValue("host");

    String jobTypeString = commandLine.getOptionValue("job-type");

    /*
     * Turn the job type String into something that the JobType cand
     * understand.
     */
    String sanitizedJobTypeString = jobTypeString.toUpperCase().replace('-', '_');
    jobType_ = JobType.valueOf(sanitizedJobTypeString);

    if(commandLine.hasOption("image-file")) {
      String imageFileName = commandLine.getOptionValue("image-file");
      imageUrls_ = new ArrayList<String>();

      try {
        BufferedReader imageFileReader = new BufferedReader(new FileReader(imageFileName));
        String line;
        while((line = imageFileReader.readLine()) != null) {
          imageUrls_.add(line.trim());
        }
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    if(commandLine.hasOption("song")) {
      songUrl_ = commandLine.getOptionValue("song");
    }

    client_ = new ApiClient(key, secret, host);

    if(jobType_ == JobType.DIRECT_AND_RENDER) {
      if(songUrl_ == null) {
        throw new MissingArgumentException("must specify a song for " + jobTypeString + " jobs");
      }

      if(imageUrls_ == null) {
        throw new MissingArgumentException("must specify images for " + jobTypeString + " jobs");
      }
    } else {
      throw new UnrecognizedOptionException("code needs to be added for job type " + jobTypeString);
    }
  }

  public void runDirectAndRender() throws HttpExpectationException, HttpException, ContractException {
    System.out.println("Directing and rendering a video...");

    // Build a directing manifest with all the visuals and audio for the
    // video.
    DirectingManifest directingManifest = new DirectingManifest();

    // Song
    Song song = new Song();
    song.setSourceUrl(songUrl_);
    directingManifest.setSong(song);

    // Images
    for(String imageUrl : imageUrls_) {
      Image image = new Image();
      image.setSourceUrl(imageUrl);
      directingManifest.addVisual(image);
    }

    // Titlecard
    TitleCard titleCard = new TitleCard();
    titleCard.setH1("Test");
    titleCard.setH2("SubmitJob");
    directingManifest.addVisual(titleCard);

    //Footage footage = new Footage();
    //footage.setSourceUrl("http://api.client.java.animoto.s3.amazonaws.com/test_assets/footage.mp4");
    //directingManifest.addVisual(footage);

    // Set the video title
    directingManifest.setTitle("SubmitJob Test Video");

    // Create a rendering manifest to control things like video resolution and
    // framerate.
    // TODO: Perhaps some of this should be specified on the command-line?
    RenderingManifest renderingManifest = new RenderingManifest();
    RenderingParameters renderingParameters = new RenderingParameters();
    renderingParameters.setFramerate(Framerate.F_30);
    renderingParameters.setFormat(Format.H264);
    renderingParameters.setResolution(Resolution.R_360P);
    renderingManifest.setRenderingParameters(renderingParameters);

    DirectingAndRenderingJob directingAndRenderingJob = client_.directAndRender(directingManifest, renderingManifest);

    while(directingAndRenderingJob.isPending()) {
      /*
       * Aargh.  I *HATE* the Java sleep call; so complicated!
       */
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
      }

      client_.reload(directingAndRenderingJob);
    }

    if(directingAndRenderingJob.isFailed()) {
      System.out.println("Error!");
    } else {
      Video video = directingAndRenderingJob.getVideo();
      client_.reload(video);
      System.out.println("Created Video Successfully!");
      System.out.println("Download URL: " + video.getDownloadUrl());
      System.out.println("Cover Image URL: " + video.getCoverImageUrl());
    }
  }

  public void run() throws HttpExpectationException, HttpException, ContractException {
    for(int iterCount = 0; iterCount < numIterations_; ++iterCount) {
      if(jobType_ == JobType.DIRECT_AND_RENDER) {
        runDirectAndRender();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    /*
     * HACK: Apache Common CLI doesn't handle help gracefully.
     */
    boolean help = args.length == 0;
    for(String arg : args) {
      if((arg.equals("--help")) || (arg.equals("-h"))) {
        help = true;
      }
    }

    if(help) {
      usage();
      return;
    }

    try {
      SubmitJob application = new SubmitJob(args);
      application.run();
    } catch(ParseException e) {
      System.out.println("Error parsing arguments: " + e.getMessage() + "\n");
      SubmitJob.usage();
      System.exit(1);
    } catch(Exception e) {
      System.out.println("Error: " + e.getMessage() + "!\n");
      e.printStackTrace();
      System.exit(1);
    }
  }

  /*
   * A wrapper around Options.addOption() that adds a required option.
   *
   * @see Options.addOption()
   */
  private static void addRequiredOption(Options options, String opt, String longOpt, boolean hasArg, String description) {
    Option option = new Option(opt, longOpt, hasArg, description);
    option.setRequired(true);
    options.addOption(option);
  }

  private static Options getOptions() {
    Options options = new Options();

    addRequiredOption(options, "j", "job-type", true, "direct or direct-and-render");
    addRequiredOption(options, "k", "key", true, "Your Animoto API key");
    addRequiredOption(options, "x", "secret", true, "Your Animoto API secret");
    addRequiredOption(options, "t", "host", true, "The API host to communicate to");

    options.addOption("h", "help", false, "display help");
    options.addOption("I", "image-file", true, "A file containing a list of (publicly accessible) image URLs, one per line");
    options.addOption("l", "loop", true, "Submit the job the specified number of times (only one job is in flight at any given time)");
    options.addOption("s", "song", true, "The (publicly accessible) URL of the video's song");

    return options;
  }

  private ApiClient client_;
  private ArrayList<String> imageUrls_;
  private String songUrl_;
  private JobType jobType_;
  private int numIterations_;
}
