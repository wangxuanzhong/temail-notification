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
import static com.syswin.temail.notification.main.constants.Constant.CdtpParams.X_PACKET_ID;
import static org.springframework.http.HttpStatus.OK;

import com.syswin.temail.notification.foundation.domains.Response;
import com.syswin.temail.notification.main.application.DmServiceImpl;
import com.syswin.temail.notification.main.dto.DmDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liusen@syswin.com
 */
@RestController
@RequestMapping("/notification")
@Api(value = "notification", tags = "notification dm service")
public class DmController {

  private final DmServiceImpl dmService;

  @Autowired
  public DmController(DmServiceImpl dmService) {
    this.dmService = dmService;
  }

  @ApiOperation(value = "save packet event 1 3000", consumes = "application/json")
  @PostMapping("/packet")
  public ResponseEntity<Response> savePacketEvent(@RequestBody DmDto dmDto,
      @RequestHeader(name = CDTP_HEADER) String header, @RequestHeader(name = X_PACKET_ID) String xPacketId) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(CDTP_HEADER, header);
    dmService.savePacketEvent(dmDto, header, xPacketId);
    return new ResponseEntity<>(new Response<>(OK), headers, OK);
  }
}
