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
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.dto.MailAgentParamsFull.TrashMsgInfo;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import com.syswin.temail.notification.main.util.GzipUtil;
import com.syswin.temail.notification.main.util.NotificationPacketUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
@ActiveProfiles("test") // mast be profile test
public class EventServiceTest {

  private static final String message = "aaa";
  private static final String from = "a";
  private static final String to = "b";
  private static final String groupTemail = "g";
  private Gson gson = new Gson();

  @MockBean
  private UnreadService unreadService;
  @MockBean
  private EventMapper eventMapper;
  @MockBean
  private UnreadMapper unreadMapper;
  @MockBean
  private MemberMapper memberMapper;
  @Autowired
  private NotificationConfig config;
  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();
  private EventService eventService;
  private NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();

  @Before
  public void setUp() {
    eventService = new EventService(unreadService, eventMapper, unreadMapper, memberMapper, mqProducerMock,
        redisServiceMock, config);
  }

  private Event initEvent() {
    Event event = new Event();
    event.setTimestamp(System.currentTimeMillis());
    event.setxPacketId(UUID.randomUUID().toString());
    return event;
  }

  /**
   * 查询结果为空
   */
  @Test
  public void TestGetEventsBranchEmpty() {
    List<Event> events = new ArrayList<>();
    Mockito.when(eventMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(events);
    List<Event> result = (List<Event>) eventService.getEventsLimited(to, 0L, null).get("events");
    Assertions.assertThat(result).isEmpty();
  }

  /**
   * 消息类事件，不返回
   */
  @Test
  public void TestGetEventsBranchMessage() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setMsgId("1");
    event.setMessage(message);
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setMsgId("2");
    event.setMessage(message);
    event.setFrom(to);
    event.setTo(to);
    event.setOwner(from);
    event.setEventSeqId(2L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();
  }

  /**
   * 包含msgId事件，直接返回
   */
  @Test
  public void TestGetEventsBranchWithMsgId() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.REPLY.getValue());
    event.setMsgId("2");
    event.setParentMsgId("1");
    event.setMessage(message);
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 撤回类事件，只有源消息不存在时才返回
   */
  @Test
  public void TestGetEventsBranchRetract() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.RETRACT.getValue());
    event.setMsgId("1");
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 回复撤回类事件，源消息存在时抵消，不存在时返回
   */
  @Test
  public void TestGetEventsBranchReplyRetract() {
    List<Event> events = new ArrayList<>();

    // 源消息不存在
    Event event = initEvent();
    event.setEventType(EventType.REPLY_RETRACT.getValue());
    event.setMsgId("2");
    event.setParentMsgId("1");
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();

    // 源消息存在
    events.clear();
    event.setEventType(EventType.REPLY.getValue());
    event.setMsgId("2");
    event.setParentMsgId("1");
    event.setMessage(message);
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event.setEventType(EventType.REPLY_RETRACT.getValue());
    event.setMsgId("2");
    event.setParentMsgId("1");
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(2L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();
  }

  /**
   * 不包含msgId事件，需要生成
   */
  @Test
  public void TestGetEventsBranchWithoutMsgId() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.APPLY.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(from);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 移动到废纸篓
   */
  @Test
  public void TestGetEventsBranchTrash() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.TRASH.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setEventSeqId(1L);
    event.setMsgIds(Arrays.asList("1", "2", "3"));
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();
  }

  /**
   * 移出废纸篓
   */
  @Test
  public void TestGetEventsBranchTrashCancel() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.TRASH.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setEventSeqId(1L);
    event.setMsgIds(Arrays.asList("1", "2", "3"));
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.TRASH_CANCEL.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setEventSeqId(2L);

    List<TrashMsgInfo> infos = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      infos.add(new TrashMsgInfo(from, to, String.valueOf(i)));
    }
    event.setTrashMsgInfo(gson.toJson(infos));
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 解散群
   */
  @Test
  public void TestGetEventsBranchDeleteGroup() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.DELETE_GROUP.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 离开群
   */
  @Test
  public void TestGetEventsBranchLeaveGroup() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.LEAVE_GROUP.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 有对立事件，需要做合并处理
   */
  @Test
  public void TestGetEventsBranchAgainst() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.APPLY_ADOPT.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(from);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();

    events.clear();

    event = initEvent();
    event.setEventType(EventType.APPLY.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.APPLY_ADOPT.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(2L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();
  }


  /**
   * 删除消息
   */
  @Test
  public void TestGetEventsBranchDelete() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setMsgId("1");
    event.setMessage(message);
    event.setFrom(from);
    event.setTo(to);
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.DELETE.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setMsgIds(Arrays.asList("1", "2", "3"));
    event.setEventSeqId(2L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.DELETE.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setDeleteAllMsg(true);
    event.setEventSeqId(3L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 删除回复消息
   */
  @Test
  public void TestGetEventsBranchReplyDelete() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.REPLY_DELETE.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setMsgIds(Arrays.asList("1", "2", "3"));
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();
  }

  /**
   * 添加管理员
   */
  @Test
  public void TestGetEventsBranchAddAdmin() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.ADD_ADMIN.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 移除管理员
   */
  @Test
  public void TestGetEventsBranchDeleteAdmin() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.ADD_ADMIN.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    event = initEvent();
    event.setEventType(EventType.DELETE_ADMIN.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(2L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isEmpty();

    event = initEvent();
    event.setEventType(EventType.DELETE_ADMIN.getValue());
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(to);
    event.setEventSeqId(3L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }

  /**
   * 报文事件
   */
  @Test
  public void TestGetEventsBranchPacket() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.PACKET.getValue());
    event.setFrom(from);
    event.setTo(to);
    event.setEventSeqId(1L);
    event.setZipPacket(GzipUtil.zipWithDecode(notificationPacketUtil.encodeData("test packet".getBytes())));
    event.autoWriteExtendParam(null);
    events.add(event);

    Assertions.assertThat(this.getEvents(events)).isNotEmpty();
  }


  /**
   * 拉取事件功能测试方法
   *
   * @param events 模拟数据库返回结果
   * @return 合并后的结果
   */
  private List<Event> getEvents(List<Event> events) {
    Mockito.when(eventMapper.selectEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(events);
    Mockito.when(eventMapper.selectLastEventSeqId(Mockito.anyString()))
        .thenReturn(events.get(events.size() - 1).getEventSeqId());
    List<Event> result = (List<Event>) eventService.getEventsLimited(to, 0L, null).get("events");
    System.out.println(result);
    return result;
  }


  @Test
  public void testReset() {
    String header = "header";
    Event event = new Event();
    event.setTo(to);
    event.setGroupTemail(groupTemail);

    Mockito.when(eventMapper.insert(Mockito.any(Event.class))).thenReturn(1);
    Mockito.when(eventMapper.selectResetEvents(Mockito.any(Event.class))).thenReturn(Arrays.asList(1L, 2L, 3l));
    Mockito.doNothing().when(eventMapper).delete(Mockito.anyList());

    eventService.reset(event, header);
  }


  @Test
  public void testGetUnread() {
    List<Event> events = new ArrayList<>();

    Event event = initEvent();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setMsgId("1");
    event.setMessage(message);
    event.setFrom(groupTemail);
    event.setTo(to);
    event.setGroupTemail(groupTemail);
    event.setTemail(from);
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(null);
    events.add(event);

    Mockito.when(unreadMapper.selectCount(Mockito.anyString()))
        .thenReturn(Collections.singletonList(new Unread(groupTemail, to, 2)));
    Mockito.when(eventMapper.selectPartEvents(Mockito.anyString(), Mockito.anyList())).thenReturn(events);

    List<UnreadResponse> result = eventService.getUnread(to);

    Assertions.assertThat(result).hasSize(1);
    Assertions.assertThat(result.get(0).getUnread()).isEqualTo(3);
  }

  @Test
  public void testUpdateGroupChatUserStatus() {
    String header = "header";
    Member member = new Member();
    member.setGroupTemail("some group");
    member.setTemail("some one");

    Mockito.doNothing().when(memberMapper).updateUserStatus(Mockito.any(Member.class));

    eventService.updateGroupChatUserStatus(member, UserStatus.NORMAL, header);
    eventService.updateGroupChatUserStatus(member, UserStatus.DO_NOT_DISTURB, header);
  }

  @Test
  public void testGetGroupChatUserStatus() {
    Mockito.when(memberMapper.selectUserStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(1);

    Map<String, Integer> result = eventService.getGroupChatUserStatus("some one", "some group");
    Assertions.assertThat(result.get("userStatus")).isEqualTo(1);
  }
}
