package com.syswin.temail.notification.main.util;

import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.ISequenceService;
import com.syswin.temail.notification.main.domains.SyncEvent;

/**
 * @author liusen@syswin.com
 */
public class SyncEventUtil {

  private SyncEventUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 转换成json，清空后端使用参数
   */
  public static String toJson(IJsonService iJsonService, SyncEvent event) {
    return iJsonService.toJson(event);
  }

  /**
   * 根据不同事件类型按照不同的key生成seqId
   */
  public static void initEventSeqId(ISequenceService iSequenceService, SyncEvent event) {
    event.setEventSeqId(iSequenceService.getNextSeq(event.getTo()));
  }
}
