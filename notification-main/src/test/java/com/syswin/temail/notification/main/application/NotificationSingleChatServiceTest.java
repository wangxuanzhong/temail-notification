package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.MailAgentParams;
import com.syswin.temail.notification.main.domains.params.MailAgentParams.TrashMsgInfo;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.mock.ConstantMock;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class NotificationSingleChatServiceTest {

  private final String TEST_FROM = "a";
  private final String TEST_TO = "b";

  private final boolean useMQ = false;
  private final boolean isMock = true;

  private MailAgentParams params = new MailAgentParams();
  private Gson gson = new Gson();

  @Value("${spring.rocketmq.topics.mailAgent.singleChat}")
  private String topic;

  @Autowired
  private IMqProducer iMqProducer;
  @Autowired
  private NotificationRedisService notificationRedisService;
  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private IJsonService iJsonService;

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceMock redisServiceMock = new RedisServiceMock();

  private NotificationSingleChatService notificationSingleChatService;

  @Before
  public void setUp() {
    if (!useMQ && isMock) {
      notificationSingleChatService = new NotificationSingleChatService(mqProducerMock, redisServiceMock, eventMapper, iJsonService);
    } else {
      notificationSingleChatService = new NotificationSingleChatService(iMqProducer, notificationRedisService, eventMapper, iJsonService);
    }

    params.setHeader(ConstantMock.HEADER);
    params.setFrom(TEST_FROM);
    params.setTo(TEST_TO);
  }

  /**
   * EventType RECEIVE 0 消息发送
   */
  @Test
  public void testEventTypeReceive() throws Exception {
    params.setSessionMessageType(EventType.RECEIVE.getValue());
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setToMsg("这是一条单聊测试消息！");
    params.setAuthor("a");
    params.setFilter(Arrays.asList("b", "c", "d"));

    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());

    params.setOwner(TEST_FROM);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType PULLED 1 消息已拉取
   */
  @Test
  public void testEventTypePulled() throws Exception {
    params.setSessionMessageType(EventType.PULLED.getValue());
    params.setMsgid("1,2,3");
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType RETRACT 2 消息已撤回
   */
  @Test
  public void testEventTypeRetract() throws Exception {
    params.setSessionMessageType(EventType.RETRACT.getValue());
    params.setMsgid("2");
    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());

    params.setOwner(TEST_FROM);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType DESTROYED 3 消息已焚毁
   */
  @Test
  public void testEventTypeDestroyed() throws Exception {
    params.setSessionMessageType(EventType.DESTROYED.getValue());
    params.setMsgid("2");
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType DESTROYED 4 消息已删除
   */
  @Test
  public void testEventTypeDelete() throws Exception {
    // 删除事件from和to与事件业务相反
//    params.setFrom(TEST_TO);
//    params.setTo(TEST_FROM);
    params.setSessionMessageType(EventType.DELETE.getValue());

    // 批量删除消息
    params.setMsgid(gson.toJson(Arrays.asList("2", "3", "4")));
    this.sendMessage(params, params.getFrom());

    // 删除会话
    params.setMsgid(null);
    params.setDeleteAllMsg(false);
//    this.sendMessage(params);

    // 删除会话和消息
    params.setDeleteAllMsg(true);
//    this.sendMessage(params);
  }

  /**
   * EventType DESTROY 17 阅后即焚消息发送
   */
  @Test
  public void testEventTypeDestroy() throws Exception {
    params.setSessionMessageType(EventType.DESTROY.getValue());
    params.setMsgid("5");
    params.setSeqNo(1L);
    params.setToMsg("这是一条单聊阅后即焚测试消息！");

    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());

    params.setOwner(TEST_FROM);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType REPLY 18 回复消息
   */
  @Test
  public void testEventTypeReply() throws Exception {
    params.setSessionMessageType(EventType.REPLY.getValue());
    params.setMsgid("reply_3");
    params.setParentMsgId("1");
    params.setSeqNo(1L);
    params.setToMsg("这是一条单聊回复测试消息！");

    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());

    params.setOwner(TEST_FROM);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType REPLY_RETRACT 19 回复消息已撤回
   */
  @Test
  public void testEventTypeReplyRetract() throws Exception {
    params.setSessionMessageType(EventType.REPLY_RETRACT.getValue());
    params.setMsgid("reply_1");
    params.setParentMsgId("1");
    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());

    params.setOwner(TEST_FROM);
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType REPLY_DELETE 20 回复消息已删除
   */
  @Test
  public void testEventTypeReplyDelete() throws Exception {
    params.setSessionMessageType(EventType.REPLY_DELETE.getValue());
    params.setMsgid(gson.toJson(Arrays.asList("reply_5", "reply_3", "reply_4")));
    params.setParentMsgId("1");
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType ARCHIVE 33 归档
   */
  @Test
  public void testEventTypeArchive() throws Exception {
    params.setSessionMessageType(EventType.ARCHIVE.getValue());
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType ARCHIVE_CANCEL 34 归档取消
   */
  @Test
  public void testEventTypeArchiveCancel() throws Exception {
    params.setSessionMessageType(EventType.ARCHIVE_CANCEL.getValue());
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType TRASH 35 移送废纸篓
   */
  @Test
  public void testEventTypeTrash() throws Exception {
    params.setSessionMessageType(EventType.TRASH.getValue());
    params.setMsgid(gson.toJson(Arrays.asList("2", "3", "4")));
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType TRASH_CANCEL 36 废纸篓消息还原
   */
  @Test
  public void testEventTypeTrashCancel() throws Exception {
    params.setSessionMessageType(EventType.TRASH_CANCEL.getValue());
    params.setFrom(null);
    params.setTo(null);
    params.setOwner("a");
    List<TrashMsgInfo> infos = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      infos.add(new TrashMsgInfo("a", "b", String.valueOf(i)));
    }
    params.setTrashMsgInfo(gson.toJson(infos));
    this.sendMessage(params, params.getOwner());
  }

  /**
   * EventType TRASH_DELETE 37 废纸篓消息还原
   */
  @Test
  public void testEventTypeTrashDelete() throws Exception {
    params.setSessionMessageType(EventType.TRASH_DELETE.getValue());
    params.setFrom(null);
    params.setTo(null);
    params.setOwner("a");
    List<TrashMsgInfo> infos = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      infos.add(new TrashMsgInfo("a", "b", String.valueOf(i)));
    }
    params.setTrashMsgInfo(gson.toJson(infos));
    this.sendMessage(params, params.getOwner());
  }

  /**
   * EventType DO_NOT_DISTURB 33 设置免打扰
   */
  @Test
  public void testEventDoNotDisturb() throws Exception {
    params.setSessionMessageType(EventType.DO_NOT_DISTURB.getValue());
    this.sendMessage(params, params.getFrom());
  }

  /**
   * EventType CROSS_DOMAIN 51 跨域事件消息
   */
  @Test
  public void testEventTypeCrossDomain() throws Exception {
    params.setSessionMessageType(EventType.CROSS_DOMAIN.getValue());
    params.setMsgid("1");
    params.setSeqNo(1L);
    params.setToMsg("这是一条跨域事件消息！");
    params.setFrom("g");
    params.setOwner(TEST_TO);
    this.sendMessage(params, params.getFrom());
  }


  private void sendMessage(MailAgentParams param, String tags) throws Exception {
    sendMessage(param, false, tags);
  }

  private void sendMessage(MailAgentParams param, boolean isSamePacket, String tags) throws Exception {
    if (!isSamePacket) {
      param.setxPacketId(ConstantMock.PREFIX + UUID.randomUUID().toString());
    }
    if (useMQ) {
      iMqProducer.sendMessage(gson.toJson(param), topic, tags, "");
      Thread.sleep(2000);
    } else {
      notificationSingleChatService.handleMqMessage(gson.toJson(param), tags);
    }
  }
}
