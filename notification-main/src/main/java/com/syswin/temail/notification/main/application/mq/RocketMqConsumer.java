package com.syswin.temail.notification.main.application.mq;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

/**
 * rocket mq 消费者
 *
 * @author liusen@syswin.com
 */
public class RocketMqConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqConsumerService iMqConsumerService;
  private final String host;
  private final String topic;
  private final String consumerGroup;

  private DefaultMQPushConsumer consumer;

  public RocketMqConsumer(IMqConsumerService iMqConsumerService, String host, String topic, String consumerGroup) {
    this.iMqConsumerService = iMqConsumerService;
    this.host = host;
    this.topic = topic;
    this.consumerGroup = consumerGroup;
  }

  /**
   * 初始化
   */
  public void start() throws MQClientException {
    LOGGER.info("MQ: start consumer: {}", consumerGroup);
    consumer = new DefaultMQPushConsumer(consumerGroup);
    consumer.setNamesrvAddr(host);
    // 从消息队列头开始消费
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    // 集群消费模式
    consumer.setMessageModel(MessageModel.CLUSTERING);
    // 订阅主题
    consumer.subscribe(topic, "");
    // 注册消息监听器
    consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
      try {
        for (MessageExt msg : list) {
          LOGGER.info("MQ: MsgId={} Topic={} Tags={} Keys={}", msg.getMsgId(), msg.getTopic(), msg.getTags(),
              msg.getKeys());
          iMqConsumerService.handleMqMessage(new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET), msg.getTags());
        }
      } catch (DuplicateKeyException e) {
        LOGGER.warn("duplicate key exception: ", e);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    });
    consumer.setInstanceName(UUID.randomUUID().toString());
    // 启动消费端
    consumer.start();
  }


  public void stop() {
    consumer.shutdown();
    LOGGER.info("MQ: stop consumer: {}", consumerGroup);
  }
}
