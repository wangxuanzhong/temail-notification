package com.syswin.temail.notification.main.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
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

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testSendMessage() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("data", "测试消息");

    mockMvc.perform(post("/notification")
        .contentType(MediaType.APPLICATION_JSON)
        .content((new Gson()).toJson(body)))
        .andExpect(status().isOk());
  }

  @Test
  public void testUpdateMenu() throws Exception {
    mockMvc.perform(put("/menus/106")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"menuName\":\"税务查询\",\"path\":\"tax\"}"))
        .andExpect(status().isOk());
  }

  @Test
  public void testDeleteMenu() throws Exception {
    mockMvc.perform(delete("/menus/106"))
        .andExpect(status().isOk());
  }

  @Test
  public void testgetMenuTree() throws Exception {
    mockMvc.perform(get("/menus/tree"))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.data.children").isNotEmpty())
        .andExpect(jsonPath("$.data.children[0].menuCode").value(100L))
        .andExpect(jsonPath("$.data.children[0].children[0].menuCode").value(100100L))
        .andExpect(jsonPath("$.data.children[0].children[0].functions[0].functionName").value("新增菜单"));
  }
}
