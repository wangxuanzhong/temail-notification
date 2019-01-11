package com.syswin.temail.notification.main.domains;

public enum EventType {
  // 消息部分
  RESET(-1, "重置消息未读数"),
  RECEIVE(0, "消息发送"),
  PULLED(1, "消息已拉取"),
  RETRACT(2, "消息已撤回"),
  DESTROYED(3, "消息已焚毁"),
  DELETE(4, "消息已删除"),
  DESTROY(17, "阅后即焚消息发送"),

  // 单聊部分
  ARCHIVE(33, "归档"),
  ARCHIVE_CANCEL(34, "归档取消"),
  TRASH(35, "移送废纸篓"),
  TRASH_CANCEL(36, "废纸篓消息还原"),
  TRASH_DELETE(37, "废纸篓消息删除"),

  // 群管理部分
  GROUP_RESET(-101, "重置群聊消息未读数"),
  GROUP_RECEIVE(100, "群聊消息发送"),
  GROUP_PULLED(101, "群聊消息已拉取"),
  GROUP_RETRACT(102, "群聊消息已撤回"),
  GROUP_DELETE(104, "群聊消息已删除"),
  APPLY(5, "入群申请"),
  APPLY_ADOPT(6, "入群申请已通过"),
  APPLY_REFUSE(7, "入群申请已拒绝"),
  INVITATION(8, "入群邀请"),
  INVITATION_ADOPT(14, "入群邀请已通过"),
  INVITATION_REFUSE(9, "入群邀请已拒绝"),
  ADD_MEMBER(10, "新成员入群"),
  DELETE_MEMBER(11, "群成员被移除"),
  DELETE_GROUP(12, "群已解散"),
  ADD_GROUP(13, "新建群"),
  LEAVE_GROUP(15, "已退出群聊"),
  UPDATE_GROUP_CARD(16, "群名片更新"),
  GROUP_ARCHIVE(27, "群聊归档"),
  GROUP_ARCHIVE_CANCEL(28, "群聊归档取消"),
  GROUP_STICK(31, "群聊置顶"),
  GROUP_STICK_CANCEL(32, "群聊置顶取消"),
  GROUP_SESSION_HIDDEN(38, "群会话隐藏"),
  BLACKLIST(40, "群成员加入黑名单"),
  BLACKLIST_CANCEL(41, "群成员移出黑名单"),
  DO_NOT_DISTURB(42, "设置群免打扰"),
  DO_NOT_DISTURB_CANCEL(43, "设置群免打扰取消"),

  // @部分
  RECEIVE_AT(44, "@消息发送"),
  DELETE_AT(45, "@消息删除"),

  // 回复部分
  REPLY(18, "回复消息"),
  REPLY_RETRACT(19, "回复消息已撤回"),
  REPLY_DELETE(20, "回复消息已删除"),
  REPLY_DESTROYED(26, "回复消息已焚毁"),

  // 话题部分
  TOPIC(21, "话题消息"),
  TOPIC_REPLY(22, "话题回复消息"),
  TOPIC_REPLY_RETRACT(23, "话题回复消息已撤回"),
  TOPIC_REPLY_DELETE(24, "话题回复消息删除"),
  TOPIC_DELETE(25, "话题已删除"),
  TOPIC_ARCHIVE(29, "话题归档"),
  TOPIC_ARCHIVE_CANCEL(30, "话题归档取消"),
  TOPIC_SESSION_DELETE(39, "话题会话删除");


  private final int value;
  private final String description;

  EventType(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public static EventType getByValue(int value) {
    for (EventType eventType : values()) {
      if (eventType.getValue() == value) {
        return eventType;
      }
    }
    return null;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }
}
