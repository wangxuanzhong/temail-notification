package com.syswin.temail.notification.main.domains.params;

/**
 *  运营后台mq消息实体类
 */
public class MailAgentOssParams extends MailAgentParams {

  private Integer id;
  //temail集合
  private String temails;
  private String type;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTemails() {
    return temails;
  }

  public void setTemails(String temails) {
    this.temails = temails;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
