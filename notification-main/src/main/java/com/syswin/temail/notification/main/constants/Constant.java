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

package com.syswin.temail.notification.main.constants;

import com.syswin.temail.notification.main.domains.EventType;
import java.util.Arrays;
import java.util.List;

/**
 * @author liusen@syswin.com
 */
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

    public static final String DM_CONSUMER_GROUP = "notificationGroupEventPushConsumer";
  }

  /**
   * MQ生产组
   */
  public static class ProducerGroup {

    public static final String PRODUCER_GROUP = "notificationProducer";
  }

  /**
   * 事件筛选条件
   */
  public static class EventCondition {

    /**
     * 统计未读数时需要查询出来的eventType
     */
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
