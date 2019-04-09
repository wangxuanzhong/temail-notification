package com.syswin.temail.notification.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NotificationRestTemplateConfiguration {

  @Bean
  public RestTemplate notificationRestTemplate(ClientHttpRequestFactory notificationSimpleClientHttpRequestFactory) {
    return new RestTemplate(notificationSimpleClientHttpRequestFactory);
  }

  @Bean
  public ClientHttpRequestFactory notificationSimpleClientHttpRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(15000);
    factory.setReadTimeout(5000);
    return factory;
  }
}
