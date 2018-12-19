package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentTopicParams;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TopicServiceTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";
  private final String TOPIC = "temail-topic";
  private final String PREFIX = "temail-notification-";
  private final boolean useMQ = false;
  MailAgentTopicParams params = new MailAgentTopicParams();
  private Gson gson = new Gson();
  @Autowired
  private TopicService topicService;
  @Autowired
  private RocketMqProducer rocketMqProducer;

  @Before
  public void setUp() {
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
    params.setTopicId("topic_1");
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
    params.setSessionMessageType(EventType.TOPIC_REPLY.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("2");
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
    params.setMsgid("1");
    this.sendMessage(params);
  }

  /**
   * EventType TOPIC_REPLY_DELETE 24 话题消息删除
   */
  @Test
  public void testEventTypeTopicReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.TOPIC_REPLY_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setMsgid(gson.toJson(Arrays.asList("2", "3", "4")));
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
}