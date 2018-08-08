package com.syswin.temail.notification.main.application;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RocketMqConsumer implements MessageListenerConcurrently {

  private static final Logger LOGGER = LoggerFactory.getLogger(MQPushConsumer.class);

  private final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("RocketMQPushConsumer");

  private NotificationService notificationService;
  private String namesrvAddr;
  private String topic;

  public RocketMqConsumer(NotificationService notificationService,
      @Value("${temail.rocketmq.namesrvAddr}") String namesrvAddr,
      @Value("${temail.rocketmq.topics.service}") String topic) {
    this.notificationService = notificationService;
    this.namesrvAddr = namesrvAddr;
    this.topic = topic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ：启动消费者");

    consumer.setNamesrvAddr(namesrvAddr);
    // 从消息队列头开始消费
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    // 集群消费模式
    consumer.setMessageModel(MessageModel.CLUSTERING);
    // 订阅主题
    consumer.subscribe(topic, "test_tag");
    // 注册消息监听器
    consumer.registerMessageListener(this);
    // 启动消费端
    consumer.start();
  }

  /**
   * 消费消息
   */
  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : msgs) {
        String body = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
        LOGGER.info("MQ：接收新信息: MsgId={} Topic={} Tags={} Keys={} Body={}", msg.getMsgId(), msg.getTopic(), msg.getTags(), msg.getKeys(), body);
        // TODO
        // do something
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  }

  @PreDestroy
  public void stop() {
    if (consumer != null) {
      consumer.shutdown();
      LOGGER.info("MQ：关闭消费者");
    }
  }
}
