package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.main.constants.Constant;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class UnreadServiceTest {

  @Autowired
  private UnreadService unreadService;

  @Test
  public void testGetUnread() {
    String from = "a";
    String from2 = "a2";
    String groupTemail = "g";
    String to = "b";
    unreadService.reset(from, to);
    unreadService.reset(from2, to);
    unreadService.reset(groupTemail + Constant.GROUP_CHAT_KEY_POSTFIX, to);
    unreadService.add(from, to, "1");
    unreadService.add(from2, to, "1");
    unreadService.add(groupTemail + Constant.GROUP_CHAT_KEY_POSTFIX, to, "1");
    unreadService.reset(from, to);
    unreadService.add(from, to, "2");
    unreadService.add(from, to, "3");
    unreadService.remove(from, to, Collections.singletonList("1"));

    List<UnreadResponse> unreadResponses = unreadService.getUnread(to);
    Assertions.assertThat(unreadResponses).isNotNull();
    Assertions.assertThat(unreadResponses).isNotEmpty();

    int unread = 0;
    for (UnreadResponse unreadResponse : unreadResponses) {
      if (unreadResponse.getFrom().equals(from)) {
        unread = unreadResponse.getUnread();
      }
    }
    Assertions.assertThat(unread).isEqualTo(2);
    Assertions.assertThat(unreadService.getUnreadSum(to)).isEqualTo(4);
  }
}