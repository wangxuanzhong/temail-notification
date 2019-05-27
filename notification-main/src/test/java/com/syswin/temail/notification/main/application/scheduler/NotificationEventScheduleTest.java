package com.syswin.temail.notification.main.application.scheduler;

import com.syswin.temail.notification.main.application.NotificationEventService;
import com.syswin.temail.notification.main.application.NotificationRedisService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test") // mast be profile test
public class NotificationEventScheduleTest {

  @Autowired
  private EventMapper eventMapper;

  @Autowired
  private UnreadMapper unreadMapper;

  @Autowired
  private NotificationEventService notificationEventService;

  @MockBean
  private NotificationRedisService notificationRedisService;

  @Autowired
  private TopicMapper topicMapper;

  private int deadline = -1;

  private NotificationEventSchedule notificationEventSchedule;

  @Before
  public void setUp() {
    notificationEventSchedule = new NotificationEventSchedule(eventMapper, unreadMapper, notificationEventService, notificationRedisService,
        topicMapper, deadline);
  }

  @Test
  public void testDeleteOldEvent() {
    Event event = new Event();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setxPacketId(UUID.randomUUID().toString());
    event.setMsgId(UUID.randomUUID().toString());
    event.setFrom("from");
    event.setTo("to");
    event.setEventSeqId(1L);
    eventMapper.insert(event);

    unreadMapper.insert(new Unread("from", "to", 2));

    Mockito.when(notificationRedisService.checkLock(Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);

    notificationEventSchedule.deleteOldEvent();
  }

  @Test
  public void testDeleteOldTopic() {
    notificationEventSchedule.deleteOldTopic();
  }
}