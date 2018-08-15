package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.CDTPResponse;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventResponse;
import com.syswin.temail.notification.main.domains.MailAgentParams;
import java.lang.invoke.MethodHandles;
import java.util.List;
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
  private final Gson gson;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, EventRepository eventRepository) {
    this.rocketMqProducer = rocketMqProducer;
    this.eventRepository = eventRepository;
    gson = new Gson();
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) throws Exception {
    MailAgentParams params = gson.fromJson(body, MailAgentParams.class);
    Event event = new Event(params.getMsgid(), params.getFromSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(),
        params.getTimestamp().getTime(), params.getSessionMssageType());

    // 不同事件做不同处理
    dealEvent(event);

    // 发送消息
    CDTPResponse cdtpResponse = new CDTPResponse(params.getHeader(), event);
    rocketMqProducer.sendMessage(gson.toJson(cdtpResponse), event.getFrom(), event.getTo());
  }

  private void dealEvent(Event event) {
    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
        eventRepository.insert(event);
        break;
      case PULLED:
        eventRepository.deleteUnreadEvent(event.getMsgId());
        break;
      case RETRACT:
      case DESTROY:
        eventRepository.deleteUnreadEvent(event.getMsgId());
        eventRepository.insert(event);
        break;
    }
  }


  /**
   * 获取新事件
   *
   * @param from 发起方
   * @param seqId 消息起始序列号
   * @param pageSize 拉取数量
   */
  @Transactional(rollbackFor = Exception.class)
  public List<Event> getEvents(String from, Long seqId, Integer pageSize) {
    LOGGER.info("拉取收件人[" + from + "]序列号[" + seqId + "]之后" + pageSize + "条事件。");

    List<Event> events = eventRepository.selectByToBetweenSeqId(from, seqId + 1, seqId + pageSize);

    // 删除以前的事件
    eventRepository.deleteByToBetweenSeqId(from, seqId + 1, seqId + pageSize);

    return events;
  }


  /**
   * 获取消息未读数
   *
   * @param from 发起人
   */
  public List<EventResponse> getUnread(String from) {
    return eventRepository.selectAllUnread(from);
  }
}
