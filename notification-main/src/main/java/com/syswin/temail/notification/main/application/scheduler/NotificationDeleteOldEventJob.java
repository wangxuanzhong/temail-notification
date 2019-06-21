package com.syswin.temail.notification.main.application.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 清理过期单群聊事件job类
 *
 * @author liusen@syswin.com
 */
@DisallowConcurrentExecution
public class NotificationDeleteOldEventJob extends QuartzJobBean {

  @Autowired
  private NotificationEventSchedule notificationEventSchedule;

  @Override
  protected void executeInternal(JobExecutionContext jobExecutionContext) {
    notificationEventSchedule.deleteOldEvent();
  }

}
