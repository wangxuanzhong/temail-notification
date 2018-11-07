package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.foundation.application.SequenceService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.MemberRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
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

  private final RocketMqProducer rocketMqProducer;
  private final SequenceService sequenceService;
  private final EventRepository eventRepository;
  private final MemberRepository memberRepository;
  private final JsonService jsonService;

  @Autowired
  public NotificationGroupChatService(RocketMqProducer rocketMqProducer, SequenceService sequenceService, EventRepository eventRepository,
      MemberRepository memberRepository, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.sequenceService = sequenceService;
    this.eventRepository = eventRepository;
    this.memberRepository = memberRepository;
    this.jsonService = jsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentGroupChatParams params = jsonService.fromJson(body, MailAgentGroupChatParams.class);
    Event event = new Event(params.getSessionMssageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getGroupTemail(), params.getTemail(), params.getType(), params.getName(),
        params.getAdminName(), params.getGroupName(), params.getAt());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("群聊收到的事件类型为：" + Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case RETRACT:
      case REPLY:
        event.notifyToAll();
        sendGroupMessage(event, header);
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentGroupChatParams.MSG_ID_SPLIT)) {
          event.setFrom(event.getGroupTemail());
          event.setTo(event.getTemail());
          event.setMsgId(msgId);
          if (eventRepository.selectEvent(event).size() == 0) {
            this.insert(event);
            sendSingleMessage(event, header);
          } else {
            LOGGER.info("消息{}已拉取，不重复处理，时间戳为：{}", msgId, event.getTimestamp());
          }
        }
        break;
      case ADD_GROUP:
        event.setRole(MemberRole.ADMIN.getValue());
        memberRepository.insert(event);
        break;
      case DELETE_GROUP:
        event.notifyToAll();
        sendGroupMessage(event, header);
        event.setTemail(null);
        memberRepository.deleteGroupMember(event);
        break;
      case ADD_MEMBER:
        // 校验群成员是否已存在，不存在时添加到数据库
        List<String> members = memberRepository.selectByGroupTemail(event);
        if (!members.contains(event.getTemail())) {
          memberRepository.insert(event);
        } else {
          LOGGER.info("{}已经是群{}的成员，不重复添加，时间戳为：{}", event.getTemail(), event.getGroupTemail(), event.getTimestamp());
        }
        event.notifyToAll();
        sendGroupMessage(event, header);
        break;
      case DELETE_MEMBER:
        List<String> temails = jsonService.fromJson(event.getTemail(), List.class);
        List<String> names = jsonService.fromJson(event.getName(), List.class);

        if (temails.size() != names.size()) {
          LOGGER.error("移除群成员temail和name不对应：temails:{}, names:{}", temails, names);
        }

        // 删除当事人
        for (int i = 0; i < temails.size(); i++) {
          event.setTemail(temails.get(i));
          memberRepository.deleteGroupMember(event);
        }

        for (int i = 0; i < temails.size(); i++) {
          event.setTemail(temails.get(i));
          event.setName(names.get(i));
          // 通知所有人
          event.notifyToAll();
          sendGroupMessage(event, header);
          // 通知当事人被移除群聊
          event.setFrom(event.getGroupTemail());
          event.setTo(temails.get(i));
          event.setTemail(temails.get(i));
          this.insert(event);
          sendSingleMessage(event, header);
        }

        break;
      case LEAVE_GROUP:
        memberRepository.deleteGroupMember(event);
        event.notifyToAll();
        sendGroupMessage(event, header);
        break;
      case APPLY:
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        sendGroupMessage(event, header);
        break;
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        // 通知所有管理员
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        sendGroupMessage(event, header);
        // 通知申请人
        event.removeEventMsgId();
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        this.insert(event);
        sendSingleMessage(event, header);
        break;
      case INVITATION:
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        this.insert(event);
        sendSingleMessage(event, header);
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        event.notifyToAdmin();
        sendGroupMessage(event, header);
        break;
      case UPDATE_GROUP_CARD:
        event.notifyToAll();
        sendGroupMessage(event, header);
        break;
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(sequenceService);
    event.autoWriteExtendParam(jsonService);
    eventRepository.insert(event);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("向{}发送通知，通知类型为：{}", event.getTo(), Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());
    if (event.getTo() != null && !event.getTo().equals("")) {
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), header, jsonService.toJson(event))));
    }
  }

  /**
   * 发送群消息
   */
  private void sendGroupMessage(Event event, String header)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    List<String> tos = memberRepository.selectByGroupTemail(event);
    tos.remove(event.getTemail());
    event.setFrom(event.getGroupTemail());
    LOGGER.info("向{}发送通知，通知类型为：{}", tos, Objects.requireNonNull(EventType.getByValue(event.getEventType())).getDescription());
    for (String to : tos) {
      event.setTo(to);
      this.insert(event);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(to, header, jsonService.toJson(event))));
    }
  }
}
