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

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.CdtpResponse;
import com.syswin.temail.notification.main.dto.MailAgentParams;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
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
public class NotificationSingleChatServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final NotificationRedisServiceImpl notificationRedisServiceImpl;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;

  @Autowired
  public NotificationSingleChatServiceImpl(IMqProducer iMqProducer,
      NotificationRedisServiceImpl notificationRedisServiceImpl,
      EventMapper eventMapper, IJsonService iJsonService) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisServiceImpl = notificationRedisServiceImpl;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    MailAgentParams params = iJsonService.fromJson(body, MailAgentParams.class);
    Event event = new Event(params.getSessionMessageType(), params.getMsgid(), params.getParentMsgId(),
        params.getSeqNo(), params.getToMsg(), params.getFrom(), params.getTo(), params.getTimestamp(),
        params.getGroupTemail(), params.getTemail(), params.getxPacketId(), params.getOwner(),
        params.getDeleteAllMsg());

    // 前端需要的头信息
    String header = params.getHeader();

    LOGGER.info("single chat params: {}, tags: {}", params, tags);
    LOGGER.info("single chat event type: {}", EventType.getByValue(event.getEventType()));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!EventUtil.checkUnique(event, redisKey, eventMapper, notificationRedisServiceImpl)) {
      return;
    }

    /* 添加透传参数 */
    event.setExtData(params.getExtData());
    event.setMemberExtData(params.getMemberExtData());
    event.setSessionExtData(params.getSessionExtData());
    // 新群聊消息字段
    event.setFilter(params.getFilter());
    event.setAuthor(params.getAuthor());

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case DESTROY:
      case REPLY:
      case CROSS_DOMAIN:
      case RETRACT:
      case DESTROYED:
      case REPLY_RETRACT:
      case REPLY_DESTROYED:
        // 发送时会分别发送到发件人收件箱和收件人收件箱
        if (event.getFrom().equals(params.getOwner())) {
          EventUtil.initEventSeqId(notificationRedisServiceImpl, event);
          event.autoWriteExtendParam(iJsonService);
          sendMessageToSender(event, header, tags);
          // 发送到发件人收件箱的消息，事件中对换to和owner字段来保存
          event.setTo(params.getOwner());
          event.setOwner(params.getTo());
          event.autoWriteExtendParam(iJsonService);
          eventMapper.insert(event);
        } else {
          sendMessage(event, header, tags);
        }
        break;
      case PULLED:
        // from是消息拉取人
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        for (String msgId : event.getMsgId().split(MailAgentParams.MSG_ID_SPLIT)) {
          event.setMsgId(msgId);
          if (eventMapper.selectEventsByMsgId(event).isEmpty()) {
            sendMessage(event, header, tags);
          } else {
            LOGGER.info("message {} is pulled, do nothing!", msgId);
          }
        }
        break;
      case DELETE:
      case REPLY_DELETE:
      case TRASH:
        // 删除操作msgId是多条，存入msgIds字段
        event.setMsgIds(iJsonService.fromJson(event.getMsgId(), new TypeToken<List<String>>() {
        }.getType()));
        event.setMsgId(null);
        // from是操作人，to是会话另一方
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header, tags);
        break;
      // 只提供多端同步
      case ARCHIVE:
      case ARCHIVE_CANCEL:
      case DO_NOT_DISTURB:
      case DO_NOT_DISTURB_CANCEL:
        // from是操作人，to是会话的另一方
        event.setFrom(params.getTo());
        event.setTo(params.getFrom());
        sendMessage(event, header, tags);
        break;
      case TRASH_CANCEL:
      case TRASH_DELETE:
        // owner是操作人，from和to都为空，msgId为空
        event.setTrashMsgInfo(params.getTrashMsgInfo());
        event.setFrom(params.getOwner());
        event.setTo(params.getOwner());
        sendMessage(event, header, tags);
        break;
      default:
        LOGGER.warn("not support event type!");
    }
  }

  /**
   * 插入数据库
   */
  private void insert(Event event) {
    EventUtil.initEventSeqId(notificationRedisServiceImpl, event);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String header, String tags) {
    this.sendMessage(event, event.getTo(), header, tags);
  }

  /**
   * 发送消息
   */
  private void sendMessage(Event event, String to, String header, String tags) {
    LOGGER.info("send message to --->> {}, event type: {}", to, EventType.getByValue(event.getEventType()));
    this.insert(event);
    iMqProducer.sendMessage(
        iJsonService.toJson(new CdtpResponse(to, event.getEventType(), header, EventUtil.toJson(iJsonService, event))),
        tags);
  }

  /**
   * 发送消息，提供多端同步功能
   */
  private void sendMessageToSender(Event event, String header, String tags) {
    LOGGER.info("send message to sender --->> {}, event type: {}", event.getFrom(),
        EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(iJsonService
            .toJson(new CdtpResponse(event.getFrom(), event.getEventType(), header, EventUtil.toJson(iJsonService, event))),
        tags);
  }
}
