package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.scheduler.EventSchedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
@Api(value = "schedule", tags = "event schedule")
@CrossOrigin
public class ScheduleController {

  private final EventSchedule eventSchedule;

  @Autowired
  public ScheduleController(EventSchedule eventSchedule) {
    this.eventSchedule = eventSchedule;
  }

  @ApiOperation(value = "delete events", consumes = "application/json")
  @DeleteMapping("/delete/event")
  public ResponseEntity<Response> deleteEvent() {
    eventSchedule.deleteOldEvent();
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }

  @ApiOperation(value = "delete topics", consumes = "application/json")
  @DeleteMapping("/delete/topic")
  public ResponseEntity<Response> deleteTopic() {
    eventSchedule.deleteOldTopic();
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }
}
