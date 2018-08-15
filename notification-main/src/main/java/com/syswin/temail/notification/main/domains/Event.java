package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Event extends Message {

  private Long id;
  private Integer eventType;

  public Event() {
  }

  public Event(String msgId, Long seqId, String message, String from, String to, Long timestamp, Integer eventType) {
    super(msgId, seqId, message, from, to, timestamp);
    this.eventType = eventType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getEventType() {
    return eventType;
  }

  public void setEventType(Integer eventType) {
    this.eventType = eventType;
  }

  public enum EventType {
    RECEIVE(0, "消息已接收"),
    PULLED(1, "消息已拉取"),
    RETRACT(2, "消息已撤回"),
    DESTROY(3, "消息已焚毁"),
    READ(4, "消息已读");

    private final int value;
    private final String description;

    EventType(int value, String description) {
      this.value = value;
      this.description = description;
    }

    public static EventType getByValue(int value) {
      for (EventType eventType : values()) {
        if (eventType.getValue() == value) {
          return eventType;
        }
      }
      return null;
    }

    public int getValue() {
      return value;
    }

    public String getDescription() {
      return description;
    }
  }
}