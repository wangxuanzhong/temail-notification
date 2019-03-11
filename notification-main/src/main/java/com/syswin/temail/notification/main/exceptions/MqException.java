package com.syswin.temail.notification.main.exceptions;

import com.syswin.temail.notification.foundation.exceptions.BaseException;

public class MqException extends BaseException {

  public MqException(String message) {
    super(message);
  }

  public MqException(String message, Throwable cause) {
    super(message, cause);
  }
}
