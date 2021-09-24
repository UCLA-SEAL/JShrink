package com.animoto.api.cli;

import com.animoto.api.ApiClient;
import com.animoto.api.ApiCommand;
import com.animoto.api.util.FileUtil;
import com.animoto.api.util.StringUtil;

import com.animoto.api.resource.Resource;
import com.animoto.api.resource.BaseResource;
import com.animoto.api.resource.DirectingJob;
import com.animoto.api.resource.RenderingJob;
import com.animoto.api.resource.DirectingAndRenderingJob;
import com.animoto.api.resource.Storyboard;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

public class ApiClientCli extends ApiClient {

  private static ApiClientCli apiClient = null;

  public static void main(String[] args) throws Exception {
    CommandLineParser parser = new PosixParser();
    Options options = getOptions();
    CommandLine commandLine = null;
    String value = null;
    String data = null;
    DirectingJob directingJob = null;
    Storyboard storyboard = null;
    RawDirectingJob rawDirectingJob = null;
    RawRenderingJob rawRenderingJob = null;
    RawDirectingAndRenderingJob rawDirectingAndRenderingJob = null;
    String key, secret, host;
 
    try {
      commandLine = parser.parse(options, args);
      if (args.length == 0 || commandLine.hasOption("help")) {
        new HelpFormatter().printHelp("animoto-api", options);
        return;
      }

      key = commandLine.getOptionValue("key");
      secret = commandLine.getOptionValue("secret");
      host = commandLine.getOptionValue("host");
      if (StringUtil.isBlank(key) || StringUtil.isBlank(secret)) {
        throw new Error("Must provide an API key and secret.");
      }
      apiClient = new ApiClientCli();
      apiClient.setKey(key);
      apiClient.setSecret(secret);
      if (host != null) {
        apiClient.setHost(host);
      }

      if (commandLine.hasOption("create-directing-job")) {
        doApiPost(new RawDirectingJob(), commandLine.getOptionValue("create-directing-job"));
      }
      else if (commandLine.hasOption("create-rendering-job")) {
        doApiPost(new RawRenderingJob(), commandLine.getOptionValue("create-rendering-job"));
      }
      else if (commandLine.hasOption("create-directing-and-rendering-job")) {
        doApiPost(new RawDirectingAndRenderingJob(), commandLine.getOptionValue("create-directing-and-rendering-job"));
      }
      else if (commandLine.hasOption("storyboard")) {
        doApiGet("Storyboard", commandLine.getOptionValue("storyboard"));
      }
      else if (commandLine.hasOption("directing-job")) {
        doApiGet("DirectingJob", commandLine.getOptionValue("directing-job"));
      }
      else if (commandLine.hasOption("rendering-job")) {
        doApiGet("RenderingJob", commandLine.getOptionValue("rendering-job"));
      }
      else if (commandLine.hasOption("directing-and-rendering-job")) {
        doApiGet("DirectingAndRenderingJob", commandLine.getOptionValue("directing-and-rendering-job"));
      }
      else if (commandLine.hasOption("video")) {
        doApiGet("Video", commandLine.getOptionValue("video"));
      }
    }
    catch (Exception e) {
      throw e;
    }
  }

  private static void doApiPost(Raw raw, String file) throws Exception {
    ApiCommand apiCommand = new ApiCommand();
    raw.setRawEntity(FileUtil.readFile(file));
    apiCommand.setBaseResource((BaseResource) raw);
    apiClient.executeApiCommandAndExpectHttp201(apiCommand);
    ((BaseResource) raw).prettyPrintToSystem();
  }

  private static void doApiGet(String clazzName, String url) throws Exception {
    BaseResource resource = (BaseResource) Class.forName("com.animoto.api.resource." + clazzName).newInstance();
    resource.setUrl(url);
    apiClient.reload(resource);
    resource.prettyPrintToSystem();
  }

  private static Options getOptions() {
    Options options = new Options();
    options.addOption("k", "key", true, "Your Animoto API key");
    options.addOption("x", "secret", true, "Your Animoto API secret");
    options.addOption("s", "storyboard", true, "The storyboard URL you want to GET.");
    options.addOption("v", "video", true, "The video URL you want to GET.");
    options.addOption("r", "rendering-job", true, "The rendering job URL you want to GET.");
    options.addOption("d", "directing-job", true, "The directing job URL you want to GET.");
    options.addOption("dr", "directing-and-rendering-job", true, "The directing_and_rendering job URL you want to GET");
    options.addOption("cr", "create-rendering-job", true, "The file with JSON payload you want to POST to API to create a rendering job.");
    options.addOption("cd", "create-directing-job", true, "The file with JSON payload you want to POST to API to create a directing job.");
    options.addOption("cdr", "create-directing-and-rendering-job", true, "The file with JSON payload you want to POST to API to create a directing_and_rendering job");
    options.addOption("h", "help", false, "Print CLI help");
    options.addOption("t", "host", true, "The API host to communicate to (default: https://api.animoto.com)."); 
    return options;
  }
}
