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

import static com.syswin.temail.notification.main.constants.Constant.EventParams.AT;
import static com.syswin.temail.notification.main.constants.Constant.EventParams.MEMBERS;
import static com.syswin.temail.notification.main.constants.Constant.EventParams.TEMAIL;
import static com.syswin.temail.notification.main.constants.Constant.GroupChatAtParams.ATALL;
import static com.syswin.temail.notification.main.constants.Constant.GroupChatAtParams.ATALL_NO_0;
import static com.syswin.temail.notification.main.constants.Constant.GroupChatAtParams.ATALL_YES_1;
import static com.syswin.temail.notification.main.constants.Constant.GroupChatAtParams.UNREAD;
import static com.syswin.temail.notification.main.constants.Constant.GroupChatAtParams.UNREADAT;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
public class SingleChatServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final UnreadService unreadService;
  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;
  private final EventMapper eventMapper;
  private final Gson gson;

  @Autowired
  public SingleChatServiceImpl(UnreadService unreadService, IMqProducer iMqProducer, RedisServiceImpl redisService,
      EventMapper eventMapper) {
    this.iMqProducer = iMqProducer;
    this.unreadService = unreadService;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
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

    LOGGER.info("single chat params: {}, tags: {}", body, tags);

    EventType eventType = EventType.getByValue(params.getSessionMessageType());
    if (eventType == null) {
      LOGGER.warn("event type is illegal! xPacketId: {}", event.getxPacketId());
      return;
    }
    LOGGER.info("single chat event type: {}", eventType);

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!EventUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    switch (eventType) {
      case RECEIVE:
      case DESTROY:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(event.getOwner())) {
          handleSenderMessage(body, tags, event, header);
        } else {
          // 发送给对方的消息记录未读数
          // 从header解析群at信息
          HashMap headerMap = gson.fromJson(header, new TypeToken<HashMap<String, String>>() {
          }.getType());
          List<String> atTemails = new ArrayList<>();
          Integer atAll = null;
          if (headerMap.containsKey(AT) && headerMap.get(AT) != null) {
            event.setAt((String)headerMap.get(AT));
            Map<String, Object> atMap = gson
                .fromJson(event.getAt(), new TypeToken<Map<String, Object>>() {
                }.getType());
            atAll = atMap.get(ATALL) == null ? null : Integer.parseInt((String)atMap.get(ATALL));
            List<Map<String, String>> atMembers = new ArrayList<>();
            if (atMap.containsKey(MEMBERS) && atMap.get(MEMBERS) != null) {
              atMembers = (List)atMap.get(MEMBERS);
              atMembers.forEach(map -> {
                atTemails.add(map.get(TEMAIL));
              });
            }
          }
          unreadService.add(event.getFrom(), event.getTo(), event.getMsgId());
          Map<String, Integer> unreadMap = new HashMap<>();
          // 兼容新群聊消息
          if ((atAll != null && ATALL_YES_1 == atAll)
              || (atAll != null && ATALL_NO_0 == atAll && atTemails.contains(event.getTo()))) {
            unreadService.addAt(event.getFrom(), event.getTo(), event.getMsgId());
          }
          unreadMap = unreadService.getPushUnread(event.getTo());
          event.setUnread(unreadMap.get(UNREAD));
          event.setUnreadAt(unreadMap.get(UNREADAT));
          sendMessage(event, header, tags, body);
        }
        break;
      case RETRACT:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(event.getOwner())) {
          handleSenderMessage(body, tags, event, header);
        } else {
          sendMessage(event, header, tags, body);
          unreadService.remove(event.getFrom(), event.getTo(), Collections.singletonList(event.getMsgId()));
          unreadService.removeAt(event.getFrom(), event.getTo(), Collections.singletonList(event.getMsgId()));
        }
        break;
      case DESTROYED:
      case REPLY:
      case REPLY_RETRACT:
      case REPLY_DESTROYED:
      case CROSS_DOMAIN:
        // 修改extData事件from和owner永远相同
      case CHANGE_EXT_DATA:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(event.getOwner())) {
          handleSenderMessage(body, tags, event, header);
        } else {
          sendMessage(event, header, tags, body);
        }
        break;
      case PULLED:
        // from是消息拉取人
        exchangeFromAndTo(event);
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventMapper.selectEventsByMsgId(event).isEmpty()) {
            sendMessage(event, header, tags, body);
          } else {
            LOGGER.info("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
      case REPLY_DELETE:
      case TRASH:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(gson.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        // from是操作人，to是会话另一方
        exchangeFromAndTo(event);
        sendMessage(event, header, tags, body);
        break;
      // 只提供多端同步
      case ARCHIVE:
      case ARCHIVE_CANCEL:
      case DO_NOT_DISTURB:
      case DO_NOT_DISTURB_CANCEL:
        // from是操作人，to是会话的另一方
        exchangeFromAndTo(event);
        sendMessage(event, header, tags, body);
        break;
      case TRASH_CANCEL:
      case TRASH_DELETE:
        // owner是操作人，from和to都为空，msgId为空
        event.setFrom(event.getOwner());
        event.setTo(event.getOwner());
        sendMessage(event, header, tags, body);
        break;
      default:
        LOGGER.warn("not support event type!");
    }
  }

  /**
   * 处理owner为发送者的消息
   */
  private void handleSenderMessage(String body, String tags, Event event, String header) {
    EventUtil.initEventSeqId(redisService, event);
    event.autoWriteExtendParam(body);
    sendMessageToSender(event, header, tags);
    // 发送到发件人收件箱的消息，事件中对换to和owner字段来保存
    String tmp = event.getTo();
    event.setTo(event.getOwner());
    event.setOwner(tmp);
    event.autoWriteExtendParam(body);
    eventMapper.insert(event);
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
   * 发送消息
   */
  private void sendMessage(Event event, String header, String tags, String body) {
    this.sendMessage(event, event.getTo(), header, tags, body);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String to, String header, String tags, String body) {
    LOGGER.info("send message to --->> {}, event type: {}", to, EventType.getByValue(event.getEventType()));
    this.insert(event, body);
    iMqProducer.sendMessage(
        gson.toJson(new DispatcherResponse(to, event.getEventType(), header, EventUtil.toJson(gson, event))), tags);
  }

  /**
   * 发送消息，提供多端同步功能
   */
  private void sendMessageToSender(Event event, String header, String tags) {
    LOGGER.info("send message to sender --->> {}, event type: {}", event.getFrom(),
        EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(gson.toJson(
        new DispatcherResponse(event.getFrom(), event.getEventType(), header, EventUtil.toJson(gson, event))), tags);
  }

  /**
   * 交换from和to
   */
  private void exchangeFromAndTo(Event event) {
    String tmp = event.getFrom();
    event.setFrom(event.getTo());
    event.setTo(tmp);
  }
}
