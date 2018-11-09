package com.syswin.temail.notification.main.domains.params;

public class MailAgentParams {

  public static final String MSG_ID_SPLIT = ",";

  // eventType
  private Integer sessionMssageType;
  private String from;
  private String to;
  private String msgid;
  // seqId
  private Long seqNo;
  // message
  private String toMsg;
  private Long timestamp;

  private String header;
  private String xPacketId;

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

  public Long getSeqNo() {
    return seqNo;
  }

  public void setSeqNo(Long seqNo) {
    this.seqNo = seqNo;
  }

  public String getToMsg() {
    return toMsg;
  }

  public void setToMsg(String toMsg) {
    this.toMsg = toMsg;
  }

  public Long getTimestamp() {
    if (this.timestamp == null) {
      this.timestamp = System.currentTimeMillis();
    }
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

  public String getxPacketId() {
    return xPacketId;
  }

  public void setxPacketId(String xPacketId) {
    this.xPacketId = xPacketId;
  }

  @Override
  public String toString() {
    return "MailAgentParams{" +
        "sessionMssageType=" + sessionMssageType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", msgid='" + msgid + '\'' +
        ", seqNo=" + seqNo +
        ", toMsg='" + toMsg + '\'' +
        ", timestamp=" + timestamp +
        ", header='" + header + '\'' +
        ", xPacketId='" + xPacketId + '\'' +
        '}';
  }
}