package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.EventService;
import com.syswin.temail.notification.main.application.TopicService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
  public ResponseEntity<Response<Map<String, Object>>> getEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = eventService.getEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "get unread 3 0002", consumes = "application/json")
  @GetMapping("/unread")
  public ResponseEntity<Response<List<UnreadResponse>>> getUnread(@RequestParam(name = "from") String to,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    List<UnreadResponse> result = eventService.getUnread(to);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "reset 3 0004", consumes = "application/json")
  @PutMapping("/reset")
  public ResponseEntity<Response> reset(@RequestBody Event event,
      @RequestHeader(name = CDTP_HEADER) String header)
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

  @ApiOperation(value = "pull reply event 3 0005", consumes = "application/json")
  @GetMapping("/reply/events")
  public ResponseEntity<Response<Map<String, Object>>> getReplyEvents(@RequestParam Long eventSeqId, @RequestParam String parentMsgId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = eventService.getReplyEvents(parentMsgId, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "pull topic event 3 0006", consumes = "application/json")
  @GetMapping("/topic/events")
  public ResponseEntity<Response<Map<String, Object>>> getTopicEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = topicService.getTopicEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "update group chat user status 3 0007", consumes = "application/json")
  @PutMapping("/groupchat/user/status")
  public ResponseEntity<Response> updateGroupChatUserStatus(@RequestBody Member member,
      @RequestHeader(name = CDTP_HEADER) String header)
      throws InterruptedException, RemotingException, UnsupportedEncodingException, MQClientException, MQBrokerException {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    UserStatus userStatus = UserStatus.getByValue(member.getUserStatus());
    if (userStatus == null) {
      LOGGER.error("status is illegal!");
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "status is illegal!"), headers, HttpStatus.BAD_REQUEST);
    }

    eventService.updateGroupChatUserStatus(member, userStatus, header);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "get do not disturb group 3 0008", consumes = "application/json")
  @GetMapping("/groupchat/user/status")
  public ResponseEntity<Response<Map<String, Integer>>> getUserDoNotDisturbGroups(@RequestParam String temail, @RequestParam String groupTemail,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    Map<String, Integer> result = eventService.getGroupChatUserStatus(temail, groupTemail);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }
}
