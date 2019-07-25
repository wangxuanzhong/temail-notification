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
import com.syswin.temail.notification.main.util.NotificationUtil;
import com.syswin.temail.notification.main.util.TopicEventUtil;
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
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  /**
   * 话题参数
   */
  private String topicId;
  private String msgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private Long timestamp;

  // 以下参数均存入扩展参数字段
  /**
   * 批量msgId
   */
  private List<String> msgIds;
  /**
   * 删除会话是否同时删除消息
   */
  private Boolean deleteAllMsg;

  @JsonIgnore
  private String extendParam;

  public TopicEvent() {
  }

  /**
   * 只需要初始化与mq消息字段名称不符的参数
   */
  public TopicEvent(Integer eventType, String msgId, Long seqId, String message) {
    this.eventType = eventType;
    this.msgId = msgId;
    this.seqId = seqId;
    this.message = message;
  }

  /**
   * 自动解析扩展字段
   */
  public TopicEvent autoReadExtendParam(IJsonService iJsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      NotificationUtil.copyField(iJsonService.fromJson(this.extendParam, TopicEvent.class), this);
    }
    return this;
  }

  /**
   * 自动配置扩展字段
   */
  public TopicEvent autoWriteExtendParam(String params) {
    this.extendParam = TopicEventUtil.initExtendParam(params, this);
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
    if (this.timestamp == null) {
      this.timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
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
        ", deleteAllMsg=" + deleteAllMsg +
        ", extendParam='" + extendParam + '\'' +
        '}';
  }
}