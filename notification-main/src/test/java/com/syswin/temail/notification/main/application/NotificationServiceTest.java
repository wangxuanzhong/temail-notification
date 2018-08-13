package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.MailAgentParams;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationServiceTest {

  private final String TEST_FROM = "11111111";
  private final String TEST_TO = "00000000";
  private Gson gson = new Gson();


  @Autowired
  private NotificationService notificationService;

  @Autowired
  private RedisService redisService;

  @Test
  public void testHandleMqMessage() throws Exception {
    MailAgentParams mailAgentParams = new MailAgentParams();
    mailAgentParams.setSessionMssageType(EventType.RECEIVE.getValue());
    mailAgentParams.setFrom(TEST_FROM);
    mailAgentParams.setTo(TEST_TO);
    mailAgentParams.setMsgid("syswin-d675d0de-2d9d-4bbc-a82f-1acf8d2bc97b-1533888096291");
    mailAgentParams.setFromSeqNo(2222L);
    mailAgentParams.setToMsg("aaaaaaaa");
    notificationService.handleMqMessage(gson.toJson(mailAgentParams));
  }

  @Test
  public void testGetEvents() {
    long sequenceNo = 1L;
    Map<String, List<Event>> events = notificationService.getEvents(TEST_TO, sequenceNo);
    System.out.println(gson.toJson(events));
    assertThat(events).isNotEmpty();
  }
}
