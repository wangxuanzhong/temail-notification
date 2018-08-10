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
