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

package com.syswin.temail.notification.main.dto;

/**
 * @author liusen@syswin.com
 */
public class MailAgentParams {

  public static final String MSG_ID_SPLIT = ",";

  /**
   * eventType 事件类型
   */
  private Integer sessionMessageType;
  /**
   * 消息ID
   */
  private String msgid;
  /**
   * seqId 消息序号
   */
  private Long seqNo;
  /**
   * message 消息体
   */
  private String toMsg;
  /**
   * CDTP报文头
   */
  private String header;

  public Integer getSessionMessageType() {
    return sessionMessageType;
  }

  public String getMsgid() {
    return msgid;
  }

  public Long getSeqNo() {
    return seqNo;
  }

  public String getToMsg() {
    return toMsg;
  }

  public String getHeader() {
    return header;
  }

  @Override
  public String toString() {
    return "MailAgentParams{" +
        "sessionMessageType=" + sessionMessageType +
        ", msgid='" + msgid + '\'' +
        ", seqNo=" + seqNo +
        ", toMsg='" + toMsg + '\'' +
        ", header='" + header + '\'' +
        '}';
  }

  public static class TrashMsgInfo {

    private String from;
    private String to;
    private String msgId;

    public TrashMsgInfo(String from, String to, String msgId) {
      this.from = from;
      this.to = to;
      this.msgId = msgId;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }

    public String getTo() {
      return to;
    }

    public void setTo(String to) {
      this.to = to;
    }

    public String getMsgId() {
      return msgId;
    }

    public void setMsgId(String msgId) {
      this.msgId = msgId;
    }
  }
}