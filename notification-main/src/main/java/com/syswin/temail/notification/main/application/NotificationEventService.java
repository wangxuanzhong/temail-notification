package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.domains.params.MailAgentParams.TrashMsgInfo;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import com.syswin.temail.notification.main.util.Constant;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationEventService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventMapper eventMapper;
  private final UnreadMapper unreadMapper;
  private final MemberMapper memberMapper;
  private final IJsonService iJsonService;
  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;

  @Autowired
  public NotificationEventService(EventMapper eventMapper, UnreadMapper unreadMapper, MemberMapper memberMapper, IJsonService iJsonService,
      IMqProducer iMqProducer, NotificationRedisService notificationRedisService) {
    this.eventMapper = eventMapper;
    this.unreadMapper = unreadMapper;
    this.memberMapper = memberMapper;
    this.iJsonService = iJsonService;
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
  }

  /**
   * 拉取事件
   *
   * @param to 发起人
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getEvents(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull events called, to: {}, eventSeqId: {}, pageSize: {}", to, eventSeqId, pageSize);

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventMapper.selectEvents(to, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = 0L;
    if (events.isEmpty()) {
      lastEventSeqId = eventMapper.selectLastEventSeqId(to);
    } else {
      lastEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    Map<String, Map<String, Event>> eventMap = new HashMap<>();
    List<String> messages = new ArrayList<>();  // 存放普通消息，以便抵消操作处理
    List<String> trashMsgIds = new ArrayList<>();  // 存放废纸篓消息，以便还原操作处理
    events.forEach(event -> {
      event.autoReadExtendParam(iJsonService);

      // 按照会话统计事件，方便对单个会话多事件进行处理
      String key = event.getFrom();

      if (event.getGroupTemail() != null && !event.getGroupTemail().equals("")) {
        key = event.getGroupTemail();
      }

      // 单聊逻辑: 当from和to相同时，数据库中存储的owner为会话的另一方，to为通知者，查询结果恢复原结构
      if (event.getFrom().equals(event.getTo()) && event.getOwner() != null) {
        event.setTo(event.getOwner());
        event.setOwner(event.getFrom());
        key = event.getTo();
      }

      if (!eventMap.containsKey(key)) {
        eventMap.put(key, new HashMap<>());
      }

      Map<String, Event> sessionEventMap = eventMap.get(key);
      EventType eventType = EventType.getByValue(event.getEventType());
      switch (Objects.requireNonNull(eventType)) {
        case RECEIVE:
        case RECEIVE_AT:
        case DESTROY:
          messages.add(event.getMsgId());
          break;
        case DESTROYED:
        case REPLY_DESTROYED:
        case REPLY:
          sessionEventMap.put(event.getMsgId(), event);
          break;
        case RETRACT:
        case DELETE_AT:
          if (!messages.contains(event.getMsgId())) {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case REPLY_RETRACT:
          if (sessionEventMap.containsKey(event.getMsgId())) {
            sessionEventMap.remove(event.getMsgId());
          } else {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case ADD_GROUP:
        case APPLY:
        case INVITATION:
        case UPDATE_GROUP_CARD:
        case GROUP_STICK:
        case GROUP_DO_NOT_DISTURB:
        case DO_NOT_DISTURB:
          // 只返回最后一条事件
          sessionEventMap.put(event.getMsgId(eventType), event);
          break;
        case TRASH: // 移动到废纸篓不需要查询返回，只需要记录移动的消息id
          trashMsgIds.addAll(event.getMsgIds());
          break;
        case TRASH_CANCEL:
          List<TrashMsgInfo> newInfos = new ArrayList<>();
          List<TrashMsgInfo> infos = iJsonService
              .fromJson(event.getTrashMsgInfo(), new TypeToken<List<TrashMsgInfo>>() {
              }.getType());
          // 如果查询到的事件中，有移入移出的操作则对于msgId不需要返回给前端
          infos.forEach(info -> {
            if (!trashMsgIds.contains(info.getMsgId())) {
              newInfos.add(info);
            }
          });
          if (!newInfos.isEmpty()) {
            event.setTrashMsgInfo(iJsonService.toJson(newInfos));
            sessionEventMap.put(UUID.randomUUID().toString(), event);
          }
          break;
        case DELETE_GROUP:
          // 清除所有人的事件，并添加此事件
          sessionEventMap.clear();
          sessionEventMap.put(UUID.randomUUID().toString(), event);
          break;
        case DELETE_MEMBER:
        case LEAVE_GROUP:
          // 只清除当事人的事件，并添加此事件
          if (to.equals(event.getTemail())) {
            sessionEventMap.clear();
          }
          sessionEventMap.put(UUID.randomUUID().toString(), event);
          break;
        case APPLY_ADOPT:
        case APPLY_REFUSE:
        case INVITATION_ADOPT:
        case INVITATION_REFUSE:
        case GROUP_STICK_CANCEL:
        case GROUP_DO_NOT_DISTURB_CANCEL:
        case DO_NOT_DISTURB_CANCEL:
          if (sessionEventMap.containsKey(event.getMsgId(eventType))) {
            sessionEventMap.remove(event.getMsgId(eventType));
          } else {
            sessionEventMap.put(event.getMsgId(eventType), event);
          }
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            List<String> msgIds = new ArrayList<>(event.getMsgIds());
            event.getMsgIds().forEach(msgId -> {
              if (messages.contains(msgId)) {
                msgIds.remove(msgId); // 删除已出现的msgId
              }
            });
            // 将此次拉取中未出现的msgId添加到删除事件中，供前端处理
            if (!msgIds.isEmpty()) {
              event.setMsgIds(msgIds);
              sessionEventMap.put(UUID.randomUUID().toString(), event);
            }
          } else {  // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              sessionEventMap.clear();
            }
            sessionEventMap.put(UUID.randomUUID().toString(), event);
          }
          break;
        case REPLY_DELETE:
          if (event.getMsgIds() != null) {
            // 删除已出现的msgId
            event.getMsgIds().forEach(sessionEventMap::remove);
          }
          break;
        case ADD_ADMIN:
          // 只有当事人添加此事件
          if (to.equals(event.getTemail())) {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case DELETE_ADMIN:
        case ABANDON_ADMIN:
          if (sessionEventMap.containsKey(event.getMsgId(eventType))) {
            sessionEventMap.remove(event.getMsgId(eventType));
          } else {
            // 只有当事人添加此事件
            if (to.equals(event.getTemail())) {
              sessionEventMap.put(event.getMsgId(eventType), event);
            }
          }
          break;
        case PACKET:
          sessionEventMap.put(UUID.randomUUID().toString(), event);
          break;
      }
    });

    List<Event> notifyEvents = new ArrayList<>();
    eventMap.values().forEach(sessionEventMap -> notifyEvents.addAll(sessionEventMap.values()));

    notifyEvents.sort(Comparator.comparing(Event::getEventSeqId));
    Map<String, Object> result = new HashMap<>();
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);

    // 返回结果最多1000条
    if (notifyEvents.size() > 1000) {
      result.put("events", notifyEvents.subList(notifyEvents.size() - 1000, notifyEvents.size()));
    } else {
      result.put("events", notifyEvents);
    }
    // LOGGER.info("pull events result: {}", result);
    return result;
  }

  /**
   * 获取消息未读数
   *
   * @param to 发起人
   */
  public List<UnreadResponse> getUnread(String to) {
    LOGGER.info("get unread, to: {}", to);

    // 获取已经删除的事件的未读数
    Map<String, Integer> unreadMap = new HashMap<>();
    unreadMapper.selectCount(to).forEach(unread -> unreadMap.put(unread.getFrom(), unread.getCount()));

    // 查询所有事件
    List<Event> events = eventMapper.selectPartEvents(to, Constant.UNREAD_EVENT_TYPES);

    // 统计未读数
    Map<String, List<String>> eventMap = this.calculateUnread(events, unreadMap);

    // 统计各个会话的未读数量
    List<UnreadResponse> unreadResponses = new ArrayList<>();
    eventMap.forEach((key, msgIds) -> {
      if (!msgIds.isEmpty()) {
        // 计算未读数表中的数据
        int unread = 0;
        if (unreadMap.containsKey(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0])) {
          unread = unreadMap.get(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0]);
        }

        UnreadResponse unreadResponse = new UnreadResponse(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to,
            msgIds.size() + unread);
        if (key.endsWith(Event.GROUP_CHAT_KEY_POSTFIX)) {
          unreadResponse.setGroupTemail(unreadResponse.getFrom());
        }
        unreadResponses.add(unreadResponse);
      }
    });

    // LOGGER.info("get unread result: {}", unreadResponses);
    return unreadResponses;
  }

  /**
   * 统计消息未读数
   */
  public Map<String, List<String>> calculateUnread(List<Event> events, Map<String, Integer> unreadMap) {
    Map<String, List<String>> eventMap = new HashMap<>();
    events.forEach(event -> {
      event.autoReadExtendParam(iJsonService);
      // 为了区分单聊和群聊，给群聊添加后缀
      String key = event.getFrom();
      if (event.getGroupTemail() != null && !event.getGroupTemail().equals("")) {
        key += Event.GROUP_CHAT_KEY_POSTFIX;
      }

      if (!eventMap.containsKey(key)) {
        eventMap.put(key, new ArrayList<>());
      }
      List<String> msgIds = eventMap.get(key);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RESET: // 清空未读数
          msgIds.clear();
          unreadMap.remove(event.getFrom());
          break;
        case RECEIVE: // 消息发送者不计未读数
        case DESTROY: // 焚毁消息发送者不计未读数
          if (!event.getFrom().equals(event.getTo()) && !event.getTo().equals(event.getTemail())) {
            msgIds.add(event.getMsgId());
          }
          break;
        case PULLED:
        case RETRACT:
          msgIds.remove(event.getMsgId());
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            event.getMsgIds().forEach(msgIds::remove);
          } else { // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              msgIds.clear();
              unreadMap.remove(event.getFrom());
            }
          }
          break;
      }
    });
    return eventMap;
  }

  /**
   * 重置消息未读数
   */
  public void reset(Event event, String header) {
    LOGGER.info("reset to: {}, param: {}", event.getTo(), event);
    event.setEventType(EventType.RESET.getValue());
    Integer cdtpEventType = event.getEventType();
    // groupTemail不为空则为群聊
    if (event.getGroupTemail() != null && !event.getGroupTemail().isEmpty()) {
      event.setFrom(event.getGroupTemail());
      cdtpEventType = EventType.GROUP_RESET.getValue();
    }
    event.setTimestamp(System.currentTimeMillis());
    event.initEventSeqId(notificationRedisService);
    eventMapper.insert(event);

    // 删除历史重置事件
    List<Long> ids = eventMapper.selectResetEvents(event);
    if (!ids.isEmpty()) {
      eventMapper.delete(ids);
    }

    // 发送到MQ以便多端同步
    LOGGER.info("send reset event to {}", event.getTo());
    iMqProducer.sendMessage(
        iJsonService.toJson(new CDTPResponse(event.getTo(), cdtpEventType, header, Event.toJson(iJsonService, event))));
  }

  /**
   * 修改群成员个人状态
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateGroupChatUserStatus(Member member, UserStatus userStatus, String header) {
    LOGGER.info("update user status, param: {}", member);
    Event event = new Event(null, null, null, null, null,
        member.getGroupTemail(), member.getTemail(), System.currentTimeMillis(), member.getGroupTemail(),
        member.getTemail(),
        null, null, null, null, null, null);

    switch (userStatus) {
      case NORMAL:
        event.setEventType(EventType.GROUP_DO_NOT_DISTURB_CANCEL.getValue());
        memberMapper.updateUserStatus(member);
        break;
      case DO_NOT_DISTURB:
        event.setEventType(EventType.GROUP_DO_NOT_DISTURB.getValue());
        memberMapper.updateUserStatus(member);
        break;
      default:
        return;
    }

    // 发送到MQ以便多端同步
    LOGGER.info("send reset event to {}", event.getTo());
    iMqProducer.sendMessage(
        iJsonService.toJson(new CDTPResponse(event.getTo(), event.getEventType(), header, Event.toJson(iJsonService, event))));
  }

  /**
   * 查询群成员个人状态
   */
  public Map<String, Integer> getGroupChatUserStatus(String temail, String groupTemail) {
    LOGGER.info("get do not disturb group, temail: {}", temail);
    Map<String, Integer> result = new HashMap<>();
    result.put("userStatus", memberMapper.selectUserStatus(temail, groupTemail));
    return result;
  }
}
