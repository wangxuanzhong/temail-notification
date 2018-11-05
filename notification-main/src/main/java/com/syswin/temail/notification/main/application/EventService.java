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
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getEvents(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("从序列号[" + eventSeqId + "]之后开始拉取接收方[" + to + "]的事件。拉取数量为：" + pageSize);

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventRepository.selectByTo(to, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    Map<String, Event> eventMap = new HashMap<>();
    List<Event> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RECEIVE:
        case DESTROY:
        case DESTROYED:
        case APPLY:
          eventMap.put(event.getMsgId(), event);
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
          if (eventMap.containsKey(event.getMsgId())) {
            eventMap.remove(event.getMsgId());
          } else {
            eventMap.put(event.getMsgId(), event);
          }
          break;
      }
    });

    notifyEvents.addAll(eventMap.values());
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
   * 拉取回复事件
   *
   * @param to 发起人
   * @param parentMsgId 父消息id
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getReplyEvents(String to, String parentMsgId, Long eventSeqId, Integer pageSize) {
    LOGGER.info("从序列号[" + eventSeqId + "]之后开始拉取[ " + to + "]消息[" + parentMsgId + "]的回复事件。拉取数量为：" + pageSize);

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventRepository.selectReplyEvents(to, parentMsgId, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    Map<String, Event> eventMap = new HashMap<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case REPLY:
          eventMap.put(event.getMsgId(), event);
          break;
        case PULLED:
        case RETRACT:
          if (eventMap.containsKey(event.getMsgId())) {
            eventMap.remove(event.getMsgId());
          } else {
            eventMap.put(event.getMsgId(), event);
          }
          break;
      }
    });

    List<Event> notifyEvents = new ArrayList<>(eventMap.values());
    notifyEvents.sort(Comparator.comparing(Event::getEventSeqId));

    Map<String, Object> result = new HashMap<>();
    if (events.isEmpty()) {
      result.put("lastEventSeqId", -1);
    } else {
      result.put("lastEventSeqId", events.get(events.size() - 1).getEventSeqId());
    }
    result.put("events", notifyEvents);
    LOGGER.info("拉取回复事件结果为：" + result);
    return result;
  }


  /**
   * 获取消息未读数
   *
   * @param to 发起人
   */
  public List<UnreadResponse> getUnread(String to) {
    LOGGER.info("获取接收方[" + to + "]的未读消息数量。");
    List<Event> events = eventRepository.selectByTo(to, null, null);

    Map<String, List<String>> unreadMap = new HashMap<>();
    events.forEach(event -> {
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
   * 获取回复消息未读数
   *
   * @param to 发起人
   * @param parentMsgId 父消息id
   */
  public UnreadResponse getReplyUnread(String to, String parentMsgId) {
    LOGGER.info("获取[{}]消息[{}]的回复消息未读数量。", to, parentMsgId);
    List<Event> events = eventRepository.selectReplyEvents(to, parentMsgId, null, null);

    List<String> msgIds = new ArrayList<>();
    events.forEach(event -> {
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RESET:
          msgIds.clear();
          break;
        case REPLY:
          msgIds.add(event.getMsgId());
          break;
        case PULLED:
        case RETRACT:
          if (msgIds.contains(event.getMsgId())) {
            msgIds.remove(event.getMsgId());
          } else {
            msgIds.add(event.getMsgId());
          }
          break;
      }
    });

    // 统计未读数量
    UnreadResponse unreadResponse = new UnreadResponse(msgIds.size());

    LOGGER.info("获取未读消息结果为：" + unreadResponse);
    return unreadResponse;
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
    eventRepository.deleteReplyEvents(event);
  }
}
