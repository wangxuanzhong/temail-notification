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

package com.syswin.temail.notification.main.constants;

import com.syswin.temail.notification.main.domains.EventType;
import java.util.Arrays;
import java.util.List;

/**
 * @author liusen@syswin.com
 */
public class Constant {

  private Constant() {
    throw new IllegalStateException("Utility class");
  }

  public static final String REDIS_KEY_PREFIX = "temail_notification_";

  public static final String GROUP_CHAT_KEY_POSTFIX = "::event_group_chat";

  /**
   * 事件筛选条件
   */
  public static class EventCondition {

    /**
     * 拉取事件返回最大条数
     */
    public static final int MAX_EVENT_RETURN_COUNT = 1000;

    /**
     * 统计未读数时需要查询出来的eventType
     */
    public static final List<Integer> UNREAD_EVENT_TYPES = Arrays.asList(
        EventType.RESET.getValue(),
        EventType.RECEIVE.getValue(),
        EventType.DESTROY.getValue(),
        EventType.PULLED.getValue(),
        EventType.RETRACT.getValue(),
        EventType.DELETE.getValue()
    );
  }

  /**
   * CDTP报文字段
   */
  public static class CdtpParams {

    public static final String CDTP_HEADER = "CDTP-header";
    public static final String X_PACKET_ID = "X-PACKET-ID";
  }

  /**
   * 事件参数中需要做处理的字段
   */
  public static class EventParams {

    /**
     * 通用参数
     */
    public static final String SESSION_MESSAGE_TYPE = "sessionMessageType";
    public static final String EVENT_TYPE = "eventType";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String MSG_ID = "msgid";
    public static final String PARENT_MSG_ID = "parentMsgId";
    public static final String SEQ_NO = "seqNo";
    public static final String TO_MSG = "toMsg";
    public static final String TIMESTAMP = "timestamp";
    public static final String HEADER = "header";
    public static final String X_PACKET_ID = "xPacketId";
    /**
     * 单聊参数
     */
    public static final String OWNER = "owner";
    /**
     * 群聊参数
     */
    public static final String GROUP_TEMAIL = "groupTemail";
    public static final String TEMAIL = "temail";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String MEMBER_EXT_DATA = "memberExtData";
    public static final String AT = "at";
    public static final String MEMBERS = "members";
    public static final String ATALL = "atAll";
    /**
     * 话题参数
     */
    public static final String TOPIC_ID = "topicId";
    /**
     * dm参数
     */
    public static final String PACKET = "packet";
    /**
     * 事件参数
     */
    public static final String MSG_IDS = "msgIds";
  }

  public static class GroupChatAtParams {
    public static final Integer ATALL_NO_0 = 0;

    public static final Integer ATALL_YES_1 = 1;
  }
}
