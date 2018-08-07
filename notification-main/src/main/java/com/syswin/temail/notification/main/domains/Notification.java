package com.syswin.temail.notification.main.domains;

import java.sql.Timestamp;

public class Notification {

  private Long id; // 主键
  private Long messageId;
  private Long userId;
  private Long publicKey;
  private String type;
  private Timestamp clientSentTimestamp;
  private String fromAddress;
  private String messageSummary;

  public Notification() {
  }

}