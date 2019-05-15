package com.syswin.temail.notification.main.interfaces.controller;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.NotificationEventService;
import com.syswin.temail.notification.main.application.NotificationTopicService;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.domains.response.UnreadResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification service")
public class NotificationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String CDTP_HEADER = "CDTP-header";

  private final NotificationEventService notificationEventService;
  private final NotificationTopicService notificationTopicService;

  @Autowired
  public NotificationController(NotificationEventService notificationEventService, NotificationTopicService notificationTopicService) {
    this.notificationEventService = notificationEventService;
    this.notificationTopicService = notificationTopicService;
  }

  @ApiOperation(value = "pull event 3 0001", consumes = "application/json")
  @GetMapping("/events")
  public ResponseEntity<Response<Map<String, Object>>> getEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = notificationEventService.getEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "get unread 3 0002", consumes = "application/json")
  @GetMapping("/unread")
  public ResponseEntity<Response<List<UnreadResponse>>> getUnread(@RequestParam(name = "from") String to,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    List<UnreadResponse> result = notificationEventService.getUnread(to);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "reset 3 0004", consumes = "application/json")
  @PutMapping("/reset")
  public ResponseEntity<Response> reset(@RequestBody Event event, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (event.getTo() == null || event.getTo().equals("")) {
      LOGGER.warn("reset 3 0004 : to mast not null!");
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "to mast not null!"), headers,
          HttpStatus.BAD_REQUEST);
    }

    if ((event.getFrom() == null || event.getFrom().equals("")) && (event.getGroupTemail() == null || event
        .getGroupTemail().equals(""))) {
      LOGGER.warn("reset 3 0004 : from and groupTemail mast not null at the same time!");
      return new ResponseEntity<>(
          new Response<>(HttpStatus.BAD_REQUEST, "from and groupTemail mast not null at the same time!"), headers,
          HttpStatus.BAD_REQUEST);
    }

    notificationEventService.reset(event, header);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  /**
   * @deprecated 接口功能合并到单群聊拉取接口中，此接口已废弃
   */
  @ApiOperation(value = "pull reply event 3 0005 (deprecated)", consumes = "application/json")
  @ApiIgnore
  @GetMapping("/reply/events")
  @Deprecated
  public ResponseEntity<Response<Map<String, Object>>> getReplyEvents(@RequestParam Long eventSeqId, @RequestParam String parentMsgId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "pull topic event 3 0006", consumes = "application/json")
  @GetMapping("/topic/events")
  public ResponseEntity<Response<Map<String, Object>>> getTopicEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = notificationTopicService.getTopicEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "update group chat user status 3 0007", consumes = "application/json")
  @PutMapping("/groupchat/user/status")
  public ResponseEntity<Response> updateGroupChatUserStatus(@RequestBody Member member, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    UserStatus userStatus = UserStatus.getByValue(member.getUserStatus());
    if (userStatus == null) {
      LOGGER.warn("update group chat user status 3 0007 : status is illegal!");
      return new ResponseEntity<>(new Response<>(HttpStatus.BAD_REQUEST, "status is illegal!"), headers,
          HttpStatus.BAD_REQUEST);
    }

    notificationEventService.updateGroupChatUserStatus(member, userStatus, header);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK), headers, HttpStatus.OK);
  }

  /**
   * 接口功能已废弃
   */
  @ApiOperation(value = "get do not disturb group 3 0008", consumes = "application/json")
  @GetMapping("/groupchat/user/status")
  @Deprecated
  public ResponseEntity<Response<Map<String, Integer>>> getUserDoNotDisturbGroups(@RequestParam String temail, @RequestParam String groupTemail,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    Map<String, Integer> result = notificationEventService.getGroupChatUserStatus(temail, groupTemail);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  /**
   * 接口功能已废弃
   */
  @ApiOperation(value = "pull event limited 3 0009", consumes = "application/json")
  @GetMapping("/limit/events")
  @Deprecated
  public ResponseEntity<Response<Map<String, Object>>> getEventsLimited(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = notificationEventService.getEventsLimited(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }

  @ApiOperation(value = "pull topic event limited 3 000A", consumes = "application/json")
  @GetMapping("/limit/topic/events")
  public ResponseEntity<Response<Map<String, Object>>> getTopicEventsLimited(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    Map<String, Object> result = notificationTopicService.getTopicEventsLimited(to, eventSeqId, pageSize);
    return new ResponseEntity<>(new Response<>(HttpStatus.OK, null, result), headers, HttpStatus.OK);
  }
}
