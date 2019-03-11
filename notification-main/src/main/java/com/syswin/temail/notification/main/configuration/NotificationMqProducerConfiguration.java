package com.syswin.temail.notification.main.configuration;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.rocketmq.RocketMqProducer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化rocketmq生产者
 */
@Configuration
public class NotificationMqProducerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("${spring.rocketmq.host}")
  private String host;
  @Value("${spring.rocketmq.topics.notify}")
  private String notifyTopic;

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "rocketmq", matchIfMissing = true)
  public IMqProducer notificationRocketMqProducer() {
    LOGGER.info("IMqProducer [RocketMQ] started!");
    return new RocketMqProducer(host, notifyTopic);
  }
}
