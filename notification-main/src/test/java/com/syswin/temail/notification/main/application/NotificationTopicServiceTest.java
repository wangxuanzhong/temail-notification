package com.syswin.temail.notification.main.application;

import static com.syswin.temail.notification.main.domains.EventType.TOPIC_REPLY;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceMock;
import java.util.Arrays;
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

  private MailAgentParams params = new MailAgentParams();
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
  private RedisServiceMock redisServiceMock = new RedisServiceMock();

  private NotificationTopicService notificationTopicService;

  @Before
  public void setUp() {
    if (!useMQ && isMock) {
      notificationTopicService = new NotificationTopicService(mqProducerMock, redisServiceMock, topicMapper, iJsonService);
    } else {
      notificationTopicService = new NotificationTopicService(iMqProducer, notificationRedisService, topicMapper, iJsonService);
    }

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
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

  /**
   * EventType TOPIC_SESSION_DELETE 39 话题会话删除
   */
  @Test
  public void topicSessionDeleteEventTest() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_SESSION_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setFrom("b");
    params.setTo(null);
    params.setDeleteAllMsg(true);
    this.sendMessage(params, params.getTopicId());
  }

  private void sendMessage(MailAgentParams param, String tags) throws Exception {
    sendMessage(param, false, tags);
  }

  private void sendMessage(MailAgentParams param, boolean isSamePacket, String tags) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
    }
    if (useMQ) {
      iMqProducer.sendMessage(gson.toJson(param), topic, tags, "");
      Thread.sleep(2000);
    } else {
      notificationTopicService.handleMqMessage(gson.toJson(param), tags);
    }
  }


}