package com.syswin.temail.notification.main.domains;

/**
 * 运营后台时间类型
 */
public enum OssEventType {

  //temail账号注销
  USER_TEMAIL_DELETED("UserTemailDeleted");


  private String value;

  OssEventType(String type) {
    this.value = type;
  }

  public static OssEventType getByValue(String value) {
    for (OssEventType event :
        values()) {
      if (event.getValue().equals(value)) {
        return event;
      }
    }
    return null;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
