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

package com.syswin.temail.notification.main.application.scheduler;

import com.syswin.temail.notification.main.application.NotificationEventService;
import com.syswin.temail.notification.main.application.NotificationRedisServiceImpl;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
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
  private NotificationEventService eventService;

  @MockBean
  private NotificationRedisServiceImpl redisService;

  @Autowired
  private TopicMapper topicMapper;

  @Autowired
  private NotificationConfig config;

  private NotificationEventSchedule eventSchedule;

  @Before
  public void setUp() {
    config.deadline = -1;
    eventSchedule = new NotificationEventSchedule(eventMapper, unreadMapper, eventService, redisService, topicMapper,
        config);
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

    Mockito.when(redisService.checkLock(Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
        .thenReturn(true);

    eventSchedule.deleteOldEvent();
  }

  @Test
  public void testDeleteOldTopic() {
    eventSchedule.deleteOldTopic();
  }
}