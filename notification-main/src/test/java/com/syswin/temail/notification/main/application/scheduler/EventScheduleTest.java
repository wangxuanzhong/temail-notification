package com.syswin.temail.notification.main.application.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventScheduleTest {

  @Autowired
  private EventSchedule eventSchedule;

  @Test
  public void testDeleteOldEvent() {
    eventSchedule.deleteOldEvent();
  }

  @Test
  public void testDeleteOldTopic() {
    eventSchedule.deleteOldTopic();
  }
}