package com.syswin.temail.notification.main.domains;

public class MailAgentGroupChatParams extends MailAgentParams {

  private String groupTemail;
  private String temail;
  private Integer type;

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  public String getTemail() {
    return temail;
  }

  public void setTemail(String temail) {
    this.temail = temail;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }
}