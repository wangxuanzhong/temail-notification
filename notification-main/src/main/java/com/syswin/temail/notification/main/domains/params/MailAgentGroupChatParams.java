package com.syswin.temail.notification.main.domains.params;

public class MailAgentGroupChatParams extends MailAgentParams {

  // 群邮件
  private String groupTemail;
  // 群名称
  private String groupName;
  // 当事人
  private String temail;
  // 当事人名称
  private String name;
  // 管理员名称
  private String adminName;
  // 成员类型
  private Integer type;


  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getTemail() {
    return temail;
  }

  public void setTemail(String temail) {
    this.temail = temail;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "MailAgentGroupChatParams{" +
        "groupTemail='" + groupTemail + '\'' +
        ", groupName='" + groupName + '\'' +
        ", temail='" + temail + '\'' +
        ", name='" + name + '\'' +
        ", adminName='" + adminName + '\'' +
        ", type=" + type +
        '}' + super.toString();
  }
}