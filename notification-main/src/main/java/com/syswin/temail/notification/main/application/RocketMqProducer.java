package com.syswin.temail.notification.main.application;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
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

  private final DefaultMQProducer producer = new DefaultMQProducer("RocketMQProducer");

  private String namesrvAddr;
  private String topic;

  public RocketMqProducer(@Value("${temail.rocketmq.namesrvAddr}") String namesrvAddr,
      @Value("${temail.rocketmq.topics.consumer}") String topic) {
    this.namesrvAddr = namesrvAddr;
    this.topic = topic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ：启动生产者");
    producer.setNamesrvAddr(namesrvAddr);
    producer.start();
  }

  /**
   * 发送消息
   */
  public void sendMessage(String body, String tags, String keys)
      throws UnsupportedEncodingException, RemotingException, MQClientException, InterruptedException {
    Message mqMsg = new Message(topic, tags, keys, body.getBytes(RemotingHelper.DEFAULT_CHARSET));

//    List<MessageQueue> messageQueues = producer.fetchPublishMessageQueues(topic);

    producer.send(mqMsg, new SendCallback() {
      @Override
      public void onSuccess(SendResult sendResult) {
        LOGGER.info("MQ: 发送消息 {}", sendResult);
      }

      @Override
      public void onException(Throwable throwable) {
        LOGGER.error(throwable.getMessage(), throwable);
      }
    });
  }

  @PreDestroy
  public void stop() {
    if (producer != null) {
      producer.shutdown();
      LOGGER.info("MQ：关闭生产者");
    }
  }
}
