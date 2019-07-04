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

package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.application.NotificationRedisServiceImpl;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class EventUtilTest {

  @MockBean
  EventMapper eventMapper;

  @MockBean
  NotificationRedisServiceImpl redisService;

  @Test
  public void testCheckUnique() {
    Event event = new Event();
    Assertions.assertThat(EventUtil.checkUnique(event, "key", eventMapper, redisService)).isFalse();

    event.setxPacketId(UUID.randomUUID().toString());
    Mockito.when(redisService.checkUnique(Mockito.anyString())).thenReturn(false);
    Assertions.assertThat(EventUtil.checkUnique(event, "key", eventMapper, redisService)).isFalse();

    Mockito.when(redisService.checkUnique(Mockito.anyString())).thenReturn(true);
    Mockito.when(eventMapper.selectEventsByPacketIdAndEventType(Mockito.any(Event.class))).thenReturn(Collections.singletonList(event));
    Assertions.assertThat(EventUtil.checkUnique(event, "key", eventMapper, redisService)).isFalse();

    Mockito.when(eventMapper.selectEventsByPacketIdAndEventType(Mockito.any(Event.class))).thenReturn(new ArrayList<>());
    Assertions.assertThat(EventUtil.checkUnique(event, "key", eventMapper, redisService)).isTrue();

  }
}