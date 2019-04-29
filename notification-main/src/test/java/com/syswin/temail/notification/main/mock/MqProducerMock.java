package com.syswin.temail.notification.main.mock;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqProducerMock implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String topic = "test";

  @Override
  public void start() {

  }

  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    LOGGER.info("MQ: body: {}", body);
    LOGGER.info("MQ: topic: {}", topic);
    LOGGER.info("MQ: tags: {}", tags);
    LOGGER.info("MQ: keys: {}", keys);
    if (tags == null || tags.isEmpty()) {
      LOGGER.info("MQ: queue id is random");
    } else {
      LOGGER.info("MQ: queue id when queue size is 4: {}", Math.abs(tags.hashCode() % 4));
    }

  }

  @Override
  public void sendMessage(String body, String tags) {
    sendMessage(body, topic, tags, "");
  }

  @Override
  public void sendMessage(String body) {
    sendMessage(body, topic, "", "");
  }

  @Override
  public void stop() {
  }
}
