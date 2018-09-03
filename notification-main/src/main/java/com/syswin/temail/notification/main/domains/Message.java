package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Message {

  // 单聊参数
  private String msgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private Long timestamp;

  // 群聊参数
  private String groupTemail;
  private String temail;
  private String name;
  @JsonIgnore
  private Integer role;

  public Message() {
  }

  public Message(String msgId, Long seqId, String message, String from, String to, Long timestamp) {
    this.msgId = msgId;
    this.seqId = seqId;
    this.message = message;
    this.from = from;
    this.to = to;
    this.timestamp = timestamp;
  }

  public Message(String msgId, Long seqId, String message, String from, String to, Long timestamp, String groupTemail, String temail,
      String name, Integer role) {
    this.msgId = msgId;
    this.seqId = seqId;
    this.message = message;
    this.from = from;
    this.to = to;
    this.timestamp = timestamp;
    this.groupTemail = groupTemail;
    this.temail = temail;
    this.name = name;
    this.role = role;
  }

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public Long getSeqId() {
    return seqId;
  }

  public void setSeqId(Long seqId) {
    this.seqId = seqId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
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

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  public String getTemail() {
    return temail;
  }

  public void setTemail(String temail) {
    this.temail = temail;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "Message{" +
        "msgId='" + msgId + '\'' +
        ", seqId=" + seqId +
        ", message='" + message + '\'' +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", timestamp=" + timestamp +
        ", groupTemail='" + groupTemail + '\'' +
        ", temail='" + temail + '\'' +
        ", name='" + name + '\'' +
        ", role=" + role +
        '}';
  }
}
