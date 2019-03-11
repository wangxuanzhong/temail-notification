package com.syswin.temail.notification.main.application;

import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY;
import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY_DELETE;
import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY_RETRACT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.params.MailAgentTopicParams;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class NotificationTopicServiceTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";

  private final boolean useMQ = false;
  private final boolean isMock = true;

  private MailAgentTopicParams params = new MailAgentTopicParams();
  private Gson gson = new Gson();

  @Value("${spring.rocketmq.topics.mailAgent.topicChat}")
  private String topic;

  @Autowired
  private IMqProducer iMqProducer;
  @Autowired
  private NotificationRedisService notificationRedisService;
  @Autowired
  private TopicMapper topicMapper;
  @Autowired
  private IJsonService iJsonService;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private TopicMapper TopicMapperMock = mock(TopicMapper.class);

  private NotificationTopicService notificationTopicService;
  private NotificationTopicService notificationTopicServiceMock;

  @Before
  public void setUp() {
    if (isMock) {
      notificationTopicService = new NotificationTopicService(mqProducerMock, notificationRedisService, topicMapper, iJsonService);
    } else {
      notificationTopicService = new NotificationTopicService(iMqProducer, notificationRedisService, topicMapper, iJsonService);
    }
    notificationTopicServiceMock = new NotificationTopicService(iMqProducer, notificationRedisService, TopicMapperMock,
        iJsonService);

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
    params.setTimestamp(System.currentTimeMillis());
  }

  /**
   * EventType TOPIC 21 话题消息发送
   */
  @Test
  public void testEventTypeTopic() throws Exception {
    params.setSessionMessageType(EventType.TOPIC.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setTopicSeqId(1L);
    params.setToMsg("这是一条话题测试消息！");
    params.setTitle("话题标题");
    params.setReceivers(Arrays.asList("b", "c", "d"));
    params.setCc(Arrays.asList("J", "Q", "K"));
    params.setTo("b");
    this.sendMessage(params, params.getTopicId());
    params.setTo("c");
    this.sendMessage(params, params.getTopicId());
    params.setTo("d");
    this.sendMessage(params, params.getTopicId());

    params.setTo("a");
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_REPLY 22 话题回复消息发送
   */
  @Test
  public void testEventTypeTopicReply() throws Exception {
    params.setSessionMessageType(TOPIC_REPLY.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("2");
    params.setSeqNo(2L);
    params.setToMsg("这是一条话题回复测试消息！");
    params.setTo("b");
    this.sendMessage(params, params.getTopicId());
    params.setTo("c");
    this.sendMessage(params, params.getTopicId());
    params.setTo("d");
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_REPLY_RETRACT 23 话题回复消息撤回
   */
  @Test
  public void testEventTypeTopicRetract() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_REPLY_RETRACT.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("1");
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_REPLY_DELETE 24 话题回复消息删除
   */
  @Test
  public void testEventTypeTopicReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_REPLY_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setMsgid(gson.toJson(Arrays.asList("22", "3", "43")));
    params.setFrom(TEST_TO);
    params.setTo(TEST_FROM);
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_DELETE 25 话题删除
   */
  @Test
  public void testEventTypeTopicDelete() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setTo(TEST_FROM);
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_ARCHIVE 29 话题归档
   */
  @Test
  public void testEventTypeTopicArchive() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType TOPIC_ARCHIVE_CANCEL 30 话题归档取消
   */
  @Test
  public void testEventTypeTopicArchiveCancel() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE_CANCEL.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params, params.getFrom());
  }

  private void sendMessage(MailAgentTopicParams param, String tags) throws Exception {
    sendMessage(param, false, tags);
  }

  private void sendMessage(MailAgentTopicParams param, boolean isSamePacket, String tags) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
    }
    if (!isMock && useMQ) {
      iMqProducer.sendMessage(gson.toJson(param), topic, tags, "");
      Thread.sleep(2000);
    } else {
      notificationTopicService.handleMqMessage(gson.toJson(param), tags);
    }
  }

  // mock测试部分
  @Test
  public void shouldGetNoneWhenTopicSilence() {
    when(TopicMapperMock.selectEvents("a@t.email", 10L, null)).thenReturn(new ArrayList<>());
    when(TopicMapperMock.selectLastEventSeqId("a@t.email")).thenReturn(10L);
    Map<String, Object> resultMap = notificationTopicServiceMock.getTopicEvents("a@t.email", 10L, null);

    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(resultMap.get("events")).isEqualTo(new ArrayList<>());
  }

  @Test
  public void shouldOffEventWhenReplyRetract() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(notificationRedisService);
    TopicEvent reply2 = new TopicEvent(2L, "x_packet_id2", TOPIC_REPLY.getValue(), "topicId", "msgid2", "c@t.email", "b@t.email", "{}", 124L);
    reply2.initTopicEventSeqId(notificationRedisService);
    TopicEvent retract1 = new TopicEvent(3L, "x_packet_id3", TOPIC_REPLY_RETRACT.getValue(), "topicId", "msgidx", "d@t.email", "b@t.email", "{}",
        125L);
    retract1.initTopicEventSeqId(notificationRedisService);
    TopicEvent retract2 = new TopicEvent(4L, "x_packet_id4", TOPIC_REPLY_RETRACT.getValue(), "topicId", "msgid2", "e@t.email", "b@t.email", "{}",
        126L);
    retract2.initTopicEventSeqId(notificationRedisService);
    when(TopicMapperMock.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, reply2, retract1, retract2));

    Map<String, Object> resultMap = notificationTopicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(((List<TopicEvent>) resultMap.get("events")).contains(retract1)).isTrue();
  }

  @Test
  public void shouldOffEventWhenReplyDelete() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(notificationRedisService);
    TopicEvent delete1 = new TopicEvent(4L, "x_packet_id4", TOPIC_REPLY_DELETE.getValue(), "topicId", "", "e@t.email", "b@t.email",
        "{\"msgIds\":[\"msgidx\", \"msgid1\"]}", 126L);

    delete1.initTopicEventSeqId(notificationRedisService);
    when(TopicMapperMock.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, delete1));

    Map<String, Object> resultMap = notificationTopicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(resultMap.get("events")).isEqualTo(new ArrayList<>());
  }

  @Test
  public void shouldReturnLastedReplyWhenManyReplyExist() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(notificationRedisService);
    TopicEvent reply2 = new TopicEvent(2L, "x_packet_id2", TOPIC_REPLY.getValue(), "topicId", "msgid2", "c@t.email", "b@t.email", "{}", 124L);
    reply2.initTopicEventSeqId(notificationRedisService);
    when(TopicMapperMock.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, reply2));

    Map<String, Object> resultMap = notificationTopicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(((List<TopicEvent>) resultMap.get("events")).contains(reply2)).isTrue();
  }

  @Test
  public void topicSessionDeleteEventTest() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_SESSION_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setFrom("b");
    params.setTo(null);
    params.setDeleteAllMsg(true);
    this.sendMessage(params, params.getTopicId());
  }

}