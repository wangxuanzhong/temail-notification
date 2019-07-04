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

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.SyncEvent;
import com.syswin.temail.notification.main.domains.SyncRelationEvent;
import com.syswin.temail.notification.main.dto.CdtpResponse;
import com.syswin.temail.notification.main.util.SyncEventUtil;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 多端同步事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class NotificationSyncServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final NotificationRedisServiceImpl redisService;
  private final IJsonService iJsonService;

  public NotificationSyncServiceImpl(IMqProducer iMqProducer, NotificationRedisServiceImpl redisService,
      IJsonService iJsonService) {
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.iJsonService = iJsonService;
  }


  /**
   * 处理从MQ收到的信息
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void handleMqMessage(String body, String tags) {
    LOGGER.info("sync params: {}, tags: {}", body, tags);

    SyncEvent event = iJsonService.fromJson(body, SyncEvent.class);

    // 前端需要的头信息
    String header = event.getHeader();

    LOGGER.info("sync event type: {}", EventType.getByValue(event.getEventType()));

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!SyncEventUtil.checkUnique(event, redisKey, redisService)) {
      return;
    }

    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RELATION_ADD:
      case RELATION_UPDATE:
      case RELATION_DELETE:
        SyncRelationEvent relationEvent = iJsonService.fromJson(body, SyncRelationEvent.class);
        this.sendMessage(relationEvent, header, tags);
        break;
      default:
        LOGGER.warn("not support event type!");
    }
  }

  /**
   * 发送消息
   */
  private void sendMessage(SyncEvent event, String header, String tags) {
    this.sendMessage(event, event.getTo(), header, tags);
  }

  /**
   * 发送消息
   */
  private void sendMessage(SyncEvent event, String to, String header, String tags) {
    SyncEventUtil.initEventSeqId(redisService, event);
    LOGGER.info("send message to --->> {}, event type: {}", to, EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(iJsonService
        .toJson(new CdtpResponse(to, event.getEventType(), header, SyncEventUtil.toJson(iJsonService, event))), tags);
  }
}
