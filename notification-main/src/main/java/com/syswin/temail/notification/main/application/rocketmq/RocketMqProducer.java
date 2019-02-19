package com.syswin.temail.notification.main.application.rocketmq;

import com.syswin.temail.notification.main.exceptions.SendMqMessageException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RocketMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final DefaultMQProducer producer = new DefaultMQProducer("notificationProducer");

  private String host;
  private String topic;

  public RocketMqProducer(@Value("${spring.rocketmq.host}") String host,
      @Value("${spring.rocketmq.topics.notify}") String topic) {
    this.host = host;
    this.topic = topic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ: start producer.");
    producer.setNamesrvAddr(host);
    producer.setInstanceName(UUID.randomUUID().toString());
    producer.start();
  }

  /**
   * 发送消息
   */
  public void sendMessage(String body, String topic, String tags, String keys)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    Message mqMsg = new Message(topic, tags, keys, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
    LOGGER.info("MQ: send message: {}", body);
    SendResult sendResult = producer.send(mqMsg, (mqs, msg, arg) -> {
      int index = Math.abs(arg.hashCode() % mqs.size());
      return mqs.get(index);
    }, tags);
    LOGGER.info("MQ: send result: {}", sendResult);

    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
      throw new SendMqMessageException(sendResult.toString());
    }
  }

  /**
   * 发送消息，使用默认的topic，不使用keys
   */
  public void sendMessage(String body, String tags)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    sendMessage(body, topic, tags, "");
  }

  /**
   * 发送消息，使用默认的topic，不使用tags和keys
   */
  public void sendMessage(String body)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    sendMessage(body, topic, "", "");
  }

  @PreDestroy
  public void stop() {
    producer.shutdown();
    LOGGER.info("MQ: stop producer.");
  }
}
