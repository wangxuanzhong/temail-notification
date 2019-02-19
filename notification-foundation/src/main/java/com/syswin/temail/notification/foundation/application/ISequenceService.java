package com.syswin.temail.notification.foundation.application;

public interface ISequenceService {

  long STEP = 1L;

  String KEY_PREFIX = "temail_notification_";

  /**
   * 获取下个序号
   */
  Long getNextSeq(String key);

}
