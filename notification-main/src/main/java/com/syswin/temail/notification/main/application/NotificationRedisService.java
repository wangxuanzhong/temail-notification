package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.ISequenceService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * redis功能类
 *
 * @author liusen
 */
@Service
public class NotificationRedisService implements ISequenceService {

  private final StringRedisTemplate redisTemplate;

  @Autowired
  public NotificationRedisService(StringRedisTemplate stringRedisTemplate) {
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

    // 设置key有效时间为5s
    redisTemplate.expire(key, 5, TimeUnit.SECONDS);
    return result == null ? false : result;
  }

  /**
   * 添加锁
   */
  public boolean checkLock(String key, long timeout, TimeUnit unit) {
    Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "value");

    // 设置key有效时间为5s
    redisTemplate.expire(key, timeout, unit);
    return result == null ? false : result;
  }
}