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

package com.syswin.temail.notification.main.application.scheduler;

import com.syswin.temail.notification.main.application.EventService;
import com.syswin.temail.notification.main.application.RedisServiceImpl;
import com.syswin.temail.notification.main.application.UnreadService;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.constants.Constant;
import com.syswin.temail.notification.main.constants.Constant.EventCondition;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.TopicMapper;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liusen@syswin.com
 */
@Service
public class EventSchedule {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DELETE_OLD_EVENT_KEY = "notification_deleteOldEvent";
  /**
   * 分页删除，每次删除10W条
   */
  private static final int PAGE_SIZE = 100000;
  private static final int TIMEOUT = 10;

  private final UnreadService unreadService;
  private final EventService eventService;
  private final RedisServiceImpl redisService;
  private final EventMapper eventMapper;
  private final TopicMapper topicMapper;

  private final NotificationConfig config;

  @Autowired
  public EventSchedule(UnreadService unreadService, EventService eventService, RedisServiceImpl redisService,
      EventMapper eventMapper, TopicMapper topicMapper, NotificationConfig config) {
    this.unreadService = unreadService;
    this.eventService = eventService;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
    this.topicMapper = topicMapper;
    this.config = config;
  }

  /**
   * 删除旧的单群聊事件，并统计未读数
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldEvent() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old event before {}", createTime);

    if (!redisService.checkLock(DELETE_OLD_EVENT_KEY, TIMEOUT, TimeUnit.MINUTES)) {
      LOGGER.warn("check lock from redis failed!");
      return;
    }

    // 保存所有查询出的未读数结果，等到删除数据库后，操作redis
    List<Unread> unreads = new ArrayList<>();

    // 查询出所有的to
    List<String> tos = eventMapper.selectOldTo(createTime);

    // 循环计算出所有to的未读数并插入数据库
    tos.forEach(to -> {
      // 获取已经删除的事件的未读数
      Map<String, Integer> cleardUnreadMap = unreadService.getCleardUnread(to);
      // 获取已经删除的带at消息事件的未读数
      Map<String, Integer> cleardUnreadAtMap = unreadService.getCleardUnreadAt(to);

      List<Event> events = eventMapper.selectOldEvent(to, createTime, EventCondition.UNREAD_EVENT_TYPES);

      // 统计未读数
      LOGGER.info("calculate [{}]'s event, size : {}", to, events.size());
      Map<String, List<String>> unreadMap = eventService.calculateUnread(events, cleardUnreadMap, cleardUnreadAtMap);
      unreads.add(new Unread(to, cleardUnreadMap, unreadMap, cleardUnreadAtMap));
    });

    // 分页删除旧数据
    LOGGER.info("delete old events!");
    while (true) {
      List<Long> ids = eventMapper.selectOldEventId(createTime, 0, PAGE_SIZE);
      if (ids.isEmpty()) {
        break;
      }
      LOGGER.info("delete {} events", ids.size());
      eventMapper.delete(ids);
    }

    // 统计各个会话的未读数量，并存入redis
    unreads.forEach(unread -> {
      unread.getUnreadMap().forEach((from, msgIds) -> {
        int count = 0;
        int atCount = 0;

        // 删除redis中的未读数，获取实际删除的条数，即为未读数
        Long removedCount = unreadService.remove(from, unread.getTo(), msgIds);

        List<String> atMsgIds = new ArrayList<>();
        msgIds.forEach(msgId -> {
          if (unreadService.isAtMsgId(from, unread.getTo(), msgId)) {
            atMsgIds.add(msgId);
          }
        });
        Long removedAtCount = unreadService.removeAt(from, unread.getTo(), atMsgIds);
        if (removedCount != null) {
          count += removedCount;
        }
        if (removedAtCount != null) {
          atCount += removedAtCount;
        }

        // 添加已经过期事件的未读数
        if (unread.getCleardUnreadMap().containsKey(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0])) {
          count += unread.getCleardUnreadMap().get(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0]);
        }

        // 添加已经过期at事件的未读数
        if (unread.getCleardUnreadAtMap().containsKey(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0])) {
          count += unread.getCleardUnreadAtMap().get(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0]);
        }
        if (count != 0) {
          unreadService.addCleardUnread(from, unread.getTo(), count);
        }
        if (atCount != 0) {
          unreadService.addCleardUnreadAt(from, unread.getTo(), atCount);
        }
      });
    });
  }

  /**
   * 删除旧的话题事件
   */
  public void deleteOldTopic() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old topic before {}", createTime);
    topicMapper.deleteOldTopic(createTime);
  }

  private LocalDateTime getDeadline() {
    return LocalDate.now().atStartOfDay().minusDays(config.deadline);
  }
}
