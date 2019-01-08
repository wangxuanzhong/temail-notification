package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.params.MailAgentTopicParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.TopicEventMapper;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
  private final TopicEventMapper topicEventMapper;
  private final JsonService jsonService;

  @Autowired
  public TopicService(RocketMqProducer rocketMqProducer, RedisService redisService, TopicEventMapper topicEventMapper, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.topicEventMapper = topicEventMapper;
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

    LOGGER.info("topic params: {}", params);
    LOGGER.info("topic event type: {}", EventType.getByValue(topicEvent.getEventType()));

    switch (Objects.requireNonNull(EventType.getByValue(topicEvent.getEventType()))) {
      case TOPIC:
        // from和to相同为话题发送者的消息
        topicEvent.setTitle(params.getTitle());
        topicEvent.setReceivers(params.getReceivers());
        topicEvent.setCc(params.getCc());
        topicEvent.setTopicSeqId(params.getTopicSeqId()); // 话题单独的序列号
        sendMessage(topicEvent, header);
        break;
      case TOPIC_REPLY:
        sendMessage(topicEvent, header);
        break;
      case TOPIC_RETRACT:
        // 向撤回的消息的所有收件人发送通知
        for (TopicEvent event : topicEventMapper.selectEventsByMsgId(topicEvent.getMsgId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header);
        }
        break;
      case TOPIC_REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段，from为操作人
        topicEvent.setMsgIds(jsonService.fromJson(topicEvent.getMsgId(), List.class));
        topicEvent.setMsgId(null);
        topicEvent.setTo(topicEvent.getFrom());
        sendMessage(topicEvent, header);
        break;
      case TOPIC_DELETE:
        // 向话题接收的所有人发送通知
        for (TopicEvent event : topicEventMapper.selectEventsByTopicId(topicEvent.getTopicId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header);
        }
        break;
      case TOPIC_ARCHIVE:
      case TOPIC_ARCHIVE_CANCEL:
        // from是操作人，to为空
        topicEvent.setTo(params.getFrom());
        topicEvent.addEventMsgId(EventType.ARCHIVE);
        sendMessage(topicEvent, header);
        break;
      case TOPIC_SESSION_DELETE:
        //话题会话删除，发送同步消息
        topicEvent.setTo(params.getFrom());
        topicEvent.addEventMsgId(EventType.TOPIC_SESSION_DELETE);
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
    topicEventMapper.insert(topicEvent);
  }

  /**
   * 发送消息
   */
  private void sendMessage(TopicEvent topicEvent, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to --->> {}, event type: {}", topicEvent.getTo(), EventType.getByValue(topicEvent.getEventType()));
    this.insert(topicEvent);
    rocketMqProducer.sendMessage(
        jsonService.toJson(new CDTPResponse(topicEvent.getTo(), topicEvent.getEventType(), header, jsonService.toJson(topicEvent))));

  }

  /**
   * 拉取话题事件
   *
   * @param to 发起人
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getTopicEvents(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull topic events called, to: {}, eventSeqId: {}, pageSize: {}", to, eventSeqId, pageSize);

    // 如果pageSize为空则不限制查询条数
    List<TopicEvent> events = topicEventMapper.selectEvents(to, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = 0L;
    if (events.isEmpty()) {
      lastEventSeqId = topicEventMapper.selectLastEventSeqId(to);
    } else {
      lastEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    List<TopicEvent> notifyEvents = new ArrayList<>();
    Map<String, Map<String, TopicEvent>> replyEventMap = new HashMap<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);

      // 按照话题统计回复消息
      String key = event.getTopicId();
      if (!replyEventMap.containsKey(key)) {
        replyEventMap.put(key, new LinkedHashMap<>());
      }
      Map<String, TopicEvent> topicMap = replyEventMap.get(key);

      // 话题归档和取消归档事件不考虑离线消息提醒
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case TOPIC:
          notifyEvents.add(event);
          break;
        case TOPIC_REPLY:
          // 单独记录所有的回复消息
          topicMap.put(event.getMsgId(), event);
          break;
        case TOPIC_RETRACT:
          // 撤回的消息需要和回复消息做抵消，如果撤回的消息不在本次拉到的回复消息范围内，需要提醒客户端
          if (topicMap.containsKey(event.getMsgId())) {
            topicMap.remove(event.getMsgId());
          } else {
            notifyEvents.add(event);
          }
          break;
        case TOPIC_REPLY_DELETE:
          // 删除的消息不考虑离线事件提醒，只需要考虑删除消息和回复消息抵消的情况
          event.getMsgIds().forEach(topicMap::remove);
          break;
        case TOPIC_DELETE:
          notifyEvents.add(event);
          break;
      }
    });

    // 每个话题只返回最新一条回复消息
    replyEventMap.values().forEach(map -> {
      if (!map.isEmpty()) {
        List<TopicEvent> replys = new ArrayList<>(map.values());
        notifyEvents.add(replys.get(replys.size() - 1));
      }
    });

    //给事件按照eventSeqId重新排序
    notifyEvents.sort(Comparator.comparing(TopicEvent::getEventSeqId));

    //返回事件超过1000条，只返回最后一千条
    final int maxReturnNum = 1000;
    if (notifyEvents.size() > maxReturnNum) {
      notifyEvents.subList(0, notifyEvents.size() - maxReturnNum).clear();
    }

    Map<String, Object> result = new HashMap<>();
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);
    result.put("events", notifyEvents);
    return result;
  }
}
