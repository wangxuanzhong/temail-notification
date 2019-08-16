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

package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member.MemberRole;
import com.syswin.temail.notification.main.dto.MailAgentParamsFull;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GroupChatServiceImplTest {

  private final String TEST_GROUP = "g";
  private final String TEST_GROUP_MSG_ID = "g_";

  private final boolean useMQ = false;
  private final boolean isMock = true;

  private MailAgentParamsFull params = new MailAgentParamsFull();
  private Gson gson = new Gson();

  @Value("${spring.rocketmq.topics.mailAgent.groupChat}")
  private String topic;

  @MockBean
  private UnreadService unreadService;
  @Autowired
  private IMqProducer iMqProducer;
  @Autowired
  private RedisServiceImpl redisService;
  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private MemberMapper memberMapper;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private GroupChatServiceImpl groupChatService;

  @Before
  public void setUp() {
    if (!useMQ && isMock) {
      groupChatService = new GroupChatServiceImpl(unreadService, mqProducerMock, redisServiceMock, eventMapper,
          memberMapper);
    } else {
      groupChatService = new GroupChatServiceImpl(unreadService, iMqProducer, redisService, eventMapper, memberMapper);
    }

    params.setHeader(ConstantMock.HEADER);
    params.setGroupTemail(TEST_GROUP);
    params.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
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
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType PULLED 1
   */
  @Test
  public void testEventTypePulled() throws Exception {
    params.setSessionMessageType(EventType.PULLED.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1," + TEST_GROUP_MSG_ID + "2," + TEST_GROUP_MSG_ID + "3");
    params.setTemail("b");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType RETRACT 2
   */
  @Test
  public void testEventTypeRetract() throws Exception {
    params.setSessionMessageType(EventType.RETRACT.getValue());
    params.setMsgid(TEST_GROUP_MSG_ID + "1");
    params.setTemail("a");
    this.sendMessage(params, params.getGroupTemail());
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
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType ADD_MEMBER 10 新成员入群
   */
  @Test
  public void testEventTypeAddMember() throws Exception {
    params.setSessionMessageType(EventType.ADD_MEMBER.getValue());
    params.setType(MemberRole.NORMAL.getValue());
    params.setSessionExtData("sessionExtData");
    params.setMemberExtData("memberExtData");

    params.setTemail("h");
    params.setName("hh");
    this.sendMessage(params, params.getGroupTemail());

    params.setTemail("i");
    params.setName("ii");
    this.sendMessage(params, params.getGroupTemail());

    params.setTemail("j");
    params.setName("jj");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType DELETE_GROUP 12
   */
  @Test
  public void testEventTypeDeleteGroup() throws Exception {
    params.setSessionMessageType(EventType.DELETE_GROUP.getValue());
    params.setGroupTemail("g");
    params.setTemail("a");
    params.setAdminName("aa");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType ADD_GROUP 13
   */
  @Test
  public void testEventTypeAddGroup() throws Exception {
    params.setSessionMessageType(EventType.ADD_GROUP.getValue());
    params.setGroupTemail("g2");
    params.setTemail("a");
    params.setExtData("ExtData");
    params.setSharedKey("sharedKey");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType DELETE_MEMBER 11 群成员被移除
   */
  @Test
  public void testEventTypeDeleteMember() throws Exception {
    params.setSessionMessageType(EventType.DELETE_MEMBER.getValue());
    params.setAdminName("测试触发人名");
    params.setTemail(gson.toJson(Arrays.asList("d", "e", "f")));
    params.setName(gson.toJson(Arrays.asList("dd", "ee", "ff")));
    params.setMemberExtData(gson.toJson(Arrays.asList("d ext data", "e ext data", "f ext data")));
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType LEAVE_GROUP 15 已退出群聊
   */
  @Test
  public void testEventTypeLeaveGroup() throws Exception {
    params.setSessionMessageType(EventType.LEAVE_GROUP.getValue());
    params.setTemail("c");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType APPLY 5
   */
  @Test
  public void testEventTypeApply() throws Exception {
    params.setSessionMessageType(EventType.APPLY.getValue());
    params.setTemail("d");
    params.setSessionExtData("SessionExtData");
    params.setMemberExtData("MemberExtData");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType APPLY_ADOPT 6
   */
  @Test
  public void testEventTypeApplyAdopt() throws Exception {
    params.setSessionMessageType(EventType.APPLY_ADOPT.getValue());
    params.setTemail("c");
    params.setSessionExtData("SessionExtData");
    params.setMemberExtData("MemberExtData");
    params.setSharedKey("sharedKey");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType APPLY_REFUSE 7
   */
  @Test
  public void testEventTypeApplyRefuse() throws Exception {
    params.setSessionMessageType(EventType.APPLY_REFUSE.getValue());
    params.setTemail("c");
    params.setSessionExtData("SessionExtData");
    params.setMemberExtData("MemberExtData");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType INVITATION 8 入群邀请
   */
  @Test
  public void testEventTypeInvitation() throws Exception {
    params.setSessionMessageType(EventType.INVITATION.getValue());
    params.setTemail("c");
    params.setSharedKey("sharedKey");
    params.setInviteExtData("inviteExtData");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType INVITATION_REFUSE 9
   */
  @Test
  public void testEventTypeInvitationRefuse() throws Exception {
    params.setSessionMessageType(EventType.INVITATION_REFUSE.getValue());
    params.setTemail("c");
    params.setSessionExtData("SessionExtData");
    params.setMemberExtData("MemberExtData");
    params.setSharedKey("sharedKey");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType INVITATION_ADOPT 14
   */
  @Test
  public void testEventTypeInvitationAdopt() throws Exception {
    params.setSessionMessageType(EventType.INVITATION_ADOPT.getValue());
    params.setTemail("c");
    params.setSessionExtData("SessionExtData");
    params.setMemberExtData("MemberExtData");
    params.setSharedKey("sharedKey");
    this.sendMessage(params, params.getGroupTemail());
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
    params.setExtData("ExtData");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType REPLY 18 回复消息
   */
  @Test
  public void testEventTypeReply() throws Exception {
    params.setSessionMessageType(EventType.REPLY.getValue());
    params.setMsgid("reply_g_1");
    params.setTemail("e");
    params.setParentMsgId("at1");
    params.setToMsg("这是一条回复消息！");
    params.setSeqNo(1L);
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType REPLY_RETRACT 19 回复消息已撤回
   */
  @Test
  public void testEventTypeReplyRetract() throws Exception {
    params.setSessionMessageType(EventType.REPLY_RETRACT.getValue());
    params.setMsgid("reply_g_2");
    params.setTemail("e");
    params.setParentMsgId("g_1");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType REPLY_DELETE 20 回复消息已删除
   */
  @Test
  public void testEventTypeReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.REPLY_DELETE.getValue());
    params.setMsgid(gson.toJson(Arrays.asList("reply_1", "reply_3", "reply_4")));
    params.setTemail("e");
    params.setParentMsgId("g_1");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType GROUP_ARCHIVE 27 群聊归档
   */
  @Test
  public void testEventTypeGroupArchive() throws Exception {
    params.setSessionMessageType(EventType.GROUP_ARCHIVE.getValue());
    params.setTemail("b");
    this.sendMessage(params, params.getTemail());
  }

  /**
   * EventType GROUP_ARCHIVE_CANCEL 28 群聊归档取消
   */
  @Test
  public void testEventTypeGroupArchiveCancel() throws Exception {
    params.setSessionMessageType(EventType.GROUP_ARCHIVE_CANCEL.getValue());
    params.setTemail("b");
    this.sendMessage(params, params.getTemail());
  }

  /**
   * EventType GROUP_STICK 31 群聊置顶
   */
  @Test
  public void testEventTypeGroupStick() throws Exception {
    params.setSessionMessageType(EventType.GROUP_STICK.getValue());
    params.setMsgid("1");
    params.setTemail("a");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType GROUP_STICK_CANCEL 32 群聊置顶取消
   */
  @Test
  public void testEventTypeGroupStickCancel() throws Exception {
    params.setSessionMessageType(EventType.GROUP_STICK_CANCEL.getValue());
    params.setMsgid("1");
    params.setTemail("b");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType BLACKLIST 40 群黑名单
   */
  @Test
  public void testEventTypeBlacklist() throws Exception {
    params.setSessionMessageType(EventType.BLACKLIST.getValue());
    params.setTemail(gson.toJson(Arrays.asList("b", "e", "f")));
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType BLACKLIST_CANCEL 41 群黑名单取消
   */
  @Test
  public void testEventTypeBlacklistCancel() throws Exception {
    params.setSessionMessageType(EventType.BLACKLIST_CANCEL.getValue());
    params.setTemail(gson.toJson(Arrays.asList("e", "f")));
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType RECEIVE_AT 44 @消息发送
   */
  @Test
  public void testEventTypeReceiveAt() throws Exception {
    params.setSessionMessageType(EventType.RECEIVE_AT.getValue());
    params.setMsgid("at1");
    params.setFrom("a");
    params.setTemail("e");
    params.setTo("e");
    params.setGroupTemail(this.TEST_GROUP);
    params.setSeqNo(1L);
    params.setToMsg("这是一条群聊测试@消息！");
    params.setAt(gson.toJson(Arrays.asList("e", "f")));
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType DELETE_AT 45 @消息删除
   */
  @Test
  public void testEventTypeDeleteAt() throws Exception {
    params.setSessionMessageType(EventType.DELETE_AT.getValue());
    params.setMsgid("at1");
    params.setTemail("a");
    params.setGroupTemail(this.TEST_GROUP);
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType ADD_ADMIN 46 添加群管理员
   */
  @Test
  public void testEventTypeAddAdmin() throws Exception {
    params.setSessionMessageType(EventType.ADD_ADMIN.getValue());
    params.setTemail("b");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType DELETE_ADMIN 47 移除群管理员
   */
  @Test
  public void testEventTypeDeleteAdmin() throws Exception {
    params.setSessionMessageType(EventType.DELETE_ADMIN.getValue());
    params.setTemail("b");
    this.sendMessage(params, params.getGroupTemail());
  }

  /**
   * EventType CHANGE_MEMBER_EXT_DATA 57 修改memberExtData
   */
  @Test
  public void testEventTypeChangeMemberExtData() throws Exception {
    params.setSessionMessageType(EventType.CHANGE_MEMBER_EXT_DATA.getValue());
    params.setTemail("b");
    params.setMemberExtData("memberExtData");
    this.sendMessage(params, params.getGroupTemail());
  }


  private void sendMessage(MailAgentParamsFull param, String tags) throws Exception {
    sendMessage(param, false, tags);
  }

  private void sendMessage(MailAgentParamsFull param, boolean isSamePacket, String tags) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
      HashMap header = new HashMap<>();
      header.put("sender","sender");
      param.setHeader(gson.toJson(header));
    }
    if (useMQ) {
      iMqProducer.sendMessage(gson.toJson(param), topic, tags, "");
      Thread.sleep(2000);
    } else {
      groupChatService.handleMqMessage(gson.toJson(param), tags);
    }
  }
}