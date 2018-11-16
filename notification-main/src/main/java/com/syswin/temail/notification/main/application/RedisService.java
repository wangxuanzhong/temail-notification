package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService implements SequenceService {

  private final RedisTemplate<String, ?> redisTemplate;

  @Autowired
  public RedisService(RedisTemplate<String, ?> redisTemplate) {
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
   * 删除数据
   */
  public void deleteKey(String key) {
    System.out.println(redisTemplate.opsForValue().getOperations().delete(getKey(key)));
  }
}