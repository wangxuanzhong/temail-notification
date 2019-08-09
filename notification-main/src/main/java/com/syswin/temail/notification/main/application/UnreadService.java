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

import com.syswin.temail.notification.main.constants.Constant;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liusen@syswin.com
 */
@Service
public class UnreadService {

  private static final String UNREAD_KEY = "unread_";
  private static final String CLEARED_KEY = "cleared_";
  private static final String SESSION_SPLIT = ":::";

  private final StringRedisTemplate redisTemplate;

  @Autowired
  public UnreadService(StringRedisTemplate stringRedisTemplate) {
    this.redisTemplate = stringRedisTemplate;
  }

  private String getUnreadKey(String body) {
    return Constant.REDIS_KEY_PREFIX + UNREAD_KEY + body;
  }

  private String getClearedUnreadKey(String body) {
    return Constant.REDIS_KEY_PREFIX + CLEARED_KEY + UNREAD_KEY + body;
  }

  /**
   * 添加msgId
   */
  public void add(String from, String to, String msgId) {
    // 添加会话
    redisTemplate.opsForSet().add(getUnreadKey(to), from);
    // 添加msgId
    redisTemplate.opsForSet().add(getUnreadKey(to + SESSION_SPLIT + from), msgId);
  }

  /**
   * 删除msgId
   */
  public void remove(String from, String to, List<String> msgIds) {
    redisTemplate.opsForSet().remove(getUnreadKey(to + SESSION_SPLIT + from), msgIds.toArray());
  }

  /**
   * 重置未读数
   */
  public void reset(String from, String to) {
    // 删除会话
    redisTemplate.opsForSet().remove(getUnreadKey(to), from);
    // 删除会话的所有msgId
    redisTemplate.delete(getUnreadKey(to + SESSION_SPLIT + from));
  }

  /**
   * 获取未读数
   */
  public List<UnreadResponse> getUnread(String to) {
    List<UnreadResponse> unreadResponses = new ArrayList<>();

    // 查询出所有会话
    Set<String> froms = redisTemplate.opsForSet().members(getUnreadKey(to));
    if (froms == null) {
      return unreadResponses;
    }

    froms.forEach(from -> {
      int unread = 0;
      // 获取会话中未读数
      Set<String> msgIds = redisTemplate.opsForSet().members(getUnreadKey(to + SESSION_SPLIT + from));
      if (msgIds != null) {
        unread += msgIds.size();
      }

      // 获取过期数据中的未读数
      String cleared = redisTemplate.opsForValue().get(getClearedUnreadKey(to + SESSION_SPLIT + from));
      if (cleared != null && !cleared.isEmpty()) {
        unread += Integer.valueOf(cleared);
      }

      UnreadResponse unreadResponse = new UnreadResponse(from.split(Constant.GROUP_CHAT_KEY_POSTFIX)[0], to, unread);
      if (from.endsWith(Constant.GROUP_CHAT_KEY_POSTFIX)) {
        unreadResponse.setGroupTemail(unreadResponse.getFrom());
      }
      unreadResponses.add(unreadResponse);
    });

    return unreadResponses;
  }

  /**
   * 获取未读数总数
   */
  public int getUnreadSum(String to) {
    return getUnread(to).stream().mapToInt(UnreadResponse::getUnread).sum();
  }
}