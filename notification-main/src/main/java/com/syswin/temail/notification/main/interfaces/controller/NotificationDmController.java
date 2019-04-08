package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.NotificationDmService;
import com.syswin.temail.notification.main.domains.Event;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification dm service")
public class NotificationDmController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String CDTP_HEADER = "CDTP-header";
  private final String X_PACKET_ID = "X-PACKET-ID";

  private final NotificationDmService notificationDmService;

  @Autowired
  public NotificationDmController(NotificationDmService notificationDmService) {
    this.notificationDmService = notificationDmService;
  }

  @ApiOperation(value = "save packet event 1 3000", consumes = "application/json")
  @PostMapping("/packet")
  public ResponseEntity<Response> savePacketEvent(@RequestBody Event event, @RequestHeader(name = CDTP_HEADER) String header,
      @RequestHeader(name = X_PACKET_ID) String xPacketId) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    notificationDmService.savePacketEvent(event, header, xPacketId, true);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }
}
