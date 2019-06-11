package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.domains.TopicEvent;

/**
 * @author liusen
 */
public class TopicEventUtil {

  private TopicEventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 转换成json，清空extendParam
   */
  public static String toJson(IJsonService iJsonService, TopicEvent topicEvent) {
    topicEvent.setExtendParam(null);
    return iJsonService.toJson(topicEvent);
  }

  /**
   * 生成seqId
   */
  public static void initTopicEventSeqId(ISequenceService iSequenceService, TopicEvent topicEvent) {
    topicEvent.setEventSeqId(iSequenceService.getNextSeq("topic_" + topicEvent.getTo()));
  }

}
