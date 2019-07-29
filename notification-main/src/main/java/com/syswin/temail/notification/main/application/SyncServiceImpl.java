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
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.application.mq.IMqConsumerService;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.SyncEvent;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.util.SyncEventUtil;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 多端同步事件处理类
 *
 * @author liusen@syswin.com
 */
@Service
public class SyncServiceImpl implements IMqConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IMqProducer iMqProducer;
  private final RedisServiceImpl redisService;
  private final Gson gson;

  public SyncServiceImpl(IMqProducer iMqProducer, RedisServiceImpl redisService) {
    this.iMqProducer = iMqProducer;
    this.redisService = redisService;
    this.gson = new Gson();
  }


  /**
   * 处理从MQ收到的信息
   */
  @Override
  public void handleMqMessage(String body, String tags) {
    LOGGER.info("sync params: {}, tags: {}", body, tags);

    SyncEvent event = gson.fromJson(body, SyncEvent.class);

    // 前端需要的头信息
    String header = event.getHeader();

    EventType eventType = EventType.getByValue(event.getEventType());
    if (eventType == null) {
      LOGGER.warn("event type is illegal! xPacketId: {}", event.getxPacketId());
      return;
    }
    LOGGER.info("sync event type: {}", eventType);

    // 校验收到的数据是否重复
    String redisKey = event.getxPacketId() + "_" + event.getEventType();
    if (!SyncEventUtil.checkUnique(event, redisKey, redisService)) {
      return;
    }

    switch (eventType) {
      case RELATION_ADD:
      case RELATION_UPDATE:
      case RELATION_DELETE:
        this.sendMessage(event, header, tags, body);
        break;
      default:
        LOGGER.warn("not support event type!");
    }
  }

  /**
   * 发送消息
   */
  private void sendMessage(SyncEvent event, String header, String tags, String body) {
    SyncEventUtil.initEventSeqId(redisService, event);
    LOGGER.info("send message to --->> {}, event type: {}", event.getTo(), EventType.getByValue(event.getEventType()));
    iMqProducer.sendMessage(gson.toJson(
        new DispatcherResponse(event.getTo(), event.getEventType(), header, SyncEventUtil.toJson(gson, event, body))),
        tags);
  }
}
