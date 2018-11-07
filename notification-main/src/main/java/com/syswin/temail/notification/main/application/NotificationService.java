package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
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
  private final SequenceService sequenceService;
  private final EventRepository eventRepository;
  private final JsonService jsonService;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer, SequenceService sequenceService,
      EventRepository eventRepository, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.sequenceService = sequenceService;
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
    Event event = new Event(params.getSessionMssageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp());

    LOGGER.info("单聊收到的事件类型为：" + Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case DESTROY:
      case RETRACT:
      case DESTROYED:
      case REPLY:
        this.insert(event);
        rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), params.getHeader(), jsonService.toJson(event))));
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventRepository.selectPulledEvents(event).size() == 0) {
            this.insert(event);
            rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), params.getHeader(), jsonService.toJson(event))));
          } else {
            LOGGER.info("消息{}已拉取，不重复处理，时间戳为：{}", msgId, event.getTimestamp());
          }
        }
        break;
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(sequenceService);
    event.autoWriteExtendParam(jsonService);
    eventRepository.insert(event);
  }
}
