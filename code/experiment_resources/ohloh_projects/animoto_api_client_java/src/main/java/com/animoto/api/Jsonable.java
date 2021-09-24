package com.animoto.api;

/**
 * All implementing classes will support the ability to be serialized into JSON.
 */
public interface Jsonable {
  public String toJson();
}
