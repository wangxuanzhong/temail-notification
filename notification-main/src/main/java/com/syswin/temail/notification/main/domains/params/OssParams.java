package com.syswin.temail.notification.main.domains.params;

import java.util.List;

/**
 * 运营后台mq消息实体类
 */
public class OssParams {

  private Long id;
  //temail集合
  private List<String> temails;
  private String type;
  private Long timestamp;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<String> getTemails() {
    return temails;
  }

  public void setTemails(List<String> temails) {
    this.temails = temails;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "OssParams{" +
        "id=" + id +
        ", temails=" + temails +
        ", type='" + type + '\'' +
        ", timestamp=" + timestamp +
        '}';
  }

}
