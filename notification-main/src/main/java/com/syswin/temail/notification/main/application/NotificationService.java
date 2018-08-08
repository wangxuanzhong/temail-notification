package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Notification;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;

  @Autowired
  public NotificationService(RocketMqProducer rocketMqProducer) {
    this.rocketMqProducer = rocketMqProducer;
  }

  public void sendMessage(String body) throws Exception {
    rocketMqProducer.sendMessage(body, "", "");
  }

  private Notification getData(String json) {
    return (new Gson()).fromJson(json, Notification.class);
  }
}
