package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.foundation.exceptions.BaseException;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.CdtpResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.EventUtil;
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
import org.springframework.web.client.RestTemplate;

/**
 * dm事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class NotificationDmServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String GET_PUBLIC_KEY_PATH = "/temails/%s";
  private static final int EVENT_COMMAND_SPACE = 0x1;
  private static final int EVENT_COMMAND = 0x3000;

  private static final String GROUP_CHAT_TYPE_PREFIX = "A";
  private static final String APPLICATION_TYPE_PREFIX = "B";

  private final IMqProducer iMqProducer;
  private final NotificationRedisServiceImpl notificationRedisServiceImpl;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;
  private final RestTemplate restTemplate;
  private final NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();

  private final String groupChatEnabled;
  private final String applicationEnabled;
  private final String groupChatTopic;
  private final String applicationTopic;
  private final String authUrl;

  @Autowired
  public NotificationDmServiceImpl(IMqProducer iMqProducer, NotificationRedisServiceImpl notificationRedisServiceImpl,
      EventMapper eventMapper, IJsonService iJsonService, RestTemplate notificationRestTemplate,
      @Value("${app.temail.notification.groupChat.enabled:false}") String groupChatEnabled,
      @Value("${app.temail.notification.application.enabled:false}") String applicationEnabled,
      @Value("${spring.rocketmq.topics.notify.groupChat:notify}") String groupChatTopic,
      @Value("${spring.rocketmq.topics.notify.application:notify}") String applicationTopic,
      @Value("${url.temail.auth:authUrl}") String authUrl) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisServiceImpl = notificationRedisServiceImpl;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
    this.restTemplate = notificationRestTemplate;
    this.groupChatEnabled = groupChatEnabled;
    this.applicationEnabled = applicationEnabled;
    this.groupChatTopic = groupChatTopic;
    this.applicationTopic = applicationTopic;
    this.authUrl = authUrl;
  }

  /**
   * 保存报文事件
   */
  @Transactional(rollbackFor = Exception.class)
  public void savePacketEvent(Event event, String header, String xPacketId) {
    LOGGER.info("save packet event: event={}, header={}, xPacketId={}", event, header, xPacketId);
    event.setEventType(EventType.PACKET.getValue());
    event.setxPacketId(xPacketId);

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, notificationRedisServiceImpl)) {
      return;
    }

    CDTPHeader cdtpHeader = iJsonService.fromJson(header, CDTPHeader.class);
    event.setFrom(cdtpHeader.getSender());
    event.setTo(cdtpHeader.getReceiver());
    EventUtil.initEventSeqId(notificationRedisServiceImpl, event);
    event.autoWriteExtendParam(iJsonService);
    event.zip();
    eventMapper.insert(event);

    LOGGER.info("send packet event to {}", event.getTo());
    String tag = event.getFrom() + "_" + event.getTo();
    CdtpResponse response = new CdtpResponse(event.getTo(), event.getEventType(), header,
        EventUtil.toJson(iJsonService, event));
    Map<String, Object> extraDataMap = iJsonService
        .fromJson(cdtpHeader.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());

    if (extraDataMap != null) {
      Object type = extraDataMap.get("type");
      if (type == null) {
        // 发送到dispatcher
        iMqProducer.sendMessage(iJsonService.toJson(response), tag);
      } else if (Boolean.valueOf(groupChatEnabled) && type instanceof String && type.toString()
          .startsWith(GROUP_CHAT_TYPE_PREFIX)) {
        // 发送到新群聊topic
        iMqProducer.sendMessage(EventUtil.toJson(iJsonService, event), groupChatTopic, tag, "");
      } else if (Boolean.valueOf(applicationEnabled) && type instanceof String && type.toString()
          .startsWith(APPLICATION_TYPE_PREFIX)) {
        // 发送到协同应用topic
        iMqProducer.sendMessage(EventUtil.toJson(iJsonService, event), applicationTopic, tag, "");
      } else {
        // 发送到dispatcher
        iMqProducer.sendMessage(iJsonService.toJson(response), tag);
      }
    } else {
      // 发送到dispatcher
      iMqProducer.sendMessage(iJsonService.toJson(response), tag);
    }
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    Event event = iJsonService.fromJson(body, Event.class);
    LOGGER.info("dm params: event={}, tags={}", event, tags);
    event.setEventType(EventType.PACKET.getValue());

    // 校验收到的消息通道是否是1x3000
    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(event.getPacket()));
    short commandSpace = cdtpPacket.getCommandSpace();
    short command = cdtpPacket.getCommand();
    if (!(commandSpace == EVENT_COMMAND_SPACE && command == EVENT_COMMAND)) {
      LOGGER.info("packet is not event packet, packetId={}, packet={}", event.getxPacketId(), cdtpPacket);
      return;
    }

    // 如果接收人不是本域的账号，则不需要发送通知
    if (!checkIsSameDomain(event.getTo())) {
      LOGGER.info("[{}] belong to another domain, packetId={}", event.getTo(), event.getxPacketId());
      return;
    }

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!NotificationUtil.checkUnique(event, redisKey, eventMapper, notificationRedisServiceImpl)) {
      return;
    }

    EventUtil.initEventSeqId(notificationRedisServiceImpl, event);
    event.autoWriteExtendParam(iJsonService);
    event.zip();
    eventMapper.insert(event);

    // 解析packet取出CDTPHeader推送给dispatcher
    String header = iJsonService.toJson(cdtpPacket.getHeader());
    CdtpResponse cdtpResponse = new CdtpResponse(event.getTo(), event.getEventType(), header,
        EventUtil.toJson(iJsonService, event));
    String tag = event.getFrom() + "_" + event.getTo();
    iMqProducer.sendMessage(iJsonService.toJson(cdtpResponse), tag);
  }


  private boolean checkIsSameDomain(String temail) {
    String url = authUrl + String.format(GET_PUBLIC_KEY_PATH, temail);
    try {
      // 调用auth获取公钥接口，接口返回404则表示用户不存在或是不在本域。
      LOGGER.debug("check domain url: {}", url);
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
      LOGGER.debug("check domain result: {}", responseEntity);
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      throw new BaseException("check domain exception: ", e);
    }
  }
}
