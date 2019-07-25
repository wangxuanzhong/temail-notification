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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.constants.Constant.EventParams;
import com.syswin.temail.notification.main.domains.TopicEvent;
import java.util.List;

/**
 * @author liusen@syswin.com
 */
public class TopicEventUtil {

  private TopicEventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 转换成json，将extendParam中字段合并并清空extendParam
   */
  public static String toJson(IJsonService iJsonService, TopicEvent topicEvent) {
    String extendParam = topicEvent.getExtendParam();
    topicEvent.setId(null);
    topicEvent.setExtendParam(null);
    return NotificationUtil.combineTwoJson(iJsonService.toJson(topicEvent), extendParam);
  }

  /**
   * 生成seqId
   */
  public static void initTopicEventSeqId(ISequenceService iSequenceService, TopicEvent topicEvent) {
    topicEvent.setEventSeqId(iSequenceService.getNextSeq("topic_" + topicEvent.getTo()));
  }

  /**
   * 初始化extendParam的json
   */
  public static String initExtendParam(String params, TopicEvent topicEvent) {
    JsonObject jsonObject = NotificationUtil.removeUsedField(params);

    if (topicEvent == null) {
      topicEvent = new TopicEvent();
    }

    // 添加批量删除操作msgIds
    List<String> msgIds = topicEvent.getMsgIds();
    if (msgIds != null && !msgIds.isEmpty()) {
      JsonArray msgIdsArray = new JsonArray();
      msgIds.forEach(msgIdsArray::add);
      jsonObject.add(EventParams.MSG_IDS, msgIdsArray);
    }

    if (jsonObject.size() == 0) {
      return null;
    } else {
      return jsonObject.toString();
    }
  }
}
