package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class NotificationTopicServiceImplMockTest {

  private static final String message = "aaa";
  private static final String from = "a";
  private static final String to = "b";

  @Value("${spring.rocketmq.topics.mailAgent.topicChat}")
  private String topic;

  @MockBean
  private TopicMapper topicMapper;
  @Autowired
  private IJsonService iJsonService;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private NotificationTopicServiceImpl notificationTopicServiceImpl;

  @Before
  public void setUp() {
    notificationTopicServiceImpl = new NotificationTopicServiceImpl(mqProducerMock, redisServiceMock, topicMapper, iJsonService);
  }

  private TopicEvent initEvent() {
    TopicEvent topicEvent = new TopicEvent();
    topicEvent.setTimestamp(System.currentTimeMillis());
    topicEvent.setxPacketId(UUID.randomUUID().toString());
    return topicEvent;
  }

  /**
   * 查询结果为空
   */
  @Test
  public void TestGetEventsBranchEmpty() {
    List<TopicEvent> topicEvents = new ArrayList<>();
    Mockito.when(topicMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(topicEvents);
    List<TopicEvent> result = (List<TopicEvent>) notificationTopicServiceImpl.getTopicEventsLimited(to, 0L, null).get("events");
    Assertions.assertThat(result).isEmpty();
  }

  /**
   * 话题消息事件
   */
  @Test
  public void TestMessage() {
    List<TopicEvent> topicEvents = new ArrayList<>();

    TopicEvent topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId");
    topicEvent.setMessage(message);
    topicEvent.setEventSeqId(1L);
    topicEvent.setTitle("话题标题");
    topicEvent.setReceivers(Arrays.asList("b", "c", "d"));
    topicEvent.setCc(Arrays.asList("J", "Q", "K"));
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId2");
    topicEvent.setMessage(message);
    topicEvent.setEventSeqId(2L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    Assertions.assertThat(this.getEvents(topicEvents)).hasSize(2);
  }

  /**
   * 撤回事件
   */
  @Test
  public void TestRetract() {
    List<TopicEvent> topicEvents = new ArrayList<>();

    // 源消息不存在
    TopicEvent topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY_RETRACT.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId");
    topicEvent.setEventSeqId(1L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    Assertions.assertThat(this.getEvents(topicEvents)).hasSize(1);

    topicEvents.clear();

    // 源消息存在
    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId2");
    topicEvent.setMessage(message);
    topicEvent.setEventSeqId(2L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY_RETRACT.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId2");
    topicEvent.setEventSeqId(3L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    Assertions.assertThat(this.getEvents(topicEvents)).isEmpty();
  }

  /**
   * 删除事件
   */
  @Test
  public void TestDelete() {
    List<TopicEvent> topicEvents = new ArrayList<>();

    TopicEvent topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId");
    topicEvent.setMessage(message);
    topicEvent.setEventSeqId(1L);
    topicEvent.setTitle("话题标题");
    topicEvent.setReceivers(Arrays.asList("b", "c", "d"));
    topicEvent.setCc(Arrays.asList("J", "Q", "K"));
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgId("msgId2");
    topicEvent.setMessage(message);
    topicEvent.setEventSeqId(2L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_REPLY_DELETE.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setMsgIds(Collections.singletonList("msgId2"));
    topicEvent.setEventSeqId(3L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_DELETE.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setEventSeqId(4L);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    topicEvent = initEvent();
    topicEvent.setEventType(EventType.TOPIC_SESSION_DELETE.getValue());
    topicEvent.setFrom(from);
    topicEvent.setTo(to);
    topicEvent.setTopicId("topicId");
    topicEvent.setEventSeqId(5L);
    topicEvent.setDeleteAllMsg(true);
    topicEvent.autoWriteExtendParam(iJsonService);
    topicEvents.add(topicEvent);

    Assertions.assertThat(this.getEvents(topicEvents)).hasSize(1);
  }


  /**
   * 拉取事件功能测试方法
   *
   * @param topicEvents 模拟数据库返回结果
   * @return 合并后的结果
   */
  private List<TopicEvent> getEvents(List<TopicEvent> topicEvents) {
    Mockito.when(topicMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(topicEvents);
    Mockito.when(topicMapper.selectLastEventSeqId(Mockito.anyString())).thenReturn(topicEvents.get(topicEvents.size() - 1).getEventSeqId());
    List<TopicEvent> result = (List<TopicEvent>) notificationTopicServiceImpl.getTopicEventsLimited(to, 0L, null).get("events");
    System.out.println(result);
    return result;
  }
}