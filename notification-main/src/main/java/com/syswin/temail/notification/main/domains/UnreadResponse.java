package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class UnreadResponse extends Message {

  private Integer unread;

  public UnreadResponse(String from, String to, Integer unread) {
    this.setFrom(from);
    this.setTo(to);
    this.unread = unread;
  }

  public Integer getUnread() {
    return unread;
  }

  public void setUnread(Integer unread) {
    this.unread = unread;
  }
}