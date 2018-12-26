package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.UnreadRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams.TrashMsgInfo;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SequenceService sequenceService;
  private final EventRepository eventRepository;
  private final UnreadRepository unreadRepository;
  private final JsonService jsonService;
  private final RocketMqProducer rocketMqProducer;

  @Autowired
  public EventService(SequenceService sequenceService, EventRepository eventRepository, UnreadRepository unreadRepository, JsonService jsonService,
      RocketMqProducer rocketMqProducer) {
    this.sequenceService = sequenceService;
    this.eventRepository = eventRepository;
    this.unreadRepository = unreadRepository;
    this.jsonService = jsonService;
    this.rocketMqProducer = rocketMqProducer;
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
    List<Event> events = eventRepository.selectEvents(to, null, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    // 获取当前最新eventSeqId
    Long maxEventSeqId = 0L;
    if (events.isEmpty()) {
      maxEventSeqId = eventRepository.selectLastEventSeqId(to, null);
    } else {
      maxEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    Map<String, Map<String, Event>> eventMap = new HashMap<>();
    List<String> messages = new ArrayList<>();  // 存放普通消息，以便抵消操作处理
    List<String> trashMsgIds = new ArrayList<>();  // 存放废纸篓消息，以便还原操作处理
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
          messages.add(event.getMsgId());
          break;
        case DESTROY:
        case DESTROYED:
          // 单聊逻辑: 当from和to相同时，数据库中存储的owner为消息接收者，to为通知者，查询结果恢复原结构
          if (event.getFrom().equals(event.getTo()) && event.getOwner() != null) {
            event.setTo(event.getOwner());
            event.setOwner(event.getFrom());
          }
          sessionEventMap.put(event.getMsgId(), event);
          break;
        case RETRACT:
          // 单聊逻辑: 当from和to相同时，数据库中存储的owner为消息接收者，to为通知者，查询结果恢复原结构
          if (event.getFrom().equals(event.getTo()) && event.getOwner() != null) {
            event.setTo(event.getOwner());
            event.setOwner(event.getFrom());
          }
          if (!messages.contains(event.getMsgId())) {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case ADD_GROUP:
        case APPLY:
        case INVITATION:
        case UPDATE_GROUP_CARD:
        case GROUP_STICK:
          sessionEventMap.put(event.getMsgId(), event);
          break;
        case TRASH: // 移动到废纸篓不需要查询返回，只需要记录移动的消息id
          trashMsgIds.addAll(event.getMsgIds());
          break;
        case TRASH_CANCEL:
          List<TrashMsgInfo> newInfos = new ArrayList<>();
          List<TrashMsgInfo> infos = jsonService.fromJson(event.getTrashMsgInfo(), new TypeToken<List<TrashMsgInfo>>() {
          }.getType());
          // 如果查询到的事件中，有移入移出的操作则对于msgId不需要返回给前端
          infos.forEach(info -> {
            if (!trashMsgIds.contains(info.getMsgId())) {
              newInfos.add(info);
            }
          });
          if (!newInfos.isEmpty()) {
            event.setTrashMsgInfo(jsonService.toJson(newInfos));
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
      }
    });

    List<Event> notifyEvents = new ArrayList<>();
    eventMap.values().forEach(sessionEventMap -> notifyEvents.addAll(sessionEventMap.values()));
    return getEventsReturn(notifyEvents, maxEventSeqId);
  }


  /**
   * 拉取回复事件
   *
   * @param parentMsgId 父消息id
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getReplyEvents(String parentMsgId, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull reply events called, parentMsgId: {}, eventSeqId: {}, pageSize: {}", parentMsgId, eventSeqId, pageSize);

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventRepository.selectEvents(null, parentMsgId, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = 0L;
    if (events.isEmpty()) {
      lastEventSeqId = eventRepository.selectLastEventSeqId(null, parentMsgId);
    } else {
      lastEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    Map<String, Event> eventMap = new HashMap<>();
    List<Event> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case REPLY:
          eventMap.put(event.getMsgId(), event);
          break;
        case REPLY_RETRACT:
          if (eventMap.containsKey(event.getMsgId())) {
            eventMap.remove(event.getMsgId());
          } else {
            eventMap.put(event.getMsgId(), event);
          }
          break;
        case REPLY_DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            List<String> msgIds = new ArrayList<>(event.getMsgIds());
            event.getMsgIds().forEach(msgId -> {
              if (eventMap.containsKey(msgId)) {
                eventMap.remove(msgId);
                msgIds.remove(msgId); // 删除已出现的msgId
              }
            });
            // 将此次拉取中未出现的msgId添加到删除事件中，供前端处理
            if (!msgIds.isEmpty()) {
              event.setMsgIds(msgIds);
              notifyEvents.add(event);
            }
          }
          break;
      }
    });
    notifyEvents.addAll(eventMap.values());
    return getEventsReturn(notifyEvents, lastEventSeqId);
  }

  /**
   * 拉取事件返回参数拼装
   */
  private Map<String, Object> getEventsReturn(List<Event> notifyEvents, Long lastEventSeqId) {
    notifyEvents.sort(Comparator.comparing(Event::getEventSeqId));
    Map<String, Object> result = new HashMap<>();
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);

    // 返回结果最多1000条
    if (notifyEvents.size() > 1000) {
      result.put("events", notifyEvents.subList(notifyEvents.size() - 1001, notifyEvents.size() - 1));
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
    unreadRepository.selectCount(to).forEach(unread -> unreadMap.put(unread.getFrom(), unread.getCount()));

    // 查询所有事件
    List<Event> events = eventRepository.selectEvents(to, null, null, null);

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

        UnreadResponse unreadResponse = new UnreadResponse(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to, msgIds.size() + unread);
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
      event.autoReadExtendParam(jsonService);
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
  public void reset(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("reset to: {}, param: {}", event.getTo(), event);
    event.setEventType(EventType.RESET.getValue());
    Integer CDTPEventType = event.getEventType();
    // groupTemail不为空则为群聊
    if (event.getGroupTemail() != null && !event.getGroupTemail().isEmpty()) {
      event.setFrom(event.getGroupTemail());
      CDTPEventType = EventType.GROUP_RESET.getValue();
    }
    event.setTimestamp(System.currentTimeMillis());
    event.initEventSeqId(sequenceService);
    eventRepository.insert(event);

    // 删除历史重置事件
    List<Long> ids = eventRepository.selectResetEvents(event);
    if (!ids.isEmpty()) {
      eventRepository.delete(ids);
    }

    // 发送到MQ以便多端同步
    LOGGER.info("send reset event to {}", event.getTo());
    rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), CDTPEventType, header, jsonService.toJson(event))));
  }
}
