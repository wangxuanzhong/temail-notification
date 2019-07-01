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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;

import com.syswin.temail.notification.main.containers.RocketMqBrokerContainer;
import com.syswin.temail.notification.main.containers.RocketMqNameServerContainer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Ignore
public class RocketMqProducerDockerTest {

  // init mq container
  private static final int MQ_SERVER_PORT = 9876;
  private static final String NAMESRV = "namesrv";
  public static RocketMqProducer producerContainer;
  private static Network NETWORK = Network.newNetwork();
  private static RocketMqNameServerContainer rocketMqNameSrv = new RocketMqNameServerContainer()
      .withNetwork(NETWORK)
      .withNetworkAliases(NAMESRV)
      .withFixedExposedPort(MQ_SERVER_PORT, MQ_SERVER_PORT);
  private static RocketMqBrokerContainer rocketMqBroker = new RocketMqBrokerContainer()
      .withNetwork(NETWORK)
      .withEnv("NAMESRV_ADDR", NAMESRV + ":" + MQ_SERVER_PORT)
      .withFixedExposedPort(10909, 10909)
      .withFixedExposedPort(10911, 10911);
  @ClassRule
  public static RuleChain rules = RuleChain.outerRule(rocketMqNameSrv).around(rocketMqBroker);
  private static String topic = "temail-notification";
  private static String producerGroup = "notificationProducer";

  public RocketMqProducerDockerTest() {
  }

  @BeforeClass
  public static void setUp() throws Exception {
    producerContainer = new RocketMqProducer(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT, topic,
        producerGroup);
    producerContainer.start();

    System.out.println(topic);

    waitAtMost(10, SECONDS).until(() -> {
      try {
        producerContainer.getProducer().createTopic(producerContainer.getProducer().getCreateTopicKey(), topic, 1);
        return true;
      } catch (MQClientException e) {
        e.printStackTrace();
        return false;
      }
    });
  }

  public RocketMqProducer getProducerContainer(String topic) throws Exception {
    this.topic = topic;
    this.setUp();
    return producerContainer;
  }

  @Test
  public void testSendMessage() throws Exception {
    for (int i = 0; i < 10; i++) {
      producerContainer.sendMessage("" + i, String.valueOf(i));
    }
  }
}