package com.syswin.temail.notification.main.application;


import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.main.domains.OssType;
import com.syswin.temail.notification.main.domains.params.OssParams;
import com.syswin.temail.notification.main.infrastructure.NotificationOssMapper;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class NotificationOssService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final NotificationOssMapper notificationOssMapper;
  private final IJsonService iJsonService;

  @Autowired
  public NotificationOssService(NotificationOssMapper notificationOssMapper, IJsonService iJsonService) {
    this.notificationOssMapper = notificationOssMapper;
    this.iJsonService = iJsonService;
  }


  @Transactional(rollbackFor = Exception.class)
  public void handleMqMessage(String body) {

    OssParams params = iJsonService.fromJson(body, OssParams.class);

    LOGGER.info("temail-oss params: {}", params);
    LOGGER.info("temail-oss type: {}", OssType.getByValue(params.getType()));

    switch (Objects.requireNonNull(OssType.getByValue(params.getType()))) {
      case USER_TEMAIL_DELETED:
        if (!CollectionUtils.isEmpty(params.getTemails())) {
          params.getTemails().forEach(notificationOssMapper::deleteTemail);
        }
        break;
    }

  }
}
