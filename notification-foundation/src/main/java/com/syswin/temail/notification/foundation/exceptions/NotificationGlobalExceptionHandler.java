package com.syswin.temail.notification.foundation.exceptions;

import com.syswin.temail.notification.foundation.domains.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author 刘森
 */
@ControllerAdvice
public class NotificationGlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationGlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Response> handleException(Exception e) {
    LOGGER.error("Exception:{}", getExceptionStack(e));
    return new ResponseEntity<>(new Response(HttpStatus.INTERNAL_SERVER_ERROR, "system exception: " + e), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * spring boot 请求参数判空校验异常
   */
  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<Response> handleServletRequestBindingException(Exception e) {
    LOGGER.warn("Exception:{}", getExceptionStack(e));
    return new ResponseEntity<>(new Response(HttpStatus.BAD_REQUEST, "bad request exception: " + e), HttpStatus.BAD_REQUEST);
  }

  private String getExceptionStack(Exception e) {
    StringBuilder stack = new StringBuilder(e.toString() + "\n");
    for (StackTraceElement s : e.getStackTrace()) {
      stack.append(s).append("\n");
    }
    return stack.toString();
  }
}

