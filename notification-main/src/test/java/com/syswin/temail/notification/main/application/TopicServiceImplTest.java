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
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import java.util.Arrays;
import java.util.Base64;
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
public class TopicServiceImplTest {

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
  private RedisServiceImpl redisService;
  @Autowired
  private TopicMapper topicMapper;
  @Autowired
  private IJsonService iJsonService;
  @Autowired
  private NotificationConfig config;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private TopicServiceImpl topicService;

  @Before
  public void setUp() {
    if (!useMQ && isMock) {
      topicService = new TopicServiceImpl(mqProducerMock, redisServiceMock, topicMapper, iJsonService,
          config);
    } else {
      topicService = new TopicServiceImpl(iMqProducer, redisService, topicMapper, iJsonService, config);
    }

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
  }

  /**
   * EventType TOPIC 21 话题消息发送
   */
  @Test
  public void testEventTypeTopic() {
    params.setSessionMessageType(EventType.TOPIC.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setTopicSeqId(1L);
    params.setToMsg(Base64.getUrlEncoder().encodeToString("这是一条话题测试消息！".getBytes()));
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
  public void testEventTypeTopicReply() {
    params.setSessionMessageType(EventType.TOPIC_REPLY.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("2");
    params.setSeqNo(2L);
    params.setToMsg(Base64.getUrlEncoder().encodeToString("这是一条话题回复测试消息！".getBytes()));
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
  public void testEventTypeTopicRetract() {
    params.setSessionMessageType(EventType.TOPIC_REPLY_RETRACT.getValue());
    params.setTopicId("topic_1");
    params.setMsgid("1");
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_REPLY_DELETE 24 话题回复消息删除
   */
  @Test
  public void testEventTypeTopicReplyDelete() {
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
  public void testEventTypeTopicDelete() {
    params.setSessionMessageType(EventType.TOPIC_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setTo(TEST_FROM);
    this.sendMessage(params, params.getTopicId());
  }

  /**
   * EventType TOPIC_ARCHIVE 29 话题归档
   */
  @Test
  public void testEventTypeTopicArchive() {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType TOPIC_ARCHIVE_CANCEL 30 话题归档取消
   */
  @Test
  public void testEventTypeTopicArchiveCancel() {
    params.setSessionMessageType(EventType.TOPIC_ARCHIVE_CANCEL.getValue());
    params.setTopicId("topic_1");
    params.setTo(null);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType TOPIC_SESSION_DELETE 39 话题会话删除
   */
  @Test
  public void testEventTypeTopicSessionDelete() {
    params.setSessionMessageType(EventType.TOPIC_SESSION_DELETE.getValue());
    params.setTopicId("topic_1");
    params.setFrom("a");
    params.setTo(null);
    params.setDeleteAllMsg(true);
    this.sendMessage(params, params.getFrom());
  }

  private void sendMessage(MailAgentParams param, String tags) {
    sendMessage(param, false, tags);
  }

  private void sendMessage(MailAgentParams param, boolean isSamePacket, String tags) {
    if (!isSamePacket) {
      param.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
    }
    if (useMQ) {
      iMqProducer.sendMessage(gson.toJson(param), topic, tags, "");
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Message Body：" + gson.toJson(param));
      System.out.println("Tag：" + tags);
      topicService.handleMqMessage(gson.toJson(param), tags);
    }
  }
}