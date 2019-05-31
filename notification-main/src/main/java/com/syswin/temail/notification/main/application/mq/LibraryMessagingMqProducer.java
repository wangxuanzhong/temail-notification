package com.syswin.temail.notification.main.application.mq;

import com.syswin.library.messaging.MessagingException;
import com.syswin.library.messaging.MqProducer;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.exceptions.MqException;
import com.syswin.temail.notification.main.util.Constant;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryMessagingMqProducer implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<String, MqProducer> rocketMqProducers;
  private String topic;

  public LibraryMessagingMqProducer(Map<String, MqProducer> rocketMqProducers, String topic) {
    this.rocketMqProducers = rocketMqProducers;
    this.topic = topic;
  }

  @Override
  public void start() {
    // 启动不在此处实现
  }

  /**
   * 发送消息
   */
  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    MqProducer mqProducer = rocketMqProducers.get(Constant.PRODUCER_GROUP);
    if (mqProducer == null) {
      throw new MqException("mq producer is empty!");
    }

    try {
      LOGGER.info("MQ: send message: {}", body);
      mqProducer.send(body, topic, tags, null);
    } catch (UnsupportedEncodingException | InterruptedException | MessagingException e) {
      throw new MqException("MQ send message exception: ", e);
    }
  }

  /**
   * 发送消息，使用默认的topic，不使用keys
   */
  @Override
  public void sendMessage(String body, String tags) {
    sendMessage(body, topic, tags, "");
  }

  /**
   * 发送消息，使用默认的topic，不使用tags和keys
   */
  @Override
  public void sendMessage(String body) {
    sendMessage(body, topic, "", "");
  }

  @Override
  public void stop() {
    // 停止不在此处实现
  }

}
