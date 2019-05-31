package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.application.NotificationRedisService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class NotificationUtilTest {

  @MockBean
  EventMapper eventMapper;

  @MockBean
  NotificationRedisService notificationRedisService;

  @Test
  public void testCheckUnique() {
    Event event = new Event();
    Assertions.assertThat(NotificationUtil.checkUnique(event, "key", eventMapper, notificationRedisService)).isFalse();

    event.setxPacketId(UUID.randomUUID().toString());
    Mockito.when(notificationRedisService.checkUnique(Mockito.anyString())).thenReturn(false);
    Assertions.assertThat(NotificationUtil.checkUnique(event, "key", eventMapper, notificationRedisService)).isFalse();

    Mockito.when(notificationRedisService.checkUnique(Mockito.anyString())).thenReturn(true);
    Mockito.when(eventMapper.checkUnique(Mockito.any(Event.class))).thenReturn(Collections.singletonList(event));
    Assertions.assertThat(NotificationUtil.checkUnique(event, "key", eventMapper, notificationRedisService)).isFalse();

    Mockito.when(eventMapper.checkUnique(Mockito.any(Event.class))).thenReturn(new ArrayList<>());
    Assertions.assertThat(NotificationUtil.checkUnique(event, "key", eventMapper, notificationRedisService)).isTrue();

  }
}