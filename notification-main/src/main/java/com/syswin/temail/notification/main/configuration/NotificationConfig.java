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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author liusen@syswin.com
 */

@Configuration
public class NotificationConfig {

  @Value("${spring.rocketmq.host}")
  public String host;

  /**
   * MQ消费topic
   */
  @Value("${spring.rocketmq.topics.mailAgent.singleChat:singleChatTopic}")
  public String singleChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.groupChat:groupChatTopic}")
  public String groupChatTopic;
  @Value("${spring.rocketmq.topics.mailAgent.topicChat:topicTopic}")
  public String topicTopic;
  @Value("${spring.rocketmq.topics.sync:syncTopic}")
  public String syncTopic;

  /**
   * MQ发送topic
   */
  @Value("${spring.rocketmq.topics.notify}")
  public String notifyTopic;

  /**
   * MQ消费组
   */
  @Value("${spring.rocketmq.topics.mailAgent.singleChatConsumerGroup:notificationSingleChatConsumer}")
  public String singleChatConsumerGroup;
  @Value("${spring.rocketmq.topics.mailAgent.groupChatConsumerGroup:notificationGroupChatConsumer}")
  public String gropuChatConsumerGroup;
  @Value("${spring.rocketmq.topics.mailAgent.topicChatConsumerGroup:notificationTopicConsumer}")
  public String topicConsumerGroup;
  @Value("${spring.rocketmq.topics.syncConsumerGroup:notificationSyncConsumer}")
  public String syncConsumerGroup;
  @Value("${spring.rocketmq.topics.dmConsumerGroup:notificationGroupEventPushConsumer}")
  public String dmConsumerGroup;

  /**
   * MQ生产组
   */
  @Value("${spring.rocketmq.topics.notifyProducerGroup:notificationProducer}")
  public String producerGroup;

  /**
   * MQ消费类型
   */
  @Value("${app.temail.notification.mq.consumerType:ROCKET_MQ_ONS}")
  public String consumerType;

  /**
   * MQ生产类型
   */
  @Value("${app.temail.notification.mq.producerType:ROCKET_MQ_ONS}")
  public String producerType;

  /**
   * dm saas 配置
   */
  @Value("${app.temail.notification.dm.groupChat.enabled:false}")
  public String dmGroupChatEnabled;
  @Value("${app.temail.notification.dm.application.enabled:false}")
  public String dmApplicationEnabled;
  @Value("${spring.rocketmq.topics.dm:dmTopic}")
  public String dmTopic;
  @Value("${spring.rocketmq.topics.notify.groupChat:notify}")
  public String notifyGroupChatTopic;
  @Value("${spring.rocketmq.topics.notify.application:notify}")
  public String notifyApplicationTopic;
  @Value("${url.temail.auth:authUrl}")
  public String authUrl;

  /**
   * 业务参数
   */
  @Value("${app.temail.notification.schedule.deadline}")
  public int deadline;
  @Value("${app.temail.notification.getEvents.defaultPageSize}")
  public int defaultPageSize;
  @Value("${app.temail.notification.crowd.enabled:false}")
  public String crowdEnabled;

}
