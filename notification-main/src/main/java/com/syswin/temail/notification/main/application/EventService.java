package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SequenceService sequenceService;
  private final EventRepository eventRepository;
  private final JsonService jsonService;

  @Autowired
  public EventService(SequenceService sequenceService, EventRepository eventRepository, JsonService jsonService) {
    this.sequenceService = sequenceService;
    this.eventRepository = eventRepository;
    this.jsonService = jsonService;
  }

  /**
   * 拉取事件
   *
   * @param to 发起人
   * @param parentMsgId 父消息id
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getEvents(String to, String parentMsgId, Long eventSeqId, Integer pageSize) {
    if (parentMsgId == null || parentMsgId.isEmpty()) {
      LOGGER.info("从序列号[" + eventSeqId + "]之后开始拉取接收方[" + to + "]的事件。拉取数量为：" + pageSize);
    } else {
      LOGGER.info("从序列号[" + eventSeqId + "]之后开始拉取[ " + to + "]消息[" + parentMsgId + "]的回复事件。拉取数量为：" + pageSize);
    }

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventRepository.selectEventsByTo(to, parentMsgId, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    Map<String, Map<String, Event>> eventMap = new HashMap<>();
    List<Event> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      // 按照会话统计事件，方便对单个会话多事件进行处理
      String key = event.getFrom();
      if (!eventMap.containsKey(key)) {
        eventMap.put(key, new HashMap<>());
      }
      Map<String, Event> sessionEventMap = eventMap.get(key);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RECEIVE:
        case DESTROY:
        case DESTROYED:
        case APPLY:
        case REPLY:
          sessionEventMap.put(event.getMsgId(), event);
          break;
        case DELETE_GROUP:
        case ADD_MEMBER:
        case DELETE_MEMBER:
        case LEAVE_GROUP:
        case INVITATION:
        case INVITATION_ADOPT:
        case INVITATION_REFUSE:
        case UPDATE_GROUP_CARD:
          notifyEvents.add(event);
          break;
        case PULLED:
        case RETRACT:
        case APPLY_ADOPT:
        case APPLY_REFUSE:
          if (sessionEventMap.containsKey(event.getMsgId())) {
            sessionEventMap.remove(event.getMsgId());
          } else {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            List<String> msgIds = new ArrayList<>(event.getMsgIds());
            event.getMsgIds().forEach(msgId -> {
              if (sessionEventMap.containsKey(msgId)) {
                sessionEventMap.remove(msgId);
                msgIds.remove(msgId); // 删除已出现的msgId
              }
            });
            // 将此次拉取中未出现的msgId添加到删除事件中，供前端处理
            if (!msgIds.isEmpty()) {
              event.setMsgIds(msgIds);
              notifyEvents.add(event);
            }
          } else {  // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              sessionEventMap.clear();
            }
            notifyEvents.add(event);
          }
          break;
      }
    });
    eventMap.values().forEach(sessionEventMap -> notifyEvents.addAll(sessionEventMap.values()));
    notifyEvents.sort(Comparator.comparing(Event::getEventSeqId));

    Map<String, Object> result = new HashMap<>();
    if (events.isEmpty()) {
      result.put("lastEventSeqId", -1);
    } else {
      result.put("lastEventSeqId", events.get(events.size() - 1).getEventSeqId());
    }
    result.put("events", notifyEvents);
    LOGGER.info("拉取事件结果为：" + result);
    return result;
  }

  /**
   * 获取消息未读数
   *
   * @param to 发起人
   * @param parentMsgId 父消息id
   */
  public List<UnreadResponse> getUnread(String to, String parentMsgId) {
    if (parentMsgId == null || parentMsgId.isEmpty()) {
      LOGGER.info("获取接收方[" + to + "]的未读消息数量。");
    } else {
      LOGGER.info("获取[{}]消息[{}]的回复消息未读数量。", to, parentMsgId);
    }
    List<Event> events = eventRepository.selectEventsByTo(to, parentMsgId, null, null);

    Map<String, List<String>> unreadMap = new HashMap<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      // 为了区分单聊和群聊，给群聊添加后缀
      String key = event.getFrom();
      if (event.getGroupTemail() != null && !event.getGroupTemail().equals("")) {
        key += Event.GROUP_CHAT_KEY_POSTFIX;
      }

      if (!unreadMap.containsKey(key)) {
        unreadMap.put(key, new ArrayList<>());
      }
      List<String> msgIds = unreadMap.get(key);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RESET:
          msgIds.clear();
          break;
        case RECEIVE:
        case DESTROY:
        case DESTROYED:
        case ADD_GROUP:
        case APPLY:
        case REPLY:
          msgIds.add(event.getMsgId());
          break;
        case DELETE_GROUP:
        case ADD_MEMBER:
        case DELETE_MEMBER:
        case LEAVE_GROUP:
        case INVITATION:
        case INVITATION_ADOPT:
        case INVITATION_REFUSE:
        case UPDATE_GROUP_CARD:
          msgIds.add("notify event msgId");
          break;
        case PULLED:
        case RETRACT:
        case APPLY_ADOPT:
        case APPLY_REFUSE:
          if (msgIds.contains(event.getMsgId())) {
            msgIds.remove(event.getMsgId());
          } else {
            msgIds.add(event.getMsgId());
          }
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            List<String> deleteMsgIds = new ArrayList<>(event.getMsgIds());
            event.getMsgIds().forEach(msgId -> {
              if (msgIds.remove(msgId)) {
                deleteMsgIds.remove(msgId);
              }
            });

            // 如果有未出现的msgId，则删除通知仍需处理，算入未读数中
            if (!deleteMsgIds.isEmpty()) {
              msgIds.add("notify event msgId");
            }
          } else { // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              msgIds.clear();
            }
            msgIds.add("notify event msgId");
          }
          break;
      }
    });

    // 统计各个会话的未读数量
    List<UnreadResponse> unreadResponses = new ArrayList<>();
    unreadMap.forEach((key, msgIds) -> {
      if (!msgIds.isEmpty()) {
        UnreadResponse unreadResponse = new UnreadResponse(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to, msgIds.size());
        if (key.endsWith(Event.GROUP_CHAT_KEY_POSTFIX)) {
          unreadResponse.setGroupTemail(unreadResponse.getFrom());
        }
        unreadResponses.add(unreadResponse);
      }
    });

    LOGGER.info("获取未读消息结果为：" + unreadResponses);
    return unreadResponses;
  }

  /**
   * 重置消息未读数
   */
  @Transactional(rollbackFor = Exception.class)
  public void reset(Event event) {
    LOGGER.info("重置[{}]的消息未读数：参数{}", event.getTo(), event);
    if (event.getGroupTemail() != null && !event.getGroupTemail().isEmpty()) {
      event.setFrom(event.getGroupTemail());
    }
    event.setEventType(EventType.RESET.getValue());
    event.setTimestamp(System.currentTimeMillis());
    event.initEventSeqId(sequenceService);
    eventRepository.insert(event);

    // 删除历史重置事件
    eventRepository.deleteResetEvents(event);
  }
}
