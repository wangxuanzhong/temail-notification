package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

/**
 * 事件扩展参数
 */
@JsonInclude(Include.NON_NULL)
class TopicExtendParam {

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

  public TopicExtendParam() {
  }

  public TopicExtendParam(String title, List<String> receivers, List<String> cc, List<String> msgIds, Boolean deleteAllMsg) {
    this.title = title;
    this.receivers = receivers;
    this.cc = cc;
    this.msgIds = msgIds;
    this.deleteAllMsg = deleteAllMsg;
  }

  public String getTitle() {
    return title;
  }

  public List<String> getReceivers() {
    return receivers;
  }

  public List<String> getCc() {
    return cc;
  }

  public List<String> getMsgIds() {
    return msgIds;
  }

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }
}
