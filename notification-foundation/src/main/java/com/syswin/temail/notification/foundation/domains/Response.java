package com.syswin.temail.notification.foundation.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.http.HttpStatus;

@JsonInclude(Include.NON_NULL)
public class Response<T> {

  private Integer code;
  private String message;
  private T data;
  private Paging paging;

  public Response() {
  }

  public Response(HttpStatus status) {
    this.code = status.value();
  }

  public Response(HttpStatus status, String message) {
    this.code = status.value();
    this.message = message;
  }

  public Response(HttpStatus status, String message, T data) {
    this.code = status.value();
    this.message = message;
    this.data = data;
  }

  public Response(HttpStatus status, String message, T data, Paging paging) {
    this.code = status.value();
    this.message = message;
    this.data = data;
    this.paging = paging;
  }

  public Integer getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public Paging getPaging() {
    return paging;
  }

}
