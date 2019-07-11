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

import com.syswin.temail.notification.main.application.DmServiceImpl;
import com.syswin.temail.notification.main.application.GroupChatServiceImpl;
import com.syswin.temail.notification.main.application.SingleChatServiceImpl;
import com.syswin.temail.notification.main.application.SyncServiceImpl;
import com.syswin.temail.notification.main.application.TopicServiceImpl;
import com.syswin.temail.notification.main.application.mq.RocketMqConsumer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化rocket mq消费者
 *
 * @author liusen@syswin.com
 */
@ConditionalOnProperty(name = "app.temail.notification.mq.consumer", havingValue = "rocketmq", matchIfMissing = true)
@Configuration
public class RocketMqConsumerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private NotificationConfig config;

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.singleChat")
  public RocketMqConsumer singleChatRocketMqConsumer(SingleChatServiceImpl singleChatService) {
    LOGGER.info("IMqConsumer [rocketmq singleChat] started!");
    return new RocketMqConsumer(singleChatService, config.host, config.singleChatTopic, config.singleChatConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.groupChat")
  public RocketMqConsumer groupChatRocketMqConsumer(GroupChatServiceImpl groupChatService) {
    LOGGER.info("IMqConsumer [rocketmq groupChat] started!");
    return new RocketMqConsumer(groupChatService, config.host, config.groupChatTopic, config.gropuChatConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "spring.rocketmq.topics.mailAgent.topicChat")
  public RocketMqConsumer topicRocketMqConsumer(TopicServiceImpl topicService) {
    LOGGER.info("IMqConsumer [rocketmq topic] started!");
    return new RocketMqConsumer(topicService, config.host, config.topicTopic, config.topicConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "spring.rocketmq.topics.sync")
  public RocketMqConsumer syncMqConsumer(SyncServiceImpl syncService) {
    LOGGER.info("IMqConsumer [rocketmq sync] started!");
    return new RocketMqConsumer(syncService, config.host, config.syncTopic, config.syncConsumerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "spring.rocketmq.topics.dm")
  public RocketMqConsumer dmRocketMqConsumer(DmServiceImpl dmService) {
    LOGGER.info("IMqConsumer [rocketmq dm] started!");
    return new RocketMqConsumer(dmService, config.host, config.dmTopic, config.dmConsumerGroup);
  }
}
