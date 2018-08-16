package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.MailAgentParams;
import com.syswin.temail.notification.main.domains.UnreadResponse;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationServiceTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";
  private Gson gson = new Gson();


  @Autowired
  private NotificationService notificationService;

  @Autowired
  private RedisService redisService;

  @Test
  public void testHandleMqMessage() throws Exception {
    MailAgentParams mailAgentParams = new MailAgentParams();
    mailAgentParams.setHeader("header");
    mailAgentParams.setSessionMssageType(EventType.PULLED.getValue());
    mailAgentParams.setFrom(TEST_FROM);
    mailAgentParams.setTo(TEST_TO);
    mailAgentParams.setMsgid("1,2");
//    mailAgentParams.setMsgid("2");
//    mailAgentParams.setFromSeqNo(2L);
//    mailAgentParams.setToMsg("aaaaaaaa");
    mailAgentParams.setTimestamp((new Date()).getTime());
    notificationService.handleMqMessage(gson.toJson(mailAgentParams));
  }

  @Test
  public void testGetEvents() {
    long sequenceNo = 1L;
    List<Event> events = notificationService.getEvents(TEST_TO);
    System.out.println(gson.toJson(events));
    assertThat(events).isNotEmpty();
  }

  @Test
  public void testGetUnread() {
    List<UnreadResponse> result = notificationService.getUnread(TEST_TO);
    assertThat(result).isNotEmpty();
  }
}
