package com.syswin.temail.notification.main.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Event.EventType;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

  private final String TEST_FROM = "11111111";
  private final String TEST_TO = "00000000";

  private Gson gson = new Gson();
  private Event event;

  @Autowired
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    event = new Event();
    event.setEventType(EventType.RECEIVE.getValue());
    event.setFrom(TEST_FROM);
    event.setTo(TEST_TO);
    event.setMessageId(12345678L);
    event.setMessage("aaaaaaaa");
  }

  @Test
  public void testSendMessage() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("data", gson.toJson(event));

    mockMvc.perform(post("/notification")
        .contentType(MediaType.APPLICATION_JSON)
        .content((new Gson()).toJson(body)))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetEvents() throws Exception {
    String userId = "601183";
    String sequenceNo = "1";
    mockMvc.perform(get("/notification/events")
        .header("CDTP-header", "aaaaa")
        .param("userId", userId)
        .param("sequenceNo", sequenceNo))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.data").isNotEmpty());
//        .andExpect(jsonPath("$.data.children[0].menuCode").value(100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].menuCode").value(100100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].functions[0].functionName").value("新增菜单"));
  }
}
