package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.CDTPResponse;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.MailAgentGroupChatParams;
import com.syswin.temail.notification.main.domains.MemberRepository;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationGroupChatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String GROUP_RMQ_TAG = "";
  private final String GROUP_RMQ_KEYS = "";

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventRepository eventRepository;
  private final MemberRepository memberRepository;
  private final Gson gson;

  @Autowired
  public NotificationGroupChatService(RocketMqProducer rocketMqProducer, RedisService redisService,
      EventRepository eventRepository,
      MemberRepository memberRepository) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.eventRepository = eventRepository;
    this.memberRepository = memberRepository;
    gson = new Gson();
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) throws Exception {
    MailAgentGroupChatParams params = gson.fromJson(body, MailAgentGroupChatParams.class);
    Event event = new Event(params.getMsgid(), params.getFromSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(),
        params.getTimestamp(), params.getSessionMssageType(), params.getGroupTemail(), params.getTemail(), params.getType());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("群聊收到的事件类型为：" + Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case RETRACT:
        event.notifyToAll();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentGroupChatParams.MSG_ID_SPLIT)) {
          event.setFrom(event.getGroupTemail());
          event.setTo(event.getTemail());
          event.setEventSeqId(redisService.getNextSeq(event.getTo()));
          event.setMsgId(msgId);
          eventRepository.insert(event);
          sendSingleMessage(event, header);
        }
        break;
      case ADD_GROUP:
        event.setRole(MemberRole.ADMIN.getValue());
        memberRepository.insert(event);
        break;
      case DELETE_GROUP:
        event.setTemail(null);
        memberRepository.deleteGroupMember(event);
        event.notifyToAll();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
      case ADD_MEMBER:
        memberRepository.insert(event);
        event.notifyToAll();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
      case DELETE_MEMBER:
        // 通知所有人
        memberRepository.deleteGroupMember(event);
        event.notifyToAll();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        // 通知当事人被移除群聊
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        event.setEventSeqId(redisService.getNextSeq(event.getTo()));
        eventRepository.insert(event);
        sendSingleMessage(event, header);
        break;
      case LEAVE_GROUP:
        memberRepository.deleteGroupMember(event);
        event.notifyToAll();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
      case APPLY:
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        // 通知所有管理员
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        // 通知申请人
        event.removeEventMsgId();
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        event.setEventSeqId(redisService.getNextSeq(event.getTo()));
        eventRepository.insert(event);
        sendSingleMessage(event, header);
        break;
      case INVITATION:
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        event.setEventSeqId(redisService.getNextSeq(event.getTo()));
        eventRepository.insert(event);
        sendSingleMessage(event, header);
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        event.notifyToAdmin();
        insertGroupEvent(event);
        sendGroupMessage(event, header);
        break;
    }
  }

  /**
   * 群消息入库
   */
  private void insertGroupEvent(Event event) {
    List<String> tos = memberRepository.selectByGroupTemail(event);
    tos.remove(event.getTemail());
    for (String to : tos) {
      event.setEventSeqId(redisService.getNextSeq(to));
      event.setFrom(event.getGroupTemail());
      event.setTo(to);
      eventRepository.insert(event);
    }
  }


  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    rocketMqProducer.sendMessage(gson.toJson(new CDTPResponse(event.getTo(), header, event)), GROUP_RMQ_TAG, GROUP_RMQ_KEYS);
  }

  /**
   * 发送群消息
   */
  private void sendGroupMessage(Event event, String header)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    List<String> tos = memberRepository.selectByGroupTemail(event);
    tos.remove(event.getTemail());
    for (String to : tos) {
      rocketMqProducer.sendMessage(gson.toJson(new CDTPResponse(to, header, event)), GROUP_RMQ_TAG, GROUP_RMQ_KEYS);
    }
  }
}
