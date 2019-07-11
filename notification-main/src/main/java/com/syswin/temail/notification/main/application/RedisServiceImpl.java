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

import com.syswin.temail.notification.foundation.application.ISequenceService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liusen@syswin.com
 */
@Service
public class RedisServiceImpl implements ISequenceService {

  private final StringRedisTemplate redisTemplate;

  @Autowired
  public RedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
    this.redisTemplate = stringRedisTemplate;
  }

  private String getKey(String body) {
    return KEY_PREFIX + body;
  }

  /**
   * 获取下个序号
   */
  @Override
  public Long getNextSeq(String key) {
    return redisTemplate.opsForValue().increment(getKey(key), STEP);
  }

  /**
   * 删除序号
   */
  public void deleteSeq(String key) {
    System.out.println(redisTemplate.delete(getKey(key)));
  }

  /**
   * 检查唯一性
   */
  public boolean checkUnique(String key) {
    Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "value");
    if (result == null) {
      return false;
    }
    // 设置key有效时间为5s
    redisTemplate.expire(key, 5, TimeUnit.SECONDS);
    return result;
  }

  /**
   * 添加锁
   */
  public boolean checkLock(String key, long timeout, TimeUnit unit) {
    Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "value");
    if (result == null) {
      return false;
    }
    // 设置key有效时间为5s
    redisTemplate.expire(key, timeout, unit);
    return result;
  }
}