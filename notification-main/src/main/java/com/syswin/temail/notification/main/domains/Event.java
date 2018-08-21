package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Event extends Message {

  private Long id;
  private Long eventSeqId;
  private Integer eventType;

  // 群聊参数
  private String groupTemail;
  private String temail;
  @JsonIgnore
  private Integer role;

  public Event() {
  }

  public Event(String msgId, Long seqId, String message, String from, String to, Long timestamp, Integer eventType) {
    super(msgId, seqId, message, from, to, timestamp);
    this.eventType = eventType;
  }

  public Event(String msgId, Long seqId, String message, String from, String to, Long timestamp, Integer eventType, String groupTemail, String temail,
      Integer role) {
    super(msgId, seqId, message, from, to, timestamp);
    this.eventType = eventType;
    this.groupTemail = groupTemail;
    this.temail = temail;
    this.role = role;
  }

  /**
   * 去除角色条件，即通知所有人
   */
  public void notifyToAll() {
    this.role = null;
  }

  /**
   * 角色设置为管理员，只通知管理员
   */
  public void notifyToAdmin() {
    this.role = MemberRole.ADMIN.getValue();
  }


  /**
   * 设置通知类消息的msgId
   *
   * @param eventType 需要匹配的通知类型
   */
  public void addEventMsgId(EventType eventType) {
    this.setMsgId(this.groupTemail + "_" + this.temail + "_" + eventType.getValue());
  }

  /**
   * 清除通知类消息的msgId
   */
  public void removeEventMsgId() {
    this.setMsgId(null);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEventSeqId() {
    return eventSeqId;
  }

  public void setEventSeqId(Long eventSeqId) {
    this.eventSeqId = eventSeqId;
  }

  public Integer getEventType() {
    return eventType;
  }

  public void setEventType(Integer eventType) {
    this.eventType = eventType;
  }

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  public String getTemail() {
    return temail;
  }

  public void setTemail(String temail) {
    this.temail = temail;
  }

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }

  public enum EventType {
    // 消息部分
    RECEIVE(0, "消息发送"),
    PULLED(1, "消息已拉取"),
    RETRACT(2, "消息已撤回"),
    DESTROY(3, "消息已焚毁"),
    DELETE(4, "消息已删除"),

    // 群管理部分
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
    LEAVE_GROUP(15, "已退出群聊");

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

  public enum MemberRole {
    NORMAL(0, "普通成员"),
    ADMIN(1, "管理员");

    private final int value;
    private final String description;

    MemberRole(int value, String description) {
      this.value = value;
      this.description = description;
    }

    public int getValue() {
      return value;
    }
  }
}