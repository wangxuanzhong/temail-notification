package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.ISequenceService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationRedisService implements ISequenceService {

  private final RedisTemplate<String, String> redisTemplate;

  @Autowired
  public NotificationRedisService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
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
   * 删除数据
   */
  public void deleteKey(String key) {
    System.out.println(redisTemplate.delete(key));
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