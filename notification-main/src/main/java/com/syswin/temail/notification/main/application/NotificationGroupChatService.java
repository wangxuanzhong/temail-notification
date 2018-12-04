package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.MemberRole;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.MemberRepository;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.util.NotificationUtil;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationGroupChatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventRepository eventRepository;
  private final MemberRepository memberRepository;
  private final JsonService jsonService;

  @Autowired
  public NotificationGroupChatService(RocketMqProducer rocketMqProducer, RedisService redisService, EventRepository eventRepository,
      MemberRepository memberRepository, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
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
    Event event = new Event(params.getSessionMessageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getGroupTemail(), params.getTemail(), params.getType(), params.getName(),
        params.getAdminName(), params.getGroupName(), params.getAt(), params.getxPacketId());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("group chat params: \n" + params);
    LOGGER.info("group chat event type: " + Objects.requireNonNull(EventType.getByValue(event.getEventType())));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType() + "_" + event.getGroupTemail() + "_" + event.getTemail();
    if (!NotificationUtil.checkUnique(event, redisKey, eventRepository, redisService)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case RETRACT:
        event.notifyToAll();
        this.sendGroupMessage(event, header);
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentGroupChatParams.MSG_ID_SPLIT)) {
          event.setFrom(event.getGroupTemail());
          event.setTo(event.getTemail());
          event.setMsgId(msgId);
          if (eventRepository.selectEventsByMsgId(event).size() == 0) {
            this.sendSingleMessage(event, header);
          } else {
            LOGGER.info("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(jsonService.fromJson(event.getMsgId(), List.class));
        event.setMsgId(null);
        event.notifyToAll();
        this.sendGroupMessage(event, header);
        break;
      case ADD_GROUP:
        event.setRole(MemberRole.ADMIN.getValue());
        memberRepository.insert(event);
        break;
      case DELETE_GROUP:
        event.notifyToAll();
        this.sendGroupMessage(event, header);
        event.setTemail(null);
        memberRepository.deleteGroupMember(event);
        break;
      case ADD_MEMBER:
        // 校验群成员是否已存在，不存在时添加到数据库
        List<String> members = memberRepository.selectByGroupTemail(event);
        if (!members.contains(event.getTemail())) {
          // 添加唯一索引校验，防止并发问题
          try {
            memberRepository.insert(event);
          } catch (DuplicateKeyException e) {
            LOGGER.warn("add member duplicate exception：" + e);
          }
          event.notifyToAll();
          this.sendGroupMessage(event, header);
        } else {
          LOGGER.info("{} was group {} member，do nothing.", event.getTemail(), event.getGroupTemail());
        }
        break;
      case DELETE_MEMBER:
        List<String> temails = jsonService.fromJson(event.getTemail(), List.class);
        List<String> names = jsonService.fromJson(event.getName(), List.class);

        if (temails.size() != names.size()) {
          LOGGER.error("delete member temail and name mismatching, temails: {}, names: {}", temails, names);
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
          this.sendGroupMessage(event, header);
          // 通知当事人被移除群聊
          event.setFrom(event.getGroupTemail());
          event.setTo(temails.get(i));
          event.setTemail(temails.get(i));
          this.sendSingleMessage(event, header);
        }

        break;
      case LEAVE_GROUP:
        memberRepository.deleteGroupMember(event);
        event.notifyToAll();
        this.sendGroupMessage(event, header);
        break;
      case APPLY:
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        this.sendGroupMessage(event, header);
        break;
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        // 通知所有管理员
        event.notifyToAdmin();
        event.addEventMsgId(EventType.APPLY);
        this.sendGroupMessage(event, header);
        // 通知申请人
        event.removeEventMsgId();
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        this.sendSingleMessage(event, header);
        break;
      case INVITATION:
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        this.sendSingleMessage(event, header);
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        event.notifyToAdmin();
        this.sendGroupMessage(event, header);
        break;
      case UPDATE_GROUP_CARD:
        event.notifyToAll();
        this.sendGroupMessage(event, header);
        break;
      case REPLY:
        // 查询父消息的at字段
        Event condition = new Event();
        condition.setEventType(EventType.RECEIVE.getValue());
        condition.setMsgId(event.getParentMsgId());
        List<Event> events = eventRepository.selectEventsByMsgId(condition);
        event.setAt(events.get(0).autoReadExtendParam(jsonService).getAt());

        event.notifyToAll();
        this.sendGroupMessageWithOneEvent(event, header);
        break;
      case REPLY_RETRACT:
        event.notifyToAll();
        this.sendGroupMessageWithOneEvent(event, header);
        break;
      case REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(jsonService.fromJson(event.getMsgId(), List.class));
        event.setMsgId(null);
        event.notifyToAll();
        this.sendGroupMessageWithOneEvent(event, header);
        break;
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(redisService);
    event.autoWriteExtendParam(jsonService);
    eventRepository.insert(event);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, String header)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    LOGGER.info("send message to {}, event type: {}", event.getTo(), Objects.requireNonNull(EventType.getByValue(event.getEventType())));
    if (event.getTo() != null && !event.getTo().isEmpty()) {
      this.insert(event);
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
    LOGGER.info("send message to {}, event type: {}", tos, Objects.requireNonNull(EventType.getByValue(event.getEventType())));
    for (String to : tos) {
      event.setTo(to);
      this.insert(event);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(to, header, jsonService.toJson(event))));
    }
  }

  /**
   * 发送群消息，事件只存单条
   */
  private void sendGroupMessageWithOneEvent(Event event, String header)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    // 只插入一次数据
    event.setFrom(event.getGroupTemail());
    event.setTo(event.getGroupTemail());
    this.insert(event);

    List<String> tos = memberRepository.selectByGroupTemail(event);
    tos.remove(event.getTemail());
    LOGGER.info("send message to {}, event type: {}", tos, Objects.requireNonNull(EventType.getByValue(event.getEventType())));
    for (String to : tos) {
      event.setTo(to);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(to, header, jsonService.toJson(event))));
    }
  }
}
