/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * @author liusen@syswin.com
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Response> handleException(Exception e) {
    LOGGER.error("Exception:{}", getExceptionStack(e));
    return new ResponseEntity<>(new Response(HttpStatus.INTERNAL_SERVER_ERROR, "system exception: " + e),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * spring boot 请求参数判空校验异常
   */
  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<Response> handleServletRequestBindingException(Exception e) {
    LOGGER.warn("Exception:{}", getExceptionStack(e));
    return new ResponseEntity<>(new Response(HttpStatus.BAD_REQUEST, "bad request exception: " + e),
        HttpStatus.BAD_REQUEST);
  }

  private String getExceptionStack(Exception e) {
    StringBuilder stack = new StringBuilder(e.toString() + "\n");
    for (StackTraceElement s : e.getStackTrace()) {
      stack.append(s).append("\n");
    }
    return stack.toString();
  }
}

