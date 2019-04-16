package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.foundation.exceptions.BaseException;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationDmService implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String GET_PUBLIC_KEY = "/temails/%s";
  public static final int EVENT_COMMAND_SPACE = 0x1;
  public static final int EVENT_COMMAND = 0x3000;

  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;
  private final RestTemplate restTemplate;
  private final NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();

  private final String saasEnabled;
  private final String groupChatTopic;
  private final String applicationTopic;
  private final String authUrl;

  @Autowired
  public NotificationDmService(IMqProducer iMqProducer, NotificationRedisService notificationRedisService,
      EventMapper eventMapper,
      IJsonService iJsonService, RestTemplate notificationRestTemplate,
      @Value("${app.temail.notification.saas.enabled:false}") String saasEnabled,
      @Value("${spring.rocketmq.topics.notify.groupChat:notify}") String groupChatTopic,
      @Value("${spring.rocketmq.topics.notify.application:notify}") String applicationTopic,
      @Value("${url.temail.auth:authUrl}") String authUrl) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
    this.restTemplate = notificationRestTemplate;
    this.saasEnabled = saasEnabled;
    this.groupChatTopic = groupChatTopic;
    this.applicationTopic = applicationTopic;
    this.authUrl = authUrl;
  }

  /**
   * 保存报文事件
   */
  @Transactional(rollbackFor = Exception.class)
  public void savePacketEvent(Event event, String header, String xPacketId) {
    LOGGER.info("save packet event: {}", xPacketId);
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
    CDTPResponse response = new CDTPResponse(event.getTo(), event.getEventType(), header,
        Event.toJson(iJsonService, event));
    Map<String, Object> extraDataMap = iJsonService
        .fromJson(cdtpHeader.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());
    if (Boolean.valueOf(saasEnabled) && extraDataMap != null) {
      String type = extraDataMap.get("type").toString();
      if (type == null) {
        iMqProducer.sendMessage(iJsonService.toJson(response));
      } else if (type.startsWith("A")) { // 新群聊 topic
        iMqProducer.sendMessage(Event.toJson(iJsonService, event), groupChatTopic, "", "");
      } else if (type.startsWith("B")) { // 协同应用 topic
        iMqProducer.sendMessage(Event.toJson(iJsonService, event), applicationTopic, "", "");
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
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    Event event = iJsonService.fromJson(body, Event.class);
    LOGGER.info("dm params: {}", event);
    event.setEventType(EventType.PACKET.getValue());

    // 如果接收人不是本域的账号，则不需要发送通知
    if (!checkIsSameDomain(event.getTo())) {
      LOGGER.info("[{}] belong to another domain.", event.getTo());
      return;
    }
    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(event.getPacket()));
    short commandSpace = cdtpPacket.getCommandSpace();
    short command = cdtpPacket.getCommand();
    if (!(commandSpace == EVENT_COMMAND_SPACE && command == EVENT_COMMAND)) {
      LOGGER.info("packet is not event packet,packetId={},packet={}", event.getxPacketId(), body);
      return;
    }

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, notificationRedisService)) {
      return;
    }

    event.initEventSeqId(notificationRedisService);
    event.autoWriteExtendParam(iJsonService);
    eventMapper.insert(event);

    // 解析packet取出CDTPHeader推送给dispatcher
    String header = iJsonService.toJson(cdtpPacket.getHeader());

    iMqProducer.sendMessage(iJsonService
        .toJson(new CDTPResponse(event.getTo(), event.getEventType(), header, Event.toJson(iJsonService, event))));
  }


  public boolean checkIsSameDomain(String temail) {
    LOGGER.info("check temail: [{}] is same domain or not.", temail);
    String url = authUrl + String.format(GET_PUBLIC_KEY, temail);
    try {
      // 调用auth获取公钥接口，接口返回404则表示用户不存在或是不在本域。
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (RestClientException e) {
      LOGGER.warn("check domain exception: ", e);
      throw new BaseException("check domain exception: ", e);
    }
  }
}
