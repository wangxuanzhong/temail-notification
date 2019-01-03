package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.infrastructure.TopicEventMapper;
import com.syswin.temail.notification.main.infrastructure.UnreadMapper;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSchedule {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventMapper eventMapper;
  private final EventService eventService;
  private final TopicEventMapper topicEventMapper;
  private final UnreadMapper unreadMapper;
  private final RedisService redisService;
  private final String DELETE_OLD_EVENT_KEY = "notification_deleteOldEvent";
  private final int PAGE_SIZE = 100000; // 分页删除，每次删除10W条
  private int deadline;

  @Autowired
  public EventSchedule(EventMapper eventMapper, EventService eventService, TopicEventMapper topicEventMapper, UnreadMapper unreadMapper,
      RedisService redisService, @Value("${app.temail.notification.schedule.deadline}") int deadline) {
    this.eventMapper = eventMapper;
    this.eventService = eventService;
    this.topicEventMapper = topicEventMapper;
    this.unreadMapper = unreadMapper;
    this.redisService = redisService;
    this.deadline = deadline;
  }

  @Scheduled(cron = "0 0 4 * * ?") // 每天4点触发
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldEvent() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old event before {}", createTime);

    if (!redisService.checkLock(DELETE_OLD_EVENT_KEY, 10, TimeUnit.MINUTES)) {
      LOGGER.warn("check lock from redis failed!");
      return;
    }

    // 查询出所有的to
    List<String> tos = eventMapper.selectOldTo(createTime);

    // 循环计算出所有to的未读数并插入数据库
    tos.forEach(to -> {
      // 获取已经删除的事件的未读数
      Map<String, Integer> unreadMap = new HashMap<>();
      unreadMapper.selectCount(to).forEach(unread -> unreadMap.put(unread.getFrom(), unread.getCount()));

      List<Event> events = eventMapper.selectOldEvent(to, createTime);

      // 统计未读数
      LOGGER.info("calculate {}'s event, size : {}", to, events.size());
      Map<String, List<String>> eventMap = eventService.calculateUnread(events, unreadMap);

      // 统计各个会话的未读数量，并插入数据库
      eventMap.forEach((key, msgIds) -> {
        // 计算未读数表中的数据
        int count = 0;
        if (unreadMap.containsKey(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0])) {
          count = unreadMap.get(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0]);
        }
        unreadMapper.insert(new Unread(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to, msgIds.size() + count));
      });
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

    // 删除未读数为0的数据
    LOGGER.info("delete zero count!");
    unreadMapper.deleteZeroCount();
  }

  @Scheduled(cron = "0 0 3 * * ?") // 每天3点触发
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldTopic() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old topic before {}", createTime);
    topicEventMapper.deleteOldTopic(createTime);
  }

  private LocalDateTime getDeadline() {
    return LocalDate.now().atStartOfDay().minusDays(deadline);
  }
}
