package com.syswin.temail.notification.main.domains;

import java.sql.Timestamp;

public class MailAgentParams {

  // eventType
  private Integer sessionMssageType;

  private String from;
  private String to;

  private String msgid;
  private Long fromSeqNo;
  private String toMsg;
  private Timestamp timestamp;

  private Integer messageSize;

  private String header;

  public Integer getSessionMssageType() {
    return sessionMssageType;
  }

  public void setSessionMssageType(Integer sessionMssageType) {
    this.sessionMssageType = sessionMssageType;
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

  public String getMsgid() {
    return msgid;
  }

  public void setMsgid(String msgid) {
    this.msgid = msgid;
  }

  public String getToMsg() {
    return toMsg;
  }

  public void setToMsg(String toMsg) {
    this.toMsg = toMsg;
  }

  public Long getFromSeqNo() {
    return fromSeqNo;
  }

  public void setFromSeqNo(Long fromSeqNo) {
    this.fromSeqNo = fromSeqNo;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getMessageSize() {
    return messageSize;
  }

  public void setMessageSize(Integer messageSize) {
    this.messageSize = messageSize;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  @Override
  public String toString() {
    return "MailAgentParams{" +
        "sessionMssageType=" + sessionMssageType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", msgid='" + msgid + '\'' +
        ", fromSeqNo=" + fromSeqNo +
        ", toMsg='" + toMsg + '\'' +
        ", timestamp=" + timestamp +
        ", messageSize=" + messageSize +
        ", header='" + header + '\'' +
        '}';
  }
}