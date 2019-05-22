package com.syswin.temail.notification.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.syswin.temail.notification.foundation.application.IJsonService;
import com.syswin.temail.notification.foundation.application.IMqProducer;
import com.syswin.temail.notification.main.domains.Event;
import com.syswin.temail.notification.main.infrastructure.EventMapper;
import com.syswin.temail.notification.main.mock.MqProducerMock;
import com.syswin.temail.notification.main.mock.RedisServiceMock;
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
import org.springframework.beans.factory.annotation.Value;
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
public class NotificationDmServiceTest {

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
  private NotificationRedisService notificationRedisService;
  @Autowired
  private RestTemplate notificationRestTemplate;
  @Value("${app.temail.notification.saas.enabled:false}")
  private String saasEnabled;
  @Value("${spring.rocketmq.topics.notify.groupChat:notify}")
  private String groupChatTopic;
  @Value("${spring.rocketmq.topics.notify.application:notify}")
  private String applicationTopic;
  @Value("${url.temail.auth:authUrl}")
  private String authUrl;

  private RestTemplate restTemplateMock = new RestTemplate() {
    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
        @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
        throws RestClientException {
      return ResponseEntity.ok().build();
    }
  };

  private MqProducerMock mqProducerMock = new MqProducerMock();
  private RedisServiceMock redisServiceMock = new RedisServiceMock();

  private NotificationDmService notificationDmService;

