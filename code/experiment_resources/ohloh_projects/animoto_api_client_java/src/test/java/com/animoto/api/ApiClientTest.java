package com.animoto.api;

import junit.framework.TestCase;

public class ApiClientTest extends TestCase {
  protected ApiClient apiClient = null;

  public void setUp() {
    apiClient = new ApiClient("foo", "bar");
  }

  public void testConstructor() {
    assertEquals("foo", apiClient.getKey());
    assertEquals("bar", apiClient.getSecret());
  }
}
