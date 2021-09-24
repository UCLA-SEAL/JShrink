package com.animoto.api;

/**
 * An ApiError represents the code:message that can be returned in an API response to the client.<p/>
 *
 * Please consult your API documentation for more information on possible errors.<p/>
 */
public class ApiError {
  private String code;
  private String message;

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String toString() {
    return code + ": " + message;
  }
}
