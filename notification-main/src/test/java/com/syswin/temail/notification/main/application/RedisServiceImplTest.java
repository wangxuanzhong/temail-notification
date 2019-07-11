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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Ignore
public class RedisServiceImplTest {

  @Autowired
  private RedisServiceImpl redisService;

  @Test
  public void getNextSeq() {
    String key = "test_user";
    redisService.deleteSeq(key);
    for (long i = 1; i <= 5; i++) {
      long seq = redisService.getNextSeq(key);
      System.out.println(seq);
      assertThat(seq).isEqualTo(i);
    }
  }

  @Test
  public void testCheckUnique() {
    String key = UUID.randomUUID().toString();
    assertThat(redisService.checkUnique(key)).isTrue();
    assertThat(redisService.checkUnique(key)).isFalse();
  }

  @Test
  @Ignore
  public void testDeleteSeq() {
    redisService.deleteSeq("jack@t.email");
    redisService.deleteSeq("sean@t.email");
    redisService.deleteSeq("Jack@t.email");
    redisService.deleteSeq("Sean@t.email");
    redisService.deleteSeq("bob@temail.com");
    redisService.deleteSeq("alice@temail.com");
  }
}