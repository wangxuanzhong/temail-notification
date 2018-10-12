package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisServiceTest {

  @Autowired
  private RedisService redisService;

  @Test
  public void getNextSeq() {
    String key = "test_user";
    redisService.deleteKey(key);
    for (long i = 1; i <= 5; i++) {
      long seq = redisService.getNextSeq(key);
      System.out.println(seq);
      assertThat(seq).isEqualTo(i);
    }
  }

  //  @Test
  public void testDeleteKey() {
    redisService.deleteKey("jack@t.email");
    redisService.deleteKey("sean@t.email");
    redisService.deleteKey("Jack@t.email");
    redisService.deleteKey("Sean@t.email");
    redisService.deleteKey("bob@temail.com");
    redisService.deleteKey("alice@temail.com");
  }
}