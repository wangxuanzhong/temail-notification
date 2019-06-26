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

package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.application.NotificationRedisServiceImpl;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liusen@syswin.com
 */
public class NotificationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private NotificationUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 幂等校验
   */
  public static boolean checkUnique(Event event, String redisKey, EventMapper eventMapper,
      NotificationRedisServiceImpl notificationRedisServiceImpl) {
    // xPacketId为空则认为是无效数据
    if (event.getxPacketId() == null || event.getxPacketId().isEmpty()) {
      LOGGER.warn("xPacketId is null!");
      return false;
    }

    // 第一步：查询redis，是否key值未过期，解决并发问题
    if (!notificationRedisServiceImpl.checkUnique(redisKey)) {
      LOGGER.warn("check unique from redis failed: {}", event);
      return false;
    }

    // 第二步：查询数据库是否存在重复数据，保证数据的唯一性
    if (!eventMapper.selectEventsByPacketIdAndEventType(event).isEmpty()) {
      LOGGER.warn("check unique from database failed: {}", event);
      return false;
    }

    return true;
  }
}
