package com.syswin.temail.notification.main.mock;

import com.syswin.temail.notification.main.application.rocketmq.RocketMqProducer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RocketMqProducerMock extends RocketMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  public RocketMqProducerMock(@Value("${spring.rocketmq.host}") String host,
      @Value("${spring.rocketmq.topics.notify}") String topic) {
    super(host, topic);
  }

  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    LOGGER.info("MQ: send message: {}", body);
    if (tags == null) {
      LOGGER.info("MQ: queue id is random");
    } else {
      LOGGER.info("MQ: queue id when queue size is 4: {}", Math.abs(tags.hashCode() % 4));
    }

  }

  @Override
  public void sendMessage(String body, String tags) {
    sendMessage(body, null, tags, "");
  }
}
