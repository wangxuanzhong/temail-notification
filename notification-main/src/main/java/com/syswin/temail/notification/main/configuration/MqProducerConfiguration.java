/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.configuration;

import com.syswin.library.messaging.MqProducer;
import com.syswin.library.messaging.all.spring.MqImplementation;
import com.syswin.library.messaging.all.spring.MqProducerConfig;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.LibraryMessagingMqProducer;
import com.syswin.temail.notification.main.application.mq.RocketMqProducer;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化mq生产者
 *
 * @author liusen@syswin.com
 */
@Configuration
public class MqProducerConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private NotificationConfig config;

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "rocketmq", matchIfMissing = true)
  public IMqProducer rocketMqProducer() {
    LOGGER.info("IMqProducer [rocketmq] started!");
    return new RocketMqProducer(config.host, config.notifyTopic, config.producerGroup);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "libraryMessage")
  public IMqProducer libraryMessagingMqProducer(Map<String, MqProducer> mqProducers) {
    LOGGER.info("IMqProducer [libraryMessage] started!");
    return new LibraryMessagingMqProducer(mqProducers, config.notifyTopic, config.producerGroup);
  }

  @Bean
  @ConditionalOnProperty(name = "app.temail.notification.mq.producer", havingValue = "libraryMessage")
  MqProducerConfig libraryMessagingMqProducerConfig() {
    return new MqProducerConfig(config.producerGroup, MqImplementation.valueOf(config.producerType));
  }
}
