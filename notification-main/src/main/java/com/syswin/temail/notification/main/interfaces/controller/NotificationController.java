package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification")
@CrossOrigin
public class NotificationController {

  private final NotificationService notificationService;

  @Autowired
  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @ApiOperation(value = "手动发送通知", consumes = "application/json")
  @PostMapping()
  public ResponseEntity<Response> sendMessage(@RequestBody Map<String, String> body) throws Exception {
    notificationService.sendMessage(body.get("data"));
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), HttpStatus.OK);
  }
}
