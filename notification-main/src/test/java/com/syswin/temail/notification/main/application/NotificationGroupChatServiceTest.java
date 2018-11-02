package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
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
  private final String TEST_GROUP_MSG_ID = "g-";
  private final String TOPIC = "temail-groupmail";
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
//    params.setGroupName("测试组名");
//    params.setName("测试当事人名");
//    params.setAdminName("测试触发人名");
//    params.setTimestamp(System.currentTimeMillis());
  }

  /**
   * EventType RECEIVE 0
   */
  @Test
  public void testEventTypeReceive() throws Exception {
    params.setSessionMssageType(EventType.RECEIVE.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setTemail("a");
    params.setSeqNo(1L);
    params.setToMsg("这是一条群聊测试消息！");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType PULLED 1
   */
  @Test
  public void testEventTypePulled() throws Exception {
    params.setSessionMssageType(EventType.PULLED.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setTemail("b");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType RETRACT 2
   */
  @Test
  public void testEventTypeRetract() throws Exception {
    params.setSessionMssageType(EventType.RETRACT.getValue());
    params.setMsgid("1");
    params.setTemail("a");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType ADD_MEMBER 10
   */
  @Test
  public void testEventTypeAddMember() throws Exception {
    params.setSessionMssageType(EventType.ADD_MEMBER.getValue());
    params.setType(MemberRole.NORMAL.getValue());
    params.setTemail("d");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
    params.setTemail("e");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
    params.setTemail("f");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType ADD_GROUP 13
   */
  @Test
  public void testEventTypeAddGroup() throws Exception {
    params.setSessionMssageType(EventType.ADD_GROUP.getValue());
    params.setTemail("a");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType DELETE_MEMBER 11
   */
  @Test
  public void testEventTypeDeleteMember() throws Exception {
    params.setName("测试当事人名");
    params.setAdminName("测试触发人名");
    params.setTemail("d,e,f");
    params.setSessionMssageType(EventType.DELETE_MEMBER.getValue());
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType UPDATE_GROUP_CARD 16
   */
  @Test
  public void testEventTypeUpdateGroupCard() throws Exception {
    params.setSessionMssageType(EventType.UPDATE_GROUP_CARD.getValue());
    params.setGroupName("测试组名");
    params.setName("测试当事人名");
    params.setAdminName("测试触发人名");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType REPLY 18
   */
  @Test
  public void testEventTypeReply() throws Exception {
    params.setSessionMssageType(EventType.REPLY.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "reply_5");
    params.setTemail("a");
    params.setParentMsgId("g_333");
    params.setAt("a;b;c");
    params.setToMsg("这是一条回复消息！");
    params.setSeqNo(1L);
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }
}