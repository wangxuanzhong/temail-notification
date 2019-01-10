package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.main.domains.OssEventType;
import com.syswin.temail.notification.main.domains.params.OSSParams;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationOssServiceTest {

  @Autowired
  private NotificationOssService service;
  private Gson gson = new Gson();
  private OSSParams params;

  @Before
  public void setUp() {
    params = new OSSParams();
    params.setId(2);
    params.setTemails(Arrays.asList("a@t.email", "b@t.email", "c@t.email"));
    params.setType(OssEventType.USER_TEMAIL_DELETED.getValue());
    params.setTimestamp(System.currentTimeMillis());
  }

  @Test
  public void deleteTemailTest() {
    service.handleMqMessage(gson.toJson(params));
  }

}
