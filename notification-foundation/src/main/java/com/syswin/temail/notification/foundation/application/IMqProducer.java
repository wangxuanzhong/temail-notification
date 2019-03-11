package com.syswin.temail.notification.foundation.application;

public interface IMqProducer {

  void start();

  void sendMessage(String body, String topic, String tags, String keys);

  void sendMessage(String body, String tags);

  void sendMessage(String body);

  void stop();

}
