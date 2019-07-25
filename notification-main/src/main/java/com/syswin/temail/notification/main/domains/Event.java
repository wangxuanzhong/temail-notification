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
import com.syswin.temail.notification.main.util.EventUtil;
import com.syswin.temail.notification.main.util.GzipUtil;
import com.syswin.temail.notification.main.util.NotificationUtil;
import java.util.Arrays;
import java.util.List;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class Event {

  public static final String GROUP_CHAT_KEY_POSTFIX = "::event_group_chat";

  /**
   * 事件参数
   */
  @JsonIgnore
  private Long id;
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  /**
   * 单聊参数
   */
  private String msgId;
  private String parentMsgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private Long timestamp;

  /**
   * 群聊参数
   */
  private String groupTemail;
  private String temail;
  @JsonIgnore
  private Integer role;

  // dm参数
  /**
   * 全报文信息
   */
  private String packet;
  /**
   * 压缩后报文
   */
  @JsonIgnore
  private byte[] zipPacket;

  // 以下参数均存入扩展参数字段extendParam
  /**
   * 当事人名称
   */
  private String name;
  /**
   * at的temail
   */
  private String at;
  /**
   * 批量msgId
   */
  private List<String> msgIds;
  /**
   * 单聊删除会话是否同时删除消息
   */
  private Boolean deleteAllMsg;
  /**
   * 单聊消息拥有者
   */
  private String owner;
  /**
   * 废纸篓删除的消息明细
   */
  private String trashMsgInfo;
  /**
   * 群成员ExtData
   */
  private String memberExtData;

  @JsonIgnore
  private String extendParam;

  public Event() {
  }

  /**
   * 只需要初始化与mq消息字段名称不符的参数
   */
  public Event(Integer eventType, String msgId, Long seqId, String message) {
    this.eventType = eventType;
    this.msgId = msgId;
    this.seqId = seqId;
    this.message = message;
  }

  /**
   * 自动解析扩展字段
   */
  public Event autoReadExtendParam(IJsonService iJsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      NotificationUtil.copyField(iJsonService.fromJson(this.extendParam, Event.class), this);
    }
    return this;
  }

  /**
   * 自动配置扩展字段
   */
  public Event autoWriteExtendParam(String params) {
    this.extendParam = EventUtil.initExtendParam(params, this);
    return this;
  }

  /**
   * 压缩packet到zipPacket
   */
  public Event zip() {
    if (this.packet != null) {
      this.zipPacket = GzipUtil.zipWithDecode(this.packet);
    }
    return this;
  }

  /**
   * 解压缩zipPacket到packet
   */
  public Event unzip() {
    if (this.zipPacket != null) {
      this.packet = GzipUtil.unzipWithEncode(this.zipPacket);
    }
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

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getParentMsgId() {
    return parentMsgId;
  }

  public void setParentMsgId(String parentMsgId) {
    this.parentMsgId = parentMsgId;
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
    if (this.timestamp == null) {
      this.timestamp = System.currentTimeMillis();
    }
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

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }

  public String getPacket() {
    return packet;
  }

  public void setPacket(String packet) {
    this.packet = packet;
  }

  public byte[] getZipPacket() {
    return zipPacket;
  }

  public void setZipPacket(byte[] zipPacket) {
    this.zipPacket = zipPacket;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
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

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getTrashMsgInfo() {
    return trashMsgInfo;
  }

  public void setTrashMsgInfo(String trashMsgInfo) {
    this.trashMsgInfo = trashMsgInfo;
  }

  public String getExtendParam() {
    return extendParam;
  }

  public void setExtendParam(String extendParam) {
    this.extendParam = extendParam;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", msgId='" + msgId + '\'' +
        ", parentMsgId='" + parentMsgId + '\'' +
        ", seqId=" + seqId +
        ", message length='" + (message == null ? 0 : message.length()) + '\'' +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", timestamp=" + timestamp +
        ", groupTemail='" + groupTemail + '\'' +
        ", temail='" + temail + '\'' +
        ", role=" + role +
        ", packet='" + packet + '\'' +
        ", zipPacket=" + Arrays.toString(zipPacket) +
        ", name='" + name + '\'' +
        ", at='" + at + '\'' +
        ", msgIds=" + msgIds +
        ", deleteAllMsg=" + deleteAllMsg +
        ", owner='" + owner + '\'' +
        ", trashMsgInfo='" + trashMsgInfo + '\'' +
        ", extendParam='" + extendParam + '\'' +
        '}';
  }
}