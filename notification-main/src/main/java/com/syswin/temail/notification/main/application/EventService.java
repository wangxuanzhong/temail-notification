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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.constants.Constant.EventCondition;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams.TrashMsgInfo;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.MemberMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单聊通知事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class EventService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventMapper eventMapper;
  private final UnreadMapper unreadMapper;
  private final MemberMapper memberMapper;
  private final Gson gson;
  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;

  private final NotificationConfig config;

  @Autowired
  public EventService(EventMapper eventMapper, UnreadMapper unreadMapper, MemberMapper memberMapper,
      IMqProducer iMqProducer, RedisServiceImpl redisService, NotificationConfig config) {
    this.eventMapper = eventMapper;
    this.unreadMapper = unreadMapper;
    this.memberMapper = memberMapper;
    this.gson = new Gson();
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.config = config;
  }

  /**
   * 拉取事件
   *
   * @param to 发起人
   * @param eventSeqId 上次拉取结尾序号
   * @param pageSize 拉取数量
   */
  public Map<String, Object> getEvents(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull events called, to: {}, eventSeqId: {}, pageSize: {}", to, eventSeqId, pageSize);

    // 如果pageSize为空则不限制查询条数
    List<Event> events = eventMapper.selectEvents(to, eventSeqId, pageSize);

    // 查询数据库中eventSeqId的最大值
    Long maxEventSeqId = eventMapper.selectLastEventSeqId(to);

    // 获取当前最新eventSeqId
    Long lastEventSeqId = events.isEmpty() ? maxEventSeqId : events.get(events.size() - 1).getEventSeqId();

    Map<String, Map<String, Event>> eventMap = new HashMap<>(16);
    // 存放普通消息，以便抵消操作处理
    List<String> messages = new ArrayList<>();
    // 存放废纸篓消息，以便还原操作处理
    List<String> trashMsgIds = new ArrayList<>();
    events.forEach(event -> {
      event.autoReadExtendParam(gson);

      // 按照会话统计事件，方便对单个会话多事件进行处理
      String key = event.getFrom();

      if (event.getGroupTemail() != null && !"".equals(event.getGroupTemail())) {
        key = event.getGroupTemail();
      }

      // 单聊逻辑: 当from和to相同时，数据库中存储的owner为会话的另一方，to为通知者，查询结果恢复原结构
      if (event.getFrom().equals(event.getTo()) && event.getOwner() != null) {
        event.setTo(event.getOwner());
        event.setOwner(event.getFrom());
        key = event.getTo();
      }

      if (!eventMap.containsKey(key)) {
        eventMap.put(key, new HashMap<>(16));
      }

      Map<String, Event> sessionEventMap = eventMap.get(key);
      EventType eventType = EventType.getByValue(event.getEventType());
      switch (Objects.requireNonNull(eventType)) {
        case RECEIVE:
        case RECEIVE_AT:
        case DESTROY:
          messages.add(event.getMsgId());
          break;
        case DESTROYED:
        case REPLY_DESTROYED:
        case REPLY:
          sessionEventMap.put(event.getMsgId(), event);
          break;
        case RETRACT:
        case DELETE_AT:
          if (!messages.contains(event.getMsgId())) {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case REPLY_RETRACT:
          if (sessionEventMap.containsKey(event.getMsgId())) {
            sessionEventMap.remove(event.getMsgId());
          } else {
            sessionEventMap.put(event.getMsgId(), event);
          }
          break;
        case ADD_GROUP:
        case APPLY:
        case INVITATION:
        case UPDATE_GROUP_CARD:
        case GROUP_STICK:
        case GROUP_DO_NOT_DISTURB:
        case DO_NOT_DISTURB:
        case CHANGE_EXT_DATA:
        case CHANGE_MEMBER_EXT_DATA:
          // 只返回最后一条事件
          sessionEventMap.put(EventUtil.getMsgId(eventType, event), event);
          break;
        case TRASH:
          // 移动到废纸篓不需要查询返回，只需要记录移动的消息id
          trashMsgIds.addAll(event.getMsgIds());
          break;
        case TRASH_CANCEL:
          List<TrashMsgInfo> newInfos = new ArrayList<>();
          List<TrashMsgInfo> infos = gson
              .fromJson(event.getTrashMsgInfo(), new TypeToken<List<TrashMsgInfo>>() {
              }.getType());
          // 如果查询到的事件中，有移入移出的操作，则对应msgId不需要返回给前端
          infos.forEach(info -> {
            if (!trashMsgIds.contains(info.getMsgId())) {
              newInfos.add(info);
            }
          });
          if (!newInfos.isEmpty()) {
            event.setTrashMsgInfo(gson.toJson(newInfos));
            sessionEventMap.put(UUID.randomUUID().toString(), event);
          }
          break;
        case DELETE_GROUP:
          // 清除所有人的事件，并添加此事件
          sessionEventMap.clear();
          sessionEventMap.put(UUID.randomUUID().toString(), event);
          break;
        case DELETE_MEMBER:
        case LEAVE_GROUP:
          // 只清除当事人的事件，并添加此事件
          if (to.equals(event.getTemail())) {
            sessionEventMap.clear();
          }
          sessionEventMap.put(UUID.randomUUID().toString(), event);
          break;
        case APPLY_ADOPT:
        case APPLY_REFUSE:
        case INVITATION_ADOPT:
        case INVITATION_REFUSE:
        case GROUP_STICK_CANCEL:
        case GROUP_DO_NOT_DISTURB_CANCEL:
        case DO_NOT_DISTURB_CANCEL:
          if (sessionEventMap.containsKey(EventUtil.getMsgId(eventType, event))) {
            sessionEventMap.remove(EventUtil.getMsgId(eventType, event));
          } else {
            sessionEventMap.put(EventUtil.getMsgId(eventType, event), event);
          }
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            List<String> msgIds = new ArrayList<>(event.getMsgIds());
            event.getMsgIds().forEach(msgId -> {
              if (messages.contains(msgId)) {
                // 删除已出现的msgId
                msgIds.remove(msgId);
              }
            });
            // 将此次拉取中未出现的msgId添加到删除事件中，供前端处理
            if (!msgIds.isEmpty()) {
              event.setMsgIds(msgIds);
              sessionEventMap.put(UUID.randomUUID().toString(), event);
            }
          } else {  // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              sessionEventMap.clear();
            }
            sessionEventMap.put(UUID.randomUUID().toString(), event);
          }
          break;
        case REPLY_DELETE:
          if (event.getMsgIds() != null) {
            // 删除已出现的msgId
            event.getMsgIds().forEach(sessionEventMap::remove);
          }
          break;
        case ADD_ADMIN:
          // 只有当事人添加此事件
          if (to.equals(event.getTemail())) {
            sessionEventMap.put(EventUtil.getMsgId(eventType, event), event);
          }
          break;
        case DELETE_ADMIN:
        case ABANDON_ADMIN:
          if (sessionEventMap.containsKey(EventUtil.getMsgId(eventType, event))) {
            sessionEventMap.remove(EventUtil.getMsgId(eventType, event));
          } else {
            // 只有当事人添加此事件
            if (to.equals(event.getTemail())) {
              sessionEventMap.put(EventUtil.getMsgId(eventType, event), event);
            }
          }
          break;
        case PACKET:
          sessionEventMap.put(UUID.randomUUID().toString(), event.unzip());
          break;
        default:
          // do nothing
      }
    });

    List<Event> notifyEvents = new ArrayList<>();
    eventMap.values().forEach(sessionEventMap -> notifyEvents.addAll(sessionEventMap.values()));

    // 给事件按照eventSeqId重新排序
    notifyEvents.sort(Comparator.comparing(Event::getEventSeqId));

    // 返回事件超过1000条，只返回最后一千条
    if (notifyEvents.size() > EventCondition.MAX_EVENT_RETURN_COUNT) {
      notifyEvents.subList(0, notifyEvents.size() - EventCondition.MAX_EVENT_RETURN_COUNT).clear();
    }

    // 将每个返回结果的extendParam合并到event中
    List<JsonElement> eventList = new ArrayList<>();
    notifyEvents
        .forEach(event -> eventList.add(new JsonParser().parse(EventUtil.toJson(gson, event))));

    Map<String, Object> result = new HashMap<>(5);
    result.put("lastEventSeqId", lastEventSeqId == null ? 0 : lastEventSeqId);
    result.put("maxEventSeqId", maxEventSeqId == null ? 0 : maxEventSeqId);
    result.put("events", eventList);

    return result;
  }

  /**
   * 获取消息未读数
   *
   * @param to 发起人
   */
  public List<UnreadResponse> getUnread(String to) {
    LOGGER.info("get unread, to: {}", to);

    // 获取已经删除的事件的未读数
    Map<String, Integer> unreadMap = new HashMap<>(16);
    unreadMapper.selectCount(to).forEach(unread -> unreadMap.put(unread.getFrom(), unread.getCount()));

    // 查询所有事件
    List<Event> events = eventMapper.selectPartEvents(to, EventCondition.UNREAD_EVENT_TYPES);

    // 统计未读数
    Map<String, List<String>> eventMap = this.calculateUnread(events, unreadMap);

    // 统计各个会话的未读数量
    List<UnreadResponse> unreadResponses = new ArrayList<>();
    eventMap.forEach((key, msgIds) -> {
      if (!msgIds.isEmpty()) {
        // 计算未读数表中的数据
        int unread = 0;
        if (unreadMap.containsKey(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0])) {
          unread = unreadMap.get(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0]);
        }

        UnreadResponse unreadResponse = new UnreadResponse(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to,
            msgIds.size() + unread);
        if (key.endsWith(Event.GROUP_CHAT_KEY_POSTFIX)) {
          unreadResponse.setGroupTemail(unreadResponse.getFrom());
        }
        unreadResponses.add(unreadResponse);
      }
    });

    LOGGER.debug("get unread result: {}", unreadResponses);
    return unreadResponses;
  }

  /**
   * 统计消息未读数
   */
  public Map<String, List<String>> calculateUnread(List<Event> events, Map<String, Integer> unreadMap) {
    Map<String, List<String>> eventMap = new HashMap<>(16);
    events.forEach(event -> {
      event.autoReadExtendParam(gson);
      // 为了区分单聊和群聊，给群聊添加后缀
      String key = event.getFrom();
      if (event.getGroupTemail() != null && !"".equals(event.getGroupTemail())) {
        key += Event.GROUP_CHAT_KEY_POSTFIX;
      }

      if (!eventMap.containsKey(key)) {
        eventMap.put(key, new ArrayList<>());
      }
      List<String> msgIds = eventMap.get(key);
      switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
        case RESET:
          // 清空未读数
          msgIds.clear();
          unreadMap.remove(event.getFrom());
          break;
        case RECEIVE:
        case DESTROY:
          // 普通消息和焚毁消息发送者不计未读数
          if (!event.getFrom().equals(event.getTo()) && !event.getTo().equals(event.getTemail())) {
            msgIds.add(event.getMsgId());
          }
          break;
        case PULLED:
        case RETRACT:
          msgIds.remove(event.getMsgId());
          break;
        case DELETE:
          // msgIds不为空，则为批量删除消息
          if (event.getMsgIds() != null) {
            msgIds.removeAll(event.getMsgIds());
          } else { // 单聊删除会话和消息
            if (event.getDeleteAllMsg() != null && event.getDeleteAllMsg()) {
              msgIds.clear();
              unreadMap.remove(event.getFrom());
            }
          }
          break;
        default:
          // do nothing
      }
    });
    return eventMap;
  }

  /**
   * 重置消息未读数
   */
  public void reset(Event event, String header) {
    LOGGER.info("reset to: {}, param: {}", event.getTo(), event);
    event.setEventType(EventType.RESET.getValue());
    Integer cdtpEventType = event.getEventType();
    // groupTemail不为空则为群聊
    if (event.getGroupTemail() != null && !event.getGroupTemail().isEmpty()) {
      event.setFrom(event.getGroupTemail());
      cdtpEventType = EventType.GROUP_RESET.getValue();
    }
    event.setTimestamp(System.currentTimeMillis());
    EventUtil.initEventSeqId(redisService, event);
    eventMapper.insert(event);

    // 删除历史重置事件
    List<Long> ids = eventMapper.selectResetEvents(event);
    if (!ids.isEmpty()) {
      eventMapper.delete(ids);
    }

    // 发送到MQ以便多端同步
    LOGGER.info("send reset event to {}", event.getTo());
    iMqProducer.sendMessage(gson
        .toJson(new DispatcherResponse(event.getTo(), cdtpEventType, header, EventUtil.toJson(gson, event))));
  }

  /**
   * 修改群成员个人状态
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateGroupChatUserStatus(Member member, UserStatus userStatus, String header) {
    LOGGER.info("update user status, param: {}", member);
    Event event = new Event();
    event.setFrom(member.getGroupTemail());
    event.setTo(member.getTemail());
    event.setGroupTemail(member.getGroupTemail());
    event.setTemail(member.getTemail());
    event.setTimestamp(System.currentTimeMillis());

    switch (userStatus) {
      case NORMAL:
        event.setEventType(EventType.GROUP_DO_NOT_DISTURB_CANCEL.getValue());
        memberMapper.updateUserStatus(member);
        break;
      case DO_NOT_DISTURB:
        event.setEventType(EventType.GROUP_DO_NOT_DISTURB.getValue());
        memberMapper.updateUserStatus(member);
        break;
      default:
        return;
    }

    // 发送到MQ以便多端同步
    LOGGER.info("send reset event to {}", event.getTo());
    iMqProducer.sendMessage(gson
        .toJson(new DispatcherResponse(event.getTo(), event.getEventType(), header,
            EventUtil.toJson(gson, event))));
  }

  /**
   * 查询群成员个人状态
   */
  public Map<String, Integer> getGroupChatUserStatus(String temail, String groupTemail) {
    LOGGER.info("get do not disturb group, temail: {}", temail);
    Map<String, Integer> result = new HashMap<>(3);
    result.put("userStatus", memberMapper.selectUserStatus(temail, groupTemail));
    return result;
  }

  /**
   * 事件拉取，限制最大条数
   */
  public Map<String, Object> getEventsLimited(String to, Long eventSeqId, Integer pageSize) {
    LOGGER.info("pull events limited called, to: {}, eventSeqId: {}, pageSize: {}", to, eventSeqId, pageSize);
    // 为pageSize配置默认值和最大值
    pageSize = pageSize == null || pageSize > config.defaultPageSize ? config.defaultPageSize : pageSize;
    return this.getEvents(to, eventSeqId, pageSize);
  }
}
