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

import static com.syswin.temail.notification.main.constants.Constant.EventParams.UNREAD;
import static com.syswin.temail.notification.main.constants.Constant.EventParams.UNREADAT;

import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.constants.Constant;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * @author liusen@syswin.com
 */
@Service
public class UnreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String UNREAD_KEY = "unread_";
  private static final String UNREAD_AT_KEY = "unread_at_";
  private static final String CLEARED_KEY = "cleared_";
  private static final String SESSION_SPLIT = ":::";

  private final StringRedisTemplate redisTemplate;
  private final NotificationConfig notificationConfig;

  @Autowired
  public UnreadService(StringRedisTemplate stringRedisTemplate, NotificationConfig notificationConfig) {
    this.redisTemplate = stringRedisTemplate;
    this.notificationConfig = notificationConfig;
  }

  private String getUnreadKey(String body) {
    return Constant.REDIS_KEY_PREFIX + UNREAD_KEY + body;
  }

  private String getUnreadAtKey(String body) {
    return Constant.REDIS_KEY_PREFIX + UNREAD_AT_KEY + body;
  }

  private String getClearedUnreadAtKey(String body) {
    return Constant.REDIS_KEY_PREFIX + CLEARED_KEY + UNREAD_AT_KEY + body;
  }

  private String getClearedUnreadKey(String body) {
    return Constant.REDIS_KEY_PREFIX + CLEARED_KEY + UNREAD_KEY + body;
  }

  /**
   * 添加msgId
   */
  public void add(String from, String to, String msgId) {
    LOGGER.info("add: msgId: {} to: {} from: {}", msgId, to, from);
    // 添加会话
    redisTemplate.opsForSet().add(getUnreadKey(to), from);
    // 添加msgId
    redisTemplate.opsForSet().add(getUnreadKey(to + SESSION_SPLIT + from), msgId);
  }

  /**
   * 添加at msgId
   */
  public void addAt(String from, String to, String msgId) {
//    // 添加at 会话
//    redisTemplate.opsForSet().add(getUnreadAtKey(to), msgId);
    // 添加at msgId
    redisTemplate.opsForSet().add(getUnreadAtKey(to + SESSION_SPLIT + from), msgId);
  }

  /**
   * 删除msgId
   */
  @Nullable
  public Long remove(String from, String to, List<String> msgIds) {
    LOGGER.info("remove: msgIds: {} to: {} from: {}", msgIds, to, from);
    Long count;
    if (msgIds == null || msgIds.isEmpty()) {
      count = 0L;
    } else {
      count = redisTemplate.opsForSet().remove(getUnreadKey(to + SESSION_SPLIT + from), msgIds.toArray());
    }
    LOGGER.info("remove: msgIds: {} to: {} from: {} count: {}", msgIds, to, from, count);
    return count;
  }

  /**
   * 删除 at msgid
   *
   * @param from 发送者
   * @param to 接收者
   * @param msgIds 批量msgId
   * @return count
   */
  @Nullable
  public Long removeAt(String from, String to, List<String> msgIds) {
    LOGGER.info("remove at: msgIds: {} to: {} from: {}", msgIds, to, from);
    Long count;
    if (msgIds == null || msgIds.isEmpty()) {
      count = 0L;
    } else {
      count = redisTemplate.opsForSet().remove(getUnreadAtKey(to + SESSION_SPLIT + from), msgIds.toArray());
    }
    LOGGER.info("remove at: msgIds: {} to: {} from: {} count: {}", msgIds, to, from, count);
    return count;
  }

  /**
   * 重置未读数
   */
  public void reset(String from, String to) {
    LOGGER.info("reset: to: {} from: {}", to, from);
    // 删除会话
    redisTemplate.opsForSet().remove(getUnreadKey(to), from);
    redisTemplate.opsForSet().remove(getClearedUnreadKey(to), from);
    // 删除会话的所有msgId
    redisTemplate.delete(getUnreadKey(to + SESSION_SPLIT + from));
    // 删除过期未读数
    redisTemplate.delete(getClearedUnreadKey(to + SESSION_SPLIT + from));
  }

  /**
   * 重置at 未读数
   *
   * @param from 群地址
   * @param to 群成员
   */
  public void resetAt(String from, String to) {
    LOGGER.info("reset at: to: {} from: {}", to, from);
    // 删除会话
//    redisTemplate.opsForSet().remove(getUnreadAtKey(to), from);
    redisTemplate.opsForSet().remove(getClearedUnreadAtKey(to), from);
    // 删除会话的所有msgId
    redisTemplate.delete(getUnreadAtKey(to + SESSION_SPLIT + from));
    // 删除过期未读数
    redisTemplate.delete(getClearedUnreadAtKey(to + SESSION_SPLIT + from));
  }

  /**
   * 更新过期未读数
   */
  public void addCleardUnread(String from, String to, int count) {
    LOGGER.info("add cleard unread: from: {} to: {} count: {}", from, to, count);
    // 添加会话
    redisTemplate.opsForSet().add(getClearedUnreadKey(to), from);
    // 添加未读数
    redisTemplate.opsForValue().set(getClearedUnreadKey(to + SESSION_SPLIT + from), String.valueOf(count));
  }

  public boolean isAtMsgId(String from, String to, String msgId) {
    boolean existKey = redisTemplate.hasKey(getUnreadAtKey(to + SESSION_SPLIT + from));
    boolean existMsgId = redisTemplate.opsForSet().isMember(getUnreadAtKey(to + SESSION_SPLIT + from), msgId);
    return existKey && existMsgId;
  }

  /**
   * 更新过期at未读数
   */
  public void addCleardUnreadAt(String from, String to, int count) {
    LOGGER.info("add cleard unread at: from: {} to: {} count: {}", from, to, count);
    // 添加at 会话
    redisTemplate.opsForSet().add(getClearedUnreadAtKey(to), from);
    // 添加at 未读数
    redisTemplate.opsForValue().set(getClearedUnreadAtKey(to + SESSION_SPLIT + from), String.valueOf(count));
  }

  /**
   * 获取过期at未读数
   *
   * @param to 接收者
   * @return at未读数
   */
  public Map<String, Integer> getCleardUnreadAt(String to) {
    LOGGER.info("get [{}]'s cleard unread at", to);
    Map<String, Integer> unreadAtMap = new HashMap<>(16);

    // 查询出所有会话
    Set<String> froms = redisTemplate.opsForSet().members(getClearedUnreadAtKey(to));
    LOGGER.info("get [{}]'s cleard unread at: froms: {}", to, froms);
    if (froms == null) {
      return unreadAtMap;
    }

    froms.forEach(from -> {
      // 获取过期数据中的未读数
      String cleared = redisTemplate.opsForValue().get(getClearedUnreadAtKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s unread at: from: {}, cleared: {}", to, from, cleared);
      if (cleared != null && !cleared.isEmpty()) {
        unreadAtMap.put(from, Integer.valueOf(cleared));
      }
    });

    return unreadAtMap;
  }

  /**
   * 获取过期未读数
   */
  public Map<String, Integer> getCleardUnread(String to) {
    LOGGER.info("get [{}]'s cleard unread", to);
    Map<String, Integer> unreadMap = new HashMap<>(16);

    // 查询出所有会话
    Set<String> froms = redisTemplate.opsForSet().members(getClearedUnreadKey(to));
    LOGGER.info("get [{}]'s cleard unread: froms: {}", to, froms);
    if (froms == null) {
      return unreadMap;
    }

    froms.forEach(from -> {
      // 获取过期数据中的未读数
      String cleared = redisTemplate.opsForValue().get(getClearedUnreadKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s unread: from: {}, cleared: {}", to, from, cleared);
      if (cleared != null && !cleared.isEmpty()) {
        unreadMap.put(from, Integer.valueOf(cleared));
      }
    });

    return unreadMap;
  }

  /**
   * 获取未读数
   */
  public List<UnreadResponse> getUnread(String to) {
    LOGGER.info("get [{}]'s unread", to);
    List<UnreadResponse> unreadResponses = new ArrayList<>();

    // 查询出所有会话
    Set<String> froms = redisTemplate.opsForSet().members(getUnreadKey(to));
    LOGGER.info("get [{}]'s unread: froms: {}", to, froms);
    if (froms == null) {
      return unreadResponses;
    }
    for (String from : froms) {
      int unread = 0;
      int unreadAt = 0;
      // 获取会话中消息未读数
      Set<String> msgIds = redisTemplate.opsForSet().members(getUnreadKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s unread: from: {}, msgIds: {}", to, from, msgIds);
      if (msgIds != null) {
        unread += msgIds.size();
      }
      // 获取会话中at消息未读数
      Set<String> atMsgIds = redisTemplate.opsForSet().members(getUnreadAtKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s unread: from: {}, msgIds: {}", to, from, atMsgIds);
      if (atMsgIds != null) {
        unreadAt += atMsgIds.size();
      }

      // 获取过期数据中的未读数
      String cleared = redisTemplate.opsForValue().get(getClearedUnreadKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s cleard unread: from: {}, cleared: {}", to, from, cleared);
      if (cleared != null && !cleared.isEmpty()) {
        unread += Integer.valueOf(cleared);
      }

      // 获取过期数据中的at未读数
      String atCleared = redisTemplate.opsForValue().get(getClearedUnreadAtKey(to + SESSION_SPLIT + from));
      LOGGER.info("get [{}]'s cleard unread: from: {}, at cleared: {}", to, from, atCleared);
      if (atCleared != null && !atCleared.isEmpty()) {
        unreadAt += Integer.valueOf(atCleared);
      }
      if (unread == 0 && unreadAt == 0) {
        continue;
      }
      UnreadResponse unreadResponse = new UnreadResponse(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0], to);
      if (from.endsWith(Constant.GROUP_CHAT_KEY_POSTFIX)) {
        unreadResponse.setGroupTemail(unreadResponse.getFrom());
      }
      if (unread != 0) {
        unreadResponse.setUnread(unread);
      }
      if (unreadAt != 0) {
        unreadResponse.setUnreadAt(unreadAt);
      }
      unreadResponses.add(unreadResponse);
    }
    LOGGER.info("get [{}]'s unread responses: {}", to, unreadResponses);
    return unreadResponses;
  }

  /**
   * 获取未读数总数
   */
  public Map<String, Integer> getPushUnread(String to) {
    // 当部署的不是C群时，不统计群聊未读数
    boolean crowdEnabled = Boolean.valueOf(notificationConfig.crowdEnabled);
    List<UnreadResponse> unreadResponses = getUnread(to);
    Map<String,Integer> unreadMap = new HashMap<>(4);
    Integer unreadCount = null;
    Integer unreadAtCount = null;
    if (crowdEnabled) {
      unreadCount = unreadResponses.stream().mapToInt(UnreadResponse::getUnread).sum();
      unreadAtCount = unreadResponses.stream().mapToInt(UnreadResponse::getUnreadAt).sum();
    } else {
      unreadCount = unreadResponses.stream().filter(unreadResponse -> unreadResponse.getGroupTemail() == null)
          .mapToInt(UnreadResponse::getUnread).sum();
      unreadAtCount = unreadResponses.stream().filter(unreadResponse -> unreadResponse.getGroupTemail() == null)
          .mapToInt(UnreadResponse::getUnreadAt).sum();
    }
    unreadMap.put(UNREAD, unreadCount);
    unreadMap.put(UNREADAT, unreadAtCount);
    return unreadMap;
  }
}