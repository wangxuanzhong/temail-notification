package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationGroupChatServiceTest {

  private final String TEST_GROUP = "g";
  private final String TEST_TETMAIL = "a";
  private final String TEST_GROUP_MSG_ID = "g-";
  MailAgentGroupChatParams params = new MailAgentGroupChatParams();
  @Autowired
  private NotificationGroupChatService notificationGroupChatService;
  @Autowired
  private RocketMqProducer rocketMqProducer;
  private Gson gson = new Gson();

  @Before
  public void setUp() {
    params.setHeader("header");
    params.setGroupTemail(TEST_GROUP);
    params.setTemail(TEST_TETMAIL);
    params.setTimestamp((new Date()).getTime());
  }

  @Test
  public void testEventTypeReceive() throws Exception {
    params.setSessionMssageType(EventType.RECEIVE.getValue());
//    params.setType(0);
//    params.setMsgid(TEST_GROUP_MSG_ID + "1," + TEST_GROUP_MSG_ID + "2");
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setFromSeqNo(1L);
    params.setToMsg("aaaaaaaa");
    notificationGroupChatService.handleMqMessage(gson.toJson(params));
  }

  @Test
  public void testEventTypeUpdateGroupCard() throws Exception {
    params.setSessionMssageType(EventType.UPDATE_GROUP_CARD.getValue());
    params.setGroupName("测试组");
    params.setNickName("测试当事人");
    rocketMqProducer.sendMessage(gson.toJson(params), "service_topic_2", "", "");
    Thread.sleep(20 * 1000);
  }
}