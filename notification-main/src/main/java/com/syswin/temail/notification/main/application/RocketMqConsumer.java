package com.syswin.temail.notification.main.application;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class RocketMqConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final DefaultMQPushConsumer singleChatConsumer = new DefaultMQPushConsumer("notificationSingleChatConsumer");
  private final DefaultMQPushConsumer groupChatConsumer = new DefaultMQPushConsumer("notificationGroupChatConsumer");
  private final DefaultMQPushConsumer topicConsumer = new DefaultMQPushConsumer("notificationTopicConsumer");

  private final int TYPE_0_SINGLE_CHAT = 0;
  private final int TYPE_1_GROUP_CHAT = 1;
  private final int TYPE_2_TOPIC = 2;

  private NotificationService notificationService;
  private NotificationGroupChatService notificationGroupChatService;
  private TopicService topicService;
  private String host;
  private String singleChatTopic;
  private String groupChatTopic;
  private String topicChatTopic;

  public RocketMqConsumer(NotificationService notificationService, NotificationGroupChatService notificationGroupChatService,
      TopicService topicService, @Value("${spring.rocketmq.host}") String host,
      @Value("${spring.rocketmq.topics.mailAgent.singleChat}") String singleChatTopic,
      @Value("${spring.rocketmq.topics.mailAgent.groupChat}") String groupChatTopic,
      @Value("${spring.rocketmq.topics.mailAgent.topicChat}") String topicChatTopic) {
    this.notificationService = notificationService;
    this.notificationGroupChatService = notificationGroupChatService;
    this.topicService = topicService;
    this.host = host;
    this.singleChatTopic = singleChatTopic;
    this.groupChatTopic = groupChatTopic;
    this.topicChatTopic = topicChatTopic;
  }

  /**
   * 初始化
   */
  @PostConstruct
  public void start() throws MQClientException {
    LOGGER.info("MQ: start consumer.");
    initConsumer(singleChatConsumer, singleChatTopic, TYPE_0_SINGLE_CHAT);
    initConsumer(groupChatConsumer, groupChatTopic, TYPE_1_GROUP_CHAT);
    initConsumer(topicConsumer, topicChatTopic, TYPE_2_TOPIC);
  }


  private void initConsumer(DefaultMQPushConsumer consumer, String topic, int type) throws MQClientException {
    consumer.setNamesrvAddr(host);
    // 从消息队列头开始消费
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    // 集群消费模式
    consumer.setMessageModel(MessageModel.CLUSTERING);
    // 订阅主题
    consumer.subscribe(topic, "");
    // 注册消息监听器
    consumer.registerMessageListener(new MessageListenerConcurrently() {
      @Override
      public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
          for (MessageExt msg : list) {
            LOGGER.info("MQ: MsgId={} TopicEvent={} Tags={} Keys={}", msg.getMsgId(), msg.getTopic(), msg.getTags(), msg.getKeys());
            handleMqMessage(new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET), type);
          }
        } catch (DuplicateKeyException e) {
          LOGGER.warn("duplicate key exception: " + e);
        } catch (InterruptedException | RemotingException | MQClientException | MQBrokerException | UnsupportedEncodingException e) {
          LOGGER.error(e.getMessage(), e);
          return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      }
    });
    consumer.setInstanceName(UUID.randomUUID().toString());
    // 启动消费端
    consumer.start();
  }


  private void handleMqMessage(String body, int type)
      throws InterruptedException, RemotingException, UnsupportedEncodingException, MQClientException, MQBrokerException {
    switch (type) {
      case TYPE_0_SINGLE_CHAT:
        notificationService.handleMqMessage(body);
        break;
      case TYPE_1_GROUP_CHAT:
        notificationGroupChatService.handleMqMessage(body);
        break;
      case TYPE_2_TOPIC:
        topicService.handleMqMessage(body);
        break;
    }
  }

  @PreDestroy
  public void stop() {
    singleChatConsumer.shutdown();
    LOGGER.info("MQ: stop singleChatConsumer.");

    groupChatConsumer.shutdown();
    LOGGER.info("MQ: stop groupChatConsumer.");

    topicConsumer.shutdown();
    LOGGER.info("MQ: stop topicConsumer.");
  }
}
