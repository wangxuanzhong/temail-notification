package com.syswin.temail.notification.main.configuration;

import com.syswin.library.messaging.all.spring.MqConsumerConfig;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.temail.notification.foundation.application.IMqConsumer;
import com.syswin.temail.notification.main.application.NotificationGroupChatService;
import com.syswin.temail.notification.main.application.NotificationOssService;
import com.syswin.temail.notification.main.application.NotificationSingleChatService;
import com.syswin.temail.notification.main.application.NotificationTopicService;
import com.syswin.temail.notification.main.application.mq.RocketMqConsumer;
import com.syswin.temail.notification.main.util.Constant;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化rocketmq消费者
 */
@Configuration
public class NotificationMqConsumerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MqImplementation mqImplementation;
  @Autowired
  private NotificationSingleChatService notificationSingleChatService;
  @Autowired
  private NotificationGroupChatService notificationGroupChatService;
  @Autowired
  private NotificationTopicService notificationTopicService;
  @Autowired
  private NotificationOssService notificationOssService;
  @Value("${spring.rocketmq.host}")
  private String host;
  @Value("${spring.rocketmq.topics.mailAgent.singleChat}")
  private String singleChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.groupChat}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.topicChat}")
  private String topicTopic;
  @Value("${spring.rocketmq.topics.oss}")
  private String ossTopic;

  @Autowired
  public NotificationMqConsumerConfiguration(@Value("${app.temail.notification.mq.consumerType:}") String consumerType) {
    mqImplementation = consumerType.isEmpty() ? MqImplementation.ROCKET_MQ : MqImplementation.valueOf(consumerType);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public IMqConsumer notificationRocketMqConsumer() {
    LOGGER.info("IMqConsumer [rocketmq] started!");
    return new RocketMqConsumer(notificationSingleChatService, notificationGroupChatService, notificationTopicService, notificationOssService, host,
        singleChatTopic, groupChatTopic, topicTopic, ossTopic);
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationSingleChatConsumerConfig() {
    LOGGER.info("IMqConsumer [libraryMessage singleChat] started!");
    Consumer<String> listener = body -> notificationSingleChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(Constant.SINGLE_CHAT_CONSUMER_GROUP)
        .topic(singleChatTopic)
        .listener(listener)
        .implementation(mqImplementation)
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationGroupChatConsumerConfig() {
    LOGGER.info("IMqConsumer [libraryMessage groupChat] started!");
    Consumer<String> listener = body -> notificationGroupChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(Constant.GROUP_CHAT_CONSUMER_GROUP)
        .topic(groupChatTopic)
        .listener(listener)
        .implementation(mqImplementation)
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationTopicChatConsumerConfig() {
    LOGGER.info("IMqConsumer [libraryMessage topicChat] started!");
    Consumer<String> listener = body -> notificationTopicService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(Constant.TOPIC_CONSUMER_GROUP)
        .topic(topicTopic)
        .listener(listener)
        .implementation(mqImplementation)
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationOssConsumerConfig() {
    LOGGER.info("IMqConsumer [libraryMessage oss] started!");
    Consumer<String> listener = body -> notificationOssService.handleMqMessage(body);
    return MqConsumerConfig.create()
        .group(Constant.OSS_CONSUMER_GROUP)
        .topic(ossTopic)
        .listener(listener)
        .implementation(mqImplementation)
        .build();
  }

}
