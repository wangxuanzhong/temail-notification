package com.syswin.temail.notification.main.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author liusen@syswin.com
 */
@JsonInclude(Include.NON_NULL)
public class UnreadResponse {

  private Integer unread;

  private String from;

  private String to;

  private String groupTemail;

  public UnreadResponse(String from, String to, Integer unread) {
    this.from = from;
    this.to = to;
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

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getGroupTemail() {
    return groupTemail;
  }

  public void setGroupTemail(String groupTemail) {
    this.groupTemail = groupTemail;
  }

  @Override
  public String toString() {
    return "UnreadResponse{" +
        "unread=" + unread +
        ", from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", groupTemail='" + groupTemail + '\'' +
        '}';
  }
}