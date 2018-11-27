package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Event {

  public static final String GROUP_CHAT_KEY_POSTFIX = "::event_group_chat";

  // 事件参数
  @JsonIgnore
  private Long id;
  @JsonIgnore
  private String xPacketId;
  private Long eventSeqId;
  private Integer eventType;

  // 单聊参数
  private String msgId;
  private String parentMsgId;
  private Long seqId;
  private String message;
  private String from;
  private String to;
  private String owner;
  private Long timestamp;

  // 群聊参数
  private String groupTemail;
  private String temail;
  @JsonIgnore
  private Integer role;

  // 以下参数均存入扩展参数字段
  // 当事人名称
  private String name;
  // 管理员名称
  private String adminName;
  // 群名称
  private String groupName;
  // @的temail
  private String at;
  // 批量msgId
  private List<String> msgIds;
  // 单聊删除会话是否同时删除消息
  private Boolean deleteAllMsg;

  @JsonIgnore
  private String extendParam;

  public Event() {
  }

  // 单聊
  public Event(Integer eventType, String msgId, String parentMsgId, Long seqId, String message, String from, String to, String owner,
      Long timestamp,
      String xPacketId, Boolean deleteAllMsg) {
    this.eventType = eventType;
    this.msgId = msgId;
    this.parentMsgId = parentMsgId;
    this.seqId = seqId;
    this.message = message;
    this.from = from;
    this.to = to;
    this.owner = owner;
    this.timestamp = timestamp;
    this.xPacketId = xPacketId;
    this.deleteAllMsg = deleteAllMsg;
  }

  // 群聊
  public Event(Integer eventType, String msgId, String parentMsgId, Long seqId, String message, String from, String to, Long timestamp,
      String groupTemail, String temail, Integer role, String name, String adminName, String groupName, String at, String xPacketId) {
    this.eventType = eventType;
    this.msgId = msgId;
    this.parentMsgId = parentMsgId;
    this.seqId = seqId;
    this.message = message;
    this.from = from;
    this.to = to;
    this.timestamp = timestamp;
    this.groupTemail = groupTemail;
    this.temail = temail;
    this.role = role;
    this.name = name;
    this.adminName = adminName;
    this.groupName = groupName;
    this.at = at;
    this.xPacketId = xPacketId;
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
    this.msgId = this.groupTemail + "_" + this.temail + "_" + eventType.getValue();
  }

  /**
   * 清除通知类消息的msgId
   */
  public void removeEventMsgId() {
    this.msgId = null;
  }

  /**
   * 自动解析扩展字段
   */
  public Event autoReadExtendParam(JsonService jsonService) {
    if (this.extendParam != null && !this.extendParam.isEmpty()) {
      EventExtendParam extendParam = jsonService.fromJson(this.extendParam, EventExtendParam.class);
      this.name = extendParam.getName();
      this.adminName = extendParam.getAdminName();
      this.groupName = extendParam.getGroupName();
      this.at = extendParam.getAt();
      this.msgIds = extendParam.getMsgIds();
      this.deleteAllMsg = extendParam.getDeleteAllMsg();
    }
    return this;
  }

  /**
   * 自动配置扩展字段
   */
  public Event autoWriteExtendParam(JsonService jsonService) {
    this.extendParam = jsonService.toJson(new EventExtendParam(this.name, this.adminName, this.groupName, this.at, this.msgIds, this.deleteAllMsg));
    return this;
  }

  /**
   * 根据不同事件类型按照不同的key生成seqId
   */
  public void initEventSeqId(SequenceService sequenceService) {
    switch (Objects.requireNonNull(EventType.getByValue(this.eventType))) {
      case REPLY:
      case REPLY_RETRACT:
      case REPLY_DELETE:
        this.eventSeqId = sequenceService.getNextSeq(this.parentMsgId);
        break;
      default:
        this.eventSeqId = sequenceService.getNextSeq(this.to);
        break;
    }
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

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getParentMsgId() {
    return parentMsgId;
  }

  public void setParentMsgId(String parentMsgId) {
    this.parentMsgId = parentMsgId;
  }

  public Long getSeqId() {
    return seqId;
  }

  public void setSeqId(Long seqId) {
    this.seqId = seqId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
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

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
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

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
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

  public List<String> getMsgIds() {
    return msgIds;
  }

  public void setMsgIds(List<String> msgIds) {
    this.msgIds = msgIds;
  }

  public Boolean getDeleteAllMsg() {
    return deleteAllMsg;
  }

  public void setDeleteAllMsg(Boolean deleteAllMsg) {
    this.deleteAllMsg = deleteAllMsg;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", xPacketId='" + xPacketId + '\'' +
        ", eventSeqId=" + eventSeqId +
        ", eventType=" + eventType +
        ", msgId='" + msgId + '\'' +
        ", parentMsgId='" + parentMsgId + '\'' +
        ", seqId=" + seqId +
        ", message length='" + (message == null ? 0 : message.length()) + '\'' +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", owner='" + owner + '\'' +
        ", timestamp=" + timestamp +
        ", groupTemail='" + groupTemail + '\'' +
        ", temail='" + temail + '\'' +
        ", role=" + role +
        ", name='" + name + '\'' +
        ", adminName='" + adminName + '\'' +
        ", groupName='" + groupName + '\'' +
        ", at='" + at + '\'' +
        ", msgIds=" + msgIds +
        ", deleteAllMsg=" + deleteAllMsg +
        ", extendParam='" + extendParam + '\'' +
        '}';
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
    UPDATE_GROUP_CARD(16, "群名片更新"),

    // 回复部分
    REPLY(18, "回复消息"),
    REPLY_RETRACT(19, "回复消息已撤回"),
    REPLY_DELETE(20, "回复消息已删除");

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