package com.syswin.temail.notification.main.configuration;

import com.syswin.temail.notification.foundation.application.IMqConsumer;
import com.syswin.temail.notification.main.application.NotificationGroupChatService;
import com.syswin.temail.notification.main.application.NotificationOssService;
import com.syswin.temail.notification.main.application.NotificationSingleChatService;
import com.syswin.temail.notification.main.application.NotificationTopicService;
import com.syswin.temail.notification.main.application.rocketmq.RocketMqConsumer;
import java.lang.invoke.MethodHandles;
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
  private String topicChatTopic;
  @Value("${spring.rocketmq.topics.oss}")
  private String ossTopic;

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public IMqConsumer notificationRocketMqConsumer() {
    LOGGER.info("IMqConsumer [RocketMQ] started!");
    return new RocketMqConsumer(notificationSingleChatService, notificationGroupChatService, notificationTopicService, notificationOssService, host,
        singleChatTopic, groupChatTopic, topicChatTopic, ossTopic);
  }
}
