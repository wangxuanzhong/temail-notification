package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.NotificationService;
import com.syswin.temail.notification.main.domains.Event;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification")
@CrossOrigin
public class NotificationController {

  private final String CDTP_HEADER = "CDTP-header";

  private final NotificationService notificationService;

  @Autowired
  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @ApiOperation(value = "手动发送通知", consumes = "application/json")
  @PostMapping()
  public ResponseEntity<Response> sendMessage(@RequestBody Map<String, String> body) throws Exception {
    notificationService.sendMqMessage(body.get("data"));
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }

  @ApiOperation(value = "拉取事件", consumes = "application/json")
  @GetMapping("/events")
  public ResponseEntity<Response<Map<String, List<Event>>>> getEvents(String userId, Long sequenceNo,
      @RequestHeader(name = CDTP_HEADER) String header) throws Exception {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, List<Event>> result = notificationService.getEvents(userId, sequenceNo);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }
}
