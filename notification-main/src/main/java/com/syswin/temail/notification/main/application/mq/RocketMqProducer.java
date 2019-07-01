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

import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.exceptions.MqException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rocket mq 生产者
 *
 * @author liusen@syswin.com
 */
public class RocketMqProducer implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private DefaultMQProducer producer;
  private String host;
  private String topic;
  private String producerGroup;

  public RocketMqProducer(String host, String topic, String producerGroup) {
    this.host = host;
    this.topic = topic;
    this.producerGroup = producerGroup;
  }

  public DefaultMQProducer getProducer() {
    return producer;
  }

  /**
   * 初始化
   */
  @Override
  public void start() {
    LOGGER.info("MQ: start producer.");
    producer = new DefaultMQProducer(producerGroup);
    producer.setNamesrvAddr(host);
    producer.setInstanceName(UUID.randomUUID().toString());
    try {
      producer.start();
    } catch (MQClientException e) {
      throw new MqException("MQ start exception: ", e);
    }
  }

  /**
   * 发送消息
   */
  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    SendResult sendResult;
    try {
      Message mqMsg = new Message(topic, tags, keys, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
      LOGGER.info("MQ: send message: body={}, topic={}, tags={}, keys={}", body, topic, tags, keys);

      if (tags == null || tags.isEmpty()) {
        sendResult = producer.send(mqMsg);
      } else {
        sendResult = producer.send(mqMsg, (mqs, msg, arg) -> {
          int index = Math.abs(arg.hashCode() % mqs.size());
          return mqs.get(index);
        }, tags);
      }
    } catch (Exception e) {
      throw new MqException("MQ send message exception: ", e);
    }

    LOGGER.info("MQ: send result: {}", sendResult);
    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
      throw new MqException(sendResult.toString());
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

  @Override
  public void stop() {
    producer.shutdown();
    LOGGER.info("MQ: stop producer.");
  }
}
