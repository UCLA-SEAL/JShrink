package com.animoto.api;

import org.apache.http.*;
import org.apache.http.protocol.*;

public class DummyHttpRequestInterceptor implements HttpRequestInterceptor {

  private boolean visited = false;
  
  public void process(HttpRequest request, HttpContext context) {
    visited = true;
  }

  public boolean isVisited() {
    return visited;
  }
}
