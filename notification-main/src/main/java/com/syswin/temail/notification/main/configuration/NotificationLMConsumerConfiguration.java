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
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化library message消费者
 *
 * @author liusen@syswin.com
 */
@ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "libraryMessage")
@Configuration
public class NotificationLMConsumerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private NotificationConfig config;

  /**
   * init library message consumer beans
   */
  @Bean
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.singleChat")
  MqConsumerConfig notificationSingleChatConsumerConfig(NotificationSingleChatServiceImpl singleChatService) {
    LOGGER.info("IMqConsumer [libraryMessage singleChat] started!");
    Consumer<String> listener = body -> singleChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(config.singleChatConsumerGroup)
        .topic(config.singleChatTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(config.consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.groupChat")
  MqConsumerConfig notificationGroupChatConsumerConfig(NotificationGroupChatServiceImpl groupChatService) {
    LOGGER.info("IMqConsumer [libraryMessage groupChat] started!");
    Consumer<String> listener = body -> groupChatService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(config.gropuChatConsumerGroup)
        .topic(config.groupChatTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(config.consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.topicChat")
  MqConsumerConfig notificationTopicConsumerConfig(NotificationTopicServiceImpl topicService) {
    LOGGER.info("IMqConsumer [libraryMessage topic] started!");
    Consumer<String> listener = body -> topicService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(config.topicConsumerGroup)
        .topic(config.topicTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(config.consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "spring.rocketmq.topics.sync")
  MqConsumerConfig notificationSyncConsumerConfig(NotificationSyncServiceImpl syncService) {
    LOGGER.info("IMqConsumer [libraryMessage sync] started!");
    Consumer<String> listener = body -> syncService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(config.syncConsumerGroup)
        .topic(config.syncTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(config.consumerType))
        .build();
  }

  @Bean
  @ConditionalOnProperty(name = "spring.rocketmq.topics.dm")
  MqConsumerConfig notificationDmConsumerConfig(NotificationDmServiceImpl dmService) {
    LOGGER.info("IMqConsumer [libraryMessage dm] started!");
    Consumer<String> listener = body -> dmService.handleMqMessage(body, null);
    return MqConsumerConfig.create()
        .group(config.dmConsumerGroup)
        .topic(config.dmTopic)
        .listener(listener)
        .implementation(MqImplementation.valueOf(config.consumerType))
        .build();
  }
}
