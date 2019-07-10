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
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.configuration.NotificationConfig;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceImplMock;
import com.syswin.temail.notification.main.util.NotificationPacketUtil;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class NotificationDmServiceImplTest {

  private final boolean isMock = true;
  private NotificationPacketUtil notificationPacketUtil = new NotificationPacketUtil();
  private Gson gson = new Gson();

  @Autowired
  private EventMapper eventMapper;
  @Autowired
  private IJsonService iJsonService;
  @Autowired
  private IMqProducer iMqProducer;
  @Autowired
  private NotificationRedisServiceImpl redisService;
  @Autowired
  private RestTemplate restTemplate;
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

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceImplMock redisServiceMock = new RedisServiceImplMock();

  private NotificationDmServiceImpl dmService;

  @Before
  public void setUp() {
    if (isMock) {
      dmService = new NotificationDmServiceImpl(mqProducerMock, redisServiceMock, eventMapper, iJsonService,
          restTemplateMock, config);
    } else {
      dmService = new NotificationDmServiceImpl(iMqProducer, redisService, eventMapper, iJsonService, restTemplate,
          config);
    }
  }

  @Test
  public void testSavePacketEvent() {
    Event event = new Event();
    event.setPacket(notificationPacketUtil.encodeData("test packet".getBytes()));

    Map<String, Object> extraData = new HashMap<>();
    extraData.put("type", "A000");

    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setSender("a@test.com");
    cdtpHeader.setReceiver("b@test.com");
    cdtpHeader.setExtraData(gson.toJson(extraData));

    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());
  }

  @Test
  public void testHandleMqMessage() {
    String body = "{\"from\":\"a.group@systoontest.com\",\"to\":\"kingskaaay@systoontest.com\",\"xPacketId\":\"98db4c73-da07-4935-8858-ac7d98bb3b0e:R\",\"packet\":\"AAAElwABMAAAAAM1CiRlM2FiZjQ5MC0yNjUwLTQ0YjUtOWNmNi1lNDVhMjY0OGExMTIQAhq6AU1JR0lBa0lCMDNhYWY4cmhxRXl4VHFwcXAzR0ZhYUtnaVhsUWxEODVRLW5kWlN4REl4bmV0YW5ORnQtVjJxUjNSYzA0bVVELTBFVHdJeXlvWHlsZVlzZnlOWnFqUG9vQ1FnQy1pVkEyUm45UVZVbVpoS2NSMFpodU1majBXOFdBNEI5aEM4cFFMMUVudVllaVRPZF9FamZtX1ZybFlpV19Qd21WeVFPaTVtbVhkeEZ6NXJYUEVKRjcyQSAEKSJeCy5qAQAAMiY5OGRiNGM3My1kYTA3LTQ5MzUtODg1OC1hYzdkOThiYjNiMGU6UjoXYS5ncm91cEBzeXN0b29udGVzdC5jb21C0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUFjRVJqbmdubDFKMGhFVTZ2T2Z2MVdDQTh6cW9oZGpKWWVpVWN6Nk1RbUx3dEhqbjZZZVNBYWZheFdjOF9QOEJYZWt2aDRna3hZeFZSeG1ZcHdqdi1SRU1CbFR1bDkwWWhkdF9OUU1JREg1UGo0Smg4TkhCaUVSYkNZZU1zN1B1VVdpUGRKYXl1UkZHM1hvaERSVUlvOHE1dVJIYjJFZlg0aGFEcmFxNm9Bc0M2SGtzShdraW5nc2t5QHN5c3Rvb250ZXN0LmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQVlrZWpaMGxwMmdRVUpaS1lSaGNER042MDJiSFNnRHBTTHE0Y1JGanZGbUVhYmxWWVR6VndPVGZmTFB0R0V4QjVQNzdKbVA4azlHVVV0Y2RmWUVSTDlQY0FmdkxRdUNqM2o1U291ZXBuUk9Sdk5zd2pUN1ZZZ3I5T3lxNmdHMi02MDJINWktYmRvUW9qdDJJOVFwbWNhQXhLT2FEQmRvTlJPVV9NQzdNbzI5UGpFTHNqHyJ7XG5cdFwidHlwZVwiIDogXCJCMDAwXCJcbn1cbiJyHG1zZ3NlYWwuc3lzdG9vbnRlc3QuY29tOjgwOTlBQUFBUXdBQUFFQUFBQUJ3QUFBQVlRTUFKTjFTS0Z6LUV6RlVoaThnbkowanhVVDdycENTejZON2xNeHlRaEg4dm9LejhtTmdEY1lyNFExQVZDc1VZbEpFVWt4RjV0LXpJQzZ3TVdYZHVCTnMtcEZ1LVJqOGlMR1VkX3hTNjR0WlBNdEpIc0F1X2NTWkp4Sm14Z0hzT3hEdnZwSjFqbE1DMHFqMjFOZm9IZWVzZlNGN01sem13N2Z6eGViMVJOTG9nVERIODcxcU4zaGR1T051aVBlajZJY0ZnYmhJLUN1QnpINmtNMTYyTGpMeUttVjRVZ0hxX2JOWlV0Q2VmNlloc3VFdUd0eUtmckdvLXZoMFRSUGNhZUpDeVkwSTNFb1B0RXAwUy1uVHl3WlNUWUtDaVVxUWFVZHJNQjZheWh0SGlsanl0bDRZd2ZlRDFsOF95bUZnLUhKcXdB\"}";
//    body = "{\"xPacketId\":\"2d879886-21b2-4c85-b0c4-06fffb0d95da\",\"from\":\"a\",\"to\":\"b\",\"packet\":\"AAEwAAABAAY6AWFKAWIA\"}";
    dmService.handleMqMessage(body, null);
    dmService.handleMqMessage(body, null);
  }


  @Test
  @Ignore
  public void test2ReturnEncryptPacket() {
    String s = "AAAHKAABMAAAAAMGCiRlM2FiZjQ5MC0yNjUwLTQ0YjUtOWNmNi1lNDVhMjY0OGExMTIQAhq6AU1JR0lBa0lCdkdiNFZseWp2bDJHQ0tleG45SEVyMmdveDVVT2VVRS12SEhmTTZxQk5kSmlSTnpMeVY2dUxFRDNaNm9NdGY0YlVyZGlCQW5GQlRlUHFaaktJRjFEc3JrQ1FnSEVZQnFSb1N2U1J5RXZUTWZDMUhwYXFrRlhMdXJPNU04ZlJ2X1ZRT0gwb19tZVlxMXdOeXJ5MHlCSV90dmJxRzFiRm4zZml5anI4eTFzSzNweEVGUkxSdyAEKaLCBPdqAQAAMiQwMjY1NjdjMi1kNjBkLTQyYTYtOGYwZC1lMTIwNGVkZjU2YmQ6FmcuMTkwNTEwMDFAbXNnc2VhbC5jb21C0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUJMcnJua1ZacGVQMENEVXN5bG5KSi1nZ3FvaXNoeEUxN09sMUUzcXJFS3c3THFXSlhCeXBMaWxxLTVJenp2ekJNVWJXMDlTQTdDVGN4STFJeURRQkptZzBCdk41NlpIVXUxVlk5TlA1dE1ydWQ4QjVDSkxua0pDeGtkN0NOMEZHZy1ZcDlidkVxOG8xbGMwY2xqT2xuNm5LdFQ2ZUl2VjVqSVkwV3d4dldVR0dCU2xJShRzb25neWFiaW5Ac3lzd2luLmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQVBHbThqWm1lRFlFaVFqTW1NSVd0bHFVRkluaHNTT0wxVHl2TTd0Zmd3NWRoRHpramU5d3V2cnNrRFQxSmxzelNxc1VfV3EwRDRtemZ2MC1CSUNCNUVTZ0FLT1FRV3NSUHFxSDR6Yk1RZW10VVZ2aEF3TExtaFRiUzNmeWwzMTBVQW9NMTItT19uc2NoS2hBam5ZTXAxYlM1b2dkVllSMFlfVk1Yb3ExNkhBVkVtUVFyFG1zZ3NlYWwudC5lbWFpbDo4MDk5QUFBQVF3QUFBRUFBQUFLQUFBQUNkZ0lCWVNfTlI5S1V4akJDXzhfbVhSbU91T3R2N0VqMHQ4Q01kRUd6VnRJSXlxSVpOT0p0V1hxQnFodEhHVWgwak4weEJVbThFWTVQUWQ4TlBTUmtBR09LRlpWVXBoUnd3S3hxY1dXVThpX29yUXI1eU5sbDdZNFFPZUQ2SzdaeDBXOUNrOWdpY09yS29uaWNDdi1aTEtZQUVVeFB1bnVKVWVmNUQydElaVEZsT05La2VQcC1ob3dBU3lneXZpUl9hdVR3WE9GZmN4NUxOS2VfOGpEZHBQcWZkSDdFWFlpZ1ZseG1BREVKVV9oQ1psQ3UyWlhLTk1UMklzVm9iSjlPLXBEc09EYTZOUW04dUNJemhpYWZCTlc4Z1NFMjZwejNJbHRLc0oweEFtUGN2VkJjUEVlblBaUFV0ckQzeHNEWnJBal9tMVpNblBHTklFMU9LMXZFYkhqbjNqZ3dPenNmX3lpUFFOanhGOWdNcVBISDdyLXJlR0RRUFFUNzJqVzlEY1M3ZzNKT0ZMdlRqUU9Xd1MxRnUyUGJTa2hrTkp0eUUzNmh3X3lqWFJsVHFVNWZBdDRyU3psd1VQLXZxdUJhTjR2emQ1cmNNMEdhczJxU2NIUGdxT3hpdkVMNVRUNEpveUY5aEtKVVlqRnFDZzJsbldQNHVraGxId19rNUF6eGlfZGRKUzluc0NuYzhVWWxrOHdTbTg5cC1EQXUtTm5NUHF2ckkzU05mSW9KaGVKc2FSeEh4RngyMXFac19jZ1ZJYjkxWUZkMlNKcnJrYkwzZVZsV09jMDlOQ2hTTlVoVEVtT3ZCSzY1U2cxZVAzOHBqcWZaX255anRjZTFQSFZkZS1EX0lmX1BFdEpDUW45ZkM5eDZyOFpjZHcxUVJEal9BOXhZV0pEc0hnRjNmS3JaTm5xUU5IeXNVeU1kazhtd1R1VXF4Z0hqZGhXcF9sT0ZnR2ZkeHQtclYwTk5FdUVLU1NRTXlXZ3VGT1RvYWVkVVpzNEtwRFpvZzFMRU5ZOS05WGU2eGNydlBpUUY5UWpVNHpkOU1neUFEamtFb3Y3b18xSmw0dFJxZFYxcnpIZFdBVHhTbWp0aVhlOUktUktRZHdXWXd4WEJoay1VNU5aUzZpNUJ6UW9FYjYzbHM3d0wxNUZCYlRvMl9ZalBPd21GamJTLXpZN205VmlKbHlySC1lUDZfU3BrVHRFOUZUNWRGN2ltMnU1ampGcEVxY1I1RWViS0t0V2lXMWVZOUZtUk9kQktPYkMyS3pFMi0xYTBEdVpMdFU4eWlEMlRSM1o4eGRjOUF6ZzBUdDBrZTBDVkU2Um02OVBOQUlWSzVEbWFwa05RRkdqYUE0NjhJdThXdnR6WDBn";
    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(s));
    System.out.println("CDTPPacket ======> " + cdtpPacket);

    CDTPPacket data = notificationPacketUtil.unpack(cdtpPacket.getData());
    System.out.println("data ======> " + data);
  }

  @Test
  @Ignore
  public void buildPacket() {
    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setSender("a");
    cdtpHeader.setReceiver("b");

    CDTPPacket cdtpPacket = new CDTPPacket();
    cdtpPacket.setCommandSpace((short) 1);
    cdtpPacket.setCommand((short) 0x3000);
    cdtpPacket.setVersion((short) 1);
    cdtpPacket.setHeader(cdtpHeader);
    cdtpPacket.setData(new byte[1]);

    Event event = new Event();
    event.setFrom("a");
    event.setTo("b");
    event.setxPacketId(UUID.randomUUID().toString());
    event.setPacket(notificationPacketUtil.encodeData(NotificationPacketUtil.pack(cdtpPacket)));

    System.out.println(gson.toJson(event));
  }

  @Test
  public void testSavePacketEventAll() {
    Event event = new Event();
    event.setPacket(notificationPacketUtil.encodeData("".getBytes()));

    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setSender("a@test.com");
    cdtpHeader.setReceiver("b@test.com");
    String xPacketId = UUID.randomUUID().toString();
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), xPacketId);
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), xPacketId);

    Map<String, Object> extraData = new HashMap<>();
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());

    extraData.put("type", "A000");
    cdtpHeader.setExtraData(gson.toJson(extraData));
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());

    extraData.put("type", "B000");
    cdtpHeader.setExtraData(gson.toJson(extraData));
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());

    extraData.put("type", "C000");
    cdtpHeader.setExtraData(gson.toJson(extraData));
    dmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());
  }
}