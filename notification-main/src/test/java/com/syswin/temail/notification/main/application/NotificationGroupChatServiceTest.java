package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.MemberRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
import java.util.Arrays;
import java.util.UUID;
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
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private MemberRepository memberRepository;
  private Gson gson = new Gson();


  @Before
  public void setUp() {
    params.setHeader("header");
    params.setGroupTemail(TEST_GROUP);
//    params.setGroupName("测试组名");
//    params.setName("测试当事人名");
//    params.setAdminName("测试触发人名");
//    params.setTimestamp(System.currentTimeMillis());
    params.setxPacketId(UUID.randomUUID().toString());
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
    params.setName("dd");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
    params.setTemail("e");
    params.setName("ee");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
    params.setTemail("f");
    params.setName("ff");
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
    params.setAdminName("测试触发人名");
    params.setTemail(gson.toJson(Arrays.asList("d", "e", "f")));
    params.setName(gson.toJson(Arrays.asList("dd", "ee", "ff")));
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


  @Test
  public void testAll() throws Exception {
    MailAgentGroupChatParams param = null;
    String groupTemail = "g_all_" + UUID.randomUUID().toString();

    // 创建群 13
    param = init(groupTemail);
    param.setSessionMssageType(EventType.ADD_GROUP.getValue());
    param.setTemail("a_all");
    this.sendMessage(param);

    // 入群申请 5
    param = init(groupTemail);
    param.setSessionMssageType(EventType.APPLY.getValue());
    param.setTemail("b_all");
    this.sendMessage(param);
    // 入群申请已通过 6
    param.setSessionMssageType(EventType.APPLY_ADOPT.getValue());
    this.sendMessage(param);
    // 入群申请已拒绝 7
    param.setSessionMssageType(EventType.APPLY_REFUSE.getValue());
    this.sendMessage(param);

    // 入群邀请 8
    param = init(groupTemail);
    param.setSessionMssageType(EventType.INVITATION.getValue());
    param.setTemail("c_all");
    this.sendMessage(param);
    // 入群邀请已通过 14
    param.setSessionMssageType(EventType.INVITATION_ADOPT.getValue());
    this.sendMessage(param);
    // 入群邀请已拒绝 9
    param.setSessionMssageType(EventType.INVITATION_REFUSE.getValue());
    this.sendMessage(param);

    // 新成员入群 10
    param = init(groupTemail);
    param.setSessionMssageType(EventType.ADD_MEMBER.getValue());
    param.setType(MemberRole.NORMAL.getValue());
    param.setTemail("b_all");
    param.setName("b_all_name");
    this.sendMessage(param);
    param.setTemail("c_all");
    param.setName("c_all_name");
    this.sendMessage(param);
    param.setTemail("d_all");
    param.setName("d_all_name");
    this.sendMessage(param);
    param.setTemail("e_all");
    param.setName("e_all_name");
    this.sendMessage(param);
    param.setTemail("f_all");
    param.setName("f_all_name");
    this.sendMessage(param);

    // 消息发送 0
    param = init(groupTemail);
    param.setSessionMssageType(EventType.RECEIVE.getValue());
    param.setTemail("a_all");
    param.setMsgid("g_all_1");
    param.setToMsg("这是一条群聊测试消息！");
    param.setSeqNo(1L);
    this.sendMessage(param);
    // 消息已拉取 1
    param = init(groupTemail);
    param.setSessionMssageType(EventType.PULLED.getValue());
    param.setTemail("b_all");
    param.setMsgid("g_all_1, g_all_2, g_all_3");
    this.sendMessage(param);
    // 消息已撤回 2
    param = init(groupTemail);
    param.setSessionMssageType(EventType.RETRACT.getValue());
    param.setTemail("a_all");
    param.setMsgid("g_all_1");
    this.sendMessage(param);
    // 回复消息 18
    param = init(groupTemail);
    param.setSessionMssageType(EventType.REPLY.getValue());
    param.setTemail("b_all");
    param.setMsgid("g_reply_a");
    param.setToMsg("这是一条回复消息！");
    param.setSeqNo(1L);
    param.setParentMsgId("g_all_1");
    param.setAt("c_all;d_all");
    this.sendMessage(param);

    // 群成员被移除 11
    param = init(groupTemail);
    param.setSessionMssageType(EventType.DELETE_MEMBER.getValue());
    param.setAdminName("测试管理员名称");
    param.setTemail(gson.toJson(Arrays.asList("e_all", "f_all")));
    param.setName(gson.toJson(Arrays.asList("e_all_name", "f_all_name")));
    this.sendMessage(param);

    // 已退出群聊 15
    param = init(groupTemail);
    param.setSessionMssageType(EventType.LEAVE_GROUP.getValue());
    param.setTemail("d_all");
    this.sendMessage(param);

    // 群名片更新 16
    param = init(groupTemail);
    param.setSessionMssageType(EventType.UPDATE_GROUP_CARD.getValue());
    param.setTemail("a_all");
    param.setGroupName("测试组名");
    param.setName("测试当事人名");
    param.setAdminName("测试管理员名称");
    this.sendMessage(param);

    // 群已解散 12
    param = init(groupTemail);
    param.setSessionMssageType(EventType.DELETE_GROUP.getValue());
    param.setTemail("a_all");
    this.sendMessage(param);
  }

  private MailAgentGroupChatParams init(String groupTemail) {
    MailAgentGroupChatParams param = new MailAgentGroupChatParams();
    param.setHeader("header");
    param.setGroupTemail(groupTemail);
    return param;
  }

  private void sendMessage(MailAgentGroupChatParams param) throws Exception {
    param.setxPacketId(UUID.randomUUID().toString());
    rocketMqProducer.sendMessage(gson.toJson(param), TOPIC, "", "");
    Thread.sleep(2000);
  }
}