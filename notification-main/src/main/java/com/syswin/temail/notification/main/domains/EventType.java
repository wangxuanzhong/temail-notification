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

  // 回复部分
  REPLY(18, "回复消息"),
  REPLY_RETRACT(19, "回复消息已撤回"),
  REPLY_DELETE(20, "回复消息已删除"),

  // 话题部分
  TOPIC(21, "话题消息"),
  TOPIC_REPLY(22, "话题回复消息"),
  TOPIC_RETRACT(23, "话题消息已撤回"),
  TOPIC_DELETE(24, "话题消息已删除");

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
