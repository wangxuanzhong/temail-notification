package com.syswin.temail.notification.main.configuration;

import com.syswin.temail.notification.main.application.scheduler.DeleteOldEventJob;
import com.syswin.temail.notification.main.application.scheduler.DeleteOldTopicJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

  private static final String deleteOldEventCron = "0 0 4 * * ?";
  private static final String deleteOldTopicCron = "0 0 3 * * ?";

  // 定时清理历史单群聊事件
  @Bean
  public JobDetail deleteOldEventTaskDetail() {
    return JobBuilder.newJob(DeleteOldEventJob.class).withIdentity("deleteOldEventTaskTrigger").storeDurably().build();
  }

  @Bean
  public Trigger deleteOldEventTaskTrigger() {
    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(deleteOldEventCron);
    return TriggerBuilder.newTrigger().forJob(deleteOldEventTaskDetail())
        .withIdentity("deleteOldEventTaskTrigger")
        .withSchedule(scheduleBuilder)
        .build();
  }

  // 定时清理历史话题事件
  @Bean
  public JobDetail deleteOldTopicTaskDetail() {
    return JobBuilder.newJob(DeleteOldTopicJob.class).withIdentity("deleteOldTopicTaskTrigger").storeDurably().build();
  }

  @Bean
  public Trigger deleteOldTopicTaskTrigger() {
    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(deleteOldTopicCron);
    return TriggerBuilder.newTrigger().forJob(deleteOldTopicTaskDetail())
        .withIdentity("deleteOldTopicTaskTrigger")
        .withSchedule(scheduleBuilder)
        .build();
  }


}
