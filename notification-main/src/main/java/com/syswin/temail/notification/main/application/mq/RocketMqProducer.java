package com.syswin.temail.notification.main.application.mq;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.constants.Constant.ProducerGroup;
import com.syswin.temail.notification.main.exceptions.MqException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rocket mq 生产者
 *
 * @author liusen
 */
public class RocketMqProducer implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final DefaultMQProducer producer = new DefaultMQProducer(ProducerGroup.PRODUCER_GROUP);

  private String host;
  private String topic;

  public RocketMqProducer(String host, String topic) {
    this.host = host;
    this.topic = topic;
  }

  public DefaultMQProducer getProducer() {
    return producer;
  }

  /**
   * 初始化
   */
  @Override
  public void start() {
    LOGGER.info("MQ: start producer.");
    producer.setNamesrvAddr(host);
    producer.setInstanceName(UUID.randomUUID().toString());
    try {
      producer.start();
    } catch (MQClientException e) {
      throw new MqException("MQ start exception: ", e);
    }
  }

  /**
   * 发送消息
   */
  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    SendResult sendResult;
    try {
      Message mqMsg = new Message(topic, tags, keys, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
      LOGGER.info("MQ: send message: body={}, topic={}, tags={}, keys={}", body, topic, tags, keys);

      if (tags == null || tags.isEmpty()) {
        sendResult = producer.send(mqMsg);
      } else {
        sendResult = producer.send(mqMsg, (mqs, msg, arg) -> {
          int index = Math.abs(arg.hashCode() % mqs.size());
          return mqs.get(index);
        }, tags);
      }
    } catch (Exception e) {
      throw new MqException("MQ send message exception: ", e);
    }

    LOGGER.info("MQ: send result: {}", sendResult);
    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
      throw new MqException(sendResult.toString());
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
    producer.shutdown();
    LOGGER.info("MQ: stop producer.");
  }
}
