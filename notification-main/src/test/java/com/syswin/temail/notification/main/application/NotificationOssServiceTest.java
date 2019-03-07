package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.params.OssParams;
import com.syswin.temail.notification.main.domains.params.OssParams.OssType;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class NotificationOssServiceTest {

  @Autowired
  private NotificationOssService service;
  private Gson gson = new Gson();
  private OssParams params;

  @Before
  public void setUp() {
    params = new OssParams();
    params.setId(2L);
    params.setTemails(Arrays.asList("a", "b", "c"));
    params.setType(OssType.UserTemailDeleted.name());
    params.setTimestamp(System.currentTimeMillis());
  }

  @Test
  public void deleteTemailTest() {
    service.handleMqMessage(gson.toJson(params));
  }

}
