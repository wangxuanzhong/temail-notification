package com.syswin.temail.notification.main.domains.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CDTPResponse {

  private String receiver;
  private String header;
  private String data;

  public CDTPResponse() {
  }

  public CDTPResponse(String receiver, String header, String data) {
    this.receiver = receiver;
    this.header = header;
    this.data = data;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "CDTPResponse{" +
        "receiver='" + receiver + '\'' +
        ", header='" + header + '\'' +
        ", data=" + data +
        '}';
  }
}