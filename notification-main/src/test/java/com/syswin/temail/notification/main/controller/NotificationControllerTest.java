package com.syswin.temail.notification.main.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testNotificationEvents() throws Exception {
    String from = "b";
    mockMvc.perform(get("/notification/events")
        .header("CDTP-header", "aaaaa")
        .param("from", from)
        .param("seqId", "1")
        .param("pageSize", "3"))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data").isArray());
//        .andExpect(jsonPath("$.data.children[0].menuCode").value(100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].menuCode").value(100100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].functions[0].functionName").value("新增菜单"));
  }

  @Test
  public void testNotificationUnread() throws Exception {
    String from = "b";
    mockMvc.perform(get("/notification/unread")
        .header("CDTP-header", "aaaaa")
        .param("from", from))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data").isArray());
//        .andExpect(jsonPath("$.data.children[0].menuCode").value(100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].menuCode").value(100100L))
//        .andExpect(jsonPath("$.data.children[0].children[0].functions[0].functionName").value("新增菜单"));
  }
}