  @Before
  public void setUp() {
    if (isMock) {
      notificationDmService = new NotificationDmService(mqProducerMock, redisServiceMock, eventMapper, iJsonService, restTemplateMock, saasEnabled,
          groupChatTopic, applicationTopic, authUrl);
    } else {
      notificationDmService = new NotificationDmService(iMqProducer, notificationRedisService, eventMapper, iJsonService, notificationRestTemplate,
          saasEnabled, groupChatTopic, applicationTopic, authUrl);
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

    notificationDmService.savePacketEvent(event, gson.toJson(cdtpHeader), UUID.randomUUID().toString());
  }

  @Test
  public void testMqMessage() {
    String body = "{\"from\":\"a.group@systoontest.com\",\"to\":\"kingskaaay@systoontest.com\",\"xPacketId\":\"98db4c73-da07-4935-8858-ac7d98bb3b0e:R\",\"packet\":\"AAAElwABMAAAAAM1CiRlM2FiZjQ5MC0yNjUwLTQ0YjUtOWNmNi1lNDVhMjY0OGExMTIQAhq6AU1JR0lBa0lCMDNhYWY4cmhxRXl4VHFwcXAzR0ZhYUtnaVhsUWxEODVRLW5kWlN4REl4bmV0YW5ORnQtVjJxUjNSYzA0bVVELTBFVHdJeXlvWHlsZVlzZnlOWnFqUG9vQ1FnQy1pVkEyUm45UVZVbVpoS2NSMFpodU1majBXOFdBNEI5aEM4cFFMMUVudVllaVRPZF9FamZtX1ZybFlpV19Qd21WeVFPaTVtbVhkeEZ6NXJYUEVKRjcyQSAEKSJeCy5qAQAAMiY5OGRiNGM3My1kYTA3LTQ5MzUtODg1OC1hYzdkOThiYjNiMGU6UjoXYS5ncm91cEBzeXN0b29udGVzdC5jb21C0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUFjRVJqbmdubDFKMGhFVTZ2T2Z2MVdDQTh6cW9oZGpKWWVpVWN6Nk1RbUx3dEhqbjZZZVNBYWZheFdjOF9QOEJYZWt2aDRna3hZeFZSeG1ZcHdqdi1SRU1CbFR1bDkwWWhkdF9OUU1JREg1UGo0Smg4TkhCaUVSYkNZZU1zN1B1VVdpUGRKYXl1UkZHM1hvaERSVUlvOHE1dVJIYjJFZlg0aGFEcmFxNm9Bc0M2SGtzShdraW5nc2t5QHN5c3Rvb250ZXN0LmNvbVLTAU1JR2JNQkFHQnlxR1NNNDlBZ0VHQlN1QkJBQWpBNEdHQUFRQVlrZWpaMGxwMmdRVUpaS1lSaGNER042MDJiSFNnRHBTTHE0Y1JGanZGbUVhYmxWWVR6VndPVGZmTFB0R0V4QjVQNzdKbVA4azlHVVV0Y2RmWUVSTDlQY0FmdkxRdUNqM2o1U291ZXBuUk9Sdk5zd2pUN1ZZZ3I5T3lxNmdHMi02MDJINWktYmRvUW9qdDJJOVFwbWNhQXhLT2FEQmRvTlJPVV9NQzdNbzI5UGpFTHNqHyJ7XG5cdFwidHlwZVwiIDogXCJCMDAwXCJcbn1cbiJyHG1zZ3NlYWwuc3lzdG9vbnRlc3QuY29tOjgwOTlBQUFBUXdBQUFFQUFBQUJ3QUFBQVlRTUFKTjFTS0Z6LUV6RlVoaThnbkowanhVVDdycENTejZON2xNeHlRaEg4dm9LejhtTmdEY1lyNFExQVZDc1VZbEpFVWt4RjV0LXpJQzZ3TVdYZHVCTnMtcEZ1LVJqOGlMR1VkX3hTNjR0WlBNdEpIc0F1X2NTWkp4Sm14Z0hzT3hEdnZwSjFqbE1DMHFqMjFOZm9IZWVzZlNGN01sem13N2Z6eGViMVJOTG9nVERIODcxcU4zaGR1T051aVBlajZJY0ZnYmhJLUN1QnpINmtNMTYyTGpMeUttVjRVZ0hxX2JOWlV0Q2VmNlloc3VFdUd0eUtmckdvLXZoMFRSUGNhZUpDeVkwSTNFb1B0RXAwUy1uVHl3WlNUWUtDaVVxUWFVZHJNQjZheWh0SGlsanl0bDRZd2ZlRDFsOF95bUZnLUhKcXdB\"}";
    notificationDmService.handleMqMessage(body, null);
  }


  @Test
  public void testCheckIsSameDomain() {
    assertThat(notificationDmService.checkIsSameDomain("ctt40@systoontest.com")).isTrue();
  }

  @Test
  @Ignore
  public void test2ReturnEncryptPacket() {
    String s = "AAAGUgABMAAAAgMbCis4NjM5OTEwMzE3ODkyNDdtc2dzZWFsLnN5c3Rvb250ZXN0LmNvbTo4MDk5GrgBTUlHSEFrRTh0X2I1NzZ2RHpjUm0xNGRTU3EzZUZQZTh0UEZudWNsSGg5ZmNZMTMtWDR1SVhMSWJMTnN3N21KWDNiLTBVXzdrdk1GdThNaFczUWc1dU5TOHo5cFFLZ0pDQU12ejRnYTJINFZHYmNYZjZaUE43dDZYb0RqM3NzRVZjWXlOZHFNQVZiV1QyeHlTY1NPU0NyVV91YTBvNGFmNFdyQm9sTFNodkZnMVRUcGlSTlYyZmpQYiAEKU16pVwAAAAAMiQ5MWRjOGI2OS00YmU5LTQ1YjQtYjVkYi0xNzc2YTFhY2FhNTM6Gnpob3VsZWkyNTVAc3lzdG9vbnRlc3QuY29tQtMBTUlHYk1CQUdCeXFHU000OUFnRUdCU3VCQkFBakE0R0dBQVFCNEh5WWs4aEIwZUowemUtS1lkd3hSUFNRbFRRYk9QQW56LW96RmN3WEdIcGZheDdkNWoxUjRMcW1kLW52VnRGU0xqTk4wSXNpZFMzSThjR1hHYkxoOE4wQmhHNVNzTkFQenFaRTU5dkl3U3BySENGLV9LRjhTb1hZeElfaUpRbXZVQi16REEycTN0WVlFakxFdW9hZmRwUVY1RUFIODYxRWVidXFEYU0xd0VfYmJHc0oaemhvdWxlaTI1NUBzeXN0b29udGVzdC5jb21S0wFNSUdiTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFqQTRHR0FBUUI0SHlZazhoQjBlSjB6ZS1LWWR3eFJQU1FsVFFiT1BBbnotb3pGY3dYR0hwZmF4N2Q1ajFSNExxbWQtbnZWdEZTTGpOTjBJc2lkUzNJOGNHWEdiTGg4TjBCaEc1U3NOQVB6cVpFNTl2SXdTcHJIQ0YtX0tGOFNvWFl4SV9pSlFtdlVCLXpEQTJxM3RZWUVqTEV1b2FmZHBRVjVFQUg4NjFFZWJ1cURhTTF3RV9iYkdzchxtc2dzZWFsLnN5c3Rvb250ZXN0LmNvbTo4MDk5QUFBQVF3QUFBRUFBQUFIUUFBQUJ3UUlBdkQwdENMLTB5Sl9UYTk3WnFWZXBjdGU2d1lCUXZ1Q0tudmdDdnNyUVVaanBlV29pV0ttclpoLUtPcVIxNEMxb1QwTGdxd1h2UC1aTEhkT0xnVmtlb1VkcG00dXRmSG53SHZ1YXdlLTVnRVBDaXNubFRDNVBjcXdzNUptTjNnRU9nT2tTNERfTHFwcnBzMDVrcGxySjVzcXZfc1plNmVGLTVzXzhOTGxPZ3FyQTZFMGs3d0F2VXQ1ZVUzam1BcUdSSUdYNXg0Yk5WYndOWUtDYi1fdUNNUU0xZlRYQlpRUU9OdUZ1LTJWdVdqOEs5eFNMYUVUeVM4UFB3WkRQM2RYRnA2b0dJZnFMWWtFdGhwcVBIUldyNTQ1akFOU0Q5MjV4NGw5a2VoUWNCd3hOZllfZGE1dUpwOW5DM0RjWDJpRHAwckY1R3dueTNMVldVVnpnNlRITllybG9yQ29LRHEwOGpGX2hzZTdwMF9VWmlLcVNTczJldXQyVnBqVWhWQlFuWEpXVmtmcHdoRUxfNXZXRWFKRGkzRm4waktVTFBqajJCZEdqTTlCQzY2WDV6VGl4UEhScTNyVU00c3QzZEkwaVA5WVFsUjdPOHh2WTVuQmU0WkNXUFR1emdzM2U5bEdmWmtpRVZjbWFSOTducm03UXRWZVlXVEo1NzkzQWljOHFURm5zb3EzSVd6VklHX0dsS0lhZFhlR0N1MFRxUFh5dUhLS3JHdUo1U3M5MTFPWlZaeXBJVXdudGRESTJHLWREa3NTd1dRb1dzWXRFWVNCV050WnVUU1M1VU95THRPR3pQOElSV0cwNHhmTC13VkFOMXhURWo3aDRQWW40WDNsYVhDQllnd0ZEVklJalplajlBQnRZUXhjazBVWlFzRzNDa3pUSnBjM2JyLThabXE2Z1p5QkpsX0gxakVwMEJFSGdaVXlqR2xLdlVUNElsT3ptNkdZM2JoZUJZSXRSZm5JaENFYXZTbjA3TDhhVUlab1YwZUpUUEhvYjMycGFENy00Rm1QR1hneVVMeksteWZZM09zYndVVkk";
    CDTPPacket cdtpPacket = notificationPacketUtil.unpack(notificationPacketUtil.decodeData(s));
    System.out.println("CDTPPacket ======> " + cdtpPacket);

    CDTPPacket data = notificationPacketUtil.unpack(cdtpPacket.getData());
    System.out.println("data ======> " + data);

  }

}