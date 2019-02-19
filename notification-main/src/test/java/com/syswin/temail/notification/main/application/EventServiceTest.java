package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.application.rocketmq.RocketMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.RocketMqProducerMock;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventServiceTest {

  private final boolean isMock = true;

  @Autowired
  private ISequenceService iSequenceService;
  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private UnreadMapper unreadMapper;
  @Autowired
  private MemberMapper memberMapper;
  @Autowired
  private IJsonService iJsonService;
  @Autowired
  private RocketMqProducer rocketMqProducer;
  @Autowired
  private RocketMqProducerMock rocketMqProducerMock;

  private EventService eventService;

  public Event setUp() {
    if (isMock) {
      eventService = new EventService(iSequenceService, eventMapper, unreadMapper, memberMapper, iJsonService, rocketMqProducerMock);
    } else {
      eventService = new EventService(iSequenceService, eventMapper, unreadMapper, memberMapper, iJsonService, rocketMqProducer);
    }

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
    Event event = setUp();
    event.setMsgId("get_unread_1");
    event.setMessage("get_unread_aaaa");
    event.setFrom("get_unread_from");
    event.setTo("get_unread_to");
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    List<UnreadResponse> result = eventService.getUnread("get_unread_to");
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

    result = eventService.getUnread("get_unread_to");
    assertThat(result).size().isEqualTo(2);
  }

  @Test
  public void testReset() throws Exception {
    // 单聊消息
    Event event = setUp();
    event.setMsgId("reset_1");
    event.setMessage("reset_aaaa");
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    List<UnreadResponse> result = eventService.getUnread("reset_to");
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

    result = eventService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(2);

    // 重置单聊消息
    event = new Event();
    event.setEventSeqId(3L);
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setxPacketId(UUID.randomUUID().toString());
    eventService.reset(event, ConstantMock.HEADER);

    result = eventService.getUnread("reset_to");
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
    eventService.reset(event, ConstantMock.HEADER);
    result = eventService.getUnread("reset_to");
    assertThat(result).isEmpty();
  }

}
