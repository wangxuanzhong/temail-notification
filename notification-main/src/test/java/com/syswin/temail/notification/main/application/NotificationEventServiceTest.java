package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceMock;
import java.util.HashMap;
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
@ActiveProfiles("test")
public class NotificationEventServiceTest {

  private final boolean isMock = true;
  private Gson gson = new Gson();

  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private UnreadMapper unreadMapper;
  @Autowired
  private MemberMapper memberMapper;
  @Autowired
  private IJsonService iJsonService;
  @Autowired
  private IMqProducer iMqProducer;
  @Autowired
  private NotificationRedisService notificationRedisService;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceMock redisServiceMock = new RedisServiceMock();

  private NotificationEventService notificationEventService;

  @Before
  public void setUp() {
    if (isMock) {
      notificationEventService = new NotificationEventService(eventMapper, unreadMapper, memberMapper, iJsonService,
          mqProducerMock, redisServiceMock);
    } else {
      notificationEventService = new NotificationEventService(eventMapper, unreadMapper, memberMapper, iJsonService,
          iMqProducer, notificationRedisService);
    }
  }

  private Event initEvent() {
    Event event = new Event();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setSeqId(1L);
    event.setTimestamp(System.currentTimeMillis());
    event.setxPacketId(UUID.randomUUID().toString());
    return event;
  }


  @Test
  public void testGetEvents() {

  }

  @Test
  public void testGetUnread() {
    // 单聊消息
    Event event = initEvent();
    event.setMsgId("get_unread_1");
    event.setMessage("get_unread_aaaa");
    event.setFrom("get_unread_from");
    event.setTo("get_unread_to");
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    List<UnreadResponse> result = notificationEventService.getUnread("get_unread_to");
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("get_unread_from");
    assertThat(result.get(0).getTo()).isEqualTo("get_unread_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event.setFrom("get_unread_group_temail");
    event.setGroupTemail("get_unread_group_temail");
    event.setTemail("get_unread_from");
    event.setEventSeqId(2L);
    eventMapper.insert(event);

    result = notificationEventService.getUnread("get_unread_to");
    assertThat(result).size().isEqualTo(2);
  }

  @Test
  public void testReset() throws Exception {
    // 单聊消息
    Event event = initEvent();
    event.setMsgId("reset_1");
    event.setMessage("reset_aaaa");
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    List<UnreadResponse> result = notificationEventService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("reset_from");
    assertThat(result.get(0).getTo()).isEqualTo("reset_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event.setFrom("reset_group_temail");
    event.setGroupTemail("reset_group_temail");
    event.setTemail("reset_from");
    event.setEventSeqId(2L);
    eventMapper.insert(event);

    result = notificationEventService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(2);

    // 重置单聊消息
    event = new Event();
    event.setEventSeqId(3L);
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setxPacketId(UUID.randomUUID().toString());
    notificationEventService.reset(event, ConstantMock.HEADER);

    result = notificationEventService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("reset_group_temail");
    assertThat(result.get(0).getTo()).isEqualTo("reset_to");
    assertThat(result.get(0).getGroupTemail()).isEqualTo("reset_group_temail");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 重置群聊消息
    event = new Event();
    event.setEventSeqId(4L);
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setGroupTemail("reset_group_temail");
    event.setxPacketId(UUID.randomUUID().toString());
    notificationEventService.reset(event, ConstantMock.HEADER);
    result = notificationEventService.getUnread("reset_to");
    assertThat(result).isEmpty();
  }


  @Test
  public void testSavePacketEvent() {
    Event event = new Event();
    event.setPacket("test packet");

    Map<String, Object> header = new HashMap<>();
    header.put("sender", "a");
    header.put("receiver", "b");

    notificationEventService.savePacketEvent(event, gson.toJson(header), UUID.randomUUID().toString(), "B000");
  }
}
