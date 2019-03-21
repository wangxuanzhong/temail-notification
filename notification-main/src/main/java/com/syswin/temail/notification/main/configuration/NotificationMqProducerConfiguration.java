package com.syswin.temail.notification.main.configuration;

import com.syswin.library.messaging.MqProducer;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.library.messaging.all.spring.MqProducerConfig;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.LibraryMessagingMqProducer;
import com.syswin.temail.notification.main.application.mq.RocketMqProducer;
import com.syswin.temail.notification.main.util.Constant;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final MqImplementation mqImplementation;
  @Value("${spring.rocketmq.host}")
  private String host;
  @Value("${spring.rocketmq.topics.notify}")
  private String notifyTopic;
  @Value("${app.temail.notification.mq.producerType:ROCKET_MQ}")
  private String producerType;

  @Autowired
  public NotificationMqProducerConfiguration(@Value("${app.temail.notification.mq.producerType:}") String producerType) {
    mqImplementation = producerType.isEmpty() ? MqImplementation.ROCKET_MQ : MqImplementation.valueOf(producerType);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "rocketmq", matchIfMissing = true)
  public IMqProducer notificationRocketMqProducer() {
    LOGGER.info("IMqProducer [rocketmq] started!");
    return new RocketMqProducer(host, notifyTopic);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "libraryMessage")
  public IMqProducer notificationLibraryMessagingMqProducer(Map<String, MqProducer> mqProducers) {
    LOGGER.info("IMqProducer [libraryMessage] started!");
    return new LibraryMessagingMqProducer(mqProducers, notifyTopic);
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "libraryMessage")
  MqProducerConfig groupmailagentTopicProducerConfig() {
    return new MqProducerConfig(Constant.PRODUCER_GROUP, mqImplementation);
  }
}
