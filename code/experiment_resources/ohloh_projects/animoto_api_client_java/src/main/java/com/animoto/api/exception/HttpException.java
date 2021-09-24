package com.animoto.api.exception;

/**
 * This exception wraps around any Java net exceptions that might be thrown, i.e. network issues, malformed response.<p/>
 */
public class HttpException extends ApiException {
  private Exception exception;

  public HttpException(Exception exception) {
    this.exception = exception;
  }

  /**
   * Get the underlying exception that was caused.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Call the underlying exception's toString().
   */
  public String toString() {
    return exception.toString();
  }
}
