package com.syswin.temail.notification.main.application;

import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.foundation.exceptions.BaseException;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.response.CDTPResponse;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.NotificationUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationDmService implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String CHECK_DOMAIN_PATH = "/xxxxxxxx";

  private final IMqProducer iMqProducer;
  private final NotificationRedisService notificationRedisService;
  private final EventMapper eventMapper;
  private final IJsonService iJsonService;
  private final RestTemplate restTemplate;

  @Value("${app.temail.notification.saas.enabled}")
  private String saasEnabled;
  @Value("${spring.rocketmq.topics.notify.groupChat}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.notify.application}")
  private String applicationTopic;
  @Value("${url.temail.auth}")
  private String authUrl;

  @Autowired
  public NotificationDmService(IMqProducer iMqProducer, NotificationRedisService notificationRedisService,
      EventMapper eventMapper, IJsonService iJsonService, RestTemplate notificationRestTemplate) {
    this.iMqProducer = iMqProducer;
    this.notificationRedisService = notificationRedisService;
    this.eventMapper = eventMapper;
    this.iJsonService = iJsonService;
    this.restTemplate = notificationRestTemplate;
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
    CDTPResponse response = new CDTPResponse(event.getTo(), event.getEventType(), header, Event.toJson(iJsonService, event));
    if (Boolean.valueOf(saasEnabled) && !isMq) {
      Map<String, Object> extraDataMap = iJsonService.fromJson(cdtpHeader.getExtraData(), new TypeToken<Map<String, Object>>() {
      }.getType());
      String type = extraDataMap.get("type").toString();

      if (type.startsWith("A")) { // 新群聊 topic
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
  @Override
  public void handleMqMessage(String body, String tags) {
    Event event = iJsonService.fromJson(body, Event.class);
    LOGGER.info("dm params: {}", event);
    event.setEventType(EventType.PACKET.getValue());

    // 如果接收人不是本域的账号，则不需要发送通知
    if (!checkSameDomain(event.getTo())) {
      LOGGER.info("[{}] is different domain", event.getTo());
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
    iMqProducer.sendMessage(iJsonService.toJson(new CDTPResponse(event.getTo(), event.getEventType(), "to do", Event.toJson(iJsonService, event))));
  }


  public boolean checkSameDomain(String temail) {
    LOGGER.info("check temail: [{}] is same domain or not.", temail);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    try {
//      String url = authUrl + CHECK_DOMAIN_PATH;
      String url = "http://temail-notification.service.innertools.com/notification/unread?from=b";
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
      System.out.println(responseEntity);
      Response<Map<String, Object>> response = iJsonService.fromJson(responseEntity.getBody(), new TypeToken<Response<Map<String, Object>>>() {
      }.getType());
      System.out.println(response);
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      LOGGER.warn("check domain exception: ", e);
      throw new BaseException("check domain exception: ", e);
    }
  }
}
