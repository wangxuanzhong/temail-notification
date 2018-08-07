package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.syswin.temail.notification.main.domains.Notification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationServiceTest {

  @Autowired
  private NotificationService notificationService;

  private Notification role = new Notification();

  @Before
  public void setup() {

  }

  @Test
  public void TestSend() {
    int a = 1;
    assertThat(a);
  }
}
