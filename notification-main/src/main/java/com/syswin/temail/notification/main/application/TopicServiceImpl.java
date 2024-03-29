/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.constants.Constant.EventCondition;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.util.TopicEventUtil;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 话题通知事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class TopicServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;
  private final TopicMapper topicMapper;
  private final Gson gson;

  private final NotificationConfig config;

  @Autowired
  public TopicServiceImpl(IMqProducer iMqProducer, RedisServiceImpl redisService, TopicMapper topicMapper,
      NotificationConfig config) {
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.topicMapper = topicMapper;
    this.gson = new Gson();
    this.config = config;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    MailAgentParams params = gson.fromJson(body, MailAgentParams.class);
    TopicEvent topicEvent = gson.fromJson(body, TopicEvent.class);
    TopicEventUtil.copyMailAgentFieldToEvent(params, topicEvent);

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("topic params: {}, tags: {}", body, tags);

    EventType eventType = EventType.getByValue(params.getSessionMessageType());
    if (eventType == null) {
      LOGGER.warn("event type is illegal! xPacketId: {}", topicEvent.getxPacketId());
      return;
    }
    LOGGER.info("topic event type: {}", eventType);

    switch (eventType) {
      case TOPIC:
      case TOPIC_REPLY:
        // from和to相同为话题发送者的消息
        sendMessage(topicEvent, header, tags, body);
        break;
      case TOPIC_REPLY_RETRACT:
        // 向撤回的消息的所有收件人发送通知
        for (TopicEvent event : topicMapper.selectEventsByMsgId(topicEvent.getMsgId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header, tags, body);
        }
        break;
      case TOPIC_REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段，from为操作人
        topicEvent.setMsgIds(gson.fromJson(topicEvent.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        topicEvent.setMsgId(null);
        topicEvent.setTo(topicEvent.getFrom());
        sendMessage(topicEvent, header, tags, body);
        break;
      case TOPIC_DELETE:
        // 向话题接收的所有人发送通知
        for (TopicEvent event : topicMapper.selectEventsByTopicId(topicEvent.getTopicId())) {
          topicEvent.setTo(event.getTo());
          sendMessage(topicEvent, header, tags, body);
        }
        break;
      case TOPIC_ARCHIVE:
      case TOPIC_ARCHIVE_CANCEL:
        // from是操作人，to为空
        topicEvent.setTo(topicEvent.getFrom());
        sendMessage(topicEvent, header, tags, body);
        break;
      case TOPIC_SESSION_DELETE:
        // from是操作人，to为空
        topicEvent.setTo(topicEvent.getFrom());
        sendMessage(topicEvent, header, tags, body);
        break;
      default:
        LOGGER.warn("unsupport event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(TopicEvent topicEvent, String body) {
    TopicEventUtil.initTopicEventSeqId(redisService, topicEvent);
    topicEvent.autoWriteExtendParam(body);
    topicMapper.insert(topicEvent);
  }

  /**
   * 发送消息
   */
  private void sendMessage(TopicEvent topicEvent, String header, String tags, String body) {
    LOGGER.info("send message to --->> {}, event type: {}", topicEvent.getTo(),
        EventType.getByValue(topicEvent.getEventType()));
    this.insert(topicEvent, body);
    iMqProducer
        .sendMessage(gson.toJson(new DispatcherResponse(topicEvent.getTo(), topicEvent.getEventType(), header,
            TopicEventUtil.toJson(gson, topicEvent))), tags);

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
    List<TopicEvent> events = topicMapper.selectEvents(to, eventSeqId, pageSize);

    // 查询数据库中eventSeqId的最大值
    Long maxEventSeqId = topicMapper.selectLastEventSeqId(to);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = events.isEmpty() ? maxEventSeqId : events.get(events.size() - 1).getEventSeqId();

    Map<String, Map<String, TopicEvent>> allTopicMap = new HashMap<>(16);
    Map<String, Map<String, TopicEvent>> allReplyMap = new HashMap<>(16);
    List<TopicEvent> notifyEvents = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(gson);

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
          topicMap.put(UUID.randomUUID().toString(), event);
          break;
        case TOPIC_SESSION_DELETE:
          if (event.getDeleteAllMsg()) {
            topicMap.clear();
            replyMap.clear();
          }
          topicMap.put(UUID.randomUUID().toString(), event);
          break;
        default:
          // do nothing
      }
    });

    allTopicMap.values().forEach(map -> notifyEvents.addAll(map.values()));

    // 每个话题只返回最新一条回复消息
    allReplyMap.values().forEach(map -> {
      if (!map.isEmpty()) {
        List<TopicEvent> replies = new ArrayList<>(map.values());
        notifyEvents.add(replies.get(replies.size() - 1));
      }
    });

    // 给事件按照eventSeqId重新排序
    notifyEvents.sort(Comparator.comparing(TopicEvent::getEventSeqId));

    // 返回事件超过1000条，只返回最后一千条
    if (notifyEvents.size() > EventCondition.MAX_EVENT_RETURN_COUNT) {
      notifyEvents.subList(0, notifyEvents.size() - EventCondition.MAX_EVENT_RETURN_COUNT).clear();
    }

    // 将每个返回结果的extendParam合并到event中
    List<JsonElement> eventList = new ArrayList<>();
    notifyEvents
        .forEach(topicEvent -> eventList.add(new JsonParser().parse(TopicEventUtil.toJson(gson, topicEvent))));

    Map<String, Object> result = new HashMap<>(5);
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);
    result.put("maxEventSeqId", maxEventSeqId == null ? 0 : maxEventSeqId);
    result.put("events", eventList);
    return result;
  }

  /**
   * 事件拉取，限制最大条数
   */
  public Map<String, Object> getTopicEventsLimited(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull topic events limited called, to: {}, eventSeqId: {}, pageSize: {}", to, eventSeqId, pageSize);
    // 为pageSize配置默认值和最大值
    pageSize = pageSize == null || pageSize > config.defaultPageSize ? config.defaultPageSize : pageSize;
    return this.getTopicEvents(to, eventSeqId, pageSize);
  }
}
