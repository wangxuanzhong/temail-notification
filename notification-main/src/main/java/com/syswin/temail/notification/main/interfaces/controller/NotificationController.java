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

import static com.syswin.temail.notification.main.constants.Constant.CdtpParams.CDTP_HEADER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.EventService;
import com.syswin.temail.notification.main.application.TopicServiceImpl;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.Member;
import com.syswin.temail.notification.main.domains.Member.UserStatus;
import com.syswin.temail.notification.main.dto.UnreadResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @author liusen@syswin.com
 */
@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification service")
public class NotificationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventService eventService;
  private final TopicServiceImpl topicService;
  private final Gson gson;

  @Autowired
  public NotificationController(EventService eventService, TopicServiceImpl topicService) {
    this.eventService = eventService;
    this.topicService = topicService;
    gson = new Gson();
  }

  @ApiOperation(value = "pull event 3 0001", consumes = "application/json")
  @GetMapping(value = "/events", produces = "application/json")
  public ResponseEntity<String> getEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (to == null || "".equals(to)) {
      LOGGER.warn("pull topic event 3 0001 : from mast not empty!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "from mast not empty!")), headers,
          BAD_REQUEST);
    }

    if (eventSeqId == null) {
      LOGGER.warn("pull topic event 3 0001 : eventSeqId mast not null!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "eventSeqId mast not null!")), headers,
          BAD_REQUEST);
    }

    Map<String, Object> result = eventService.getEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(gson.toJson(new Response<>(OK, null, result)), headers, OK);
  }

  @ApiOperation(value = "get unread 3 0002", consumes = "application/json")
  @GetMapping("/unread")
  public ResponseEntity<Response<List<UnreadResponse>>> getUnread(@RequestParam(name = "from") String to,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (to == null || "".equals(to)) {
      LOGGER.warn("get unread 3 0002 : from mast not empty!");
      return new ResponseEntity<>(new Response<>(BAD_REQUEST, "from mast not empty!"), headers, BAD_REQUEST);
    }

    List<UnreadResponse> result = eventService.getUnread(to);
    return new ResponseEntity<>(new Response<>(OK, null, result), headers, OK);
  }

  @ApiOperation(value = "reset 3 0004", consumes = "application/json")
  @PutMapping("/reset")
  public ResponseEntity<Response> reset(@RequestBody Event event, @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (event.getTo() == null || "".equals(event.getTo())) {
      LOGGER.warn("reset 3 0004 : to mast not empty!");
      return new ResponseEntity<>(new Response<>(BAD_REQUEST, "to mast not empty!"), headers, BAD_REQUEST);
    }

    boolean fromIsEmpty = event.getFrom() == null || "".equals(event.getFrom());
    boolean groupTemailIsEmpty = event.getGroupTemail() == null || "".equals(event.getGroupTemail());
    if (fromIsEmpty && groupTemailIsEmpty) {
      LOGGER.warn("reset 3 0004 : from and groupTemail mast not empty at the same time!");
      return new ResponseEntity<>(new Response<>(BAD_REQUEST, "from and groupTemail mast not empty at the same time!"),
          headers, BAD_REQUEST);
    }

    eventService.reset(event, header);
    return new ResponseEntity<>(new Response<>(OK), headers, OK);
  }

  /**
   * @deprecated 接口功能合并到单群聊拉取接口中，此接口已废弃
   */
  @ApiOperation(value = "pull reply event 3 0005 (deprecated)", consumes = "application/json")
  @ApiIgnore
  @GetMapping("/reply/events")
  @Deprecated
  public ResponseEntity<Response<Map<String, Object>>> getReplyEvents(@RequestParam Long eventSeqId,
      @RequestParam String parentMsgId, Integer pageSize,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    return new ResponseEntity<>(new Response<>(OK), headers, OK);
  }

  @ApiOperation(value = "pull topic event 3 0006", consumes = "application/json")
  @GetMapping(value = "/topic/events", produces = "application/json")
  public ResponseEntity<String> getTopicEvents(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (to == null || "".equals(to)) {
      LOGGER.warn("pull topic event 3 0006 : from mast not empty!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "from mast not empty!")), headers,
          BAD_REQUEST);
    }

    if (eventSeqId == null) {
      LOGGER.warn("pull topic event 3 0006 : eventSeqId mast not null!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "eventSeqId mast not null!")), headers,
          BAD_REQUEST);
    }

    Map<String, Object> result = topicService.getTopicEvents(to, eventSeqId, pageSize);
    return new ResponseEntity<>(gson.toJson(new Response<>(OK, null, result)), headers, OK);
  }

  /**
   * 接口功能只有旧群聊使用
   */
  @ApiOperation(value = "update group chat user status 3 0007", consumes = "application/json")
  @PutMapping("/groupchat/user/status")
  public ResponseEntity<Response> updateGroupChatUserStatus(@RequestBody Member member,
      @RequestHeader(name = CDTP_HEADER) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (member.getUserStatus() == null) {
      LOGGER.warn("update group chat user status 3 0007 : status is illegal!");
      return new ResponseEntity<>(new Response<>(BAD_REQUEST, "status is illegal!"), headers, BAD_REQUEST);
    }
    UserStatus userStatus = UserStatus.getByValue(member.getUserStatus());

    eventService.updateGroupChatUserStatus(member, userStatus, header);
    return new ResponseEntity<>(new Response<>(OK), headers, OK);
  }

  /**
   * 接口功能只有旧群聊使用
   */
  @ApiOperation(value = "get do not disturb group 3 0008", consumes = "application/json")
  @GetMapping("/groupchat/user/status")
  public ResponseEntity<Response<Map<String, Integer>>> getUserDoNotDisturbGroups(@RequestParam String temail,
      @RequestParam String groupTemail, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    Map<String, Integer> result = eventService.getGroupChatUserStatus(temail, groupTemail);
    return new ResponseEntity<>(new Response<>(OK, null, result), headers, OK);
  }

  @ApiOperation(value = "pull event limited 3 0009", consumes = "application/json")
  @GetMapping(value = "/limit/events", produces = "application/json")
  public ResponseEntity<String> getEventsLimited(@RequestParam(name = "from") String to, @RequestParam Long eventSeqId,
      Integer pageSize, @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (to == null || "".equals(to)) {
      LOGGER.warn("pull topic event 3 0009 : from mast not empty!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "from mast not empty!")), headers,
          BAD_REQUEST);
    }

    if (eventSeqId == null) {
      LOGGER.warn("pull topic event 3 0009 : eventSeqId mast not null!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "eventSeqId mast not null!")), headers,
          BAD_REQUEST);
    }

    Map<String, Object> result = eventService.getEventsLimited(to, eventSeqId, pageSize);
    return new ResponseEntity<>(gson.toJson(new Response<>(OK, null, result)), headers, OK);
  }

  @ApiOperation(value = "pull topic event limited 3 000A", consumes = "application/json")
  @GetMapping(value = "/limit/topic/events", produces = "application/json")
  public ResponseEntity<String> getTopicEventsLimited(@RequestParam(name = "from") String to,
      @RequestParam Long eventSeqId, Integer pageSize,
      @RequestHeader(name = CDTP_HEADER, required = false) String header) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);

    if (to == null || "".equals(to)) {
      LOGGER.warn("pull topic event 3 000A : from mast not empty!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "from mast not empty!")), headers,
          BAD_REQUEST);
    }

    if (eventSeqId == null) {
      LOGGER.warn("pull topic event 3 000A : eventSeqId mast not null!");
      return new ResponseEntity<>(gson.toJson(new Response<>(BAD_REQUEST, "eventSeqId mast not null!")), headers,
          BAD_REQUEST);
    }

    Map<String, Object> result = topicService.getTopicEventsLimited(to, eventSeqId, pageSize);
    return new ResponseEntity<>(gson.toJson(new Response<>(OK, null, result)), headers, OK);
  }
}
