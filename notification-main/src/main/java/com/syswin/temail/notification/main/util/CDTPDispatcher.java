package com.syswin.temail.notification.main.util;

import com.syswin.temail.ps.client.Header;
import com.syswin.temail.ps.client.Message;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;

public class CDTPDispatcher {

  public static CDTPPacket MessageToCDTPPacket(Message message) {
    if (message == null) {
      return null;
    }

    Header header = message.getHeader();
    CDTPHeader cdtpHeader = new CDTPHeader();
    cdtpHeader.setDataEncryptionMethod(header.getDataEncryptionMethod());
    cdtpHeader.setSignatureAlgorithm(header.getSignatureAlgorithm());
    cdtpHeader.setTimestamp(header.getTimestamp());
    cdtpHeader.setPacketId(header.getPacketId());
    cdtpHeader.setSender(header.getSender());
    cdtpHeader.setSenderPK(header.getSenderPK());
    cdtpHeader.setReceiver(header.getReceiver());
    cdtpHeader.setReceiverPK(header.getReceiverPK());
    cdtpHeader.setAt(header.getAt());
    cdtpHeader.setTopic(header.getTopic());
    cdtpHeader.setExtraData(header.getExtraData());
    cdtpHeader.setTargetAddress(header.getTargetAddress());

    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(header.getCommandSpace());
    packet.setCommand(header.getCommand());
    packet.setVersion((short) 1);
    packet.setHeader(cdtpHeader);
    packet.setData(message.getPayload());
    return packet;
  }


  public static String getPacket(CDTPPacket cdtpPacket) {
    if (cdtpPacket == null) {
      return null;
    }

    return "";
  }
}