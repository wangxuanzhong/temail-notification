/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.foundation.exceptions.BaseException;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.DmDto;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import com.syswin.temail.notification.main.util.NotificationPacketUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DmServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String GET_PUBLIC_KEY_PATH = "/temails/%s";
  private static final int EVENT_COMMAND_SPACE = 0x1;
  private static final int EVENT_COMMAND = 0x3000;

  private static final String GROUP_CHAT_TYPE_PREFIX = "A";
  private static final String APPLICATION_TYPE_PREFIX = "B";

  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;
  private final EventMapper eventMapper;
  private final Gson gson;
  private final RestTemplate restTemplate;
  private final NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();

  private final NotificationConfig config;

  @Autowired
  public DmServiceImpl(IMqProducer iMqProducer, RedisServiceImpl redisService, EventMapper eventMapper,
      RestTemplate notificationRestTemplate, NotificationConfig config) {
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.eventMapper = eventMapper;
    this.gson = new Gson();
    this.restTemplate = notificationRestTemplate;
    this.config = config;
  }

  /**
   * 保存报文事件
   */
  @Transactional(rollbackFor = Exception.class)
  public void savePacketEvent(DmDto dmDto, String header, String xPacketId) {
    LOGGER.info("save packet event: packet={}, header={}, xPacketId={}", dmDto.getPacket(), header, xPacketId);
    Event event = new Event();
    event.setPacket(dmDto.getPacket());
    event.setEventType(EventType.PACKET.getValue());
    event.setxPacketId(xPacketId);

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!EventUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    CDTPHeader cdtpHeader = gson.fromJson(header, CDTPHeader.class);
    event.setFrom(cdtpHeader.getSender());
    event.setTo(cdtpHeader.getReceiver());
    EventUtil.initEventSeqId(redisService, event);
    event.zip();
    eventMapper.insert(event);

    LOGGER.info("send packet event to {}", event.getTo());
    String tag = event.getFrom() + "_" + event.getTo();
    DispatcherResponse response = new DispatcherResponse(event.getTo(), event.getEventType(), header,
        EventUtil.toJson(gson, event));
    Map<String, Object> extraDataMap = gson
        .fromJson(cdtpHeader.getExtraData(), new TypeToken<Map<String, Object>>() {
        }.getType());

    if (extraDataMap != null) {
      Object type = extraDataMap.get("type");
      if (type == null) {
        // 发送到dispatcher
        iMqProducer.sendMessage(gson.toJson(response), tag);
      } else if (Boolean.valueOf(config.dmGroupChatEnabled) && type instanceof String && type.toString()
          .startsWith(GROUP_CHAT_TYPE_PREFIX)) {
        // 发送到新群聊topic
        iMqProducer.sendMessage(EventUtil.toJson(gson, event), config.notifyGroupChatTopic, tag, "");
      } else if (Boolean.valueOf(config.dmApplicationEnabled) && type instanceof String && type.toString()
          .startsWith(APPLICATION_TYPE_PREFIX)) {
        // 发送到协同应用topic
        iMqProducer.sendMessage(EventUtil.toJson(gson, event), config.notifyApplicationTopic, tag, "");
      } else {
        // 发送到dispatcher
        iMqProducer.sendMessage(gson.toJson(response), tag);
      }
    } else {
      // 发送到dispatcher
      iMqProducer.sendMessage(gson.toJson(response), tag);
    }
  }

  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    Event event = gson.fromJson(body, Event.class);
    LOGGER.info("dm params: body={}, tags={}", body, tags);
    event.setEventType(EventType.PACKET.getValue());

    // 校验收到的消息通道是否是1x3000
    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(event.getPacket()));
    short commandSpace = cdtpPacket.getCommandSpace();
    short command = cdtpPacket.getCommand();
    if (!(commandSpace == EVENT_COMMAND_SPACE && command == EVENT_COMMAND)) {
      LOGGER.info("packet is not event packet, packetId={}, packet={}", event.getxPacketId(), cdtpPacket);
      return;
    }

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!EventUtil.checkUnique(event, redisKey, eventMapper, redisService)) {
      return;
    }

    // 如果接收人不是本域的账号，则不需要发送通知
    if (!checkIsSameDomain(event.getTo())) {
      LOGGER.info("[{}] belong to another domain, packetId={}", event.getTo(), event.getxPacketId());
      return;
    }

    EventUtil.initEventSeqId(redisService, event);
    event.autoWriteExtendParam(body);
    event.zip();
    eventMapper.insert(event);

    // 解析packet取出CDTPHeader推送给dispatcher
    String header = gson.toJson(cdtpPacket.getHeader());
    DispatcherResponse dispatcherResponse = new DispatcherResponse(event.getTo(), event.getEventType(), header,
        EventUtil.toJson(gson, event));
    String tag = event.getFrom() + "_" + event.getTo();
    iMqProducer.sendMessage(gson.toJson(dispatcherResponse), tag);
  }


  private boolean checkIsSameDomain(String temail) {
    String url = config.authUrl + String.format(GET_PUBLIC_KEY_PATH, temail);
    try {
      // 调用auth获取公钥接口，接口返回404则表示用户不存在或是不在本域。
      LOGGER.info("check domain url: {}", url);
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
      LOGGER.info("check domain result: {}", responseEntity);
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      throw new BaseException("check domain exception: ", e);
    }
  }
}
