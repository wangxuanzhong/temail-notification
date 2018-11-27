package com.syswin.temail.notification.main.domains.params;

public class MailAgentSingleChatParams extends MailAgentParams {

  // 单聊删除会话是否同时删除消息
  private Boolean deleteAllMsg;
  // 单聊消息拥有人
  private String owner;

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

  @Override
  public String toString() {
    return "MailAgentSingleChatParams{" +
        "deleteAllMsg=" + deleteAllMsg +
        ", owner='" + owner + '\'' +
        '}' + super.toString();
  }
}