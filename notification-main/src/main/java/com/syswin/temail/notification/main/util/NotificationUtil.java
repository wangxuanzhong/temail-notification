package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.application.RedisService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventRepository;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * 幂等校验
   */
  public static boolean checkUnique(Event event, String redisKey, EventRepository eventRepository, RedisService redisService) {
    // xPacketId为空则认为是无效数据
    if (event.getxPacketId() == null || event.getxPacketId().isEmpty()) {
      LOGGER.warn("xPacketId is null!");
      return false;
    }

    // 第一步：查询redis，是否key值未过期，解决并发问题
    if (!redisService.checkUnique(redisKey)) {
      LOGGER.warn("check unique from redis failed: {}", event);
      return false;
    }

    // 第二步：查询数据库是否存在重复数据，保证数据的唯一性
    if (!eventRepository.checkUnique(event).isEmpty()) {
      LOGGER.warn("check unique from database failed: {}", event);
      return false;
    }

    return true;
  }
}
