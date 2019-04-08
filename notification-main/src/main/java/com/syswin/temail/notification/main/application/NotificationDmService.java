package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.params.DmParams;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.CDTPDispatcher;
import com.syswin.temail.notification.main.util.NotificationPacketUtil;
import com.syswin.temail.notification.main.util.NotificationUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationDmService implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;

  @Value("${app.temail.notification.saas.enabled}")
  private String saasEnabled;
  @Value("${spring.rocketmq.topics.notify.groupChat}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.notify.application}")
  private String applicationTopic;

  @Autowired
  public NotificationDmService(IMqProducer iMqProducer, NotificationRedisService notificationRedisService,
      EventMapper eventMapper, IJsonService iJsonService) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
  }

  /**
   * 保存报文事件
   */
  public void savePacketEvent(Event event, String header, String xPacketId, boolean isMq) {
    LOGGER.info("save packet event: {}, from mq: {}", xPacketId, isMq);
    event.setEventType(EventType.PACKET.getValue());
    event.setxPacketId(xPacketId);

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, notificationRedisService)) {
      return;
    }

    CDTPHeader cdtpHeader = iJsonService.fromJson(header, CDTPHeader.class);
    event.setFrom(cdtpHeader.getSender());
    event.setTo(cdtpHeader.getReceiver());
    event.initEventSeqId(notificationRedisService);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    LOGGER.info("send packet event to {}", event.getTo());
    CDTPResponse response = new CDTPResponse(event.getTo(), event.getEventType(), header, event.toJson(iJsonService));
    if (Boolean.valueOf(saasEnabled) && !isMq) {
      Map<String, Object> extraDataMap = iJsonService.fromJson(cdtpHeader.getExtraData(), new TypeToken<Map<String, Object>>() {
      }.getType());
      String type = extraDataMap.get("type").toString();

      if (type.startsWith("A")) { // 新群聊 topic
        iMqProducer.sendMessage(iJsonService.toJson(response), groupChatTopic, "", "");
      } else if (type.startsWith("B")) { // 协同应用 topic
        iMqProducer.sendMessage(iJsonService.toJson(response), applicationTopic, "", "");
      } else {  // dispatcher topic
        iMqProducer.sendMessage(iJsonService.toJson(response));
      }
    } else { // dispatcher tpoic
      iMqProducer.sendMessage(iJsonService.toJson(response));
    }
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

    // 如果接收人不是本域的账号，则不需要发送通知


    Event event = new Event();
    event.setPacket(NotificationPacketUtil.encodeData(NotificationPacketUtil.pack(cdtpPacket)));

    this.savePacketEvent(event, iJsonService.toJson(cdtpPacket.getHeader()), cdtpPacket.getHeader().getPacketId(), true);
  }

}
