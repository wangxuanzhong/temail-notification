package com.syswin.temail.notification.main.domains.params;

import java.util.List;

public class MailAgentTopicParams extends MailAgentParams {

  // 话题id
  private String topicId;
  // 话题序列号
  private Long topicSeqId;
  // 话题主题
  private String title;
  // 收件人
  private List<String> receivers;
  // 抄送
  private List<String> cc;
  // 删除会话是否同时删除消息
  private Boolean deleteAllMsg;

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

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }

  public void setDeleteAllMsg(Boolean deleteAllMsg) {
    this.deleteAllMsg = deleteAllMsg;
  }

  @Override
  public String toString() {
    return "MailAgentTopicParams{" +
        "topicId='" + topicId + '\'' +
        ", topicSeqId='" + topicSeqId + '\'' +
        ", title='" + title + '\'' +
        ", receivers=" + receivers +
        ", cc=" + cc +
        ", deleteAllMsg=" + deleteAllMsg +
        '}' + super.toString();
  }
}