package com.syswin.temail.notification.main.exceptions;

import com.syswin.temail.notification.foundation.exceptions.BaseException;

/**
 * @author liusen@syswin.com
 */
public class MqException extends BaseException {

  public MqException(String message) {
    super(message);
  }

  public MqException(String message, Throwable cause) {
    super(message, cause);
  }
}
