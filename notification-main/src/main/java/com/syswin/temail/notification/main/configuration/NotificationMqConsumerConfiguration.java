/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.configuration;

import com.syswin.library.messaging.all.spring.MqConsumerConfig;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.temail.notification.main.application.NotificationDmServiceImpl;
import com.syswin.temail.notification.main.application.NotificationGroupChatServiceImpl;
import com.syswin.temail.notification.main.application.NotificationSingleChatServiceImpl;
import com.syswin.temail.notification.main.application.NotificationSyncServiceImpl;
import com.syswin.temail.notification.main.application.NotificationTopicServiceImpl;
import com.syswin.temail.notification.main.application.mq.RocketMqConsumer;
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
 * @author liusen@syswin.com
 */
@Configuration
public class NotificationMqConsumerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("${spring.rocketmq.host}")
  private String host;

  /**
   * MQ消费topic
   */
  @Value("${spring.rocketmq.topics.mailAgent.singleChat}")
  private String singleChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.groupChat}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.topicChat}")
  private String topicTopic;
  @Value("${spring.rocketmq.topics.dm:dmTopic}")
  private String dmTopic;
  @Value("${spring.rocketmq.topics.sync}")
  private String syncTopic;

  /**
   * MQ消费组
   */
  @Value("${spring.rocketmq.topics.mailAgent.singleChatConsumerGroup:notificationSingleChatConsumer}")
  private String singleChatConsumerGroup;
  @Value("${spring.rocketmq.topics.mailAgent.groupChatConsumerGroup:notificationGroupChatConsumer}")
  private String gropuChatConsumerGroup;
  @Value("${spring.rocketmq.topics.mailAgent.topicChatConsumerGroup:notificationTopicConsumer}")
  private String topicConsumerGroup;
  @Value("${spring.rocketmq.topics.syncConsumerGroup:notificationSyncConsumer}")
  private String syncConsumerGroup;
  @Value("${spring.rocketmq.topics.dmConsumerGroup:notificationGroupEventPushConsumer}")
  private String dmConsumerGroup;

  /**
   * MQ消费类型
   */
  @Value("${app.temail.notification.mq.consumerType:ROCKET_MQ_ONS}")
  private String consumerType;

  /**
   * init rocket mq consumer beans
   */
  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationSingleChatRocketMqConsumer(NotificationSingleChatServiceImpl singleChatService) {
    LOGGER.info("IMqConsumer [rocketmq singleChat] started!");
    return new RocketMqConsumer(singleChatService, host, singleChatTopic, singleChatConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationGroupChatRocketMqConsumer(NotificationGroupChatServiceImpl groupChatService) {
    LOGGER.info("IMqConsumer [rocketmq groupChat] started!");
    return new RocketMqConsumer(groupChatService, host, groupChatTopic, gropuChatConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationTopicRocketMqConsumer(NotificationTopicServiceImpl topicService) {
    LOGGER.info("IMqConsumer [rocketmq topic] started!");
    return new RocketMqConsumer(topicService, host, topicTopic, topicConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
  public RocketMqConsumer notificationSyncMqConsumer(NotificationSyncServiceImpl syncService) {
    LOGGER.info("IMqConsumer [rocketmq sync] started!");
    return new RocketMqConsumer(syncService, host, syncTopic, syncConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.dm.consumer", havingValue = "rocketmq")
  public RocketMqConsumer notificationDmRocketMqConsumer(NotificationDmServiceImpl dmService) {
    LOGGER.info("IMqConsumer [rocketmq dm] started!");
    return new RocketMqConsumer(dmService, host, dmTopic, dmConsumerGroup);
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
        .group(singleChatConsumerGroup)
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
        .group(gropuChatConsumerGroup)
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
        .group(topicConsumerGroup)
        .topic(topicTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationSyncConsumerConfig(NotificationSyncServiceImpl syncService) {
    LOGGER.info("IMqConsumer [libraryMessage sync] started!");
    Consumer<String> listener = body -> syncService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(syncConsumerGroup)
        .topic(syncTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.dm.consumer", havingValue = "libraryMessage")
  MqConsumerConfig notificationDmConsumerConfig(NotificationDmServiceImpl dmService) {
    LOGGER.info("IMqConsumer [libraryMessage dm] started!");
    Consumer<String> listener = body -> dmService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(dmConsumerGroup)
        .topic(dmTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(consumerType))
        .build();
  }
}
