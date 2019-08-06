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
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member.MemberRole;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 旧群聊通知事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class GroupChatServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;
  private final EventMapper eventMapper;
  private final MemberMapper memberMapper;
  private final Gson gson;

  @Autowired
  public GroupChatServiceImpl(IMqProducer iMqProducer, RedisServiceImpl redisService, EventMapper eventMapper,
      MemberMapper memberMapper) {
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
    this.memberMapper = memberMapper;
    this.gson = new Gson();
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    MailAgentParams params = gson.fromJson(body, MailAgentParams.class);
    Event event = gson.fromJson(body, Event.class);
    EventUtil.copyMailAgentFieldToEvent(params, event);

    // 前端需要的头信息
    String header = params.getHeader();
    LOGGER.info("group chat params: {}, tags: {}", body, tags);

    EventType eventType = EventType.getByValue(params.getSessionMessageType());
    if (eventType == null) {
      LOGGER.warn("event type is illegal! xPacketId: {}", event.getxPacketId());
      return;
    }
    LOGGER.info("group chat event type: {}", eventType);

    // 校验收到的数据是否重复
    String redisKey =
        event.getxPacketId() + "_" + event.getEventType() + "_" + event.getGroupTemail() + "_" + event.getTemail();
    if (!EventUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    switch (eventType) {
      case RECEIVE:
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, EventType.GROUP_RECEIVE.getValue(), header, tags, body);
        break;
      case RETRACT:
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, EventType.GROUP_RETRACT.getValue(), header, tags, body);
        break;
      case PULLED:
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventMapper.selectEventsByMsgId(event).isEmpty()) {
            this.sendSingleMessage(event, EventType.GROUP_PULLED.getValue(), header, tags, body);
          } else {
            LOGGER.warn("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(gson.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, EventType.GROUP_DELETE.getValue(), header, tags, body);
        break;
      case ADD_GROUP:
        event.setRole(MemberRole.ADMIN.getValue());
        memberMapper.insert(event);
        this.sendSingleMessage(event, header, tags, body);
        break;
      case DELETE_GROUP:
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        event.setTemail(null);
        memberMapper.deleteGroupMember(event);
        break;
      case ADD_MEMBER:
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
          EventUtil.notifyToAll(event);
          this.sendGroupMessageToAll(event, header, tags, body);
        } else {
          LOGGER.warn("{} was group {} member, do nothing.", event.getTemail(), event.getGroupTemail());
        }
        break;
      // 只通知被删除的人
      case DELETE_MEMBER:
        List<String> temails = gson.fromJson(event.getTemail(), new TypeToken<List<String>>() {
        }.getType());
        List<String> names = gson.fromJson(event.getName(), new TypeToken<List<String>>() {
        }.getType());
        List<String> memberExtDatas = gson.fromJson(event.getMemberExtData(), new TypeToken<List<String>>() {
        }.getType());

        // 兼容旧群聊，没有memberExtDatas字段
        if (memberExtDatas == null) {
          memberExtDatas = new ArrayList<>(temails.size());
          for (int i = 0; i < temails.size(); i++) {
            memberExtDatas.add("");
          }
        }

        if (temails.size() != names.size() || temails.size() != memberExtDatas.size()) {
          LOGGER.error(
              "delete member temail, name, memberExtData mismatching, temails: {}, names: {}, memberExtDatas: {}",
              temails, names, memberExtDatas);
          break;
        }

        // 删除当事人
        for (String temail : temails) {
          event.setTemail(temail);
          memberMapper.deleteGroupMember(event);
        }

        for (int i = 0; i < temails.size(); i++) {
          event.setTemail(temails.get(i));
          event.setName(names.get(i));
          event.setMemberExtData(memberExtDatas.get(i));
          // 通知所有人
          EventUtil.notifyToAll(event);
          this.sendGroupMessageToAll(event, header, tags, body);
          // 通知当事人被移除群聊
          event.setFrom(event.getGroupTemail());
          event.setTo(temails.get(i));
          this.sendSingleMessage(event, header, tags, body);
        }
        break;
      case LEAVE_GROUP:
        memberMapper.deleteGroupMember(event);
        // 通知所有人
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        // 通知当事人
        event.setFrom(event.getGroupTemail());
        event.setTo(event.getTemail());
        this.sendSingleMessage(event, header, tags, body);
        break;
      case APPLY:
        EventUtil.notifyToAdmin(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        break;
      case INVITATION:
        this.sendSingleMessage(event, header, tags, body);
        break;
      case APPLY_ADOPT:
      case APPLY_REFUSE:
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        // 通知所有管理员，提供多端同步功能
        EventUtil.notifyToAdmin(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        // 通知申请人，此通知当跨域时有可能申请人接收不到，因此修改为通过单聊发送，通知服务不单独通知申请人(向前兼容)
        this.sendSingleMessage(event, header, tags, body);
        break;
      case UPDATE_GROUP_CARD:
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        break;
      case REPLY:
      case REPLY_RETRACT:
        EventUtil.notifyToAll(event);
        this.sendReplyMessage(event, header, tags, body);
        break;
      case REPLY_DELETE:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(gson.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        EventUtil.notifyToAll(event);
        this.sendReplyMessage(event, header, tags, body);
        break;
      case GROUP_ARCHIVE:
      case GROUP_ARCHIVE_CANCEL:
      case GROUP_SESSION_HIDDEN:
      case GROUP_DO_NOT_DISTURB:
      case GROUP_DO_NOT_DISTURB_CANCEL:
        this.sendSingleMessage(event, header, tags, body);
        break;
      case GROUP_STICK:
      case GROUP_STICK_CANCEL:
      case CHANGE_MEMBER_EXT_DATA:
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        break;
      case BLACKLIST:
      case BLACKLIST_CANCEL:
        temails = gson.fromJson(event.getTemail(), new TypeToken<List<String>>() {
        }.getType());
        for (String temail : temails) {
          event.setTemail(temail);
          EventUtil.notifyToAdmin(event);
          this.sendGroupMessageToAll(event, header, tags, body);
        }
        break;
      case RECEIVE_AT:
        // @消息下发时为多条，from为发送者，to和temail为接收者
        event.setTemail(event.getFrom());
        // 当事人为发件人
        this.sendSingleMessageDirectly(event, header, tags, body);
        break;
      case DELETE_AT:
        // 查询父消息中的at字段，通知所有at的人
        Event condition = new Event();
        condition.setEventType(EventType.RECEIVE_AT.getValue());
        condition.setMsgId(event.getMsgId());

        List<Event> events = eventMapper.selectEventsByMsgId(condition);
        if (events.isEmpty()) {
          LOGGER.warn("do not found source message!");
          break;
        } else {
          Event parentEvent = events.get(0).autoReadExtendParam(gson);
          List<String> tos = gson.fromJson(parentEvent.getAt(), new TypeToken<List<String>>() {
          }.getType());
          // 添加原消息发送者
          tos.add(parentEvent.getTemail());
          this.sendGroupMessage(event, tos, event.getEventType(), header, tags, body);
        }
        break;
      case ADD_ADMIN:
        event.setRole(MemberRole.ADMIN.getValue());
        memberMapper.updateRole(event);
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        break;
      case DELETE_ADMIN:
      case ABANDON_ADMIN:
        event.setRole(MemberRole.NORMAL.getValue());
        memberMapper.updateRole(event);
        EventUtil.notifyToAll(event);
        this.sendGroupMessageToAll(event, header, tags, body);
        break;
      case CROWD_ADD_GROUP:
        // 添加管理员
        event.setRole(MemberRole.ADMIN.getValue());
        memberMapper.insert(event);

        // 添加成员
        Event e = new Event();
        e.setGroupTemail(event.getGroupTemail());
        e.setRole(MemberRole.NORMAL.getValue());
        List<String> tos = gson.fromJson(event.getTo(), new TypeToken<List<String>>() {
        }.getType());
        tos.forEach(member -> {
          e.setTemail(member);
          memberMapper.insert(e);
        });

        // 通知所有人
        tos.add(event.getTemail());
        this.sendGroupMessage(event, tos, event.getEventType(), header, tags, body);
        break;
      default:
        LOGGER.warn("unsupported event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event, String body) {
    EventUtil.initEventSeqId(redisService, event);
    event.autoWriteExtendParam(body);
    eventMapper.insert(event);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, String header, String tags, String body) {
    this.sendSingleMessage(event, event.getEventType(), header, tags, body);
  }

  /**
   * 发送单人消息
   */
  private void sendSingleMessage(Event event, Integer cdtpEventType, String header, String tags, String body) {
    event.setFrom(event.getGroupTemail());
    event.setTo(event.getTemail());
    this.insert(event, body);
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(
        gson.toJson(new DispatcherResponse(event.getTo(), cdtpEventType, header, EventUtil.toJson(gson, event))), tags);
  }

  /**
   * 发送单人消息，不进行赋值操作
   */
  private void sendSingleMessageDirectly(Event event, String header, String tags, String body) {
    this.insert(event, body);
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(
        gson.toJson(new DispatcherResponse(event.getTo(), event.getEventType(), header, EventUtil.toJson(gson, event))),
        tags);
  }

  /**
   * 发送多人消息
   */
  private void sendGroupMessageToAll(Event event, String header, String tags, String body) {
    this.sendGroupMessage(event, memberMapper.selectMember(event), event.getEventType(), header, tags, body);
  }

  /**
   * 发送多人消息
   */
  private void sendGroupMessageToAll(Event event, Integer cdtpEventType, String header, String tags, String body) {
    this.sendGroupMessage(event, memberMapper.selectMember(event), cdtpEventType, header, tags, body);
  }

  /**
   * 发送群聊消息
   */
  private void sendGroupMessage(Event event, List<String> tos, Integer cdtpEventType, String header, String tags,
      String body) {
    LOGGER.info("send message to --->> {}, event type: {}", tos, EventType.getByValue(event.getEventType()));
    if (event.getFrom() == null) {
      event.setFrom(event.getGroupTemail());
    }
    for (String to : tos) {
      event.setTo(to);
      this.insert(event, body);
      iMqProducer
          .sendMessage(gson.toJson(new DispatcherResponse(to, cdtpEventType, header, EventUtil.toJson(gson, event))),
              tags);
    }
  }

  /**
   * 发送回复消息
   */
  private void sendReplyMessage(Event event, String header, String tags, String body) {
    // 查询父消息，如果是@消息则只发送给@的人，否则发送给所有人
    Event condition = new Event();
    condition.setEventType(EventType.RECEIVE_AT.getValue());
    condition.setMsgId(event.getParentMsgId());

    List<String> tos;
    List<Event> events = eventMapper.selectEventsByMsgId(condition);
    if (events.isEmpty()) {
      tos = memberMapper.selectMember(event);
    } else {
      Event parentEvent = events.get(0).autoReadExtendParam(gson);
      tos = gson.fromJson(parentEvent.getAt(), new TypeToken<List<String>>() {
      }.getType());
      // 添加原消息发送者
      tos.add(parentEvent.getTemail());
    }

    this.sendGroupMessage(event, tos, event.getEventType(), header, tags, body);
  }
}