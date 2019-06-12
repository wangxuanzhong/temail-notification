package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 事件扩展参数
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


  public EventExtendParam() {
  }

  public EventExtendParam(String name, String adminName, String groupName, String at, List<String> msgIds,
      Boolean deleteAllMsg, String owner, String trashMsgInfo, String author, List<String> filter) {
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
}
