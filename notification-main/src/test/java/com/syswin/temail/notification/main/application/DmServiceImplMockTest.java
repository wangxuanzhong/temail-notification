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

package com.syswin.temail.notification.main.application;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.domains.EventType;
import com.syswin.temail.notification.main.dto.DispatcherResponse;
import com.syswin.temail.notification.main.dto.DmDto;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.util.EventUtil;
import com.syswin.temail.notification.main.util.NotificationPacketUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DmServiceImplMockTest {

  private NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();
  private Gson gson = new Gson();

  @MockBean
  private EventMapper eventMapper;
  @MockBean
  private IMqProducer iMqProducer;
  @MockBean
  private RedisServiceImpl redisService;

  @Autowired
  private NotificationConfig config;

  private RestTemplate restTemplateMock = new RestTemplate() {
    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
        @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
        throws RestClientException {
      return ResponseEntity.ok().build();
    }
  };

  private DmServiceImpl dmService;

  @Before
  public void setUp() {
    dmService = new DmServiceImpl(iMqProducer, redisService, eventMapper, restTemplateMock, config);
  }

  @Test
  public void testSavePacketEvent() {
    DmDto dmDto = new DmDto();
    dmDto.setPacket(notificationPacketUtil.encodeData("test packet".getBytes()));

    Event event = new Event();
    event.setPacket(notificationPacketUtil.encodeData("test packet".getBytes()));

    Map<String, Object> extraData = new HashMap<>();
    extraData.put("type", "A000");

    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setSender("a@test.com");
    cdtpHeader.setReceiver("b@test.com");
    cdtpHeader.setExtraData(gson.toJson(extraData));

    Mockito.when(redisService.checkUnique(Mockito.anyString())).thenReturn(true);
    Mockito.when(eventMapper.selectEventsByPacketIdAndEventType(Mockito.any(Event.class)))
        .thenReturn(new ArrayList<>());
    Mockito.when(redisService.getNextSeq(Mockito.anyString())).thenReturn(1L);

    String xPacketId = UUID.randomUUID().toString();
    dmService.savePacketEvent(dmDto, gson.toJson(cdtpHeader), xPacketId);

    event.setEventType(EventType.PACKET.getValue());
    event.setxPacketId(xPacketId);
    event.setFrom(cdtpHeader.getSender());
    event.setTo(cdtpHeader.getReceiver());
    event.setEventSeqId(1L);
    event.zip();

    String tag = event.getFrom() + "_" + event.getTo();

    Mockito.verify(iMqProducer)
        .sendMessage(EventUtil.toJson(gson, event), config.notifyGroupChatTopic, tag, "");
  }

  @Test
  public void testHandleMqMessage() {
    String body = "{\"from\":\"a.group@systoontest.com\",\"to\":\"kingskaaay@systoontest.com\",\"xPacketId\":\"98db4c73-da07-4935-8858-ac7d98bb3b0e:R\",\"packet\":\"AAAElwABMAAAAAM1CiRlM2FiZjQ5MC0yNjUwLTQ0YjUtOWNmNi1lNDVhMjY0OGExMTIQAhq6AU1JR0lBa0lCMDNhYWY4cmhxRXl4VHFwcXAzR0ZhYUtnaVhsUWxEODVRLW5kWlN4REl4bmV0YW5ORnQtVjJxUjNSYzA0bVVELTBFVHdJeXlvWHlsZVlzZnlOWnFqUG9vQ1FnQy1pVkEyUm45UVZVbVpoS2NSMFpodU1majBXOFdBNEI5aEM4cFFMMUVudVllaVRPZF9FamZtX1ZybFlpV19Qd21WeVFPaTVtbVhkeEZ6NXJYUEVKRjcyQSAEKSJeCy5qAQAAMiY5OGRiNGM3My1kYTA3LTQ5MzUtODg1OC1hYzdkOThiYjNiMGU6UjoXYS5ncm91cEBzeXN0b29udGVzdC5jb21C0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUFjRVJqbmdubDFKMGhFVTZ2T2Z2MVdDQTh6cW9oZGpKWWVpVWN6Nk1RbUx3dEhqbjZZZVNBYWZheFdjOF9QOEJYZWt2aDRna3hZeFZSeG1ZcHdqdi1SRU1CbFR1bDkwWWhkdF9OUU1JREg1UGo0Smg4TkhCaUVSYkNZZU1zN1B1VVdpUGRKYXl1UkZHM1hvaERSVUlvOHE1dVJIYjJFZlg0aGFEcmFxNm9Bc0M2SGtzShdraW5nc2t5QHN5c3Rvb250ZXN0LmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQVlrZWpaMGxwMmdRVUpaS1lSaGNER042MDJiSFNnRHBTTHE0Y1JGanZGbUVhYmxWWVR6VndPVGZmTFB0R0V4QjVQNzdKbVA4azlHVVV0Y2RmWUVSTDlQY0FmdkxRdUNqM2o1U291ZXBuUk9Sdk5zd2pUN1ZZZ3I5T3lxNmdHMi02MDJINWktYmRvUW9qdDJJOVFwbWNhQXhLT2FEQmRvTlJPVV9NQzdNbzI5UGpFTHNqHyJ7XG5cdFwidHlwZVwiIDogXCJCMDAwXCJcbn1cbiJyHG1zZ3NlYWwuc3lzdG9vbnRlc3QuY29tOjgwOTlBQUFBUXdBQUFFQUFBQUJ3QUFBQVlRTUFKTjFTS0Z6LUV6RlVoaThnbkowanhVVDdycENTejZON2xNeHlRaEg4dm9LejhtTmdEY1lyNFExQVZDc1VZbEpFVWt4RjV0LXpJQzZ3TVdYZHVCTnMtcEZ1LVJqOGlMR1VkX3hTNjR0WlBNdEpIc0F1X2NTWkp4Sm14Z0hzT3hEdnZwSjFqbE1DMHFqMjFOZm9IZWVzZlNGN01sem13N2Z6eGViMVJOTG9nVERIODcxcU4zaGR1T051aVBlajZJY0ZnYmhJLUN1QnpINmtNMTYyTGpMeUttVjRVZ0hxX2JOWlV0Q2VmNlloc3VFdUd0eUtmckdvLXZoMFRSUGNhZUpDeVkwSTNFb1B0RXAwUy1uVHl3WlNUWUtDaVVxUWFVZHJNQjZheWh0SGlsanl0bDRZd2ZlRDFsOF95bUZnLUhKcXdB\"}";

    Event event = gson.fromJson(body, Event.class);
    event.setEventType(EventType.PACKET.getValue());

    Mockito.when(redisService.checkUnique(Mockito.anyString())).thenReturn(true);
    Mockito.when(eventMapper.selectEventsByPacketIdAndEventType(Mockito.any(Event.class)))
        .thenReturn(new ArrayList<>());
    Mockito.when(redisService.getNextSeq(Mockito.anyString())).thenReturn(1L);

    event.setEventSeqId(1L);
    event.autoWriteExtendParam(body);
    event.zip();

    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(event.getPacket()));
    String header = gson.toJson(cdtpPacket.getHeader());
    DispatcherResponse dispatcherResponse = new DispatcherResponse(event.getTo(), event.getEventType(), header,
        EventUtil.toJson(gson, event));
    String tag = event.getFrom() + "_" + event.getTo();

    dmService.handleMqMessage(body, null);
    Mockito.verify(iMqProducer).sendMessage(gson.toJson(dispatcherResponse), tag);
  }
}