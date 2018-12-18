package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.util.NotificationUtil;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventRepository eventRepository;
  private final JsonService jsonService;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, RedisService redisService,
      EventRepository eventRepository, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.eventRepository = eventRepository;
    this.jsonService = jsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentSingleChatParams params = jsonService.fromJson(body, MailAgentSingleChatParams.class);
    Event event = new Event(params.getSessionMessageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getxPacketId(), params.getOwner(), params.getDeleteAllMsg());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("single chat params: {}", params);
    LOGGER.info("single chat event type: {}", EventType.getByValue(event.getEventType()));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventRepository, redisService)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case DESTROY:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(params.getOwner())) {
          event.initEventSeqId(redisService);
          event.autoWriteExtendParam(jsonService);
          sendMessageToSender(event, header);
          // 发送到发件人收件箱的消息，事件中对换to和owner字段来保存
          event.setTo(params.getOwner());
          event.setOwner(params.getTo());
          event.autoWriteExtendParam(jsonService);
          eventRepository.insert(event);
        } else {
          sendMessage(event, header);
        }
        break;
      case RETRACT:
      case DESTROYED:
        // 多端同步功能消息同时发给双方
        event.setOwner(params.getTo());
        sendMessage(event, header);
        // 发送给发送方
        event.setOwner(params.getFrom());
        event.initEventSeqId(redisService);
        event.autoWriteExtendParam(jsonService);
        sendMessageToSender(event, header);
        // 插入数据库，使用owner保存原消息的to，to字段保存接收者
        event.setTo(params.getFrom());
        event.setOwner(params.getTo());
        event.autoWriteExtendParam(jsonService);
        eventRepository.insert(event);
        break;
      case PULLED:
        // from是消息拉取人
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventRepository.selectEventsByMsgId(event).size() == 0) {
            sendMessage(event, header);
          } else {
            LOGGER.info("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
      case REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(jsonService.fromJson(event.getMsgId(), List.class));
        event.setMsgId(null);
        // from是删除者
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header);
        break;
      case REPLY:
        // 回复拉取时是根据parentMsgId拉取，因此发送人的通知不需要入库
        // 发送时会分别发送到发件人收件箱和收件人收件箱，只有收件人收件箱的事件才会入库
        if (event.getTo().equals(params.getOwner())) {
          sendMessage(event, header);
        } else {
          sendMessageToSender(event, header);
        }
        break;
      case REPLY_RETRACT:
      case REPLY_DESTROYED:
        sendMessage(event, header);
        sendMessageToSender(event, header);
        break;
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(redisService);
    event.autoWriteExtendParam(jsonService);
    eventRepository.insert(event);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    this.insert(event);
    rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), event.getEventType(), header, jsonService.toJson(event))));
  }

  /**
   * 发送消息，提供多端同步功能
   */
  private void sendMessageToSender(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to sender --->> {}, event type: {}", event.getFrom(), EventType.getByValue(event.getEventType()));
    rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getFrom(), event.getEventType(), header, jsonService.toJson(event))));
  }
}
