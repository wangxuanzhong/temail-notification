package com.syswin.temail.notification.main.application.mq;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "library.messaging.type=redis",
//    "app.temail.notification.mq.producer=rocketmq",
    "app.temail.notification.mq.consumer=rocketmq",
    "app.temail.notification.mq.producer=libraryMessage"
})
@ActiveProfiles("test")
@Ignore
public class RocketMqProducerTest {

  @Autowired
  private IMqProducer iMqProducer;
  @Value("${spring.rocketmq.topics.mailAgent.singleChat}")
  private String singleChatTopic;

  @Test
  public void testSendMessage() throws Exception {
    for (int i = 0; i < 10; i++) {
      iMqProducer.sendMessage("" + i, singleChatTopic, null, null);
    }
  }
}