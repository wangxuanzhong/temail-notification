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

package com.syswin.temail.notification.main.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.NotificationEventService;
import com.syswin.temail.notification.main.application.NotificationTopicServiceImpl;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private NotificationEventService notificationEventService;

  @MockBean
  private NotificationTopicServiceImpl notificationTopicServiceImpl;

  private String header = "header";

  @Test
  public void testGetEvents() throws Exception {
    Response<Map<String, Object>> response = new Response<>(HttpStatus.OK, null, new HashMap<>());
    Mockito.when(notificationEventService.getEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(new HashMap<>());

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/events")
        .header("CDTP-header", header)
        .param("from", "b")
        .param("eventSeqId", "1")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }

  @Test
  public void testGetUnread() throws Exception {
    UnreadResponse unreadResponse = new UnreadResponse("from", "to", 1);
    Response<List<UnreadResponse>> response = new Response<>(HttpStatus.OK, null, Collections.singletonList(unreadResponse));
    Mockito.when(notificationEventService.getUnread(Mockito.anyString())).thenReturn(Collections.singletonList(unreadResponse));

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/unread")
        .header("CDTP-header", header)
        .param("from", "b")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }

  @Test
  public void testReset() throws Exception {
    Event event = new Event();

    mvc.perform(MockMvcRequestBuilders.put("/notification/reset")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("CDTP-header", "header")
        .content(new ObjectMapper().writeValueAsString(event))
    ).andExpect(MockMvcResultMatchers.status().isBadRequest());

    event.setTo("b");
    mvc.perform(MockMvcRequestBuilders.put("/notification/reset")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("CDTP-header", "header")
        .content(new ObjectMapper().writeValueAsString(event))
    ).andExpect(MockMvcResultMatchers.status().isBadRequest());

    event.setGroupTemail("g");
    mvc.perform(MockMvcRequestBuilders.put("/notification/reset")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("CDTP-header", "header")
        .content(new ObjectMapper().writeValueAsString(event))
    ).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void testGetReplyEvents() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/notification/reply/events")
        .header("CDTP-header", header)
        .param("parentMsgId", UUID.randomUUID().toString())
        .param("eventSeqId", "1")
    ).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void testGetTopicEvents() throws Exception {
    Response<Map<String, Object>> response = new Response<>(HttpStatus.OK, null, new HashMap<>());
    Mockito.when(notificationTopicServiceImpl.getTopicEvents(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(new HashMap<>());

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/topic/events")
        .header("CDTP-header", header)
        .param("from", "b")
        .param("eventSeqId", "1")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }

  @Test
  public void testUpdateGroupChatUserStatus() throws Exception {
    Member member = new Member();

    mvc.perform(MockMvcRequestBuilders.put("/notification/groupchat/user/status")
        .header("CDTP-header", header)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content((new ObjectMapper()).writeValueAsString(member))
    ).andExpect(MockMvcResultMatchers.status().isBadRequest());

    member.setUserStatus(1);
    Mockito.doNothing().when(notificationEventService)
        .updateGroupChatUserStatus(Mockito.any(Member.class), Mockito.any(UserStatus.class), Mockito.anyString());

    mvc.perform(MockMvcRequestBuilders.put("/notification/groupchat/user/status")
        .header("CDTP-header", header)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content((new ObjectMapper()).writeValueAsString(member))
    ).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void testGetUserDoNotDisturbGroups() throws Exception {
    Response<Map<String, Integer>> response = new Response<>(HttpStatus.OK, null, new HashMap<>());
    Mockito.when(notificationEventService.getGroupChatUserStatus(Mockito.anyString(), Mockito.anyString())).thenReturn(new HashMap<>());

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/groupchat/user/status")
        .header("CDTP-header", header)
        .param("temail", "b")
        .param("groupTemail", "g")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }

  @Test
  public void testGetEventsLimited() throws Exception {
    Response<Map<String, Object>> response = new Response<>(HttpStatus.OK, null, new HashMap<>());
    Mockito.when(notificationEventService.getEventsLimited(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(new HashMap<>());

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/limit/events")
        .header("CDTP-header", header)
        .param("from", "b")
        .param("eventSeqId", "1")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }

  @Test
  public void testGetTopicEventsLimited() throws Exception {
    Response<Map<String, Object>> response = new Response<>(HttpStatus.OK, null, new HashMap<>());
    Mockito.when(notificationTopicServiceImpl.getTopicEventsLimited(Mockito.anyString(), Mockito.anyLong(), Mockito.anyInt()))
        .thenReturn(new HashMap<>());

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/notification/limit/topic/events")
        .header("CDTP-header", header)
        .param("from", "b")
        .param("eventSeqId", "1")
    ).andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    Assertions.assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(new ObjectMapper().writeValueAsString(response));
  }
}