package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.EventService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "通知服务")
@CrossOrigin
public class NotificationController {

  private final String CDTP_HEADER = "CDTP-header";

  private final EventService eventService;

  @Autowired
  public NotificationController(EventService eventService) {
    this.eventService = eventService;
  }

  @ApiOperation(value = "拉取事件 3 0001", consumes = "application/json")
  @GetMapping("/events")
  public ResponseEntity<Response<Map<String, Object>>> getEvents(@RequestParam(name = "from") String to,
      @RequestParam(required = true) Long eventSeqId, String parentMsgId, Integer pageSize, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    Map<String, Object> result = null;
    if (parentMsgId == null || parentMsgId.isEmpty()) {
      result = eventService.getEvents(to, eventSeqId, pageSize);
    } else {
      result = eventService.getReplyEvents(parentMsgId, eventSeqId, pageSize);
    }

    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "获取未读数 3 0002", consumes = "application/json")
  @GetMapping("/unread")
  public ResponseEntity<Response<List<UnreadResponse>>> getUnread(@RequestParam(name = "from", required = true) String to, String parentMsgId,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    List<UnreadResponse> result = eventService.getUnread(to);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "重置未读数 3 0004", consumes = "application/json")
  @PutMapping("/reset")
  public ResponseEntity<Response<List<UnreadResponse>>> reset(@RequestBody Event event, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (event.getTo() == null || event.getTo().equals("")) {
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "to不能为空！"), headers, HttpStatus.BAD_REQUEST);
    }

    if ((event.getFrom() == null || event.getFrom().equals("")) && (event.getGroupTemail() == null || event.getGroupTemail().equals(""))) {
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "from和groupTemail不能同时为空！"), headers, HttpStatus.BAD_REQUEST);
    }

    eventService.reset(event);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "获取回复消息总数 3 0005", consumes = "application/json")
  @GetMapping("/reply/sum")
  public ResponseEntity<Response<Map<String, Integer>>> getReplySum(@RequestParam(required = true) List<String> msgIds,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Integer> result = eventService.getReplySum(msgIds);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }
}
