package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import java.util.Arrays;
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

  private final String TEST_FROM = "11111111";
  private final String TEST_TO = "00000000";
  private Gson gson = new Gson();

  private Event event;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private RedisService redisService;

  @Before
  public void setUp() {
    event = new Event();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setFrom(TEST_FROM);
    event.setTo(TEST_TO);
    event.setMessageId(12345678L);
    event.setMessage("aaaaaaaa");
  }

  @Test
  public void testBatchInsert() {
    event.setSequenceNo(redisService.getNextSeq(TEST_TO));
    notificationService.batchInsert(Arrays.asList(event));
  }

  @Test
  public void testSendMqMessage() throws Exception {
    notificationService.sendMqMessage(gson.toJson(event));
//    notificationService.sendMqMessage("a");
  }

  @Test
  public void testGetEvents() {
    long sequenceNo = 1L;
    Map<String, List<Event>> events = notificationService.getEvents(TEST_TO, sequenceNo);
    System.out.println(gson.toJson(events));
    assertThat(events).isNotEmpty();
  }

  @Test
  public void testHandleMqMessage() throws Exception {
    notificationService.handleMqMessage(gson.toJson(event));
  }
}
