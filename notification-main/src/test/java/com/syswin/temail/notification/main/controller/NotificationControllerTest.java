package com.syswin.temail.notification.main.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class NotificationControllerTest {

  @Autowired
  WebApplicationContext wac;

  MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testSaveMenu() throws Exception {
    mockMvc.perform(post("/menus")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"menuName\":\"功能管理\",\"path\":\"/userrights-functions\",\"parentMenuCode\":1}"))
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
