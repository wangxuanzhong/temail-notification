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
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
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

    relationEvent.setMyVcardId("myVcardId");
    relationEvent.setOppositeId("");
    relationEvent.setRemark("");
    relationEvent.setStatus(1);
    relationEvent.setIsEmail(2);
    relationEvent.setContactType(3);
    relationEvent.setCreateTimeStp(1111L);
    relationEvent.setUpdateTimeStp(2222L);

    syncService.handleMqMessage(gson.toJson(relationEvent), "");
  }

  /**
   * EventType RELATION_DELETE 55 增加关系
   */
  @Test
  public void testEventTypeRelationDelete() {
    SyncRelationEvent relationEvent = new SyncRelationEvent();
    relationEvent.setxPacketId(UUID.randomUUID().toString());
    relationEvent.setEventType(EventType.RELATION_DELETE.getValue());
    relationEvent.setFrom("b");
    relationEvent.setTo("a");
    SyncEventUtil.initEventSeqId(redisServiceMock, relationEvent);
    relationEvent.setHeader(ConstantMock.HEADER);

    relationEvent.setMyVcardId("myVcardId");
    relationEvent.setOppositeId("");
    relationEvent.setRemark("");
    relationEvent.setStatus(1);
    relationEvent.setIsEmail(2);
    relationEvent.setContactType(3);
    relationEvent.setCreateTimeStp(1111L);
    relationEvent.setUpdateTimeStp(2222L);
    relationEvent.setDeleteList(Arrays.asList("J", "Q", "K"));

    syncService.handleMqMessage(gson.toJson(relationEvent), "");
  }
}