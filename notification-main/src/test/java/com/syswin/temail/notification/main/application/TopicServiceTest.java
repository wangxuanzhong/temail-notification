package com.syswin.temail.notification.main.application;

import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY;
import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY_DELETE;
import static com.syswin.temail.notification.main.domains.EventType.TOPIC_RETRACT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.domains.params.MailAgentTopicParams;
import com.syswin.temail.notification.main.infrastructure.TopicEventMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("h2")
public class TopicServiceTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";
  private final String TOPIC = "temail-topic";
  private final String PREFIX = "temail-notification-";
  private final boolean useMQ = false;
  MailAgentTopicParams params = new MailAgentTopicParams();
  TopicEventMapper topicRepo = mock(TopicEventMapper.class);
  private Gson gson = new Gson();
  @Autowired
  private TopicService topicService;
  @Autowired
  private TopicService topicServiceMock;
  @Autowired
  private RocketMqProducer rocketMqProducer;
  @Autowired
  private JsonService jsonService;
  @Autowired
  private RedisService redisService;

  @Before
  public void setUp() {
    topicServiceMock = new TopicService(rocketMqProducer, redisService, topicRepo, jsonService);

    params.setHeader("notification-header");
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
    params.setTopicId("topic_2");
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setTopicSeqId(1L);
    params.setToMsg("这是一条话题测试消息！");
    params.setTitle("话题标题");
    params.setReceivers(Arrays.asList("b", "c", "d"));
    params.setCc(Arrays.asList("J", "Q", "K"));
    params.setTo("b");
    this.sendMessage(params);
    params.setTo("c");
    this.sendMessage(params);
    params.setTo("d");
    this.sendMessage(params);

    params.setTo("a");
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_REPLY 22 话题回复消息发送
   */
  @Test
  public void testEventTypeTopicReply() throws Exception {
    params.setSessionMessageType(TOPIC_REPLY.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("3");
    params.setSeqNo(2L);
    params.setToMsg("这是一条话题回复测试消息！");
    params.setTo("b");
    this.sendMessage(params);
    params.setTo("c");
    this.sendMessage(params);
    params.setTo("d");
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_RETRACT 23 话题回复消息撤回
   */
  @Test
  public void testEventTypeTopicRetract() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_RETRACT.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("3");
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_REPLY_DELETE 24 话题消息删除
   */
  @Test
  public void testEventTypeTopicReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_REPLY_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setMsgid(gson.toJson(Arrays.asList("22", "3", "43")));
    params.setFrom(TEST_TO);
    params.setTo(TEST_FROM);
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_DELETE 25 话题删除
   */
  @Test
  public void testEventTypeTopicDelete() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setTo(TEST_FROM);
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_ARCHIVE 29 话题归档
   */
  @Test
  public void testEventTypeTopicArchive() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_ARCHIVE_CANCEL 30 话题归档取消
   */
  @Test
  public void testEventTypeTopicArchiveCancel() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE_CANCEL.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params);
  }

  private void sendMessage(MailAgentTopicParams param) throws Exception {
    sendMessage(param, false);
  }

  private void sendMessage(MailAgentTopicParams param, boolean isSamePacket) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(PREFIX + UUID.randomUUID().toString());
    }
    if (useMQ) {
      rocketMqProducer.sendMessage(gson.toJson(param), TOPIC, "", "");
      Thread.sleep(2000);
    } else {
      topicService.handleMqMessage(gson.toJson(param));
    }
  }

  // mock测试部分
  @Test
  public void shouldGetNoneWhenTopicSilence() {
    when(topicRepo.selectEvents("a@t.email", 10L, null)).thenReturn(new ArrayList<>());
    when(topicRepo.selectLastEventSeqId("a@t.email")).thenReturn(10L);
    Map<String, Object> resultMap = topicServiceMock.getTopicEvents("a@t.email", 10L, null);

    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(resultMap.get("events")).isEqualTo(new ArrayList<>());
  }

  @Test
  public void shouldOffEventWhenReplyRetract() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(redisService);
    TopicEvent reply2 = new TopicEvent(2L, "x_packet_id2", TOPIC_REPLY.getValue(), "topicId", "msgid2", "c@t.email", "b@t.email", "{}", 124L);
    reply2.initTopicEventSeqId(redisService);
    TopicEvent retract1 = new TopicEvent(3L, "x_packet_id3", TOPIC_RETRACT.getValue(), "topicId", "msgidx", "d@t.email", "b@t.email", "{}", 125L);
    retract1.initTopicEventSeqId(redisService);
    TopicEvent retract2 = new TopicEvent(4L, "x_packet_id4", TOPIC_RETRACT.getValue(), "topicId", "msgid2", "e@t.email", "b@t.email", "{}", 126L);
    retract2.initTopicEventSeqId(redisService);
    when(topicRepo.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, reply2, retract1, retract2));

    Map<String, Object> resultMap = topicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(((List<TopicEvent>) resultMap.get("events")).contains(retract1)).isTrue();
  }

  @Test
  public void shouldOffEventWhenReplyDelete() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(redisService);
    TopicEvent delete1 = new TopicEvent(4L, "x_packet_id4", TOPIC_REPLY_DELETE.getValue(), "topicId", "", "e@t.email", "b@t.email",
        "{\"msgIds\":[\"msgidx\", \"msgid1\"]}", 126L);

    delete1.initTopicEventSeqId(redisService);
    when(topicRepo.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, delete1));

    Map<String, Object> resultMap = topicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(resultMap.get("events")).isEqualTo(new ArrayList<>());
  }

  @Test
  public void shouldReturnLastedReplyWhenManyReplyExist() {
    TopicEvent reply1 = new TopicEvent(1L, "x_packet_id1", TOPIC_REPLY.getValue(), "topicId", "msgid1", "a@t.email", "b@t.email", "{}", 123L);
    reply1.initTopicEventSeqId(redisService);
    TopicEvent reply2 = new TopicEvent(2L, "x_packet_id2", TOPIC_REPLY.getValue(), "topicId", "msgid2", "c@t.email", "b@t.email", "{}", 124L);
    reply2.initTopicEventSeqId(redisService);
    when(topicRepo.selectEvents("b@t.email", 1L, null)).thenReturn(Arrays.asList(reply1, reply2));

    Map<String, Object> resultMap = topicServiceMock.getTopicEvents("b@t.email", 1L, null);
    assertThat(resultMap).isNotEmpty();
    assertThat(resultMap).containsKeys("events");
    assertThat(((List<TopicEvent>) resultMap.get("events")).contains(reply2)).isTrue();
  }
}