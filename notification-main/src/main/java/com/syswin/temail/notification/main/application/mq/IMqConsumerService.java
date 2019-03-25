package com.syswin.temail.notification.main.application.mq;

public interface IMqConsumerService {

  void handleMqMessage(String body, String tags);

}
