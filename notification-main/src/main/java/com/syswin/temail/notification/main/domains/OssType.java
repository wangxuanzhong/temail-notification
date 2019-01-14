package com.syswin.temail.notification.main.domains;

/**
 * 运营后台时间类型
 */
public enum OssType {

  //temail账号注销
  USER_TEMAIL_DELETED("UserTemailDeleted");


  private String value;

  OssType(String type) {
    this.value = type;
  }

  public static OssType getByValue(String value) {
    for (OssType event :
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
