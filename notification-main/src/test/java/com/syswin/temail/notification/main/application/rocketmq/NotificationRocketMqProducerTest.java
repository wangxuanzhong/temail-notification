package com.syswin.temail.notification.main.application.rocketmq;

import com.syswin.temail.notification.main.containers.RocketMqBrokerContainer;
import com.syswin.temail.notification.main.containers.RocketMqNameServerContainer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Ignore
public class NotificationRocketMqProducerTest {

  // init mq container
  private static final int MQ_SERVER_PORT = 9876;
  private static final String NAMESRV = "namesrv";
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
  public NotificationRocketMqProducer producerContainer;

  @Value("${spring.rocketmq.topics.notify}")
  private String topic;

  public NotificationRocketMqProducerTest() {
  }

  public NotificationRocketMqProducer getProducerContainer(String topic) throws Exception {
    this.topic = topic;
    this.setUp();
    return producerContainer;
  }

  @Before
  public void setUp() throws Exception {
    producerContainer = new NotificationRocketMqProducer(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT, topic);
    producerContainer.start();
  }

  @Test
  public void testSendMessage() throws Exception {
    for (int i = 0; i < 10; i++) {
      producerContainer.sendMessage("" + i, String.valueOf(i));
    }
  }
}