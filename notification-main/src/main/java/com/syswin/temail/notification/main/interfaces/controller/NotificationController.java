package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.EventService;
import com.syswin.temail.notification.main.application.TopicService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
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
@Api(value = "notification", tags = "notification service")
@CrossOrigin
public class NotificationController {

  private final String CDTP_HEADER = "CDTP-header";

  private final EventService eventService;
  private final TopicService topicService;

  @Autowired
  public NotificationController(EventService eventService, TopicService topicService) {
    this.eventService = eventService;
    this.topicService = topicService;
  }

  @ApiOperation(value = "pull event 3 0001", consumes = "application/json")
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

  @ApiOperation(value = "get unread 3 0002", consumes = "application/json")
  @GetMapping("/unread")
  public ResponseEntity<Response<List<UnreadResponse>>> getUnread(@RequestParam(name = "from", required = true) String to, String parentMsgId,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    List<UnreadResponse> result = eventService.getUnread(to);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "reset 3 0004", consumes = "application/json")
  @PutMapping("/reset")
  public ResponseEntity<Response<List<UnreadResponse>>> reset(@RequestBody Event event, @RequestHeader(name = CDTP_HEADER) String header)
      throws InterruptedException, RemotingException, UnsupportedEncodingException, MQClientException, MQBrokerException {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (event.getTo() == null || event.getTo().equals("")) {
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "to mast not null!"), headers, HttpStatus.BAD_REQUEST);
    }

    if ((event.getFrom() == null || event.getFrom().equals("")) && (event.getGroupTemail() == null || event.getGroupTemail().equals(""))) {
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "from and groupTemail mast not null at the same time!"), headers,
          HttpStatus.BAD_REQUEST);
    }

    eventService.reset(event, header);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "get reply sum 3 0005", consumes = "application/json")
  @GetMapping("/reply/sum")
  public ResponseEntity<Response<Map<String, Integer>>> getReplySum(@RequestParam(required = true) List<String> msgIds,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Integer> result = eventService.getReplySum(msgIds);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "pull topic event sum 3 0006", consumes = "application/json")
  @GetMapping("/topic/events")
  public ResponseEntity<Response<Map<String, Object>>> getTopicEvents(@RequestParam(name = "from", required = true) String to,
      @RequestParam(required = true) Long eventSeqId, @RequestParam(required = true) String topicId, Integer pageSize,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = topicService.getTopicEvents(to, topicId, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "get topic sum 3 0007", consumes = "application/json")
  @GetMapping("/topic/sum")
  public ResponseEntity<Response<Map<String, Object>>> getTopicSum(@RequestParam(name = "from", required = true) String to,
      @RequestParam(required = true) String topicId, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = topicService.getTopicSum(to, topicId);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }
}
