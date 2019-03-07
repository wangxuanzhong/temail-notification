package com.syswin.temail.notification.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.syswin.temail.notification")
public class Application {

  public static void main(String[] args) {

    System.setProperty("rocketmq.client.logLevel", "ERROR");
//    System.setProperty("rocketmq.client.logUseSlf4j", "true");

    SpringApplication.run(Application.class, args);
  }
}
