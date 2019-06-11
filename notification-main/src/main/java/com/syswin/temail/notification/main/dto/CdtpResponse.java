package com.syswin.temail.notification.main.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author liusen
 */
@JsonInclude(Include.NON_NULL)
public class CdtpResponse {

  private String receiver;
  private Integer eventType;
  private String header;
  private String data;

  public CdtpResponse() {
  }

  public CdtpResponse(String receiver, Integer eventType, String header, String data) {
    this.receiver = receiver;
    this.eventType = eventType;
    this.header = header;
    this.data = data;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public Integer getEventType() {
    return eventType;
  }

  public void setEventType(Integer eventType) {
    this.eventType = eventType;
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
    return "CdtpResponse{" +
        "receiver='" + receiver + '\'' +
        ", eventType=" + eventType +
        ", header='" + header + '\'' +
        ", data='" + data + '\'' +
        '}';
  }
}