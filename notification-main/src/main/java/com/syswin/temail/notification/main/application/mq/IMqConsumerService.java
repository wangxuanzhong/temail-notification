package com.syswin.temail.notification.main.application.mq;

/**
 * rocket mq 消费者
 * @author liusen@syswin.com
 */
public interface IMqConsumerService {

  /**
   * 消费mq消息
   *
   * @param body 消息内容
   * @param tags 标签
   */
  void handleMqMessage(String body, String tags);

}
