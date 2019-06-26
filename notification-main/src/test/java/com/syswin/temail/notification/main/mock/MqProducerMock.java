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

package com.syswin.temail.notification.main.mock;

import com.syswin.temail.notification.foundation.application.IMqProducer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqProducerMock implements IMqProducer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String topic = "test";

  @Override
  public void sendMessage(String body, String topic, String tags, String keys) {
    LOGGER.info("MQ: body: {}", body);
    LOGGER.info("MQ: topic: {}", topic);
    LOGGER.info("MQ: tags: {}", tags);
    LOGGER.info("MQ: keys: {}", keys);
    if (tags == null || tags.isEmpty()) {
      LOGGER.info("MQ: queue id is random");
    } else {
      LOGGER.info("MQ: queue id when queue size is 4: {}", Math.abs(tags.hashCode() % 4));
    }

  }

  @Override
  public void sendMessage(String body, String tags) {
    sendMessage(body, topic, tags, "");
  }

  @Override
  public void sendMessage(String body) {
    sendMessage(body, topic, "", "");
  }
}
