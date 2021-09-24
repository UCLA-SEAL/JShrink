package com.animoto.api.dto;

import junit.framework.TestCase;

import com.animoto.api.resource.DirectingJob;

public class PayloadTest extends TestCase {
  public void testGetBaseResource() {
		Payload payload = new Payload();
		assertNull(payload.getBaseResource(DirectingJob.class));
  }
}
