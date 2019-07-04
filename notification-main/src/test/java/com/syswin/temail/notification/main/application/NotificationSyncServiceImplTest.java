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
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.SyncRelationEvent;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import com.syswin.temail.notification.main.util.SyncEventUtil;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class NotificationSyncServiceImplTest {

  private Gson gson = new Gson();

  @Value("${spring.rocketmq.topics.sync}")
  private String topic;

  @Autowired
  private IJsonService iJsonService;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private NotificationSyncServiceImpl syncService;

  @Before
  public void setUp() {
    syncService = new NotificationSyncServiceImpl(mqProducerMock, redisServiceMock, iJsonService);
  }


  /**
   * EventType RELATION_ADD 53 增加关系
   */
  @Test
  public void testEventTypeRelationAdd() {
    SyncRelationEvent relationEvent = new SyncRelationEvent();
    relationEvent.setxPacketId(UUID.randomUUID().toString());
    relationEvent.setEventType(EventType.RELATION_ADD.getValue());
    relationEvent.setFrom("b");
    relationEvent.setTo("a");
    SyncEventUtil.initEventSeqId(redisServiceMock, relationEvent);
    relationEvent.setHeader(ConstantMock.HEADER);

    relationEvent.setMyVcard("myVcard");
    relationEvent.setOppositeVcard("oppositeVcard");
    relationEvent.setRemark("remark");
    relationEvent.setStatus(1);
    relationEvent.setIsEmail(2);
    relationEvent.setContactType(3);
    relationEvent.setCreateTimeStp(1111L);
    relationEvent.setUpdateTimeStp(2222L);

    syncService.handleMqMessage(gson.toJson(relationEvent), "");
    syncService.handleMqMessage(gson.toJson(relationEvent), "");
  }

  /**
   * EventType RELATION_UPDATE 54 更新关系
   */
  @Test
  public void testEventTypeRelationUpdate() {
    SyncRelationEvent relationEvent = new SyncRelationEvent();
    relationEvent.setxPacketId(UUID.randomUUID().toString());
    relationEvent.setEventType(EventType.RELATION_UPDATE.getValue());
    relationEvent.setFrom("b");
    relationEvent.setTo("a");
    SyncEventUtil.initEventSeqId(redisServiceMock, relationEvent);
    relationEvent.setHeader(ConstantMock.HEADER);

    relationEvent.setMyVcard("myVcard");
    relationEvent.setOppositeVcard("oppositeVcard");
    relationEvent.setRemark("remark");
    relationEvent.setStatus(1);
    relationEvent.setIsEmail(2);
    relationEvent.setContactType(3);
    relationEvent.setCreateTimeStp(1111L);
    relationEvent.setUpdateTimeStp(2222L);

    syncService.handleMqMessage(gson.toJson(relationEvent), "");
  }

  /**
   * EventType RELATION_DELETE 55 删除关系
   */
  @Test
  public void testEventTypeRelationDelete() {
    SyncRelationEvent relationEvent = new SyncRelationEvent();
    relationEvent.setxPacketId(UUID.randomUUID().toString());
    relationEvent.setEventType(EventType.RELATION_DELETE.getValue());
//    relationEvent.setFrom("a");
    relationEvent.setTo("a");
    relationEvent.setCreateTimeStp(1111L);
    relationEvent.setUpdateTimeStp(2222L);
    relationEvent.setDeleteList(Arrays.asList("b", "c", "d"));
    SyncEventUtil.initEventSeqId(redisServiceMock, relationEvent);
    relationEvent.setHeader(ConstantMock.HEADER);
    syncService.handleMqMessage(gson.toJson(relationEvent), "");
  }
}