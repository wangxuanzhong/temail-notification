package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationTopicService implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;
  private final TopicMapper topicMapper;
  private final IJsonService iJsonService;

  @Autowired
  public NotificationTopicService(IMqProducer iMqProducer, NotificationRedisService notificationRedisService,
      TopicMapper topicMapper, IJsonService iJsonService) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
    this.topicMapper = topicMapper;
    this.iJsonService = iJsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    MailAgentParams params = iJsonService.fromJson(body, MailAgentParams.class);
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
        sendMessage(topicEvent, header, tags);
        break;
      case TOPIC_REPLY:
        sendMessage(topicEvent, header, tags);
        break;
      case TOPIC_REPLY_RETRACT:
        // 向撤回的消息的所有收件人发送通知
        for (TopicEvent event : topicMapper.selectEventsByMsgId(topicEvent.getMsgId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header, tags);
        }
        break;
      case TOPIC_REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段，from为操作人
        topicEvent.setMsgIds(iJsonService.fromJson(topicEvent.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        topicEvent.setMsgId(null);
        topicEvent.setTo(topicEvent.getFrom());
        sendMessage(topicEvent, header, tags);
        break;
      case TOPIC_DELETE:
        // 向话题接收的所有人发送通知
        for (TopicEvent event : topicMapper.selectEventsByTopicId(topicEvent.getTopicId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header, tags);
        }
        break;
      case TOPIC_ARCHIVE:
      case TOPIC_ARCHIVE_CANCEL:
        // from是操作人，to为空
        topicEvent.setTo(params.getFrom());
        sendMessage(topicEvent, header, tags);
        break;
      case TOPIC_SESSION_DELETE:
        // from是操作人，to为空
        topicEvent.setTo(params.getFrom());
        topicEvent.setDeleteAllMsg(params.getDeleteAllMsg());
        sendMessage(topicEvent, header, tags);
        break;
      default:
        LOGGER.warn("unsupport event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(TopicEvent topicEvent) {
    topicEvent.initTopicEventSeqId(notificationRedisService);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicMapper.insert(topicEvent);
  }

  /**
   * 发送消息
   */
  private void sendMessage(TopicEvent topicEvent, String header, String tags) {
    LOGGER.info("send message to --->> {}, event type: {}", topicEvent.getTo(), EventType.getByValue(topicEvent.getEventType()));
    this.insert(topicEvent);
    iMqProducer.sendMessage(
        iJsonService.toJson(new CDTPResponse(topicEvent.getTo(), topicEvent.getEventType(), header, TopicEvent.toJson(iJsonService, topicEvent))),
        tags);

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
    List<TopicEvent> events = topicMapper.selectEvents(to, eventSeqId, pageSize == null ? null : eventSeqId + pageSize);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = 0L;
    if (events.isEmpty()) {
      lastEventSeqId = topicMapper.selectLastEventSeqId(to);
    } else {
      lastEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    Map<String, Map<String, TopicEvent>> allTopicMap = new HashMap<>();
    Map<String, Map<String, TopicEvent>> allReplyMap = new HashMap<>();
    List<TopicEvent> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(iJsonService);

      // 按照话题统计事件
      if (!allTopicMap.containsKey(event.getTopicId())) {
        allTopicMap.put(event.getTopicId(), new LinkedHashMap<>());
      }
      Map<String, TopicEvent> topicMap = allTopicMap.get(event.getTopicId());

      // 按照话题统计回复消息
      if (!allReplyMap.containsKey(event.getTopicId())) {
        allReplyMap.put(event.getTopicId(), new LinkedHashMap<>());
      }
      Map<String, TopicEvent> replyMap = allReplyMap.get(event.getTopicId());

      // 话题归档和取消归档事件不考虑离线消息提醒
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case TOPIC:
          topicMap.put(event.getMsgId(), event);
          break;
        case TOPIC_REPLY:
          // 单独记录所有的回复消息
          replyMap.put(event.getMsgId(), event);
          break;
        case TOPIC_REPLY_RETRACT:
          // 撤回的消息需要和回复消息做抵消，如果撤回的消息不在本次拉到的回复消息范围内，需要提醒客户端
          if (replyMap.containsKey(event.getMsgId())) {
            replyMap.remove(event.getMsgId());
          } else {
            topicMap.put(event.getMsgId(), event);
          }
          break;
        case TOPIC_REPLY_DELETE:
          // 删除的消息不考虑离线事件提醒，只需要考虑删除消息和回复消息抵消的情况
          event.getMsgIds().forEach(replyMap::remove);
          break;
        case TOPIC_DELETE:
          topicMap.clear();
          replyMap.clear();
          topicMap.put(event.getTopicId(), event);
          break;
        case TOPIC_SESSION_DELETE:
          if (event.getDeleteAllMsg()) {
            topicMap.clear();
            replyMap.clear();
          }
          topicMap.put(event.getTopicId(), event);
          break;
      }
    });

    allTopicMap.values().forEach(map -> notifyEvents.addAll(map.values()));

    // 每个话题只返回最新一条回复消息
    allReplyMap.values().forEach(map -> {
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
