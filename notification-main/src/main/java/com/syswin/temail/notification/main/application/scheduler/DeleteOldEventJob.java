package com.syswin.temail.notification.main.application.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class DeleteOldEventJob extends QuartzJobBean {

  @Autowired
  private EventSchedule eventSchedule;

  @Override
  protected void executeInternal(JobExecutionContext jobExecutionContext) {
    eventSchedule.deleteOldEvent();
  }

}
