package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class TopicEvent {

  // 事件参数
  @JsonIgnore
  private Long id;
  @JsonIgnore
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  // 话题参数
  private String topicId;
  private String msgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private Long timestamp;

  // 以下参数均存入扩展参数字段
  // 话题主题
  private String title;
  // 收件人
  private List<String> receivers;
  // 抄送
  private List<String> cc;
  // 批量msgId
  private List<String> msgIds;

  @JsonIgnore
  private String extendParam;

  public TopicEvent() {
  }

  public TopicEvent(String xPacketId, Integer eventType, String topicId, String msgId, Long seqId, String message, String from, String to,
      Long timestamp) {
    this.xPacketId = xPacketId;
    this.eventType = eventType;
    this.topicId = topicId;
    this.msgId = msgId;
    this.seqId = seqId;
    this.message = message;
    this.from = from;
    this.to = to;
    this.timestamp = timestamp;
  }

  /**
   * 生成seqId
   */
  public void initTopicEventSeqId(SequenceService sequenceService) {
    this.eventSeqId = sequenceService.getNextSeq(this.topicId + "_" + this.to);
  }

  /**
   * 自动解析扩展字段
   */
  public TopicEvent autoReadExtendParam(JsonService jsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      TopicExtendParam extendParam = jsonService.fromJson(this.extendParam, TopicExtendParam.class);
      this.msgIds = extendParam.getMsgIds();
    }
    return this;
  }

  /**
   * 自动配置扩展字段
   */
  public TopicEvent autoWriteExtendParam(JsonService jsonService) {
    this.extendParam = jsonService.toJson(new TopicExtendParam(this.title, this.receivers, this.cc, this.msgIds));
    return this;
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

  public String getTopicId() {
    return topicId;
  }

  public void setTopicId(String topicId) {
    this.topicId = topicId;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<String> getReceivers() {
    return receivers;
  }

  public void setReceivers(List<String> receivers) {
    this.receivers = receivers;
  }

  public List<String> getCc() {
    return cc;
  }

  public void setCc(List<String> cc) {
    this.cc = cc;
  }

  public List<String> getMsgIds() {
    return msgIds;
  }

  public void setMsgIds(List<String> msgIds) {
    this.msgIds = msgIds;
  }

  public String getExtendParam() {
    return extendParam;
  }

  public void setExtendParam(String extendParam) {
    this.extendParam = extendParam;
  }

  @Override
  public String toString() {
    return "TopicEvent{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", topicId='" + topicId + '\'' +
        ", msgId='" + msgId + '\'' +
        ", seqId=" + seqId +
        ", message length='" + (message == null ? 0 : message.length()) + '\'' +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", timestamp=" + timestamp +
        ", msgIds=" + msgIds +
        ", extendParam='" + extendParam + '\'' +
        '}';
  }
}