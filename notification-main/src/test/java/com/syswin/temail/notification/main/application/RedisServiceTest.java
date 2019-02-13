package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RedisServiceTest {

  @Autowired
  private RedisService redisService;

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

  //  @Test
  public void testDeleteSeq() {
    redisService.deleteSeq("jack@t.email");
    redisService.deleteSeq("sean@t.email");
    redisService.deleteSeq("Jack@t.email");
    redisService.deleteSeq("Sean@t.email");
    redisService.deleteSeq("bob@temail.com");
    redisService.deleteSeq("alice@temail.com");
  }
}