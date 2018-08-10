package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.sql.Timestamp;

@JsonInclude(Include.NON_NULL)
public class Event {

  private Long id;
  private Long sequenceNo;
  private Integer eventType;
  private String from;
  private String to;

  private Long messageId;
  private Long messageSeqNo;
  private String message;

  private Timestamp clientSentTimestamp;

  private String header;

  public Event() {
  }

  public Event(Integer eventType, String from, String to, Long messageId, Long messageSeqNo, String message, String header) {
    this.eventType = eventType;
    this.from = from;
    this.to = to;
    this.messageId = messageId;
    this.messageSeqNo = messageSeqNo;
    this.message = message;
    this.header = header;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(Long sequenceNo) {
    this.sequenceNo = sequenceNo;
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

  public Long getMessageId() {
    return messageId;
  }

  public void setMessageId(Long messageId) {
    this.messageId = messageId;
  }

  public Long getMessageSeqNo() {
    return messageSeqNo;
  }

  public void setMessageSeqNo(Long messageSeqNo) {
    this.messageSeqNo = messageSeqNo;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Timestamp getClientSentTimestamp() {
    return clientSentTimestamp;
  }

  public void setClientSentTimestamp(Timestamp clientSentTimestamp) {
    this.clientSentTimestamp = clientSentTimestamp;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", sequenceNo=" + sequenceNo +
        ", eventType=" + eventType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", messageId=" + messageId +
        ", messageSeqNo='" + messageSeqNo + '\'' +
        ", message='" + message + '\'' +
        ", clientSentTimestamp=" + clientSentTimestamp +
        ", header='" + header + '\'' +
        '}';
  }

  public enum EventType {
    RECEIVE(0, "消息已接收"),
    READ(1, "消息已读"),
    RETRACT(2, "消息已撤回"),
    DESTROY(3, "消息已焚毁");

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