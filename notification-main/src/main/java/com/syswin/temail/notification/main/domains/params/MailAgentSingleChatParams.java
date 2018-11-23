package com.syswin.temail.notification.main.domains.params;

public class MailAgentSingleChatParams extends MailAgentParams {

  // 单聊删除会话是否同时删除消息
  private Boolean deleteAllMsg;

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }

  public void setDeleteAllMsg(Boolean deleteAllMsg) {
    this.deleteAllMsg = deleteAllMsg;
  }

  @Override
  public String toString() {
    return "MailAgentSingleChatParams{" +
        "deleteAllMsg=" + deleteAllMsg +
        '}' + super.toString();
  }
}