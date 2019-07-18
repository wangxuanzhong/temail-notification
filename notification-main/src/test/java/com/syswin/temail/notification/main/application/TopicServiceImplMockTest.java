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
import com.syswin.temail.notification.main.domains.TopicEvent;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import com.syswin.temail.notification.main.util.TopicEventUtil;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TopicServiceImplMockTest {

  private static final String message = "aaa";
  private static final String from = "a";
  private static final String to = "b";

  private MailAgentParams params = new MailAgentParams();
  private Gson gson = new Gson();

  @MockBean
  private TopicMapper topicMapper;
  @MockBean
  private IMqProducer iMqProducer;
  @MockBean
  private RedisServiceImpl redisService;
  @Autowired
  private IJsonService iJsonService;
  @Autowired
  private NotificationConfig config;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private TopicServiceImpl topicServiceForGet;
  private TopicServiceImpl topicServiceForHandle;

  @Before
  public void setUp() {
    topicServiceForGet = new TopicServiceImpl(mqProducerMock, redisServiceMock, topicMapper, iJsonService,
        config);
    topicServiceForHandle = new TopicServiceImpl(iMqProducer, redisService, topicMapper, iJsonService,
        config);

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(from);
    params.setTo(to);
    params.setxPacketId(UUID.randomUUID().toString());
    params.setTimestamp(System.currentTimeMillis());
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
    Mockito.when(topicMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt()))
        .thenReturn(topicEvents);
    List<TopicEvent> result = (List<TopicEvent>) topicServiceForGet.getTopicEventsLimited(to, 0L, null).get("events");
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
    Mockito.when(topicMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt()))
        .thenReturn(topicEvents);
    Mockito.when(topicMapper.selectLastEventSeqId(Mockito.anyString()))
        .thenReturn(topicEvents.get(topicEvents.size() - 1).getEventSeqId());
    List<TopicEvent> result = (List<TopicEvent>) topicServiceForGet.getTopicEventsLimited(to, 0L, null).get("events");
    System.out.println(result);
    return result;
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
    params.setToMsg("这是一条话题测试消息！");
    params.setTitle("话题标题");
    params.setReceivers(Arrays.asList("b", "c", "d"));
    params.setCc(Arrays.asList("J", "Q", "K"));
    params.setTo("b");

    TopicEvent topicEvent = this.mock();
    topicEvent.setTitle(params.getTitle());
    topicEvent.setReceivers(params.getReceivers());
    topicEvent.setCc(params.getCc());
    topicEvent.setTopicSeqId(params.getTopicSeqId());
    topicEvent.setEventSeqId(1L);
    topicEvent.autoWriteExtendParam(iJsonService);

    DispatcherResponse dispatcherResponse = new DispatcherResponse(topicEvent.getTo(), topicEvent.getEventType(),
        ConstantMock.HEADER,
        TopicEventUtil.toJson(iJsonService, topicEvent));

    topicServiceForHandle.handleMqMessage(gson.toJson(params), params.getTopicId());

    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), params.getTopicId());
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
    params.setToMsg("这是一条话题回复测试消息！");
    params.setTo("b");

    TopicEvent topicEvent = this.mock();
    topicEvent.setEventSeqId(1L);
    topicEvent.autoWriteExtendParam(iJsonService);

    DispatcherResponse dispatcherResponse = new DispatcherResponse(topicEvent.getTo(), topicEvent.getEventType(),
        ConstantMock.HEADER,
        TopicEventUtil.toJson(iJsonService, topicEvent));

    topicServiceForHandle.handleMqMessage(gson.toJson(params), params.getTopicId());

    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), params.getTopicId());

  }

  private TopicEvent mock() {
    Mockito.when(redisService.getNextSeq(Mockito.anyString())).thenReturn(1L);

    return new TopicEvent(params.getxPacketId(), params.getSessionMessageType(), params.getTopicId(),
        params.getMsgid(), params.getSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(),
        params.getTimestamp());
  }
}