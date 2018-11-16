package com.syswin.temail.notification.main.domains.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.syswin.temail.notification.main.domains.Event;

@JsonInclude(Include.NON_NULL)
public class UnreadResponse extends Event {

  private Integer unread;

  public UnreadResponse(String from, String to, Integer unread) {
    this.setFrom(from);
    this.setTo(to);
    this.unread = unread;
  }

  public UnreadResponse(Integer unread) {
    this.unread = unread;
  }

  public Integer getUnread() {
    return unread;
  }

  public void setUnread(Integer unread) {
    this.unread = unread;
  }

  @Override
  public String toString() {
    return "UnreadResponse{" +
        "unread=" + unread +
        ", from=" + this.getFrom() +
        ", to=" + this.getTo() +
        ", groupTemail=" + this.getGroupTemail() +
        '}';
  }
}