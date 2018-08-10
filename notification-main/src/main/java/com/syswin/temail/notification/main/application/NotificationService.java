package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
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
public class NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final EventRepository eventRepository;
  private final RedisService redisService;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, EventRepository eventRepository,
      RedisService redisService) {
    this.rocketMqProducer = rocketMqProducer;
    this.eventRepository = eventRepository;
    this.redisService = redisService;
  }

  /**
   * 批量插入事件信息
   */
  public void batchInsert(List<Event> events) {
    eventRepository.batchInsert(events);
  }

  /**
   * 向RocketMq发送消息
   */
  public void sendMqMessage(String body) throws Exception {
    rocketMqProducer.sendMessage(body, "", "");
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) throws Exception {
    Event event = (new Gson()).fromJson(body, Event.class);
    event.setSequenceNo(redisService.getNextSeq(event.getTo()));
    batchInsert(Arrays.asList(event));
    sendMqMessage(body);
  }

  /**
   * 获取新事件
   *
   * @param userId 接收方
   * @param sequenceNo 上次拉取结尾序号
   */
  public Map<String, List<Event>> getEvents(String userId, Long sequenceNo) {
    eventRepository.deleteByTo(userId, sequenceNo);
    List<Event> events = eventRepository.selectByTo(userId, sequenceNo);

    Map<String, List<Event>> result = new HashMap<>();
    for (Event event : events) {

      List<Event> toEvents;
      if (!result.containsKey(event.getFrom())) {
        toEvents = new ArrayList<>();
        result.put(event.getFrom(), toEvents);
      } else {
        toEvents = result.get(event.getFrom());
      }

      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RECEIVE:
          toEvents.add(event);
          break;
        case READ:
          toEvents.forEach(toEvent -> {
            if (toEvent.getMessageId() == event.getMessageId()) {
              toEvents.remove(toEvent);
            }
          });
          toEvents.add(event);
          break;
        case RETRACT:
        case DESTROY:
          toEvents.forEach(toEvent -> {
            if (toEvent.getMessageId() == event.getMessageId()) {
              toEvents.remove(toEvent);
            }
          });
          break;
      }
    }

    result.forEach((s, toEvents) -> {
      if (toEvents.isEmpty()) {
        result.remove(s);
      }
    });

    return result;
  }
}
