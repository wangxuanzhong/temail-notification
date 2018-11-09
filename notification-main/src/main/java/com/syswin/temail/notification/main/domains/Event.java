package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.syswin.temail.notification.foundation.application.JsonService;

@JsonInclude(Include.NON_NULL)
public class Event extends Message {

  public static final String GROUP_CHAT_KEY_POSTFIX = "::event_group_chat";

  @JsonIgnore
  private Long id;
  private Long eventSeqId;
  private Integer eventType;

  // 当事人名称
  private String name;
  // 触发人名称
  private String adminName;
  // 群名称
  private String groupName;

  // 扩展参数
  @JsonIgnore
  private String extendParam;

  @JsonIgnore
  private String xPacketId;

  public Event() {
  }

  public Event(String msgId, Long seqId, String message, String from, String to, Long timestamp, Integer eventType, String xPacketId) {
    super(msgId, seqId, message, from, to, timestamp);
    this.eventType = eventType;
    this.xPacketId = xPacketId;
  }


  public Event(String msgId, Long seqId, String message, String from, String to, Long timestamp, String groupTemail, String temail,
      Integer role, Integer eventType, String name, String adminName, String groupName, String xPacketId) {
    super(msgId, seqId, message, from, to, timestamp, groupTemail, temail, role);
    this.eventType = eventType;
    this.name = name;
    this.adminName = adminName;
    this.groupName = groupName;
    this.xPacketId = xPacketId;
  }

  /**
   * 去除角色条件，即通知所有人
   */
  public void notifyToAll() {
    this.setRole(null);
  }

  /**
   * 角色设置为管理员，只通知管理员
   */
  public void notifyToAdmin() {
    this.setRole(MemberRole.ADMIN.getValue());
  }


  /**
   * 设置通知类消息的msgId
   *
   * @param eventType 需要匹配的通知类型
   */
  public void addEventMsgId(EventType eventType) {
    this.setMsgId(this.getGroupTemail() + "_" + this.getTemail() + "_" + eventType.getValue());
  }

  /**
   * 清除通知类消息的msgId
   */
  public void removeEventMsgId() {
    this.setMsgId(null);
  }

  /**
   * 自动解析扩展字段
   */
  public void autoReadExtendParam(JsonService jsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      ExtendParam extendParam = jsonService.fromJson(this.extendParam, ExtendParam.class);
      this.name = extendParam.getName();
      this.adminName = extendParam.getAdminName();
      this.groupName = extendParam.getGroupName();
    }
  }

  /**
   * 自动配置扩展字段
   */
  public void autoWriteExtendParam(JsonService jsonService) {
    this.extendParam = jsonService.toJson(new ExtendParam(this.name, this.adminName, this.getGroupName()));
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

  public String getExtendParam() {
    return extendParam;
  }

  public void setExtendParam(String extendParam) {
    this.extendParam = extendParam;
  }

  public String getxPacketId() {
    return xPacketId;
  }

  public void setxPacketId(String xPacketId) {
    this.xPacketId = xPacketId;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", name='" + name + '\'' +
        ", adminName='" + adminName + '\'' +
        ", groupName='" + groupName + '\'' +
        ", xPacketId='" + xPacketId + '\'' +
        '}' + " " +
        super.toString();
  }

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
    UPDATE_GROUP_CARD(16, "群名片更新");

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

  /**
   * 事件扩展参数
   */
  class ExtendParam {

    // 当事人名称
    private String name;
    // 触发人名称
    private String adminName;
    // 群名称
    private String groupName;

    public ExtendParam(String name, String adminName, String groupName) {
      this.name = name;
      this.adminName = adminName;
      this.groupName = groupName;
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
  }
}