package com.syswin.temail.notification.main.test;

import com.syswin.temail.notification.main.application.scheduler.NotificationEventSchedule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class NotificationScheduleControllerTest {

  @MockBean
  NotificationEventSchedule schedule;

  @Autowired
  private MockMvc mvc;

  @Test
  public void TestDeleteEvent() throws Exception {
    Mockito.doNothing().when(schedule).deleteOldEvent();
    mvc.perform(MockMvcRequestBuilders.delete("/notification/schedule/delete/event"))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void TestDeleteTopic() throws Exception {
    Mockito.doNothing().when(schedule).deleteOldTopic();
    mvc.perform(MockMvcRequestBuilders.delete("/notification/schedule/delete/topic"))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }
}
