package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.main.exctptions.SendMqMessageException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
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

  private final DefaultMQProducer producer = new DefaultMQProducer("producer");

  private String host;
  private String topic;

  public RocketMqProducer(@Value("${temail.notification.rocketmq.host}") String host,
      @Value("${temail.notification.rocketmq.topics.notify}") String topic) {
    this.host = host;
    this.topic = topic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ：启动生产者");
    producer.setNamesrvAddr(host);
    producer.start();
  }

  /**
   * 发送消息
   */
  public void sendMessage(String body, String tags, String keys)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    Message mqMsg = new Message(topic, tags, keys, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
    LOGGER.info("MQ: 发送消息 {}", body);
    SendResult sendResult = producer.send(mqMsg);
    LOGGER.info("MQ: 发送结果 {}", sendResult);

    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
      throw new SendMqMessageException(sendResult.toString());
    }
  }

  /**
   * 发送消息，不使用tags和keys
   */
  public void sendMessage(String body)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    sendMessage(body, "", "");
  }

  @PreDestroy
  public void stop() {
    if (producer != null) {
      producer.shutdown();
      LOGGER.info("MQ：关闭生产者");
    }
  }
}
