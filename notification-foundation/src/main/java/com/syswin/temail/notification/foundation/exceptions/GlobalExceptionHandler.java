package com.syswin.temail.notification.foundation.exceptions;

import com.syswin.temail.notification.foundation.domains.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Response> handleException(Exception e) {
    logger.error("Exception:{}", getExceptionStack(e));
    return new ResponseEntity<>(new Response(HttpStatus.INTERNAL_SERVER_ERROR, "system exception: " + e), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private String getExceptionStack(Exception e) {
    StringBuilder stack = new StringBuilder(e.toString() + "\n");
    System.out.println(e.getLocalizedMessage());
    for (StackTraceElement s : e.getStackTrace()) {
      stack.append(s + "\n");
    }
    return stack.toString();
  }
}

