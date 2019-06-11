package com.syswin.temail.notification.foundation.application;

/**
 * @author 刘森
 */
public interface ISequenceService {

  long STEP = 1L;

  String KEY_PREFIX = "temail_notification_";

  /**
   * 获取下个序号
   *
   * @param key 键
   * @return 序列号
   */
  Long getNextSeq(String key);

}
