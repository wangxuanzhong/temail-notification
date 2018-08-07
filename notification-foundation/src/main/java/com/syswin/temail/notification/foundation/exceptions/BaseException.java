package com.syswin.temail.notification.foundation.exceptions;

public class BaseException extends RuntimeException {

  private String message;

  public BaseException(String message) {
    super(message);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
