package com.syswin.temail.notification.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 *
 * @author liusen@syswin.com
 */
@Configuration
public class NotificationRestTemplateConfiguration {

  @Bean
  public RestTemplate notificationRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    // 添加默认handler，返回4xx时不会抛出异常
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      public void handleError(ClientHttpResponse clientHttpResponse) {
      }
    });
    return restTemplate;
  }
}
