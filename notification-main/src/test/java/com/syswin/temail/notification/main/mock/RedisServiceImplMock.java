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
