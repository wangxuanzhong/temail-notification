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

package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.scheduler.EventSchedule;
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
 * @author liusen@syswin.com
 */
@RestController
@RequestMapping("/notification/schedule")
@Api(value = "schedule", tags = "event schedule")
@ApiIgnore
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
