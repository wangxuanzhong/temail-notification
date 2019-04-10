package com.syswin.temail.notification.main.util;

import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.packet.PacketUtil;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationPacketUtil extends PacketUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * base64加密
   */
  public String encodeData(byte[] data) {
    return Base64.getUrlEncoder().encodeToString(data);
  }

  /**
   * base64解密
   */
  public byte[] decodeData(String message) {
    return Base64.getUrlDecoder().decode(message);
  }

  @Override
  protected BodyExtractor getBodyExtractor() {
    return SimpleBodyExtractor.INSTANCE;
  }

  @Override
  public String encodeData(CDTPPacket packet) {
    return null;
  }

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    return new byte[0];
  }


}
