package com.syswin.temail.notification.main.domains.params;

public class MailAgentParams {

  public static final String MSG_ID_SPLIT = ",";

  // eventType
  private Integer sessionMssageType;
  private String from;
  private String to;
  private String msgid;
  // message
  private String toMsg;
  private Long timestamp;

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

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
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
        ", toMsg='" + toMsg + '\'' +
        ", timestamp=" + timestamp +
        ", header='" + header + '\'' +
        '}';
  }
}