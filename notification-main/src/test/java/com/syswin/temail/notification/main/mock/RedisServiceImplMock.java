package com.syswin.temail.notification.main.mock;

import com.syswin.temail.notification.main.application.NotificationRedisServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisServiceImplMock extends NotificationRedisServiceImpl {

  private static List<String> keys = new ArrayList<>();
  private static Map<String, Long> seqMap = new HashMap<>();

  public RedisServiceImplMock() {
    super(Mockito.mock(StringRedisTemplate.class));
  }


  @Override
  public synchronized Long getNextSeq(String key) {
    if (seqMap.containsKey(key)) {
      Long seq = seqMap.get(key);
      seqMap.put(key, ++seq);
      return seq;
    } else {
      Long seq = 1L;
      seqMap.put(key, seq);
      return seq;
    }
  }

  @Override
  public synchronized boolean checkUnique(String key) {
    if (keys.contains(key)) {
      return false;
    } else {
      keys.add(key);
      return true;
    }
  }
}
