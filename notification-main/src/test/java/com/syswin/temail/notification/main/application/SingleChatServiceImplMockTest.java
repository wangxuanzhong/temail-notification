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

import static com.syswin.temail.notification.main.constants.Constant.EventParams.UNREAD;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.dto.MailAgentParamsFull;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.util.EventUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SingleChatServiceImplMockTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";

  private MailAgentParamsFull params = new MailAgentParamsFull();
  private Gson gson = new Gson();

  @MockBean
  private UnreadService unreadService;
  @MockBean
  private IMqProducer iMqProducer;
  @MockBean
  private RedisServiceImpl redisService;
  @MockBean
  private EventMapper eventMapper;

  private SingleChatServiceImpl singleChatService;

  @Before
  public void setUp() {
    singleChatService = new SingleChatServiceImpl(unreadService, iMqProducer, redisService, eventMapper);

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
    params.setxPacketId(UUID.randomUUID().toString());
    params.setTimestamp(System.currentTimeMillis());
  }

  /**
   * EventType RECEIVE 0 消息发送
   */
  @Test
  public void testEventTypeReceive() {
    params.setSessionMessageType(EventType.RECEIVE.getValue());
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setToMsg("这是一条单聊测试消息！");
    params.setAuthor("a");
    params.setFilter(Arrays.asList("b", "c", "d"));
    params.setOwner(TEST_TO);
    params.setSessionExtData("SessionExtData");
    params.setFromNickName("发送方昵称");
    params.setFromGroupName("新群聊群昵称");

    Event event = this.mock();
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(gson.toJson(params));
    event.setUnread(unreadService.getPushUnread(event.getTo()).get(UNREAD));

    DispatcherResponse dispatcherResponse = new DispatcherResponse(event.getTo(), event.getEventType(),
        ConstantMock.HEADER, EventUtil.toJson(gson, event));

    singleChatService.handleMqMessage(gson.toJson(params), params.getFrom());

    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), params.getFrom());
  }


  /**
   * EventType DESTROYED 4 消息已删除
   */
  @Test
  public void testEventTypeDelete() {
    // 删除事件from和to与事件业务相反
    params.setFrom(TEST_TO);
    params.setTo(TEST_FROM);
    params.setSessionMessageType(EventType.DELETE.getValue());

    // 批量删除消息
    params.setMsgid(gson.toJson(Arrays.asList("2", "3", "4")));
    // 删除会话和消息
    params.setDeleteAllMsg(true);

    Event event = this.mock();
    event.setMsgIds(gson.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
    }.getType()));
    event.setMsgId(null);
    // from是操作人，to是会话另一方
    event.setFrom(params.getTo());
    event.setTo(params.getFrom());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(gson.toJson(params));

    DispatcherResponse dispatcherResponse = new DispatcherResponse(event.getTo(), event.getEventType(),
        ConstantMock.HEADER, EventUtil.toJson(gson, event));

    singleChatService.handleMqMessage(gson.toJson(params), params.getFrom());

    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), params.getFrom());
  }


  /**
   * EventType ARCHIVE 33 归档
   */
  @Test
  public void testEventTypeArchive() {
    // from和to相反
    params.setFrom(TEST_TO);
    params.setTo(TEST_FROM);
    params.setSessionMessageType(EventType.ARCHIVE.getValue());

    Event event = this.mock();
    // from是操作人，to是会话另一方
    event.setFrom(params.getTo());
    event.setTo(params.getFrom());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(gson.toJson(params));

    DispatcherResponse dispatcherResponse = new DispatcherResponse(event.getTo(), event.getEventType(),
        ConstantMock.HEADER, EventUtil.toJson(gson, event));

    singleChatService.handleMqMessage(gson.toJson(params), params.getFrom());

    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), params.getFrom());
  }


  private Event mock() {
    Mockito.when(redisService.checkUnique(Mockito.anyString())).thenReturn(true);
    Mockito.when(eventMapper.selectEventsByPacketIdAndEventType(Mockito.any(Event.class)))
        .thenReturn(new ArrayList<>());
    Mockito.when(redisService.getNextSeq(Mockito.anyString())).thenReturn(1L);

    MailAgentParams params = gson.fromJson(gson.toJson(this.params), MailAgentParams.class);
    Event event = gson.fromJson(gson.toJson(this.params), Event.class);
    EventUtil.copyMailAgentFieldToEvent(params, event);
    return event;
  }
}
