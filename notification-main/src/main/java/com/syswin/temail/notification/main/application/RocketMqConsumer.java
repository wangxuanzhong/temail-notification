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
public class RocketMqConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MQPushConsumer.class);

  private final DefaultMQPushConsumer singleChatConsumer = new DefaultMQPushConsumer("singleChatConsumer");
  private final DefaultMQPushConsumer groupChatConsumer = new DefaultMQPushConsumer("groupChatConsumer");

  private NotificationService notificationService;
  private NotificationGroupChatService notificationGroupChatService;
  private String host;
  private String singleChatTopic;
  private String groupChatTopic;

  public RocketMqConsumer(NotificationService notificationService,
      NotificationGroupChatService notificationGroupChatService,
      @Value("${temail.notification.rocketmq.host}") String host,
      @Value("${temail.notification.rocketmq.topics.mailAgent.singleChat}") String singleChatTopic,
      @Value("${temail.notification.rocketmq.topics.mailAgent.groupChat}") String groupChatTopic) {
    this.notificationService = notificationService;
    this.notificationGroupChatService = notificationGroupChatService;
    this.host = host;
    this.singleChatTopic = singleChatTopic;
    this.groupChatTopic = groupChatTopic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ：启动消费者");

    initConsumer(singleChatConsumer, singleChatTopic);
    initConsumer(groupChatConsumer, groupChatTopic);

    // 注册消息监听器
    singleChatConsumer.registerMessageListener(new MessageListenerConcurrently() {
      @Override
      public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
          for (MessageExt msg : list) {
            String body = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
            LOGGER.info("MQ：接收单聊信息: MsgId={} Topic={} Tags={} Keys={} \nBody={}", msg.getMsgId(), msg.getTopic(), msg.getTags(), msg.getKeys(), body);
            notificationService.handleMqMessage(body);
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      }
    });

    groupChatConsumer.registerMessageListener(new MessageListenerConcurrently() {
      @Override
      public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
          for (MessageExt msg : list) {
            String body = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
            LOGGER.info("MQ：接收群聊信息: MsgId={} Topic={} Tags={} Keys={} \nBody={}", msg.getMsgId(), msg.getTopic(), msg.getTags(), msg.getKeys(), body);
            notificationGroupChatService.handleMqMessage(body);
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      }
    });

    // 启动消费端
    singleChatConsumer.start();
    groupChatConsumer.start();
  }

  private void initConsumer(DefaultMQPushConsumer consumer, String topic) throws MQClientException {
    consumer.setNamesrvAddr(host);
    // 从消息队列头开始消费
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    // 集群消费模式
    consumer.setMessageModel(MessageModel.CLUSTERING);
    // 订阅主题
    consumer.subscribe(topic, "");
  }

  @PreDestroy
  public void stop() {
    if (singleChatConsumer != null) {
      singleChatConsumer.shutdown();
      LOGGER.info("MQ：关闭单聊消费者");
    }

    if (groupChatConsumer != null) {
      groupChatConsumer.shutdown();
      LOGGER.info("MQ：关闭群聊消费者");
    }
  }
}
