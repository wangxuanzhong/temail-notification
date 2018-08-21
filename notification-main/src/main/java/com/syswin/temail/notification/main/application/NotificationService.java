package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.CDTPResponse;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.MailAgentParams;
import com.syswin.temail.notification.main.domains.UnreadResponse;
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
  public void handleMqMessage(String body) throws Exception {
    MailAgentParams params = gson.fromJson(body, MailAgentParams.class);
    Event event = new Event(params.getMsgid(), params.getFromSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(),
        params.getTimestamp(), params.getSessionMssageType());

    LOGGER.info("单聊收到的事件类型为：" + Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());

    for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
      event.setEventSeqId(redisService.getNextSeq(event.getTo()));
      event.setMsgId(msgId);
      eventRepository.insert(event);
      rocketMqProducer.sendMessage(gson.toJson(new CDTPResponse(event.getTo(), params.getHeader(), event)), "", "");
    }
  }


  /**
   * 获取新事件
   *
   * @param to 发起人
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getEvents(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("从序列号[" + eventSeqId + "]之后开始拉取接收方[" + to + "]的事件。拉取数量为：" + pageSize);

    // 如果pageSize为空则不限制查询条数
    Long end = null;
    if (pageSize != null) {
      end = eventSeqId + pageSize;
    }
    List<Event> events = eventRepository.selectByTo(to, eventSeqId, end);

    Map<String, Event> eventMap = new HashMap<>();
    List<Event> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RECEIVE:
        case DESTROY:
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
    result.put("lastEventSeqId", events.get(events.size() - 1).getEventSeqId());
    result.put("events", notifyEvents);
    LOGGER.info("拉取事件结果为：" + result);
    return result;
  }


  /**
   * 获取消息未读数
   *
   * @param to 发起人
   */
  public List<UnreadResponse> getUnread(String to, Long eventSeqId) {
    LOGGER.info("从序列号[" + eventSeqId + "]之后获取接收方[" + to + "]的未读消息数量。");
    List<Event> events = eventRepository.selectByTo(to, eventSeqId, null);

    Map<String, List<String>> unreadMap = new HashMap<>();
    events.forEach(event -> {
      if (!unreadMap.containsKey(event.getFrom())) {
        unreadMap.put(event.getFrom(), new ArrayList<>());
      }
      List<String> msgIds = unreadMap.get(event.getFrom());
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RECEIVE:
        case DESTROY:
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
    unreadMap.forEach((from, msgIds) -> {
      if (!msgIds.isEmpty()) {
        unreadResponses.add(new UnreadResponse(from, to, msgIds.size()));
      }
    });

    LOGGER.info("获取未读消息结果为：" + unreadResponses);
    return unreadResponses;
  }
}
