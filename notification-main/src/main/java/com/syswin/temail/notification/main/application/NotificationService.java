package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.MailAgentParams;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
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
  private final Gson gson;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, EventRepository eventRepository,
      RedisService redisService) {
    this.rocketMqProducer = rocketMqProducer;
    this.eventRepository = eventRepository;
    this.redisService = redisService;
    gson = new Gson();
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) throws Exception {
    MailAgentParams params = gson.fromJson(body, MailAgentParams.class);
    Event event = new Event(params.getSessionMssageType(), params.getFrom(), params.getTo(), params.getMsgid(), params.getFromSeqNo(),
        params.getToMsg(), params.getHeader());
    event.setSequenceNo(redisService.getNextSeq(event.getTo()));

    // 需要通知发件人的消息，将from和to对调
    if (event.getEventType().equals(EventType.DESTROY)) {
      event.exchangeSides();
    }

    // 入库
    eventRepository.insert(event);
    // 发送消息
    rocketMqProducer.sendMessage(gson.toJson(event), event.getTo(), "");
  }

  /**
   * 获取新事件
   *
   * @param to 接收方
   * @param sequenceNo 上次拉取结尾序号
   */
  public Map<String, List<Event>> getEvents(String to, Long sequenceNo) {
    LOGGER.info("从序列号[" + sequenceNo + "]之后开始拉取收件人[" + to + "]的事件。");

    // 删除以前的事件
    eventRepository.deleteByTo(to, sequenceNo);

    Map<String, List<Event>> result = new HashMap<>();
    eventRepository.selectByTo(to, sequenceNo).forEach(event -> mergeEvent(result, event));

    // 删除value为空的数据
    result.forEach((s, toEvents) -> {
      if (toEvents.isEmpty()) {
        result.remove(s);
      }
    });

    return result;
  }

  private void mergeEvent(Map<String, List<Event>> result, Event event) {
    List<Event> fromEvents = getFromEvents(result, event.getFrom());
    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
        fromEvents.add(event);
        break;
      case PULLED:
        deleteEvent(fromEvents, event.getMessageId());
        fromEvents.add(event);
        break;
      case RETRACT:
      case DESTROY:
        deleteEvent(fromEvents, event.getMessageId());
        break;
    }
  }

  private List<Event> getFromEvents(Map<String, List<Event>> result, String from) {
    if (!result.containsKey(from)) {
      result.put(from, new ArrayList<>());
    }
    return result.get(from);
  }

  private void deleteEvent(List<Event> fromEvents, String messageId) {
    fromEvents.removeIf(event -> event.getMessageId().equals(messageId));
  }
}
