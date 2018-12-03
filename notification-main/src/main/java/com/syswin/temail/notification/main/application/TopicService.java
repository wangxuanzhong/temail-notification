package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.TopicEventRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentTopicParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final TopicEventRepository topicEventRepository;
  private final JsonService jsonService;

  @Autowired
  public TopicService(RocketMqProducer rocketMqProducer, RedisService redisService, TopicEventRepository topicEventRepository,
      JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.topicEventRepository = topicEventRepository;
    this.jsonService = jsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentTopicParams params = jsonService.fromJson(body, MailAgentTopicParams.class);
    TopicEvent topicEvent = new TopicEvent(params.getxPacketId(), params.getSessionMessageType(), params.getTopicId(), params.getMsgid(),
        params.getSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(), params.getTimestamp());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("topic params: \n" + params);
    LOGGER.info("topic event type: " + Objects.requireNonNull(EventType.getByValue(topicEvent.getEventType())));

    switch (Objects.requireNonNull(EventType.getByValue(topicEvent.getEventType()))) {
      case TOPIC:
        // from和to相同为话题发送者的消息，通知服务不处理
        if (!topicEvent.getTo().equals(topicEvent.getFrom())) {
          topicEvent.setTitle(params.getTitle());
          topicEvent.setReceivers(params.getReceivers());
          topicEvent.setCC(params.getCC());
          sendMessage(topicEvent, header);
        }
        break;
      case TOPIC_REPLY:
        // from和to相同为话题发送者的消息，通知服务不处理
        if (!topicEvent.getTo().equals(topicEvent.getFrom())) {
          sendMessage(topicEvent, header);
        }
        break;
      case TOPIC_RETRACT:
        // 向撤回的消息的所有收件人发送撤回通知
        for (TopicEvent event : topicEventRepository.selectEventsByMsgId(topicEvent.getMsgId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header);
        }
        break;
      case TOPIC_DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        topicEvent.setMsgIds(jsonService.fromJson(topicEvent.getMsgId(), List.class));
        topicEvent.setMsgId(null);
        // from和to与正常业务相反
        topicEvent.setFrom(params.getTo());
        topicEvent.setTo(params.getFrom());
        sendMessage(topicEvent, header);
        break;
    }
  }

  /**
   * 插入数据库
   */
  private void insert(TopicEvent topicEvent) {
    topicEvent.initTopicEventSeqId(redisService);
    topicEvent.autoWriteExtendParam(jsonService);
    topicEventRepository.insert(topicEvent);
  }

  /**
   * 发送消息
   */
  private void sendMessage(TopicEvent topicEvent, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to {}, event type: {}", topicEvent.getTo(), Objects.requireNonNull(EventType.getByValue(topicEvent.getEventType())));
    if (topicEvent.getTo() != null && !topicEvent.getTo().isEmpty()) {
      this.insert(topicEvent);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(topicEvent.getTo(), header, jsonService.toJson(topicEvent))));
    }
  }

  /**
   * 拉取话题事件
   *
   * @param to 发起人
   * @param topicId 话题id
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getTopicEvents(String to, String topicId, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull reply events called, to: {}, topicId: {}, eventSeqId: {}, pageSize: {}", to, topicId, eventSeqId, pageSize);

    // 如果pageSize为空则不限制查询条数
    List<TopicEvent> events = topicEventRepository.selectEvents(to, topicId, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    Map<String, TopicEvent> eventMap = new HashMap<>();
    List<TopicEvent> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case TOPIC:
        case TOPIC_REPLY:
          eventMap.put(event.getMsgId(), event);
          break;
        case TOPIC_RETRACT:
          if (eventMap.containsKey(event.getMsgId())) {
            eventMap.remove(event.getMsgId());
          } else {
            eventMap.put(event.getMsgId(), event);
          }
          break;
        case TOPIC_DELETE:
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
    notifyEvents.sort(Comparator.comparing(TopicEvent::getEventSeqId));

    Map<String, Object> result = new HashMap<>();
    if (events.isEmpty()) {
      result.put("lastEventSeqId", -1);
    } else {
      result.put("lastEventSeqId", events.get(events.size() - 1).getEventSeqId());
    }
    result.put("events", notifyEvents);
    LOGGER.info("pull events result: " + result);
    return result;
  }

  /**
   * 计算话题总数
   *
   * @param to 发起人
   * @param topicId 话题id
   */
  public Map<String, Object> getTopicSum(String to, String topicId) {
    LOGGER.info("pull reply events called, to: {}, topicId: {}", to, topicId);

    List<TopicEvent> events = topicEventRepository.selectEvents(to, topicId, null, null);

    List<String> msgIds = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case TOPIC:
        case TOPIC_REPLY:
          msgIds.add(event.getMsgId());
          break;
        case TOPIC_RETRACT:
          msgIds.remove(event.getMsgId());
          break;
        case TOPIC_DELETE:
          // 计算总数直接删除
          event.getMsgIds().forEach(msgIds::remove);
          break;
      }
    });

    Map<String, Object> result = new HashMap<>();
    result.put("sum", msgIds.size());
    LOGGER.info("pull events result: " + result);
    return result;
  }
}