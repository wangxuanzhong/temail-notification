package com.syswin.temail.notification.main.mock;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqProducerMock implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void start() {

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

  @Override
  public void sendMessage(String body) {

  }

  @Override
  public void stop() {
  }
}
