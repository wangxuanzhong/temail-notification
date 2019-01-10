package com.syswin.temail.notification.main.application;


import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.OssEventType;
import com.syswin.temail.notification.main.domains.params.MailAgentOssParams;
import com.syswin.temail.notification.main.infrastructure.OssEventMapper;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationOssService {

  private Logger LOGGER  = LoggerFactory.getLogger(NotificationOssService.class);
  private final OssEventMapper ossEventMapper;
  private final JsonService jsonService;

  @Autowired
  public NotificationOssService(OssEventMapper ossEventMapper, JsonService jsonService) {
    this.ossEventMapper = ossEventMapper;
    this.jsonService = jsonService;
  }


  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) {

    MailAgentOssParams params = jsonService.fromJson(body, MailAgentOssParams.class);

    LOGGER.info("temail-oss params: {}", params);
    LOGGER.info("temail-oss event type: {}", OssEventType.getByValue(params.getType()));


    switch (Objects.requireNonNull(OssEventType.getByValue(params.getType()))) {
      case USER_TEMAIL_DELETED:
        List<String> temails = jsonService.fromJson(params.getTemails(), List.class);
        temails.forEach(ossEventMapper::deleteTemail);
        break;
    }

  }
}
