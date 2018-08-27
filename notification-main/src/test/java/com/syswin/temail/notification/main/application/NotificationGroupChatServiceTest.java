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
    MailAgentGroupChatParams mailAgentGroupChatParams = new MailAgentGroupChatParams();
    mailAgentGroupChatParams.setHeader("header");
    mailAgentGroupChatParams.setSessionMssageType(EventType.RECEIVE.getValue());
    mailAgentGroupChatParams.setGroupTemail(TEST_GROUP);
    mailAgentGroupChatParams.setTemail(TEST_TETMAIL);
//    mailAgentGroupChatParams.setType(0);
//    mailAgentGroupChatParams.setMsgid(TEST_GROUP_MSG_ID + "1," + TEST_GROUP_MSG_ID + "2");
    mailAgentGroupChatParams.setMsgid(TEST_GROUP_MSG_ID + "1");
    mailAgentGroupChatParams.setFromSeqNo(1L);
    mailAgentGroupChatParams.setToMsg("aaaaaaaa");
    mailAgentGroupChatParams.setTimestamp((new Date()).getTime());
    notificationGroupChatService.handleMqMessage(gson.toJson(mailAgentGroupChatParams));
  }


}