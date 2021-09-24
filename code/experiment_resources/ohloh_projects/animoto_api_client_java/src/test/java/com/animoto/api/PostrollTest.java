package com.animoto.api;

import junit.framework.TestCase;

import com.animoto.api.postroll.*;

import com.google.gson.*;
import com.animoto.api.gson.deserializer.PostrollDeserializer;

public class PostrollTest extends TestCase {
  protected GsonBuilder parserFactory = null;
  protected Gson parser = null;

  public void setUp() {
    parserFactory = new GsonBuilder();
    parserFactory.registerTypeAdapter(Postroll.class, new PostrollDeserializer());
    this.parser = parserFactory.create();
  }

  public static String DEFAULT_SOURCE_URL = "http://www.postrolls.com/animoto_postroll.mp4";

  public void testBasicPostrollInstantiation() {
    BasicPostroll postroll = new BasicPostroll();

    assertEquals(BasicPostroll.POWERED_BY_ANIMOTO, postroll.getTemplate());
  }

  public void testBasicPostrollTemplate() {
    BasicPostroll postroll = new BasicPostroll();
    String template = "fancy";
    postroll.setTemplate(template);

    assertEquals(template, postroll.getTemplate());
  }

  public void testBasicPostrollSerialization() {
    BasicPostroll postroll = new BasicPostroll();
    String json = parser.toJson(postroll);
    BasicPostroll parsed = parser.fromJson(json, BasicPostroll.class);

    assert(parsed instanceof BasicPostroll);
    assertEquals(postroll.getTemplate(), parsed.getTemplate());
  }

  public void testBasicPostrollDeserialization() {
    String json = "{\"template\":\"powered_by_animoto\"}";
    BasicPostroll postroll = parser.fromJson(json, BasicPostroll.class);

    assert(postroll instanceof BasicPostroll);
    assertEquals(BasicPostroll.POWERED_BY_ANIMOTO, postroll.getTemplate());
  }

  public void testCustomFootagePostrollInstantiation() {
    CustomFootagePostroll postroll = new CustomFootagePostroll();
    postroll.setSourceUrl(PostrollTest.DEFAULT_SOURCE_URL);

    assertEquals("custom_footage", postroll.getTemplate());
    assertEquals(PostrollTest.DEFAULT_SOURCE_URL, postroll.getSourceUrl());
  }

  public void testCustomFootagePostrollTemplate() {
    CustomFootagePostroll postroll = new CustomFootagePostroll();
    postroll.setSourceUrl(PostrollTest.DEFAULT_SOURCE_URL);
    String template = "fancy";
    try {
      postroll.setTemplate(template);
      fail("CustomFootagePostroll allowed template to be changed");
    } catch (Exception e) {
      return;
    }
  }

  public void testCustomFootagePostrollSourceUrl() {
    CustomFootagePostroll postroll = new CustomFootagePostroll();
    postroll.setSourceUrl(PostrollTest.DEFAULT_SOURCE_URL);
    String sourceUrl = "http://www.postrolls.com/other_postroll.mp4";
    postroll.setSourceUrl(sourceUrl);

    assertEquals(sourceUrl, postroll.getSourceUrl());
  }

  public void testCustomFootagePostrollSerialization() {
    CustomFootagePostroll postroll = new CustomFootagePostroll();
    postroll.setSourceUrl(PostrollTest.DEFAULT_SOURCE_URL);
    String json = parser.toJson(postroll);
    CustomFootagePostroll parsed = parser.fromJson(json, CustomFootagePostroll.class);

    assert(parsed instanceof CustomFootagePostroll);
    assertEquals(postroll.getTemplate(), parsed.getTemplate());
    assertEquals(postroll.getSourceUrl(), parsed.getSourceUrl());
  }

  public void testCustomFootagePostrollDeserialization() {
    String json = "{\"template\":\"custom_footage\",\"source_url\":\"" + PostrollTest.DEFAULT_SOURCE_URL + "\"}";
    CustomFootagePostroll postroll = (com.animoto.api.postroll.CustomFootagePostroll) parser.fromJson(json, Postroll.class);

    assert(postroll instanceof CustomFootagePostroll);
    assertEquals("custom_footage", postroll.getTemplate());
    assertEquals(PostrollTest.DEFAULT_SOURCE_URL, postroll.getSourceUrl());
  }
}
