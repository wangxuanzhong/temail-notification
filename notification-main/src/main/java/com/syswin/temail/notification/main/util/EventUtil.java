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
import com.syswin.temail.notification.main.application.RedisServiceImpl;
import com.syswin.temail.notification.main.constants.Constant.EventParams;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.domains.Member.MemberRole;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liusen@syswin.com
 */
public class EventUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private EventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 幂等校验
   */
  public static boolean checkUnique(Event event, String redisKey, EventMapper eventMapper,
      RedisServiceImpl redisService) {
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

    // 第二步：查询数据库是否存在重复数据，保证数据的唯一性
    if (!eventMapper.selectEventsByPacketIdAndEventType(event).isEmpty()) {
      LOGGER.warn("check unique from database failed: {}", event);
      return false;
    }

    return true;
  }

  /**
   * 转换成json，清空后端使用参数
   */
  public static String toJson(IJsonService iJsonService, Event event) {
    String extendParam = event.getExtendParam();
    event.setId(null);
    event.setExtendParam(null);
    event.setZipPacket(null);
    return NotificationUtil.combineTwoJson(iJsonService.toJson(event), extendParam);
  }

  /**
   * 获取msgId，如果msgId为空则临时生成，反向业务使用对立事件类型生成
   */
  public static String getMsgId(EventType eventType, Event event) {
    EventType againstEventType;
    switch (eventType) {
      // 单聊
      case DO_NOT_DISTURB_CANCEL:
        againstEventType = EventType.DO_NOT_DISTURB;
        break;
      // 群聊
      case APPLY_ADOPT:
      case APPLY_REFUSE:
        againstEventType = EventType.APPLY;
        break;
      case INVITATION_ADOPT:
      case INVITATION_REFUSE:
        againstEventType = EventType.INVITATION;
        break;
      case DELETE_ADMIN:
      case ABANDON_ADMIN:
        againstEventType = EventType.ADD_ADMIN;
        break;
      case GROUP_DO_NOT_DISTURB_CANCEL:
        againstEventType = EventType.GROUP_DO_NOT_DISTURB;
        break;
      default:
        againstEventType = eventType;
        break;
    }

    if (event.getMsgId() == null) {
      return event.getFrom() + "_" + event.getTo() + "_" + event.getTemail() + "_" + againstEventType;
    } else {
      return event.getMsgId();
    }
  }

  /**
   * 去除角色条件，即通知所有人
   */
  public static void notifyToAll(Event event) {
    event.setRole(null);
  }

  /**
   * 角色设置为管理员，只通知管理员
   */
  public static void notifyToAdmin(Event event) {
    event.setRole(MemberRole.ADMIN.getValue());
  }

  /**
   * 根据不同事件类型按照不同的key生成seqId
   */
  public static void initEventSeqId(ISequenceService iSequenceService, Event event) {
    switch (Objects.requireNonNull(EventType.getByValue(event.getEventType()))) {
      case RECEIVE:
      case RETRACT:
      case DESTROY:
      case DESTROYED:
      case REPLY:
      case REPLY_RETRACT:
      case REPLY_DESTROYED:
      case CROSS_DOMAIN:
      case CHANGE_EXT_DATA:
        event.setEventSeqId(iSequenceService.getNextSeq(event.getOwner() == null ? event.getTo() : event.getOwner()));
        break;
      default:
        event.setEventSeqId(iSequenceService.getNextSeq(event.getTo()));
        break;
    }
  }

  /**
   * 初始化extendParam的json
   */
  public static String initExtendParam(String params, Event event) {
    JsonObject jsonObject = NotificationUtil.removeUsedField(params);

    if (event == null) {
      event = new Event();
    }

    // 添加单聊owner
    if (event.getOwner() != null) {
      jsonObject.addProperty(EventParams.OWNER, event.getOwner());
    }

    // 添加批量删除操作msgIds
    List<String> msgIds = event.getMsgIds();
    if (msgIds != null && !msgIds.isEmpty()) {
      JsonArray msgIdsArray = new JsonArray();
      msgIds.forEach(msgIdsArray::add);
      jsonObject.add(EventParams.MSG_IDS, msgIdsArray);
    }

    // 添加群聊批量移除群成员事件，群成员名称字段name
    if (event.getName() != null) {
      jsonObject.addProperty(EventParams.NAME, event.getName());
    }

    // 添加群聊批量移除群成员事件，群成员扩展字段
    if (event.getMemberExtData() != null) {
      jsonObject.addProperty(EventParams.MEMBER_EXT_DATA, event.getMemberExtData());
    }

    if (jsonObject.size() == 0) {
      return null;
    } else {
      return jsonObject.toString();
    }
  }
}
