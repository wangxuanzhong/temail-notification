package com.syswin.temail.notification.main.configuration;

import com.syswin.library.messaging.all.spring.MqConsumerConfig;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.temail.notification.main.application.NotificationDmServiceImpl;
import com.syswin.temail.notification.main.application.NotificationGroupChatServiceImpl;
import com.syswin.temail.notification.main.application.NotificationSingleChatServiceImpl;
import com.syswin.temail.notification.main.application.NotificationTopicServiceImpl;
import com.syswin.temail.notification.main.application.mq.RocketMqConsumer;
import com.syswin.temail.notification.main.constants.Constant.ConsumerGroup;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化rocket mq消费者
 *
 * @author liusen
 */
@Configuration
public class NotificationMqConsumerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("${spring.rocketmq.host}")
  private String host;
  @Value("${spring.rocketmq.topics.mailAgent.singleChat}")
  private String singleChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.groupChat}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.topicChat}")
  private String topicTopic;
  @Value("${spring.rocketmq.topics.saas:saasTopic}")
  private String saasTopic;
  @Value("${app.temail.notification.mq.consumerType:REDIS}")
  private String consumerType;

  /**
   * init rocket mq consumer beans
   */
  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationSingleChatRocketMqConsumer(NotificationSingleChatServiceImpl singleChatService) {
    LOGGER.info("IMqConsumer [rocketmq singleChat] started!");
    return new RocketMqConsumer(singleChatService, host, singleChatTopic, ConsumerGroup.SINGLE_CHAT_CONSUMER_GROUP);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationGroupChatRocketMqConsumer(NotificationGroupChatServiceImpl groupChatService) {
    LOGGER.info("IMqConsumer [rocketmq groupChat] started!");
    return new RocketMqConsumer(groupChatService, host, groupChatTopic, ConsumerGroup.GROUP_CHAT_CONSUMER_GROUP);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationTopicRocketMqConsumer(NotificationTopicServiceImpl topicService) {
    LOGGER.info("IMqConsumer [rocketmq topic] started!");
    return new RocketMqConsumer(topicService, host, topicTopic, ConsumerGroup.TOPIC_CONSUMER_GROUP);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.saas.enabled", havingValue = "true")
  public RocketMqConsumer notificationSaasRocketMqConsumer(NotificationDmServiceImpl dmService) {
    LOGGER.info("IMqConsumer [rocketmq saas] started!");
    return new RocketMqConsumer(dmService, host, saasTopic, ConsumerGroup.SAAS_CONSUMER_GROUP);
  }

  /**
   * init library message consumer beans
   */
  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationSingleChatConsumerConfig(NotificationSingleChatServiceImpl singleChatService) {
    LOGGER.info("IMqConsumer [libraryMessage singleChat] started!");
    Consumer<String> listener = body -> singleChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(ConsumerGroup.SINGLE_CHAT_CONSUMER_GROUP)
        .topic(singleChatTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationGroupChatConsumerConfig(NotificationGroupChatServiceImpl groupChatService) {
    LOGGER.info("IMqConsumer [libraryMessage groupChat] started!");
    Consumer<String> listener = body -> groupChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(ConsumerGroup.GROUP_CHAT_CONSUMER_GROUP)
        .topic(groupChatTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationTopicConsumerConfig(NotificationTopicServiceImpl topicService) {
    LOGGER.info("IMqConsumer [libraryMessage topic] started!");
    Consumer<String> listener = body -> topicService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(ConsumerGroup.TOPIC_CONSUMER_GROUP)
        .topic(topicTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }
}
