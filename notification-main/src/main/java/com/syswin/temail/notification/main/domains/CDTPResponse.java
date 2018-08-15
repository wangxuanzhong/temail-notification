package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CDTPResponse {

  private String toTemail;
  private String header;
  private Event data;

  public CDTPResponse() {
  }

  public CDTPResponse(String toTemail, String header, Event data) {
    this.toTemail = toTemail;
    this.header = header;
    this.data = data;
  }

  public String getToTemail() {
    return toTemail;
  }

  public void setToTemail(String toTemail) {
    this.toTemail = toTemail;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public Event getData() {
    return data;
  }

  public void setData(Event data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "CDTPResponse{" +
        "toTemail='" + toTemail + '\'' +
        ", header='" + header + '\'' +
        ", data=" + data +
        '}';
  }
}