package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.domains.EventType;
import java.util.Arrays;
import java.util.List;

public class Constant {


  private Constant() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * MQ消费组
   */
  public static class ConsumerGroup {

    public static final String SINGLE_CHAT_CONSUMER_GROUP = "notificationSingleChatConsumer";

    public static final String GROUP_CHAT_CONSUMER_GROUP = "notificationGroupChatConsumer";

    public static final String TOPIC_CONSUMER_GROUP = "notificationTopicConsumer";

    public static final String SAAS_CONSUMER_GROUP = "notificationGroupEventPushConsumer";
  }

  /**
   * MQ生产组
   */
  public static class ProducerGroup {

    public static final String PRODUCER_GROUP = "notificationProducer";
  }


  public static class EventCondition {

    // 统计未读数时需要查询出来的eventType
    public static final List<Integer> UNREAD_EVENT_TYPES = Arrays.asList(
        EventType.RESET.getValue(),
        EventType.RECEIVE.getValue(),
        EventType.DESTROY.getValue(),
        EventType.PULLED.getValue(),
        EventType.RETRACT.getValue(),
        EventType.DELETE.getValue()
    );
  }
}
