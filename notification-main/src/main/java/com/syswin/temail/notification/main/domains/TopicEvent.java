/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.syswin.temail.notification.foundation.application.IJsonService;
import java.util.List;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class TopicEvent {

  /**
   * 事件参数
   */
  @JsonIgnore
  private Long id;
  @JsonIgnore
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  /**
   * 话题参数
   */
  private String topicId;
  private Long topicSeqId;
  private String msgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private Long timestamp;

  // 以下参数均存入扩展参数字段
  /**
   * 话题主题
   */
  private String title;
  /**
   * 收件人
   */
  private List<String> receivers;
  /**
   * 抄送
   */
  private List<String> cc;
  /**
   * 批量msgId
   */
  private List<String> msgIds;
  /**
   * 删除会话是否同时删除消息
   */
  private Boolean deleteAllMsg;
  /**
   * 话题ExtData
   */
  private String extData;
  /**
   * 话题成员ExtData
   */
  private String memberExtData;

  @JsonIgnore
  private String extendParam;

  public TopicEvent() {
  }

  public TopicEvent(String xPacketId, Integer eventType, String topicId, String msgId, Long seqId, String message,
      String from, String to, Long timestamp) {
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
   * 自动解析扩展字段
   */
  public TopicEvent autoReadExtendParam(IJsonService iJsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      TopicExtendParam topicExtendParam = iJsonService.fromJson(this.extendParam, TopicExtendParam.class);
      this.title = topicExtendParam.getTitle();
      this.receivers = topicExtendParam.getReceivers();
      this.cc = topicExtendParam.getCc();
      this.msgIds = topicExtendParam.getMsgIds();
      this.deleteAllMsg = topicExtendParam.getDeleteAllMsg();
      this.extData = topicExtendParam.getExtData();
      this.memberExtData = topicExtendParam.getMemberExtData();
    }
    return this;
  }

  /**
   * 自动配置扩展字段
   */
  public TopicEvent autoWriteExtendParam(IJsonService iJsonService) {
    this.extendParam = iJsonService.toJson(
        new TopicExtendParam(this.title, this.receivers, this.cc, this.msgIds, this.deleteAllMsg, this.extData,
            this.memberExtData));
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

  public Long getTopicSeqId() {
    return topicSeqId;
  }

  public void setTopicSeqId(Long topicSeqId) {
    this.topicSeqId = topicSeqId;
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

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }

  public void setDeleteAllMsg(Boolean deleteAllMsg) {
    this.deleteAllMsg = deleteAllMsg;
  }

  public String getExtendParam() {
    return extendParam;
  }

  public void setExtendParam(String extendParam) {
    this.extendParam = extendParam;
  }

  public String getMemberExtData() {
    return memberExtData;
  }

  public void setMemberExtData(String memberExtData) {
    this.memberExtData = memberExtData;
  }

  public String getExtData() {
    return extData;
  }

  public void setExtData(String extData) {
    this.extData = extData;
  }

  @Override
  public String toString() {
    return "TopicEvent{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", topicId='" + topicId + '\'' +
        ", topicSeqId=" + topicSeqId +
        ", msgId='" + msgId + '\'' +
        ", seqId=" + seqId +
        ", message length='" + (message == null ? 0 : message.length()) + '\'' +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", timestamp=" + timestamp +
        ", title='" + title + '\'' +
        ", receivers=" + receivers +
        ", cc=" + cc +
        ", msgIds=" + msgIds +
        ", deleteAllMsg=" + deleteAllMsg +
        ", extData='" + extData + '\'' +
        ", memberExtData='" + memberExtData + '\'' +
        ", extendParam='" + extendParam + '\'' +
        '}';
  }
}