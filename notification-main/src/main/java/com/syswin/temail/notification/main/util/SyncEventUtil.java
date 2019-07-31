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

package com.syswin.temail.notification.main.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.application.RedisServiceImpl;
import com.syswin.temail.notification.main.constants.Constant.EventParams;
import com.syswin.temail.notification.main.domains.SyncEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liusen@syswin.com
 */
public class SyncEventUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private SyncEventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 幂等校验
   */
  public static boolean checkUnique(SyncEvent event, String redisKey, RedisServiceImpl redisService) {
    // xPacketId为空则认为是无效数据
    if (event.getxPacketId() == null || event.getxPacketId().isEmpty()) {
      LOGGER.warn("xPacketId is null!");
      return false;
    }

    // 第一步：查询redis，是否key值未过期，解决并发问题
    if (!redisService.checkUnique(redisKey)) {
      LOGGER.warn("check unique from redis failed: {}", event);
      return false;
    }

    return true;
  }

  /**
   * 转换成json，清空后端使用参数
   */
  public static String toJson(Gson gson, SyncEvent event, String params) {
    List<String> removeKeys = new ArrayList<>();
    removeKeys.add(EventParams.EVENT_TYPE);
    removeKeys.add(EventParams.FROM);
    removeKeys.add(EventParams.TO);
    removeKeys.add(EventParams.HEADER);
    removeKeys.add(EventParams.X_PACKET_ID);
    JsonObject jsonObject = NotificationUtil.removeUsedField(params, removeKeys);

    event.setHeader(null);
    return NotificationUtil.combineTwoJson(jsonObject.toString(), gson.toJson(event));
  }

  /**
   * 根据不同事件类型按照不同的key生成seqId
   */
  public static void initEventSeqId(ISequenceService iSequenceService, SyncEvent event) {
    event.setEventSeqId(iSequenceService.getNextSeq(event.getTo()));
  }


}
