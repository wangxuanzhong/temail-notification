package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member.MemberRole;
import java.util.Objects;

public class EventUtil {

  private EventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 转换成json，清空后端使用参数
   */
  public static String toJson(IJsonService iJsonService, Event event) {
    event.setExtendParam(null);
    event.setZipPacket(null);
    return iJsonService.toJson(event);
  }

  /**
   * 获取msgId，如果msgId为空则临时生成，反向业务使用对立事件类型生成
   */
  public static String getMsgId(EventType eventType, Event event) {
    EventType againstEventType;
    switch (eventType) {
      // 单聊
      case DO_NOT_DISTURB_CANCEL:
        againstEventType = EventType.DO_NOT_DISTURB;
        break;
      // 群聊
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        againstEventType = EventType.APPLY;
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        againstEventType = EventType.INVITATION;
        break;
      case DELETE_ADMIN:
      case ABANDON_ADMIN:
        againstEventType = EventType.ADD_ADMIN;
        break;
      case GROUP_DO_NOT_DISTURB_CANCEL:
        againstEventType = EventType.GROUP_DO_NOT_DISTURB;
        break;
      default:
        againstEventType = eventType;
        break;
    }

    if (event.getMsgId() == null) {
      return event.getFrom() + "_" + event.getTo() + "_" + event.getTemail() + "_" + againstEventType;
    } else {
      return event.getMsgId();
    }
  }

  /**
   * 去除角色条件，即通知所有人
   */
  public static void notifyToAll(Event event) {
    event.setRole(null);
  }

  /**
   * 角色设置为管理员，只通知管理员
   */
  public static void notifyToAdmin(Event event) {
    event.setRole(MemberRole.ADMIN.getValue());
  }

  /**
   * 根据不同事件类型按照不同的key生成seqId
   */
  public static void initEventSeqId(ISequenceService iSequenceService, Event event) {
    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case RETRACT:
      case DESTROY:
      case DESTROYED:
      case REPLY:
      case REPLY_RETRACT:
      case REPLY_DELETE:
      case REPLY_DESTROYED:
        event.setEventSeqId(iSequenceService.getNextSeq(event.getOwner() == null ? event.getTo() : event.getOwner()));
        break;
      default:
        event.setEventSeqId(iSequenceService.getNextSeq(event.getTo()));
        break;
    }
  }
}
