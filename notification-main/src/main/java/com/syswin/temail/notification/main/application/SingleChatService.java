package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.main.application.rocketmq.RocketMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
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
public class SingleChatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;

  @Autowired
  public SingleChatService(RocketMqProducer rocketMqProducer, RedisService redisService,
      EventMapper eventMapper, IJsonService iJsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentSingleChatParams params = iJsonService.fromJson(body, MailAgentSingleChatParams.class);
    Event event = new Event(params.getSessionMessageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getxPacketId(), params.getOwner(), params.getDeleteAllMsg());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("single chat params: {}", params);
    LOGGER.info("single chat event type: {}", EventType.getByValue(event.getEventType()));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case DESTROY:
      case REPLY:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(params.getOwner())) {
          event.initEventSeqId(redisService);
          event.autoWriteExtendParam(iJsonService);
          sendMessageToSender(event, header, tags);
          // 发送到发件人收件箱的消息，事件中对换to和owner字段来保存
          event.setTo(params.getOwner());
          event.setOwner(params.getTo());
          event.autoWriteExtendParam(iJsonService);
          eventMapper.insert(event);
        } else {
          sendMessage(event, header, tags);
        }
        break;
      case RETRACT:
      case DESTROYED:
      case REPLY_RETRACT:
      case REPLY_DESTROYED:
        // 多端同步功能消息同时发给双方
        event.setOwner(params.getTo());
        sendMessage(event, header, tags);
        // 发送给发送方
        event.setOwner(params.getFrom());
        event.initEventSeqId(redisService);
        event.autoWriteExtendParam(iJsonService);
        sendMessageToSender(event, header, tags);
        // 插入数据库，使用owner保存原消息的to，to字段保存接收者
        event.setTo(params.getFrom());
        event.setOwner(params.getTo());
        event.autoWriteExtendParam(iJsonService);
        eventMapper.insert(event);
        break;
      case PULLED:
        // from是消息拉取人
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventMapper.selectEventsByMsgId(event).size() == 0) {
            sendMessage(event, header, tags);
          } else {
            LOGGER.info("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
      case REPLY_DELETE:
      case TRASH:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(iJsonService.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        // from是操作人，to是会话另一方
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header, tags);
        break;
      case ARCHIVE:
      case ARCHIVE_CANCEL:
        // from是操作人，to是会话的另一方
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header, tags);
        break;
      case TRASH_CANCEL:
      case TRASH_DELETE:
        // owner是操作人，from和to都为空，msgId为空
        event.setTrashMsgInfo(params.getTrashMsgInfo());
        event.setFrom(params.getOwner());
        event.setTo(params.getOwner());
        sendMessage(event, header, tags);
        break;
      case DO_NOT_DISTURB:
      case DO_NOT_DISTURB_CANCEL:
        this.sendMessage(event, header, tags);
        break;
      default:
        LOGGER.warn("unsupport event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(redisService);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    this.sendMessage(event, event.getTo(), header, tags);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String to, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to --->> {}, event type: {}", to, EventType.getByValue(event.getEventType()));
    this.insert(event);
    rocketMqProducer.sendMessage(iJsonService.toJson(new CDTPResponse(to, event.getEventType(), header, iJsonService.toJson(event))), tags);
  }

  /**
   * 发送消息，提供多端同步功能
   */
  private void sendMessageToSender(Event event, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to sender --->> {}, event type: {}", event.getFrom(), EventType.getByValue(event.getEventType()));
    rocketMqProducer
        .sendMessage(iJsonService.toJson(new CDTPResponse(event.getFrom(), event.getEventType(), header, iJsonService.toJson(event))), tags);
  }
}
