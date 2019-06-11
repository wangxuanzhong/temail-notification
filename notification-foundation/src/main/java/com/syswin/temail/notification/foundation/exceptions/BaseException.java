package com.syswin.temail.notification.foundation.exceptions;

/**
 * @author 刘森
 */
public class BaseException extends RuntimeException {

  public BaseException(String message) {
    super(message);
  }

  public BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
