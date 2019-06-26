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

package com.syswin.temail.notification.main.application.mq;

import com.syswin.library.messaging.MessagingException;
import com.syswin.library.messaging.MqProducer;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.constants.Constant.ProducerGroup;
import com.syswin.temail.notification.main.exceptions.MqException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rocket mq生产者
 * @author liusen@syswin.com
 */
public class LibraryMessagingMqProducer implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<String, MqProducer> rocketMqProducers;
  private String topic;

  public LibraryMessagingMqProducer(Map<String, MqProducer> rocketMqProducers, String topic) {
    this.rocketMqProducers = rocketMqProducers;
    this.topic = topic;
  }

  /**
   * 发送消息
   */
  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    MqProducer mqProducer = rocketMqProducers.get(ProducerGroup.PRODUCER_GROUP);
    if (mqProducer == null) {
      throw new MqException("mq producer is empty!");
    }

    try {
      LOGGER.info("MQ: send message: {}", body);
      mqProducer.send(body, topic, tags, null);
    } catch (UnsupportedEncodingException | InterruptedException | MessagingException e) {
      throw new MqException("MQ send message exception: ", e);
    }
  }

  /**
   * 发送消息，使用默认的topic，不使用keys
   */
  @Override
  public void sendMessage(String body, String tags) {
    sendMessage(body, topic, tags, "");
  }

  /**
   * 发送消息，使用默认的topic，不使用tags和keys
   */
  @Override
  public void sendMessage(String body) {
    sendMessage(body, topic, "", "");
  }
}
