package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventType;
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
  private final String PREFIX = "temail-notification-";
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
    params.setHeader("notification-header");
    params.setGroupTemail(TEST_GROUP);
//    params.setGroupName("测试组名");
//    params.setName("测试当事人名");
//    params.setAdminName("测试触发人名");
//    params.setTimestamp(System.currentTimeMillis());
    params.setxPacketId(PREFIX + UUID.randomUUID().toString());
  }

  /**
   * EventType RECEIVE 0
   */
  @Test
  public void testEventTypeReceive() throws Exception {
    params.setSessionMessageType(EventType.RECEIVE.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setTemail("a");
    params.setSeqNo(1L);
    params.setToMsg("这是一条群聊测试消息！");
    params.setAt("b,c,d");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType PULLED 1
   */
  @Test
  public void testEventTypePulled() throws Exception {
    params.setSessionMessageType(EventType.PULLED.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1," + TEST_GROUP_MSG_ID + "2," + TEST_GROUP_MSG_ID + "3");
    params.setTemail("b");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType RETRACT 2
   */
  @Test
  public void testEventTypeRetract() throws Exception {
    params.setSessionMessageType(EventType.RETRACT.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setTemail("a");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType DESTROYED 4 消息已删除
   */
  @Test
  public void testEventTypeDelete() throws Exception {
    params.setSessionMessageType(EventType.DELETE.getValue());
    params.setTemail("a");
    params.setAdminName("aa");
    params.setMsgid(gson.toJson(Arrays.asList("g-2", "g-3", "g-4")));
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType ADD_MEMBER 10
   */
  @Test
  public void testEventTypeAddMember() throws Exception {
//    params.setxPacketId("aaaaaaaaa");
    params.setSessionMessageType(EventType.ADD_MEMBER.getValue());
    params.setType(MemberRole.NORMAL.getValue());
    params.setTemail("d");
    params.setName("dd");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    params.setTemail("e");
    params.setName("ee");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    params.setTemail("f");
    params.setName("ff");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType DELETE_GROUP 12
   */
  @Test
  public void testEventTypeDeleteGroup() throws Exception {
    params.setSessionMessageType(EventType.DELETE_GROUP.getValue());
    params.setGroupTemail("g");
    params.setTemail("a");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType ADD_GROUP 13
   */
  @Test
  public void testEventTypeAddGroup() throws Exception {
    params.setSessionMessageType(EventType.ADD_GROUP.getValue());
    params.setGroupTemail("g2");
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
    params.setSessionMessageType(EventType.DELETE_MEMBER.getValue());
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType LEAVE_GROUP 15
   */
  @Test
  public void testEventTypeLeaveGroup() throws Exception {
    params.setSessionMessageType(EventType.LEAVE_GROUP.getValue());
    params.setTemail("c");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType APPLY 5
   */
  @Test
  public void testEventTypeApply() throws Exception {
    params.setSessionMessageType(EventType.APPLY.getValue());
    params.setTemail("c");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType APPLY_REFUSE 7
   */
  @Test
  public void testEventTypeApplyRefuse() throws Exception {
    params.setSessionMessageType(EventType.APPLY_REFUSE.getValue());
    params.setTemail("c");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType INVITATION 8
   */
  @Test
  public void testEventTypeInvitation() throws Exception {
    params.setSessionMessageType(EventType.INVITATION.getValue());
    params.setTemail("c");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType INVITATION_ADOPT 14
   */
  @Test
  public void testEventTypeInvitationAdopt() throws Exception {
    params.setSessionMessageType(EventType.INVITATION_ADOPT.getValue());
    params.setTemail("c");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType UPDATE_GROUP_CARD 16
   */
  @Test
  public void testEventTypeUpdateGroupCard() throws Exception {
    params.setSessionMessageType(EventType.UPDATE_GROUP_CARD.getValue());
    params.setTemail("a");
    params.setGroupName("测试组名");
    params.setName("测试当事人名");
    params.setAdminName("测试触发人名");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
  }

  /**
   * EventType REPLY 18 回复消息
   */
  @Test
  public void testEventTypeReply() throws Exception {
    params.setSessionMessageType(EventType.REPLY.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "reply_2");
    params.setTemail("c");
    params.setParentMsgId(TEST_GROUP_MSG_ID + "1");
    params.setToMsg("这是一条回复消息！");
    params.setSeqNo(1L);
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType REPLY_RETRACT 19 回复消息已撤回
   */
  @Test
  public void testEventTypeReplyRetract() throws Exception {
    params.setSessionMessageType(EventType.REPLY_RETRACT.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "reply_1");
    params.setTemail("c");
    params.setParentMsgId(TEST_GROUP_MSG_ID + "1");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }

  /**
   * EventType REPLY_DELETE 20 回复消息已删除
   */
  @Test
  public void testEventTypeReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.REPLY_DELETE.getValue());
    params.setMsgid(gson.toJson(Arrays.asList(TEST_GROUP_MSG_ID + "reply_2", TEST_GROUP_MSG_ID + "reply_3", TEST_GROUP_MSG_ID + "reply_4")));
    params.setTemail("c");
    params.setParentMsgId(TEST_GROUP_MSG_ID + "1");
    rocketMqProducer.sendMessage(gson.toJson(params), TOPIC, "", "");
    Thread.sleep(2000);
  }


  /**
   * 完成群聊流程
   *
   * 期待结果5 66 77 8 14 9 10*15 0*5 1*3 2*5 18*5 11*10 15*5 16*2 12*2 = 58
   */
  @Test
  public void testAll() throws Exception {
    MailAgentGroupChatParams param = null;
    String groupTemail = "g_all_" + UUID.randomUUID().toString();

    // 创建群 13
    param = init(groupTemail);
    param.setSessionMessageType(EventType.ADD_GROUP.getValue());
    param.setTemail("a_all");
    this.sendMessage(param);

    // 入群申请 5
    param = init(groupTemail);
    param.setSessionMessageType(EventType.APPLY.getValue());
    param.setTemail("b_all");
    this.sendMessage(param);
    // 入群申请已通过 6
    param.setSessionMessageType(EventType.APPLY_ADOPT.getValue());
    this.sendMessage(param);
    // 入群申请已拒绝 7
    param.setSessionMessageType(EventType.APPLY_REFUSE.getValue());
    this.sendMessage(param);

    // 入群邀请 8
    param = init(groupTemail);
    param.setSessionMessageType(EventType.INVITATION.getValue());
    param.setTemail("c_all");
    this.sendMessage(param);
    // 入群邀请已通过 14
    param.setSessionMessageType(EventType.INVITATION_ADOPT.getValue());
    this.sendMessage(param);
    // 入群邀请已拒绝 9
    param.setSessionMessageType(EventType.INVITATION_REFUSE.getValue());
    this.sendMessage(param);

    // 新成员入群 10
    param = init(groupTemail);
    param.setSessionMessageType(EventType.ADD_MEMBER.getValue());
    param.setType(MemberRole.NORMAL.getValue());
    param.setTemail("b_all");
    param.setName("b_all_name");
    this.sendMessage(param);
    param.setTemail("c_all");
    param.setName("c_all_name");
    this.sendMessage(param, true);
    param.setTemail("d_all");
    param.setName("d_all_name");
    this.sendMessage(param, true);
    param.setTemail("e_all");
    param.setName("e_all_name");
    this.sendMessage(param, true);
    param.setTemail("f_all");
    param.setName("f_all_name");
    this.sendMessage(param, true);

    // 消息发送 0
    param = init(groupTemail);
    param.setSessionMessageType(EventType.RECEIVE.getValue());
    param.setTemail("a_all");
    param.setMsgid("g_all_1");
    param.setToMsg("这是一条群聊测试消息！");
    param.setSeqNo(1L);
    this.sendMessage(param);
    // 消息已拉取 1
    param = init(groupTemail);
    param.setSessionMessageType(EventType.PULLED.getValue());
    param.setTemail("b_all");
    param.setMsgid("g_all_1, g_all_2, g_all_3");
    this.sendMessage(param);
    // 消息已撤回 2
    param = init(groupTemail);
    param.setSessionMessageType(EventType.RETRACT.getValue());
    param.setTemail("a_all");
    param.setMsgid("g_all_1");
    this.sendMessage(param);
    // 回复消息 18
    param = init(groupTemail);
    param.setSessionMessageType(EventType.REPLY.getValue());
    param.setTemail("b_all");
    param.setMsgid("g_reply_a");
    param.setToMsg("这是一条回复消息！");
    param.setSeqNo(1L);
    param.setParentMsgId("g_all_1");
    param.setAt("c_all;d_all");
    this.sendMessage(param);

    // 群成员被移除 11
    param = init(groupTemail);
    param.setSessionMessageType(EventType.DELETE_MEMBER.getValue());
    param.setAdminName("测试管理员名称");
    param.setTemail(gson.toJson(Arrays.asList("e_all", "f_all")));
    param.setName(gson.toJson(Arrays.asList("e_all_name", "f_all_name")));
    this.sendMessage(param);

    // 已退出群聊 15
    param = init(groupTemail);
    param.setSessionMessageType(EventType.LEAVE_GROUP.getValue());
    param.setTemail("d_all");
    this.sendMessage(param);

    // 群名片更新 16
    param = init(groupTemail);
    param.setSessionMessageType(EventType.UPDATE_GROUP_CARD.getValue());
    param.setTemail("a_all");
    param.setGroupName("测试组名");
    param.setName("测试当事人名");
    param.setAdminName("测试管理员名称");
    this.sendMessage(param);

    // 群已解散 12
    param = init(groupTemail);
    param.setSessionMessageType(EventType.DELETE_GROUP.getValue());
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
    sendMessage(param, false);
  }

  private void sendMessage(MailAgentGroupChatParams param, boolean isSamePacket) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(PREFIX + UUID.randomUUID().toString());
    }
    rocketMqProducer.sendMessage(gson.toJson(param), TOPIC, "", "");
    Thread.sleep(2000);
  }
}