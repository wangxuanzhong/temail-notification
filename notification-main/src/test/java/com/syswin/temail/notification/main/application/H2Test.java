package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("h2")
public class H2Test {

  Gson gson = new Gson();

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private EventRepository eventRepository;


  @Test
  public void testGetEvents() {

  }

  @Test
  public void testGetUnread() {
    // 单聊消息
    Event event = new Event("get_unread_1", 1L, "get_unread_aaaa", "get_unread_from", "get_unread_to", System.currentTimeMillis(), 0);
    event.setEventSeqId(1L);
    eventRepository.insert(event);
    List<UnreadResponse> result = notificationService.getUnread("get_unread_to");
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("get_unread_from");
    assertThat(result.get(0).getTo()).isEqualTo("get_unread_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event = new Event("get_unread_1", 1L, "get_unread_aaaa", "get_unread_group_temail", "get_unread_to", System.currentTimeMillis(),
        "get_unread_group_temail", null, null, null, 0);
    event.setEventSeqId(2L);
    eventRepository.insert(event);
    result = notificationService.getUnread("get_unread_to");
    assertThat(result).size().isEqualTo(2);
  }

  @Test
  public void testReset() {
    // 单聊消息
    Event event = new Event("reset_1", 1L, "aaaa", "reset_from", "reset_to", System.currentTimeMillis(), 0);
    event.setEventSeqId(1L);
    eventRepository.insert(event);
    List<UnreadResponse> result = notificationService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(1);
    assertThat(result.get(0).getFrom()).isEqualTo("reset_from");
    assertThat(result.get(0).getTo()).isEqualTo("reset_to");
    assertThat(result.get(0).getUnread()).isEqualTo(1);

    // 群聊消息
    event = new Event("reset_1", 1L, "aaaa", "reset_group_temail", "reset_to", System.currentTimeMillis(), "reset_group_temail", null, null, null, 0);
    event.setEventSeqId(2L);
    eventRepository.insert(event);
    result = notificationService.getUnread("reset_to");
    assertThat(result).size().isEqualTo(2);

    // 重置单聊消息
    event = new Event();
    event.setEventSeqId(3L);
    event.setFrom("reset_from");
    event.setTo("reset_to");
    notificationService.reset(event);

    result = notificationService.getUnread("reset_to");
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
    notificationService.reset(event);
    result = notificationService.getUnread("reset_to");
    assertThat(result).isEmpty();
  }

}
