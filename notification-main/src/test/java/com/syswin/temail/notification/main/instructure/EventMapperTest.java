package com.syswin.temail.notification.main.instructure;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.main.Application;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Application.class)
@ActiveProfiles("test")
public class EventMapperTest {

  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private IJsonService iJsonService;

  @Test
  public void insertTest(){
    Event event = new Event();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setSeqId(1L);
    event.setTimestamp(System.currentTimeMillis());
    event.setxPacketId(UUID.randomUUID().toString());
    event.setMsgId("get_unread_1");
    event.setMessage("get_unread_aaaa");
    event.setFrom("get_unread_from");
    event.setTo("get_unread_to");
    event.setOwner(event.getTo());
    event.setEventSeqId(1L);
    event.autoWriteExtendParam(iJsonService);
    int count = eventMapper.insert(event);
    Assert.assertEquals(count,1);
  }

}
