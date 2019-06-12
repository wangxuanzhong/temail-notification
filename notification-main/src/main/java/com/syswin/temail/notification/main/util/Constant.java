package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.domains.EventType;
import java.util.Arrays;
import java.util.List;

/**
 * @author liusen
 */
public interface Constant {

  /**
   * MQ消费组
   */
  interface ConsumerGroup {

    String SINGLE_CHAT_CONSUMER_GROUP = "notificationSingleChatConsumer";

    String GROUP_CHAT_CONSUMER_GROUP = "notificationGroupChatConsumer";

    String TOPIC_CONSUMER_GROUP = "notificationTopicConsumer";

    String SAAS_CONSUMER_GROUP = "notificationGroupEventPushConsumer";
  }

  /**
   * MQ生产组
   */
  interface ProducerGroup {

    String PRODUCER_GROUP = "notificationProducer";
  }

  /**
   * 事件筛选条件
   */
  interface EventCondition {

    /**
     * 统计未读数时需要查询出来的eventType
     */
    List<Integer> UNREAD_EVENT_TYPES = Arrays.asList(
        EventType.RESET.getValue(),
        EventType.RECEIVE.getValue(),
        EventType.DESTROY.getValue(),
        EventType.PULLED.getValue(),
        EventType.RETRACT.getValue(),
        EventType.DELETE.getValue()
    );
  }
}
