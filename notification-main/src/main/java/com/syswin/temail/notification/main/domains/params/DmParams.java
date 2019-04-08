package com.syswin.temail.notification.main.domains.params;

import com.syswin.temail.ps.client.Message;

public class DmParams {

  public static final int TYPE_3_RESPONSE = 3;

  private Message groupchatMessage;
  private int type;
  private String groupTemail;

  public Message getGroupchatMessage() {
    return groupchatMessage;
  }

  public void setGroupchatMessage(Message groupchatMessage) {
    this.groupchatMessage = groupchatMessage;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  @Override
  public String toString() {
    return "DmParams{" +
        "groupchatMessage=" + groupchatMessage +
        ", type=" + type +
        ", groupTemail='" + groupTemail + '\'' +
        '}';
  }
}