package com.syswin.temail.notification.main.domains.params;

public class MailAgentGroupChatParams extends MailAgentParams {

  // seqId
  private Long fromSeqNo;
  private String groupTemail;
  private String groupName;
  private String temail;
  private String name;
  private Integer type;

  public Long getFromSeqNo() {
    return fromSeqNo;
  }

  public void setFromSeqNo(Long fromSeqNo) {
    this.fromSeqNo = fromSeqNo;
  }

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

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }
}