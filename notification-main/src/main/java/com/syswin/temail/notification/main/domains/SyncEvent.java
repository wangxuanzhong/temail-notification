package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class SyncEvent {

  @JsonIgnore
  private Long id;
  @JsonIgnore
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  private String from;
  private String to;

  @JsonIgnore
  private String header;

  public SyncEvent() {
  }

  public SyncEvent(String xPacketId, Integer eventType, String from, String to) {
    this.xPacketId = xPacketId;
    this.eventType = eventType;
    this.from = from;
    this.to = to;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getxPacketId() {
    return xPacketId;
  }

  public void setxPacketId(String xPacketId) {
    this.xPacketId = xPacketId;
  }

  public Long getEventSeqId() {
    return eventSeqId;
  }

  public void setEventSeqId(Long eventSeqId) {
    this.eventSeqId = eventSeqId;
  }

  public Integer getEventType() {
    return eventType;
  }

  public void setEventType(Integer eventType) {
    this.eventType = eventType;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  @Override
  public String toString() {
    return "SyncEvent{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", header='" + header + '\'' +
        '}';
  }
}