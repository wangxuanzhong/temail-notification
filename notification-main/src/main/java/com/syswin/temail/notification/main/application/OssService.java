package com.syswin.temail.notification.main.application;


import com.syswin.temail.notification.foundation.application.JsonService;
import com.syswin.temail.notification.main.domains.OssType;
import com.syswin.temail.notification.main.domains.params.OssParams;
import com.syswin.temail.notification.main.infrastructure.OssMapper;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class OssService {

  private final OssMapper ossMapper;
  private final JsonService jsonService;
  private Logger LOGGER = LoggerFactory.getLogger(OssService.class);

  @Autowired
  public OssService(OssMapper ossMapper, JsonService jsonService) {
    this.ossMapper = ossMapper;
    this.jsonService = jsonService;
  }


  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) {

    OssParams params = jsonService.fromJson(body, OssParams.class);

    LOGGER.info("temail-oss params: {}", params);
    LOGGER.info("temail-oss event type: {}", OssType.getByValue(params.getType()));

    switch (Objects.requireNonNull(OssType.getByValue(params.getType()))) {
      case USER_TEMAIL_DELETED:
        if (!CollectionUtils.isEmpty(params.getTemails())) {
          params.getTemails().forEach(ossMapper::deleteTemail);
        }
        break;
    }

  }
}
