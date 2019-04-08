package com.syswin.temail.notification.main.util;

public class Constant {

  /**
   * MQ消费组
   */
  public static final String SINGLE_CHAT_CONSUMER_GROUP = "notificationSingleChatConsumer";

  public static final String GROUP_CHAT_CONSUMER_GROUP = "notificationGroupChatConsumer";

  public static final String TOPIC_CONSUMER_GROUP = "notificationTopicConsumer";

  public static final String OSS_CONSUMER_GROUP = "temail_oss_notification_consumer";

  public static final String SAAS_NEW_GROUP_CHAT_CONSUMER_GROUP = "notificationSaasConsumer";

  /**
   * MQ生产组
   */
  public static final String PRODUCER_GROUP = "notificationProducer";
}
