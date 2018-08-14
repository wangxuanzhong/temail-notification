package com.syswin.temail.notification.main.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RocketMqProducerTest {

  @Autowired
  private RocketMqProducer rocketMqProducer;

  @Test
  public void testSendMessage() throws Exception {
    for (int i = 0; i < 10; i++) {
      rocketMqProducer.sendMessage("" + i, "test_tag", "test_key");
//      sleep(200);
    }
  }
}