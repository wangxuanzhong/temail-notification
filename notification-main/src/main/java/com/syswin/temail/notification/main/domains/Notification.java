package com.syswin.temail.notification.main.domains;

import java.sql.Timestamp;

public class Notification {

  private String from;
  private String to;

  private Long messageId;
  private String sequenceId;
  private String messageSummary;

  private String type;
  private Timestamp clientSentTimestamp;

  public Notification() {
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

  public String getSequenceId() {
    return sequenceId;
  }

  public void setSequenceId(String sequenceId) {
    this.sequenceId = sequenceId;
  }

  public String getMessageSummary() {
    return messageSummary;
  }

  public void setMessageSummary(String messageSummary) {
    this.messageSummary = messageSummary;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Timestamp getClientSentTimestamp() {
    return clientSentTimestamp;
  }

  public void setClientSentTimestamp(Timestamp clientSentTimestamp) {
    this.clientSentTimestamp = clientSentTimestamp;
  }

  @Override
  public String toString() {
    return "Notification{" +
        "from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", messageId=" + messageId +
        ", sequenceId='" + sequenceId + '\'' +
        ", messageSummary='" + messageSummary + '\'' +
        ", type='" + type + '\'' +
        ", clientSentTimestamp=" + clientSentTimestamp +
        '}';
  }
}