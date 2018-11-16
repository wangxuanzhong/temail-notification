package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
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
@ActiveProfiles("h2")
public class EventServiceTest {

  Gson gson = new Gson();

  @Autowired
  private EventService eventService;

  @Autowired
  private EventRepository eventRepository;

  public Event setUp() {
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
    event.setEventSeqId(1L);
    eventRepository.insert(event);

    List<UnreadResponse> result = eventService.getUnread("get_unread_to", null);
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("get_unread_from");
    assertThat(result.get(0).getTo()).isEqualTo("get_unread_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event.setFrom("get_unread_group_temail");
    event.setGroupTemail("get_unread_group_temail");
    event.setTemail("get_unread_to");
    event.setEventSeqId(2L);
    eventRepository.insert(event);

    result = eventService.getUnread("get_unread_to", null);
    assertThat(result).size().isEqualTo(2);
  }

  @Test
  public void testReset() {
    // 单聊消息
    Event event = setUp();
    event.setMsgId("reset_1");
    event.setMessage("reset_aaaa");
    event.setFrom("reset_from");
    event.setTo("reset_to");
    event.setEventSeqId(1L);
    eventRepository.insert(event);

    List<UnreadResponse> result = eventService.getUnread("reset_to", null);
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("reset_from");
    assertThat(result.get(0).getTo()).isEqualTo("reset_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event.setFrom("reset_group_temail");
    event.setGroupTemail("reset_group_temail");
    event.setTemail("reset_to");
    event.setEventSeqId(2L);
    eventRepository.insert(event);

    result = eventService.getUnread("reset_to", null);
    assertThat(result).size().isEqualTo(2);

    // 重置单聊消息
    event = new Event();
    event.setEventSeqId(3L);
    event.setFrom("reset_from");
    event.setTo("reset_to");
    eventService.reset(event);

    result = eventService.getUnread("reset_to", null);
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
    eventService.reset(event);
    result = eventService.getUnread("reset_to", null);
    assertThat(result).isEmpty();
  }

}
