package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import com.syswin.temail.notification.main.domains.TopicEventRepository;
import com.syswin.temail.notification.main.domains.Unread;
import com.syswin.temail.notification.main.domains.UnreadRepository;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private final EventRepository eventRepository;
  private final EventService eventService;
  private final TopicEventRepository topicEventRepository;
  private final UnreadRepository unreadRepository;
  private int deadline;

  @Autowired
  public EventSchedule(EventRepository eventRepository, EventService eventService, TopicEventRepository topicEventRepository,
      UnreadRepository unreadRepository, @Value("${app.temail.notification.schedule.deadline}") int deadline) {
    this.eventRepository = eventRepository;
    this.eventService = eventService;
    this.topicEventRepository = topicEventRepository;
    this.unreadRepository = unreadRepository;
    this.deadline = deadline;
  }

  @Scheduled(cron = "0 0 4 * * ?") // 每天4点触发
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldEvent() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old event before {}", createTime);

    // 查询出所有的to
    List<String> tos = eventRepository.selectOldTo(createTime);

    // 循环计算出所有to的未读数并插入数据库
    tos.forEach(to -> {
      // 获取已经删除的事件的未读数
      Map<String, Integer> unreadMap = new HashMap<>();
      unreadRepository.selectCount(to).forEach(unread -> unreadMap.put(unread.getFrom(), unread.getCount()));

      List<Event> events = eventRepository.selectOldEvent(to, createTime);

      // 统计未读数
      LOGGER.info("calculate {}'s events: {}", to, events);
      Map<String, List<String>> eventMap = eventService.calculateUnread(events, unreadMap);

      // 统计各个会话的未读数量，并插入数据库
      eventMap.forEach((key, msgIds) -> {
        // 计算未读数表中的数据
        int count = 0;
        if (unreadMap.containsKey(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0])) {
          count = unreadMap.get(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0]);
        }
        unreadRepository.insert(new Unread(key.split(Event.GROUP_CHAT_KEY_POSTFIX)[0], to, msgIds.size() + count));
      });
    });

    // 删除旧数据
    LOGGER.info("delete old events!");
    eventRepository.deleteOldEvent(createTime);

    // 删除未读数为0的数据
    LOGGER.info("delete zero count!");
    unreadRepository.deleteZeroCount();
  }

  @Scheduled(cron = "0 0 3 * * ?") // 每天3点触发
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldTopic() {
    LocalDateTime createTime = this.getDeadline();
    LOGGER.info("delete old topic before {}", createTime);
    topicEventRepository.deleteOldTopic(createTime);
  }

  private LocalDateTime getDeadline() {
    return LocalDate.now().atStartOfDay().minusDays(deadline);
  }
}
