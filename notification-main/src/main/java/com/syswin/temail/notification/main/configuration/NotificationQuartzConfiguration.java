/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.notification.main.configuration;

import com.syswin.temail.notification.main.application.scheduler.NotificationDeleteOldEventJob;
import com.syswin.temail.notification.main.application.scheduler.NotificationDeleteOldTopicJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * quartz配置类
 *
 * @author liusen@syswin.com
 */
@Configuration
public class NotificationQuartzConfiguration {

  private static final String DELETE_OLD_EVENT_CRON = "0 0 4 * * ?";
  private static final String DELETE_OLD_TOPIC_CRON = "0 0 3 * * ?";

  /**
   * 定时清理历史单群聊事件
   *
   * @return JobDetail实例
   */
  @Bean
  public JobDetail deleteOldEventTaskDetail() {
    return JobBuilder.newJob(NotificationDeleteOldEventJob.class).withIdentity("deleteOldEventTaskTrigger")
        .storeDurably().build();
  }

  @Bean
  public Trigger deleteOldEventTaskTrigger() {
    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(DELETE_OLD_EVENT_CRON);
    return TriggerBuilder.newTrigger().forJob(deleteOldEventTaskDetail())
        .withIdentity("deleteOldEventTaskTrigger")
        .withSchedule(scheduleBuilder)
        .build();
  }

  /**
   * 定时清理历史话题事件
   *
   * @return JobDetail实例
   */
  @Bean
  public JobDetail deleteOldTopicTaskDetail() {
    return JobBuilder.newJob(NotificationDeleteOldTopicJob.class).withIdentity("deleteOldTopicTaskTrigger")
        .storeDurably().build();
  }

  @Bean
  public Trigger deleteOldTopicTaskTrigger() {
    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(DELETE_OLD_TOPIC_CRON);
    return TriggerBuilder.newTrigger().forJob(deleteOldTopicTaskDetail())
        .withIdentity("deleteOldTopicTaskTrigger")
        .withSchedule(scheduleBuilder)
        .build();
  }


}
