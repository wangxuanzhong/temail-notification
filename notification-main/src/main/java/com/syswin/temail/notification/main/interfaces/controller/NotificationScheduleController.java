package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.scheduler.NotificationEventSchedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author liusen
 */
@RestController
@RequestMapping("/notification/schedule")
@Api(value = "schedule", tags = "event schedule")
@ApiIgnore
public class NotificationScheduleController {

  private final NotificationEventSchedule notificationEventSchedule;

  @Autowired
  public NotificationScheduleController(NotificationEventSchedule notificationEventSchedule) {
    this.notificationEventSchedule = notificationEventSchedule;
  }

  @ApiOperation(value = "delete events", consumes = "application/json")
  @DeleteMapping("/delete/event")
  public ResponseEntity<Response> deleteEvent() {
    notificationEventSchedule.deleteOldEvent();
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }

  @ApiOperation(value = "delete topics", consumes = "application/json")
  @DeleteMapping("/delete/topic")
  public ResponseEntity<Response> deleteTopic() {
    notificationEventSchedule.deleteOldTopic();
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }
}
