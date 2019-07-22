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

package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventExtendParam;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class NotificationUtilTest {

  @Test
  public void testCopyField() {
    Event event = new Event();
    event.setName("name");
    event.setAdminName("adminName");
    event.setGroupName("groupName");
    event.setAt("at");
    event.setMsgIds(Arrays.asList("id1", "id2", "id3"));
    event.setDeleteAllMsg(true);
    event.setOwner("owner");
    event.setTrashMsgInfo("trashMsgInfo");
    event.setAuthor("author");
    event.setFilter(Arrays.asList("a", "b", "c"));
    event.setExtData("extData");
    event.setMemberExtData("memberExtData");
    event.setSessionExtData("sessionExtData");
    event.setSharedKey("sharedKey");
    event.setFromNickName("发送方昵称");
    event.setFromGroupName("新群聊群昵称");

    EventExtendParam eventExtendParam = new EventExtendParam();
    NotificationUtil.copyField(event, eventExtendParam);

    Assertions.assertThat(eventExtendParam.getName()).isEqualTo(event.getName());
    Assertions.assertThat(eventExtendParam.getAdminName()).isEqualTo(event.getAdminName());
    Assertions.assertThat(eventExtendParam.getGroupName()).isEqualTo(event.getGroupName());
    Assertions.assertThat(eventExtendParam.getAt()).isEqualTo(event.getAt());
    Assertions.assertThat(eventExtendParam.getMsgIds()).isEqualTo(event.getMsgIds());
    Assertions.assertThat(eventExtendParam.getDeleteAllMsg()).isEqualTo(event.getDeleteAllMsg());
    Assertions.assertThat(eventExtendParam.getOwner()).isEqualTo(event.getOwner());
    Assertions.assertThat(eventExtendParam.getTrashMsgInfo()).isEqualTo(event.getTrashMsgInfo());
    Assertions.assertThat(eventExtendParam.getAuthor()).isEqualTo(event.getAuthor());
    Assertions.assertThat(eventExtendParam.getFilter()).isEqualTo(event.getFilter());
    Assertions.assertThat(eventExtendParam.getExtData()).isEqualTo(event.getExtData());
    Assertions.assertThat(eventExtendParam.getMemberExtData()).isEqualTo(event.getMemberExtData());
    Assertions.assertThat(eventExtendParam.getSessionExtData()).isEqualTo(event.getSessionExtData());
    Assertions.assertThat(eventExtendParam.getSharedKey()).isEqualTo(event.getSharedKey());
    Assertions.assertThat(eventExtendParam.getFromNickName()).isEqualTo(event.getFromNickName());
    Assertions.assertThat(eventExtendParam.getFromGroupName()).isEqualTo(event.getFromGroupName());
  }
}