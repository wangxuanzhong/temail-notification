package com.syswin.temail.notification.main.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Member {

  @JsonIgnore
  private Long id;
  private String groupTemail;
  private String temail;
  @JsonIgnore
  private Integer role;
  private Integer userStatus;
  private Integer groupStatus;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }

  public Integer getUserStatus() {
    return userStatus;
  }

  public void setUserStatus(Integer userStatus) {
    this.userStatus = userStatus;
  }

  public Integer getGroupStatus() {
    return groupStatus;
  }

  public void setGroupStatus(Integer groupStatus) {
    this.groupStatus = groupStatus;
  }

  @Override
  public String toString() {
    return "Member{" +
        "id=" + id +
        ", groupTemail='" + groupTemail + '\'' +
        ", temail='" + temail + '\'' +
        ", role=" + role +
        ", userStatus=" + userStatus +
        ", groupStatus=" + groupStatus +
        '}';
  }


  public enum MemberRole {
    NORMAL(0, "普通成员"),
    ADMIN(1, "管理员");

    private final int value;
    private final String description;

    MemberRole(int value, String description) {
      this.value = value;
      this.description = description;
    }

    public int getValue() {
      return value;
    }
  }

  public enum UserStatus {
    NORMAL(0, "正常"),
    DO_NOT_DISTURB(1, "免打扰");

    private final int value;
    private final String description;

    UserStatus(int value, String description) {
      this.value = value;
      this.description = description;
    }

    public static UserStatus getByValue(int value) {
      for (UserStatus userStatus : values()) {
        if (userStatus.getValue() == value) {
          return userStatus;
        }
      }
      return null;
    }

    public int getValue() {
      return value;
    }
  }

  public enum GroupStatus {
    NORMAL(0, "正常"),
    BLACKLIST(1, "群黑名单");

    private final int value;
    private final String description;

    GroupStatus(int value, String description) {
      this.value = value;
      this.description = description;
    }

    public static GroupStatus getByValue(int value) {
      for (GroupStatus groupStatus : values()) {
        if (groupStatus.getValue() == value) {
          return groupStatus;
        }
      }
      return null;
    }

    public int getValue() {
      return value;
    }
  }
}
