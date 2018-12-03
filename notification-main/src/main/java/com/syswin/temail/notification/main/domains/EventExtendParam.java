package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 事件扩展参数
 */
@JsonInclude(Include.NON_NULL)
class EventExtendParam {

  // 当事人名称
  private String name;
  // 触发人名称
  private String adminName;
  // 群名称
  private String groupName;
  // @的temail
  private String at;
  // 批量msgId
  private List<String> msgIds;
  // 单聊删除会话是否同时删除消息
  private Boolean deleteAllMsg;

  public EventExtendParam() {
  }

  public EventExtendParam(String name, String adminName, String groupName, String at, List<String> msgIds, Boolean deleteAllMsg) {
    this.name = name;
    this.adminName = adminName;
    this.groupName = groupName;
    this.at = at;
    this.msgIds = msgIds;
    this.deleteAllMsg = deleteAllMsg;
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

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
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
}
