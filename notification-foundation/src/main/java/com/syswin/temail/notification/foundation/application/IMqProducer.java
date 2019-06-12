package com.syswin.temail.notification.foundation.application;

/**
 * @author 刘森
 */
public interface IMqProducer {

  /**
   * 启动
   */
  default void start() {
  }

  /**
   * 发消息
   *
   * @param body 内容
   * @param topic 队列topic
   * @param tags 标签
   * @param keys ‘KEYS’属性值
   */
  void sendMessage(String body, String topic, String tags, String keys);

  /**
   * 发消息
   *
   * @param body 内容
   * @param tags 标签
   */
  void sendMessage(String body, String tags);

  /**
   * 发消息
   *
   * @param body 内容
   */
  void sendMessage(String body);

  /**
   * 关闭资源
   */
  default void stop() {
  }

}
