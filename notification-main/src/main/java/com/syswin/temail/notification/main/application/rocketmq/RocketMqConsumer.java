package com.syswin.temail.notification.main.application.rocketmq;

import com.syswin.temail.notification.foundation.application.IMqConsumer;
import com.syswin.temail.notification.main.application.NotificationGroupChatService;
import com.syswin.temail.notification.main.application.NotificationOssService;
import com.syswin.temail.notification.main.application.NotificationSingleChatService;
import com.syswin.temail.notification.main.application.NotificationTopicService;
import com.syswin.temail.notification.main.exceptions.MqException;
import com.syswin.temail.notification.main.util.Constant;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
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
import org.springframework.dao.DuplicateKeyException;

public class RocketMqConsumer implements IMqConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final DefaultMQPushConsumer singleChatConsumer = new DefaultMQPushConsumer(Constant.SINGLE_CHAT_CONSUMER_GROUP);
  private final DefaultMQPushConsumer groupChatConsumer = new DefaultMQPushConsumer(Constant.GROUP_CHAT_CONSUMER_GROUP);
  private final DefaultMQPushConsumer topicConsumer = new DefaultMQPushConsumer(Constant.TOPIC_CONSUMER_GROUP);
  private final DefaultMQPushConsumer ossConsumer = new DefaultMQPushConsumer(Constant.OSS_CONSUMER_GROUP);

  private final int TYPE_0_SINGLE_CHAT = 0;
  private final int TYPE_1_GROUP_CHAT = 1;
  private final int TYPE_2_TOPIC = 2;
  private final int TYPE_3_OSS = 3;

  private NotificationSingleChatService notificationSingleChatService;
  private NotificationGroupChatService notificationGroupChatService;
  private NotificationTopicService notificationTopicService;
  private NotificationOssService notificationOssService;
  private String host;
  private String singleChatTopic;
  private String groupChatTopic;
  private String topicChatTopic;
  private String ossTopic;

  public RocketMqConsumer(NotificationSingleChatService notificationSingleChatService,
      NotificationGroupChatService notificationGroupChatService,
      NotificationTopicService notificationTopicService, NotificationOssService notificationOssService, String host, String singleChatTopic,
      String groupChatTopic, String topicChatTopic, String ossTopic) {
    this.notificationSingleChatService = notificationSingleChatService;
    this.notificationGroupChatService = notificationGroupChatService;
    this.notificationTopicService = notificationTopicService;
    this.notificationOssService = notificationOssService;
    this.host = host;
    this.singleChatTopic = singleChatTopic;
    this.groupChatTopic = groupChatTopic;
    this.topicChatTopic = topicChatTopic;
    this.ossTopic = ossTopic;
  }

  /**
   * 初始化
   */
  @Override
  public void start() {
    LOGGER.info("MQ: start consumer.");
    try {
      initConsumer(singleChatConsumer, singleChatTopic, TYPE_0_SINGLE_CHAT);
      initConsumer(groupChatConsumer, groupChatTopic, TYPE_1_GROUP_CHAT);
      initConsumer(topicConsumer, topicChatTopic, TYPE_2_TOPIC);
      initConsumer(ossConsumer, ossTopic, TYPE_3_OSS);
    } catch (MQClientException e) {
      throw new MqException("start mq consumer exception: ", e);
    }
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
            LOGGER.info("MQ: MsgId={} Topic={} Tags={} Keys={}", msg.getMsgId(), msg.getTopic(), msg.getTags(), msg.getKeys());
            handleMqMessage(new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET), msg.getTags(), type);
          }
        } catch (DuplicateKeyException e) {
          LOGGER.warn("duplicate key exception: ", e);
        } catch (MqException | UnsupportedEncodingException e) {
          LOGGER.error(e.getMessage(), e);
          return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      }
    });
    consumer.setInstanceName(UUID.randomUUID().toString());
    // 启动消费端
    consumer.start();
  }


  private void handleMqMessage(String body, String tags, int type) {
    switch (type) {
      case TYPE_0_SINGLE_CHAT:
        notificationSingleChatService.handleMqMessage(body, tags);
        break;
      case TYPE_1_GROUP_CHAT:
        notificationGroupChatService.handleMqMessage(body, tags);
        break;
      case TYPE_2_TOPIC:
        notificationTopicService.handleMqMessage(body, tags);
        break;
      case TYPE_3_OSS:
        notificationOssService.handleMqMessage(body);
        break;
    }
  }

  @Override
  public void stop() {
    singleChatConsumer.shutdown();
    LOGGER.info("MQ: stop singleChatConsumer.");

    groupChatConsumer.shutdown();
    LOGGER.info("MQ: stop groupChatConsumer.");

    topicConsumer.shutdown();
    LOGGER.info("MQ: stop topicConsumer.");

    ossConsumer.shutdown();
    LOGGER.info("MQ: stop ossConsumer.");
  }
}
