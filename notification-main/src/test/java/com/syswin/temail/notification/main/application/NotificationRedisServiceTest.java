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
public class NotificationRedisServiceTest {

  @Autowired
  private NotificationRedisService notificationRedisService;

  @Test
  public void getNextSeq() {
    String key = "test_user";
    notificationRedisService.deleteSeq(key);
    for (long i = 1; i <= 5; i++) {
      long seq = notificationRedisService.getNextSeq(key);
      System.out.println(seq);
      assertThat(seq).isEqualTo(i);
    }
  }

  @Test
  public void testCheckUnique() {
    String key = UUID.randomUUID().toString();
    assertThat(notificationRedisService.checkUnique(key)).isTrue();
    assertThat(notificationRedisService.checkUnique(key)).isFalse();
  }

  @Test
  @Ignore
  public void testDeleteSeq() {
    notificationRedisService.deleteSeq("jack@t.email");
    notificationRedisService.deleteSeq("sean@t.email");
    notificationRedisService.deleteSeq("Jack@t.email");
    notificationRedisService.deleteSeq("Sean@t.email");
    notificationRedisService.deleteSeq("bob@temail.com");
    notificationRedisService.deleteSeq("alice@temail.com");
  }
}