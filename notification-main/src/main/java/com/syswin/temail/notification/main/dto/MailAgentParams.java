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

package com.syswin.temail.notification.main.dto;

import java.util.List;

/**
 * @author liusen@syswin.com
 */
public class MailAgentParams {

  public static final String MSG_ID_SPLIT = ",";

  /**
   * eventType 事件类型
   */
  private Integer sessionMessageType;
  /**
   * 发起方
   */
  private String from;
  /**
   * 接收方
   */
  private String to;
  /**
   * 消息ID
   */
  private String msgid;
  /**
   * 父消息ID
   */
  private String parentMsgId;
  /**
   * seqId 消息序号
   */
  private Long seqNo;
  /**
   * message 消息体
   */
  private String toMsg;
  /**
   * 时间戳
   */
  private Long timestamp;


  /**
   * 删除会话是否同时删除消息
   */
  private Boolean deleteAllMsg;
  /**
   * 单聊消息拥有人
   */
  private String owner;
  /**
   * 废纸篓删除的消息明细
   */
  private String trashMsgInfo;
  /**
   * 新群聊发起方
   */
  private String author;
  /**
   * 新群聊被通知人员
   */
  private List<String> filter;


  /**
   * 群邮件
   */
  private String groupTemail;
  /**
   * 群名称
   */
  private String groupName;
  /**
   * 当事人
   */
  private String temail;
  /**
   * 当事人名称
   */
  private String name;
  /**
   * 管理员名称
   */
  private String adminName;
  /**
   * 成员类型
   */
  private Integer type;
  /**
   * at对象
   */
  private String at;


  /**
   * 话题id
   */
  private String topicId;
  /**
   * 话题序列号
   */
  private Long topicSeqId;
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
   * 群ExtData
   */
  private String extData;
  /**
   * 群成员ExtData
   */
  private String memberExtData;
  /**
   * 会话ExtData
   */
  private String sessionExtData;
  /**
   * 邀请人ExtData
   */
  private String inviteExtData;
  /**
   * crowd群对称密钥
   */
  private String sharedKey;
  /**
   * 发送人名称
   */
  private String fromNickName;
  /**
   * 新群聊群名称
   */
  private String fromGroupName;


  /**
   * CDTP报文头
   */
  private String header;
  private String xPacketId;

  public Integer getSessionMessageType() {
    return sessionMessageType;
  }

  public void setSessionMessageType(Integer sessionMessageType) {
    this.sessionMessageType = sessionMessageType;
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

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public List<String> getFilter() {
    return filter;
  }

  public void setFilter(List<String> filter) {
    this.filter = filter;
  }

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
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

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
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

  public String getExtData() {
    return extData;
  }

  public void setExtData(String extData) {
    this.extData = extData;
  }

  public String getMemberExtData() {
    return memberExtData;
  }

  public void setMemberExtData(String memberExtData) {
    this.memberExtData = memberExtData;
  }

  public String getSessionExtData() {
    return sessionExtData;
  }

  public void setSessionExtData(String sessionExtData) {
    this.sessionExtData = sessionExtData;
  }

  public String getInviteExtData() {
    return inviteExtData;
  }

  public void setInviteExtData(String inviteExtData) {
    this.inviteExtData = inviteExtData;
  }

  public String getSharedKey() {
    return sharedKey;
  }

  public void setSharedKey(String sharedKey) {
    this.sharedKey = sharedKey;
  }

  public String getFromNickName() {
    return fromNickName;
  }

  public void setFromNickName(String fromNickName) {
    this.fromNickName = fromNickName;
  }

  public String getFromGroupName() {
    return fromGroupName;
  }

  public void setFromGroupName(String fromGroupName) {
    this.fromGroupName = fromGroupName;
  }

  @Override
  public String toString() {
    return "MailAgentParams{" +
        "sessionMessageType=" + sessionMessageType +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", msgid='" + msgid + '\'' +
        ", parentMsgId='" + parentMsgId + '\'' +
        ", seqNo=" + seqNo +
        ", toMsg length='" + (toMsg == null ? 0 : toMsg.length()) + '\'' +
        ", timestamp=" + timestamp +
        ", deleteAllMsg=" + deleteAllMsg +
        ", owner='" + owner + '\'' +
        ", trashMsgInfo='" + trashMsgInfo + '\'' +
        ", author='" + author + '\'' +
        ", filter=" + filter +
        ", groupTemail='" + groupTemail + '\'' +
        ", groupName='" + groupName + '\'' +
        ", temail='" + temail + '\'' +
        ", name='" + name + '\'' +
        ", adminName='" + adminName + '\'' +
        ", type=" + type +
        ", at='" + at + '\'' +
        ", topicId='" + topicId + '\'' +
        ", topicSeqId=" + topicSeqId +
        ", title='" + title + '\'' +
        ", receivers=" + receivers +
        ", cc=" + cc +
        ", extData='" + extData + '\'' +
        ", memberExtData='" + memberExtData + '\'' +
        ", sessionExtData='" + sessionExtData + '\'' +
        ", inviteExtData='" + inviteExtData + '\'' +
        ", sharedKey='" + sharedKey + '\'' +
        ", fromNickName='" + fromNickName + '\'' +
        ", fromGroupName='" + fromGroupName + '\'' +
        ", header='" + header + '\'' +
        ", xPacketId='" + xPacketId + '\'' +
        '}';
  }

  public static class TrashMsgInfo {

    private String from;
    private String to;
    private String msgId;

    public TrashMsgInfo(String from, String to, String msgId) {
      this.from = from;
      this.to = to;
      this.msgId = msgId;
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

    public String getMsgId() {
      return msgId;
    }

    public void setMsgId(String msgId) {
      this.msgId = msgId;
    }
  }
}