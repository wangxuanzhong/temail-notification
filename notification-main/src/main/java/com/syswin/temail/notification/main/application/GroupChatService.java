package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member.MemberRole;
import com.syswin.temail.notification.main.domains.params.MailAgentGroupChatParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
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
public class GroupChatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RocketMqProducer rocketMqProducer;
  private final RedisService redisService;
  private final EventMapper eventMapper;
  private final MemberMapper memberMapper;
  private final JsonService jsonService;

  @Autowired
  public GroupChatService(RocketMqProducer rocketMqProducer, RedisService redisService, EventMapper eventMapper,
      MemberMapper memberMapper, JsonService jsonService) {
    this.rocketMqProducer = rocketMqProducer;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
    this.memberMapper = memberMapper;
    this.jsonService = jsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    MailAgentGroupChatParams params = jsonService.fromJson(body, MailAgentGroupChatParams.class);
    Event event = new Event(params.getSessionMessageType(), params.getMsgid(), params.getParentMsgId(), params.getSeqNo(), params.getToMsg(),
        params.getFrom(), params.getTo(), params.getTimestamp(), params.getGroupTemail(), params.getTemail(), params.getType(), params.getName(),
        params.getAdminName(), params.getGroupName(), params.getAt(), params.getxPacketId());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("group chat params: {}", params);
    LOGGER.info("group chat event type: {}", EventType.getByValue(event.getEventType()));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType() + "_" + event.getGroupTemail() + "_" + event.getTemail();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
        event.notifyToAll();
        this.sendGroupMessageToAll(event, EventType.GROUP_RECEIVE.getValue(), header, tags);
        break;
      case RETRACT:
        event.notifyToAll();
        this.sendGroupMessageToAll(event, EventType.GROUP_RETRACT.getValue(), header, tags);
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentGroupChatParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventMapper.selectEventsByMsgId(event).size() == 0) {
            this.sendSingleMessage(event, EventType.GROUP_PULLED.getValue(), header, tags);
          } else {
            LOGGER.warn("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(jsonService.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        event.notifyToAll();
        this.sendGroupMessageToAll(event, EventType.GROUP_DELETE.getValue(), header, tags);
        break;
      case ADD_GROUP:
        event.setRole(MemberRole.ADMIN.getValue());
        memberMapper.insert(event);
        this.sendSingleMessage(event, header, tags);
        break;
      case DELETE_GROUP:
        event.notifyToAll();
        this.sendGroupMessageToAll(event, header, tags);
        event.setTemail(null);
        memberMapper.deleteGroupMember(event);
        break;
      case ADD_MEMBER: // 只通知被添加的人
        // 校验群成员是否已存在，不存在时添加到数据库
        List<String> members = memberMapper.selectMember(event);
        if (!members.contains(event.getTemail())) {
          // 添加唯一索引校验，防止并发问题
          try {
            memberMapper.insert(event);
          } catch (DuplicateKeyException e) {
            LOGGER.warn("add member duplicate exception: ", e);
            break;
          }
          this.sendSingleMessage(event, header, tags);
        } else {
          LOGGER.warn("{} was group {} member, do nothing.", event.getTemail(), event.getGroupTemail());
        }
        break;
      case DELETE_MEMBER: // 只通知被删除的人
        List<String> temails = jsonService.fromJson(event.getTemail(), new TypeToken<List<String>>() {
        }.getType());
        List<String> names = jsonService.fromJson(event.getName(), new TypeToken<List<String>>() {
        }.getType());

        if (temails.size() != names.size()) {
          LOGGER.error("delete member temail and name mismatching, temails: {}, names: {}", temails, names);
          break;
        }

        // 删除当事人
        for (String temail : temails) {
          event.setTemail(temail);
          memberMapper.deleteGroupMember(event);
        }

        // 通知当事人被移除群聊
        for (int i = 0; i < temails.size(); i++) {
          event.setName(names.get(i));
          event.setTemail(temails.get(i));
          this.sendSingleMessage(event, header, tags);
        }
        break;
      case LEAVE_GROUP: // 只通知当事人
        memberMapper.deleteGroupMember(event);
        // 通知当事人
        this.sendSingleMessage(event, header, tags);
        break;
      case APPLY:
        event.notifyToAdmin();
        this.sendGroupMessageToAll(event, header, tags);
        break;
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        // 通知所有管理员，提供多端同步功能
        event.notifyToAdmin();
        this.sendGroupMessageToAll(event, header, tags);
        // 通知申请人
        this.sendSingleMessage(event, header, tags);
        break;
      case INVITATION:
        this.sendSingleMessage(event, header, tags);
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        event.notifyToAdmin();
        this.sendGroupMessageToAll(event, header, tags);
        // 通知自己，提供多端同步功能
        this.sendSingleMessage(event, header, tags);
        break;
      case UPDATE_GROUP_CARD:
        event.notifyToAll();
        this.sendGroupMessageToAll(event, header, tags);
        break;
      case REPLY:
      case REPLY_RETRACT:
        event.notifyToAll();
        this.sendReplyMessage(event, header, tags);
        break;
      case REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(jsonService.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        event.notifyToAll();
        this.sendReplyMessage(event, header, tags);
        break;
      case GROUP_ARCHIVE:
      case GROUP_ARCHIVE_CANCEL:
      case GROUP_SESSION_HIDDEN:
      case GROUP_DO_NOT_DISTURB:  // 暂时无此事件
      case GROUP_DO_NOT_DISTURB_CANCEL:  // 暂时无此事件
        this.sendSingleMessage(event, header, tags);
        break;
      case GROUP_STICK:
      case GROUP_STICK_CANCEL:
        event.notifyToAll();
        this.sendGroupMessageToAll(event, header, tags);
        break;
      case BLACKLIST:
      case BLACKLIST_CANCEL:
        event.notifyToAdmin();
        temails = jsonService.fromJson(event.getTemail(), new TypeToken<List<String>>() {
        }.getType());
        for (String temail : temails) {
          event.setTemail(temail);
          this.sendGroupMessageToAll(event, header, tags);
        }
        break;
      case RECEIVE_AT:  // @消息下发时为多条，temail为发送者，to为接收者
        event.setFrom(params.getGroupTemail()); // 群消息统一from是群
        this.sendSingleMessageDirectly(event, header, tags);
        break;
      case DELETE_AT:
        // 查询父消息，如果是@消息则只发送给@的人，否则发送给所有人
        Event condition = new Event();
        condition.setEventType(EventType.RECEIVE_AT.getValue());
        condition.setMsgId(event.getMsgId());

        List<Event> events = eventMapper.selectEventsByMsgId(condition);
        if (events.isEmpty()) {
          LOGGER.warn("do not found source message!");
          break;
        } else {
          Event parentEvent = events.get(0).autoReadExtendParam(jsonService);
          List<String> tos = jsonService.fromJson(parentEvent.getAt(), new TypeToken<List<String>>() {
          }.getType());
          // 添加原消息发送者
          tos.add(parentEvent.getTemail());
          this.sendGroupMessage(event, tos, event.getEventType(), header, tags);
        }
        break;
      case ADD_ADMIN:
        event.setRole(MemberRole.ADMIN.getValue());
        memberMapper.updateRole(event);
        event.notifyToAll();
        this.sendGroupMessageToAll(event, header, tags);
        break;
      case DELETE_ADMIN:
      case ABANDON_ADMIN:
        event.setRole(MemberRole.NORMAL.getValue());
        memberMapper.updateRole(event);
        event.notifyToAll();
        this.sendGroupMessageToAll(event, header, tags);
        break;
      default:
        LOGGER.warn("unsupport event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    event.initEventSeqId(redisService);
    event.autoWriteExtendParam(jsonService);
    eventMapper.insert(event);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    this.sendSingleMessage(event, event.getEventType(), header, tags);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, Integer CDTPEventType, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    event.setFrom(event.getGroupTemail());
    event.setTo(event.getTemail());
    this.insert(event);
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), CDTPEventType, header, jsonService.toJson(event))), tags);
  }

  /**
   * 发送单人消息，不进行赋值操作
   */
  private void sendSingleMessageDirectly(Event event, String header, String tags)
      throws InterruptedException, RemotingException, MQClientException, MQBrokerException, UnsupportedEncodingException {
    this.insert(event);
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(event.getTo(), event.getEventType(), header, jsonService.toJson(event))), tags);
  }

  /**
   * 向所有群成员发送群消息
   */
  private void sendGroupMessageToAll(Event event, String header, String tags)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    this.sendGroupMessage(event, memberMapper.selectMember(event), event.getEventType(), header, tags);
  }

  /**
   * 向正常状态群成员发送群消息
   */
  private void sendGroupMessageToAll(Event event, Integer CDTPEventType, String header, String tags)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    this.sendGroupMessage(event, memberMapper.selectMember(event), CDTPEventType, header, tags);
  }

  /**
   * 发送群聊消息
   */
  private void sendGroupMessage(Event event, List<String> tos, Integer CDTPEventType, String header, String tags)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    LOGGER.info("send message to --->> {}, event type: {}", tos, EventType.getByValue(event.getEventType()));
    event.setFrom(event.getGroupTemail());
    for (String to : tos) {
      event.setTo(to);
      this.insert(event);
      rocketMqProducer.sendMessage(jsonService.toJson(new CDTPResponse(to, CDTPEventType, header, jsonService.toJson(event))), tags);
    }
  }

  /**
   * 发送回复消息
   */
  private void sendReplyMessage(Event event, String header, String tags)
      throws UnsupportedEncodingException, InterruptedException, RemotingException, MQClientException, MQBrokerException {
    // 查询父消息，如果是@消息则只发送给@的人，否则发送给所有人
    Event condition = new Event();
    condition.setEventType(EventType.RECEIVE_AT.getValue());
    condition.setMsgId(event.getParentMsgId());

    List<String> tos;
    List<Event> events = eventMapper.selectEventsByMsgId(condition);
    if (events.isEmpty()) {
      tos = memberMapper.selectMember(event);
    } else {
      Event parentEvent = events.get(0).autoReadExtendParam(jsonService);
      tos = jsonService.fromJson(parentEvent.getAt(), new TypeToken<List<String>>() {
      }.getType());
      // 添加原消息发送者
      tos.add(parentEvent.getTemail());
    }

    this.sendGroupMessage(event, tos, event.getEventType(), header, tags);
  }
}
