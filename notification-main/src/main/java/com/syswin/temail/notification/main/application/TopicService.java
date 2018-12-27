package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.Event;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY;

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
        for (TopicEvent event : topicEventRepository.selectEventsByMsgId(topicEvent.getMsgId())) {
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
        for (TopicEvent event : topicEventRepository.selectEventsByTopicId(topicEvent.getTopicId())) {
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
    LOGGER.info("send message to --->> {}, event type: {}", topicEvent.getTo(), EventType.getByValue(topicEvent.getEventType()));
    this.insert(topicEvent);
    rocketMqProducer.sendMessage(
        jsonService.toJson(new CDTPResponse(topicEvent.getTo(), topicEvent.getEventType(), header, jsonService.toJson(topicEvent))));

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

    // 获取当前最新eventSeqId
    Long lastEventSeqId = 0L;
    if (events.isEmpty()) {
      lastEventSeqId = topicEventRepository.selectLastEventSeqId(to, topicId);
    } else {
      lastEventSeqId = events.get(events.size() - 1).getEventSeqId();
    }

    Map<String, TopicEvent> eventMap = new HashMap<>();
    List<TopicEvent> notifyEvents = new ArrayList<>();
    String replyEventKey = "reply";
    Set<String> replyMsgIds = new HashSet<>();
    events.forEach(event -> {
      event.autoReadExtendParam(jsonService);
      //话题归档和取消归档事件不考虑离线消息提醒
        switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
          case TOPIC:
              eventMap.put(event.getMsgId(), event);
              break;
            case TOPIC_REPLY:
                //回复消息保留最新一条
                eventMap.put(replyEventKey, event);
                //记录所有的回复消息，用于与撤回的消息事件抵消
                replyMsgIds.add(event.getMsgId());
                break;
            case REPLY_RETRACT:
                //撤回的消息需要和回复消息被抵消，如果撤回的消息不在本次拉到的回复消息范围内，需要提醒
                if (replyMsgIds.add(event.getMsgId())) {
                    //这里set.add()返回true，说明这条撤回的消息，不再本次拉到的回复消息内，需要提醒客户端
                    eventMap.put(event.getMsgId(), event);
                } else if(event.getMsgId().equals(eventMap.get(replyEventKey).getMsgId())){
                    //若set.add()返回false，说明这条撤回的消息，在本次拉到的回复的消息内，需要做事件抵消。
                    // 未记录的回复消息事件，也不用记录它的撤回事件，最新一条回复消息，需要remove
                    eventMap.remove(replyEventKey);
                }
                break;
            case TOPIC_REPLY_DELETE:
                //删除的消息不考虑离线事件提醒，只有最新一条的回复消息被删除需要抵消'回复消息事件'的提醒
                for (String msgId :
                        event.getMsgIds()) {
                    if (eventMap.get(replyEventKey) != null && msgId.equals(eventMap.get(replyEventKey).getMsgId())) {
                        eventMap.remove(replyEventKey);
                        break;
                    }
                }
                break;
        case TOPIC_DELETE:
          eventMap.put(event.getTopicId(), event);
          break;
      }
    });
    notifyEvents.addAll(eventMap.values());
    notifyEvents.sort(Comparator.comparing(TopicEvent::getEventSeqId));

    Map<String, Object> result = new HashMap<>();
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);
    result.put("events", notifyEvents);
    // LOGGER.info("pull events result: {}", result);
    return result;
  }
}
