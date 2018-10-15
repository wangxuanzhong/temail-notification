package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
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
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventRepository eventRepository;
  private final Gson gson;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, RedisService redisService,
      EventRepository eventRepository) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.eventRepository = eventRepository;
    gson = new Gson();
  }

  /**
   * 处理从MQ收到的信息
   */
  public void handleMqMessage(String body)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentSingleChatParams params = gson.fromJson(body, MailAgentSingleChatParams.class);
    Event event = new Event(params.getMsgid(), params.getSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(),
        params.getTimestamp(), params.getSessionMssageType());

    LOGGER.info("单聊收到的事件类型为：" + Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case DESTROY:
      case RETRACT:
      case DESTROYED:
        event.setEventSeqId(redisService.getNextSeq(event.getTo()));
        eventRepository.insert(event);
        rocketMqProducer.sendMessage(gson.toJson(new CDTPResponse(event.getTo(), params.getHeader(), gson.toJson(event))));
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventRepository.selectPulledEvent(event) == null) {
            event.setEventSeqId(redisService.getNextSeq(event.getTo()));
            eventRepository.insert(event);
            rocketMqProducer.sendMessage(gson.toJson(new CDTPResponse(event.getTo(), params.getHeader(), gson.toJson(event))));
          } else {
            LOGGER.info("消息{}已拉取，不重复处理，时间戳为：{}", msgId, event.getTimestamp());
          }
        }
        break;
    }
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
   * 获取消息未读数
   *
   * @param to 发起人
   */
  public List<UnreadResponse> getUnread(String to) {
    LOGGER.info("获取接收方[" + to + "]的未读消息数量。");
    List<Event> events = eventRepository.selectByTo(to, null, null);

    Map<String, List<String>> unreadMap = new HashMap<>();
    events.forEach(event -> {
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
   * 重置消息未读数
   */
  public void reset(Event event) {
    LOGGER.info("重置[{}]的消息未读数，from: {}, groupTemail: {}", event.getTo(), event.getFrom(), event.getGroupTemail());
    if (event.getGroupTemail() != null && !event.getGroupTemail().equals("")) {
      event.setFrom(event.getGroupTemail());
    }
    event.setEventType(EventType.RESET.getValue());
    event.setEventSeqId(redisService.getNextSeq(event.getTo()));
    event.setTimestamp(System.currentTimeMillis());
    eventRepository.insert(event);
  }
}
