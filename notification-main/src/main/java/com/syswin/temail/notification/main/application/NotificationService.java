package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
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
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getxPacketId(), params.getDeleteAllMsg());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("single chat params: \n" + params);
    LOGGER.info("single chat event type: " + Objects.requireNonNull(EventType.getByValue(event.getEventType())));

    // 校验收到的数据是否重复
    if (!this.checkUnique(event)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case REPLY:
        // 只通知收件箱的消息
        if (event.getTo().equals(params.getOwner())) {
          sendMessage(event, header);
        }
        break;
      case DESTROY:
      case RETRACT:
      case DESTROYED:
      case REPLY_RETRACT:
        sendMessage(event, header);
        break;
      case PULLED:
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
        // from和to与正常业务相反
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header);
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
    LOGGER.info("send message to {}, event type: {}", event.getTo(), Objects.requireNonNull(EventType.getByValue(event.getEventType())));
    if (event.getTo() != null && !event.getTo().isEmpty()) {
      this.insert(event);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), header, jsonService.toJson(event))));
    }
  }

  /**
   * 幂等校验
   */
  public boolean checkUnique(Event event) {
    if (event.getxPacketId() == null || event.getxPacketId().isEmpty()) {
      LOGGER.warn("xPacketId is null!");
      return true;
    }

    if (redisService.checkUnique(event.getxPacketId() + "_" + event.getEventType())) {
      return true;
    } else {
      LOGGER.warn("check duplicate failure: ", event);
      return false;
    }
  }
}
