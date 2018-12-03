package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 事件扩展参数
 */
@JsonInclude(Include.NON_NULL)
class TopicExtendParam {

  // 话题主题
  private String title;
  // 收件人
  private List<String> receivers;
  // 抄送
  private List<String> CC;
  // 批量msgId
  private List<String> msgIds;

  public TopicExtendParam() {
  }

  public TopicExtendParam(String title, List<String> receivers, List<String> CC, List<String> msgIds) {
    this.title = title;
    this.receivers = receivers;
    this.CC = CC;
    this.msgIds = msgIds;
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

  public List<String> getCC() {
    return CC;
  }

  public void setCC(List<String> CC) {
    this.CC = CC;
  }

  public List<String> getMsgIds() {
    return msgIds;
  }

  public void setMsgIds(List<String> msgIds) {
    this.msgIds = msgIds;
  }
}
