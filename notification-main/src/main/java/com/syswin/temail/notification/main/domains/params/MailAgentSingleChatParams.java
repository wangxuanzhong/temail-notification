package com.syswin.temail.notification.main.domains.params;

public class MailAgentSingleChatParams extends MailAgentParams {

  // 单聊删除会话是否同时删除消息
  private Boolean deleteAllMsg;
  // 单聊消息拥有人
  private String owner;
  // 废纸篓删除的消息明细
  private String trashMsgInfo;

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

  @Override
  public String toString() {
    return "MailAgentSingleChatParams{" +
        "deleteAllMsg=" + deleteAllMsg +
        ", owner='" + owner + '\'' +
        ", trashMsgInfo='" + trashMsgInfo + '\'' +
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