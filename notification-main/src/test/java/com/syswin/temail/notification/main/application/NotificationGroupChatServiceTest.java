package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.MailAgentGroupChatParams;
import java.util.Date;
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

  @Autowired
  NotificationGroupChatService notificationGroupChatService;
  private Gson gson = new Gson();

  @Test
  public void testHandleMqMessage() throws Exception {
    MailAgentGroupChatParams params = new MailAgentGroupChatParams();
    params.setHeader("header");
    params.setSessionMssageType(EventType.RECEIVE.getValue());
    params.setGroupTemail(TEST_GROUP);
    params.setTemail(TEST_TETMAIL);
//    params.setType(0);
//    params.setMsgid(TEST_GROUP_MSG_ID + "1," + TEST_GROUP_MSG_ID + "2");
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setFromSeqNo(1L);
    params.setToMsg("aaaaaaaa");
    params.setTimestamp((new Date()).getTime());
    notificationGroupChatService.handleMqMessage(gson.toJson(params));
  }


}