package com.syswin.temail.notification.main.application;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.params.DmParams;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.CDTPDispatcher;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationDmService implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;

  @Autowired
  public NotificationDmService(IMqProducer iMqProducer, NotificationRedisService notificationRedisService,
      EventMapper eventMapper, IJsonService iJsonService) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
  }

  /**
   * 处理从MQ收到的信息
   */
  @Override
  public void handleMqMessage(String body, String tags) {
    DmParams params = iJsonService.fromJson(body, DmParams.class);

    LOGGER.info("dm params: {}", params);

    if (params.getType() != DmParams.TYPE_3_RESPONSE) {
      LOGGER.info("message is not mq response!");
      return;
    }

    CDTPPacket cdtpPacket = CDTPDispatcher.MessageToCDTPPacket(params.getGroupchatMessage());

    Event event = new Event();
    event.setPacket("");

    this.savePacketEvent(event, iJsonService.toJson(cdtpPacket.getHeader()), cdtpPacket.getHeader().getPacketId());
  }

  public void savePacketEvent(Event event, String header, String xPacketId) {

  }

}
