package com.syswin.temail.notification.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.syswin.temail.notification")
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {

    System.setProperty("rocketmq.client.logLevel", "ERROR");
//    System.setProperty("rocketmq.client.logUseSlf4j", "true");

    SpringApplication.run(Application.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }
}
