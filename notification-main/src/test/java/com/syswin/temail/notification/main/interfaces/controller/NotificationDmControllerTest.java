package com.syswin.temail.notification.main.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syswin.temail.notification.main.application.NotificationDmServiceImpl;
import com.syswin.temail.notification.main.domains.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class NotificationDmControllerTest {

  @MockBean
  NotificationDmServiceImpl dmService;

  @Autowired
  private MockMvc mvc;

  @Test
  public void testSavePacketEvent() throws Exception {
    Mockito.doNothing().when(dmService).savePacketEvent(Mockito.any(Event.class), Mockito.anyString(), Mockito.anyString());
    mvc.perform(MockMvcRequestBuilders.post("/notification/packet")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .header("CDTP-header", "header")
        .header("X-PACKET-ID", "xPacketId")
        .content(new ObjectMapper().writeValueAsString(new Event()))
    ).andExpect(MockMvcResultMatchers.status().isOk());
  }
}