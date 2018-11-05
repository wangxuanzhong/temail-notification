package com.syswin.temail.notification.main.domains.params;

public class MailAgentParams {

  public static final String MSG_ID_SPLIT = ",";

  // eventType 事件类型
  private Integer sessionMssageType;
  private String from;
  private String to;
  private String msgid;
  // 父消息ID
  private String parentMsgId;
  // seqId 消息序号
  private Long seqNo;
  // message 消息体
  private String toMsg;
  // 时间戳
  private Long timestamp;
  // @对象
  private String at;

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

  public String getParentMsgId() {
    return parentMsgId;
  }

  public void setParentMsgId(String parentMsgId) {
    this.parentMsgId = parentMsgId;
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

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
  }

  @Override
  public String toString() {
    return "MailAgentParams{" +
        "sessionMssageType=" + sessionMssageType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", msgid='" + msgid + '\'' +
        ", parentMsgId='" + parentMsgId + '\'' +
        ", seqNo=" + seqNo +
        ", toMsg='" + toMsg + '\'' +
        ", timestamp=" + timestamp +
        ", at='" + at + '\'' +
        ", header='" + header + '\'' +
        '}';
  }
}