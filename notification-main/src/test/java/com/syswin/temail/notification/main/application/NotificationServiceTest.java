package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Before
  public void setup() {

  }

  @Test
  public void TestSendMessage() throws Exception {
    notificationService.sendMessage("测试消息");
    assertThat(1);
  }
}
