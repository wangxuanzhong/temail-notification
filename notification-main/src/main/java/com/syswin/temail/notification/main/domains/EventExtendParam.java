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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 事件扩展参数
 *
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
class EventExtendParam {

  /**
   * 当事人名称
   */
  private String name;
  /**
   * 触发人名称
   */
  private String adminName;
  /**
   * 群名称
   */
  private String groupName;
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
   * 消息发送者
   */
  private String author;
  /**
   * 被通知人员
   */
  private List<String> filter;
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
   * crowd群对称密钥
   */
  private String sharedKey;


  public EventExtendParam() {
  }

  public EventExtendParam(String name, String adminName, String groupName, String at, List<String> msgIds,
      Boolean deleteAllMsg, String owner, String trashMsgInfo, String author, List<String> filter,
      String extData, String memberExtData, String sessionExtData, String sharedKey) {
    this.name = name;
    this.adminName = adminName;
    this.groupName = groupName;
    this.at = at;
    this.msgIds = msgIds;
    this.deleteAllMsg = deleteAllMsg;
    this.owner = owner;
    this.trashMsgInfo = trashMsgInfo;
    this.author = author;
    this.filter = filter;
    this.extData = extData;
    this.memberExtData = memberExtData;
    this.sessionExtData = sessionExtData;
    this.sharedKey = sharedKey;
  }

  public String getName() {
    return name;
  }

  public String getAdminName() {
    return adminName;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getAt() {
    return at;
  }

  public List<String> getMsgIds() {
    return msgIds;
  }

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }

  public String getOwner() {
    return owner;
  }

  public String getTrashMsgInfo() {
    return trashMsgInfo;
  }

  public String getAuthor() {
    return author;
  }

  public List<String> getFilter() {
    return filter;
  }

  public String getMemberExtData() {
    return memberExtData;
  }

  public String getExtData() {
    return extData;
  }

  public String getSessionExtData() {
    return sessionExtData;
  }

  public String getSharedKey() {
    return sharedKey;
  }
}
