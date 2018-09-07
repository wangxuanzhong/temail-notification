package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentSingleChatParams;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Before;
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
  private final String TOPIC = "temail-usermail";
  MailAgentSingleChatParams params = new MailAgentSingleChatParams();
  private Gson gson = new Gson();
  @Autowired
  private RocketMqProducer rocketMqProducer;
  @Autowired
  private NotificationService notificationService;

  @Before
  public void setUp() {
    params.setHeader("header");
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
    params.setTimestamp((new Date()).getTime());
  }

  @Test
  public void testGetEvents() {
    long sequenceNo = 1L;
    Map<String, Object> events = notificationService.getEvents(TEST_TO, 0L, null);
    System.out.println(gson.toJson(events));
    assertThat(events).isNotEmpty();
  }

  @Test
  public void testGetUnread() {
    List<UnreadResponse> result = notificationService.getUnread(TEST_TO, 0L);
    assertThat(result).isNotEmpty();
  }

  @Test
  public void testEventTypeReceive() throws Exception {
    params.setSessionMssageType(EventType.RECEIVE.getValue());
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setToMsg("aaaaaaaa");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  @Test
  public void testEventTypePulled() throws Exception {
    params.setSessionMssageType(EventType.PULLED.getValue());
    params.setMsgid("1");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  @Test
  public void testEventTypeDestroy() throws Exception {
    params.setSessionMssageType(EventType.DESTROY.getValue());
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setToMsg("aaaaaaaa");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }
}
