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
public class NotificationRedisServiceImplTest {

  @Autowired
  private NotificationRedisServiceImpl notificationRedisServiceImpl;

  @Test
  public void getNextSeq() {
    String key = "test_user";
    notificationRedisServiceImpl.deleteSeq(key);
    for (long i = 1; i <= 5; i++) {
      long seq = notificationRedisServiceImpl.getNextSeq(key);
      System.out.println(seq);
      assertThat(seq).isEqualTo(i);
    }
  }

  @Test
  public void testCheckUnique() {
    String key = UUID.randomUUID().toString();
    assertThat(notificationRedisServiceImpl.checkUnique(key)).isTrue();
    assertThat(notificationRedisServiceImpl.checkUnique(key)).isFalse();
  }

  @Test
  @Ignore
  public void testDeleteSeq() {
    notificationRedisServiceImpl.deleteSeq("jack@t.email");
    notificationRedisServiceImpl.deleteSeq("sean@t.email");
    notificationRedisServiceImpl.deleteSeq("Jack@t.email");
    notificationRedisServiceImpl.deleteSeq("Sean@t.email");
    notificationRedisServiceImpl.deleteSeq("bob@temail.com");
    notificationRedisServiceImpl.deleteSeq("alice@temail.com");
  }
}