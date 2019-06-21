package com.syswin.temail.notification.foundation.exceptions;

/**
 * @author liusen@syswin.com
 */
public class BaseException extends RuntimeException {

  public BaseException(String message) {
    super(message);
  }

  public BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
